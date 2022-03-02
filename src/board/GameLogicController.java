package board;

import App.ChessApp;
import pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class GameLogicController implements ActionListener{
    /** Contains game board given as a parameter in constructor */
    private final Board m_board;
    /** Stores pieces that can move and positions to move to */
    protected ArrayList<Pair<Piece, Pos>> m_piecesToMove;
    /** Indicates color of player to move */
    protected PieceAttributes.Color m_colorToMove = PieceAttributes.Color.WHITE;
    /** Indicates which color player is(needed only in game against computer) */
    private PieceAttributes.Color m_playerColor;
    /** Current clicked square(always has a piece) */
    private BoardSquare m_boardSquareClicked;

    /** Move history */
    private final ArrayList<Move> m_moveHistory = new ArrayList<>();

    /**
     * Indicates whether current move is castling.
     * Needed for printing move history
     */
    private boolean m_isCastling = false;
    /** Indicates the state of a current chess game (NONE, CHECK, MATE, STALEMATE) */
    protected GameState m_gameState = GameState.NONE;
    /** Indicates the type of a current chess game (NONE, SINGLE, MULTI) */
    private GameType m_gameType = GameType.NONE;
    /** Timer for white player */
    private final App.Timer m_whiteTimer = new App.Timer(0, 0);
    /** Timer for black player */
    private final App.Timer m_blackTimer = new App.Timer(0, 0);
    /** True if game is still in progress or it's player's move */
    private boolean m_canPlay = false;
    /** True if future moves are calculated */
    private boolean m_isPseudoMoving = false;
    /** Max depth of minmax search algorithm */
    private int m_engineDepth = 0;
    /** Pseudo <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a> */
    private GameStateCache m_cache;
    /** Counter for moves with no capture or no pawn moves */
    protected int m_halfMoves = 0;
    /** Counter for all moves, incremented after black move */
    protected int m_fullMoves = 1;
    /** Winning color of game */
    protected PieceAttributes.Color m_winningColor = PieceAttributes.Color.NONE;

    /**
     * @param board object of class Board that contains a current chess board position
     */
    public GameLogicController(Board board){
        m_piecesToMove = new ArrayList<>();
        m_board = board;
    }

    /**
     * Start game of given type and setups the timers
     *
     * @param timePerSide      timer for each player
     * @param incrementPerMove time added to player's timer every move
     * @param gameType         game type (none, single, multi, analyze)
     */
    public void startLogic(int timePerSide, int incrementPerMove, GameType gameType){
        unselectPossibleMoves();
        m_fullMoves = 1;
        m_halfMoves = 0;
        m_boardSquareClicked = null;
        m_winningColor = PieceAttributes.Color.NONE;
        m_whiteTimer.setNewValues(timePerSide, incrementPerMove);
        m_blackTimer.setNewValues(timePerSide, incrementPerMove);
        m_isCastling = false;
        m_isPseudoMoving = false;
        m_canPlay = true;
        m_colorToMove = PieceAttributes.Color.WHITE;
        m_gameType = gameType;
        handleGameState();

        if(timePerSide == 0 && gameType == GameType.SINGLE){
            m_whiteTimer.setNewValues(0, 0);
            m_blackTimer.setNewValues(0, 0);
            m_whiteTimer.stop();
            m_blackTimer.stop();
            m_gameTimer.stop();
        }else if(timePerSide != 0)
            if(gameType == GameType.SINGLE && m_playerColor == PieceAttributes.Color.BLACK){
                m_whiteTimer.setNewValues(0, 0);
                m_whiteTimer.start();
                m_whiteTimer.stop();
                m_blackTimer.start();
            }else if(gameType == GameType.SINGLE && m_playerColor == PieceAttributes.Color.WHITE){
                m_blackTimer.setNewValues(0, 0);
                m_blackTimer.start();
                m_blackTimer.stop();
                m_whiteTimer.start();
            }else{
                m_blackTimer.start();
                m_blackTimer.stop();
                m_whiteTimer.stop();
                m_whiteTimer.start();
            }
        m_gameTimer.start();

        if(m_gameType == GameType.SINGLE && m_colorToMove != m_playerColor){
            m_isPseudoMoving = true;
            makeBestMove();
            m_isPseudoMoving = false;
        }
        System.out.println("Moves: " + generateMoves(2));
        handleGameState();
    }


    /** Stops the timer of both players and shows dialog with game result **/
    private void gameFinished(){
        String message;
        // White won
        if((m_gameState == GameState.MATE && m_colorToMove == PieceAttributes.Color.BLACK) ||
           (m_gameTimer.isRunning() && m_blackTimer.hasFinished())){
            m_winningColor = PieceAttributes.Color.WHITE;
            message = "WHITE WON";
        }else if((m_gameState == GameState.MATE && m_colorToMove == PieceAttributes.Color.WHITE) ||
                 (m_gameTimer.isRunning() && m_whiteTimer.hasFinished())){
            m_winningColor = PieceAttributes.Color.BLACK;
            message = "BLACK WON";
        }else
            message = "DRAW";

        m_canPlay = false;
        m_blackTimer.stop();
        m_whiteTimer.stop();
        m_gameTimer.stop();
        unselectPossibleMoves();


        JOptionPane.showMessageDialog(null, message);
    }

    /** Increase timer of a player that has just moved **/
    private void changeRunningTimer(){
        if(m_whiteTimer.isRunning()){
            m_whiteTimer.increment();
            m_whiteTimer.stop();
            m_blackTimer.start();
        }else if(m_blackTimer.isRunning()){
            m_blackTimer.increment();
            m_blackTimer.stop();
            m_whiteTimer.start();
        }
    }

    public App.Timer getWhiteTimer(){return m_whiteTimer;}

    public App.Timer getBlackTimer(){return m_blackTimer;}

    /** Called when any board square has been clicked **/
    @Override
    public void actionPerformed(ActionEvent e){
        BoardSquare squareClicked = ((BoardSquare) e.getSource());
        if(!m_canPlay || m_gameState == GameState.MATE || m_gameState == GameState.STALEMATE){
            squareClicked.setSelected(false);
            return;
        }

        if(m_boardSquareClicked == null){
            // Executed when no piece has been clicked yet
            Piece pieceClicked = squareClicked.getPiece();
            if(pieceClicked != null && pieceClicked.getColor() == m_colorToMove){
                m_boardSquareClicked = squareClicked;
                selectPossibleMoves();
            }else squareClicked.setSelected(false);
        }else{
            // Executed when piece has already been clicked on and waits for a move
            Piece pieceWaitingToMove = m_boardSquareClicked.getPiece();
            ArrayList<Pos> possibleMoves = pieceWaitingToMove.calculatePossibleMoves(m_board.m_boardSquares);

            Pos pos = new Pos(squareClicked.getPos().row(), squareClicked.getPos().col());
            // Check if clicked square is selected for possible move
            if(possibleMoves.contains(pos)){
                if(m_piecesToMove.contains(new Pair<>(pieceWaitingToMove, pos)))
                    movePiece(m_boardSquareClicked.getPiece(), m_boardSquareClicked, squareClicked);
            }
            unselectPossibleMoves();
            m_boardSquareClicked.setSelected(false);
            squareClicked.setSelected(false);
            m_boardSquareClicked = null;
        }
        // Make computer move if it's desired
        if(m_gameType == GameType.SINGLE && m_colorToMove != m_playerColor){
            makeBestMove();
            handleGameState();
        }
    }

    /** Change color of squares that are possible to move to **/
    private void selectPossibleMoves(){
        unselectPossibleMoves();
        Piece piece = m_boardSquareClicked.getPiece();
        ArrayList<Pos> possibleMoves = piece.calculatePossibleMoves(m_board.m_boardSquares);
        for(Pos pos : possibleMoves){
            if(piece.getType() != PieceAttributes.Type.KING){
                if(m_piecesToMove.contains(new Pair<>(piece, pos)))
                    m_board.m_boardSquares[pos.row()][pos.col()].setBackground(new Color(135, 206, 235));
            }else m_board.m_boardSquares[pos.row()][pos.col()].setBackground(new Color(135, 206, 235));
        }
    }

    /** Change color of all square to its original color **/
    private void unselectPossibleMoves(){
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                m_board.m_boardSquares[i][j].setBackground(m_board.m_boardSquares[i][j].getColor());
            }
        }
    }

    int generateMoves(int depth){
        m_isPseudoMoving = true;

        handleGameState();
        ArrayList<Pair<Piece, Pos>> possibleMoves = new ArrayList<>(m_piecesToMove);

        if(depth == 1)
            return possibleMoves.size();
        int moves = 0;
        for(Pair<Piece, Pos> move : possibleMoves){
            Piece pieceToMove = move.getFirst();
            Pos currentPos = pieceToMove.getPos(), nextPos = move.getSecond();

            BoardSquare fromSquare = m_board.m_boardSquares[currentPos.row()][currentPos.col()];
            BoardSquare toSquare = m_board.m_boardSquares[nextPos.row()][nextPos.col()];

            m_isPseudoMoving = true;
            Move m = movePiece(pieceToMove, fromSquare, toSquare);
            moves += generateMoves(depth - 1);
            unmakeMove(m);
        }

        m_isPseudoMoving = false;

        return moves;
    }

    /**
     * Check if selected move is castling or enpassant
     * If so, function calls either <code>castlingMove</code> or <code>enpassantMove</code> function
     *
     * @return true if special move has been called, false otherwise
     */
    private Move isSpecialMove(Piece pieceToMove, BoardSquare fromSquare, BoardSquare toSquare){
        Piece toSquarePiece = toSquare.getPiece();
        // Check for castling
        if(toSquarePiece != null && pieceToMove.getType() == PieceAttributes.Type.KING
           && toSquarePiece.getType() == PieceAttributes.Type.ROOK
           && pieceToMove.getColor() == toSquarePiece.getColor()){
            return castlingMove(pieceToMove, toSquarePiece);
        }

        // Check for enpassant
        if(toSquarePiece == null && pieceToMove.getType() == PieceAttributes.Type.PAWN
           && Math.abs(pieceToMove.getPos().col() - toSquare.getPos().col()) == 1){
            return enpassantMove(pieceToMove, fromSquare, toSquare);
        }


        // Check for promotion
        if(pieceToMove.getColor() == PieceAttributes.Color.WHITE && pieceToMove.getType() == PieceAttributes.Type.PAWN
           && toSquare.getPos().row() == 0){
            return promotionMove(pieceToMove, fromSquare, toSquare);
        }else if(pieceToMove.getColor() == PieceAttributes.Color.BLACK && pieceToMove.getType() == PieceAttributes.Type.PAWN
                 && toSquare.getPos().row() == 7){
            return promotionMove(pieceToMove, fromSquare, toSquare);
        }
        return null;
    }

    private void fiftyMoveRule(Piece movedPiece, boolean didCapture){
        if(movedPiece.getColor() == PieceAttributes.Color.BLACK)
            ++m_fullMoves;
        if(!didCapture && movedPiece.getType() != PieceAttributes.Type.PAWN)
            ++m_halfMoves;
        else m_halfMoves = 0;

        if(m_halfMoves >= 100){
            m_gameState = GameState.STALEMATE;
            gameFinished();
        }
    }

    /**
     * Moves passed as piece to a new square, removes piece from current position square.
     * Changes color of player to move and stops the current playing timer.
     * Calls <code>createMoveNotation</code> method if move is not castling.
     *
     * @param pieceToMove piece to move
     * @param fromSquare  square that currently contains pieceToMove
     * @param toSquare    square that pieceToMove will move to
     */
    private Move movePiece(Piece pieceToMove, BoardSquare fromSquare, BoardSquare toSquare){
        if(fromSquare == toSquare)
            return null;

        Move move;
        if((move = isSpecialMove(pieceToMove, fromSquare, toSquare)) != null){
            return move;
        }

        Piece capturedPiece = toSquare.getPiece();

        pieceToMove.movePiece(toSquare.getPos().row(), toSquare.getPos().col());
        toSquare.setPiece(pieceToMove);
        fromSquare.setPiece(null);

        move = new Move(pieceToMove, capturedPiece, fromSquare.getPos(), Move.MoveType.NORMAL);
        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;

        if(!m_isCastling && !m_isPseudoMoving){
            m_moveHistory.add(move);
            changeRunningTimer();
            fiftyMoveRule(pieceToMove, capturedPiece != null);
            createMoveNotation(move, false, capturedPiece != null);
        }
        return move;
    }

    /**
     * <a href="">Enpassant</a>
     **/
    private Move enpassantMove(Piece pawn, BoardSquare fromSquare, BoardSquare toSquare){
        int direction = -pawn.getColor().getValue();
        pawn.movePiece(toSquare.getPos().row(), toSquare.getPos().col());
        toSquare.setPiece(pawn);
        fromSquare.setPiece(null);

        Piece capturedPawn = m_board.m_boardSquares[pawn.getPos().row() - direction][pawn.getPos().col()].getPiece();
        m_board.m_boardSquares[pawn.getPos().row() - direction][pawn.getPos().col()].setPiece(null);

        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;

        Move move = new Move(pawn, capturedPawn, fromSquare.getPos(), Move.MoveType.ENPASSANT);
        if(!m_isPseudoMoving){
            m_moveHistory.add(move);
            changeRunningTimer();
            fiftyMoveRule(pawn, true);
            createMoveNotation(move, false, true);
        }
        return move;
    }

    /**
     * Moves king and a rook if both pieces haven't moved yet and new position of both pieces will not be covered by
     * opposite color pieces.
     * Changes color of player to move and stops the current playing timer.
     * Calls <code>createMoveNotation</code> method.
     **/
    private Move castlingMove(Piece king, Piece rook){
        int diff = king.getPos().col() - rook.getPos().col();
        Pos kingPos = king.getPos();
        m_isCastling = true;
        if(diff > 0){
            // Castling king side
            movePiece(king, m_board.m_boardSquares[king.getPos().row()][king.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() - 2]);
            movePiece(rook, m_board.m_boardSquares[rook.getPos().row()][rook.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() + 1]);
        }else{
            // Castling queen side
            movePiece(king, m_board.m_boardSquares[king.getPos().row()][king.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() + 2]);
            movePiece(rook, m_board.m_boardSquares[rook.getPos().row()][rook.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() - 1]);
        }
        m_isCastling = false;

        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;

        Move move = new Move(king, rook, kingPos, Move.MoveType.CASTLING);
        if(!m_isPseudoMoving){
            m_moveHistory.add(move);
            changeRunningTimer();
            fiftyMoveRule(king, false);
            createMoveNotation(move, diff > 0, false);
        }
        return move;
    }

    private Move promotionMove(Piece pawnToMove, BoardSquare fromSquare, BoardSquare toSquare){
        Piece pieceCaptured = toSquare.getPiece();

        Piece newQueen = new Queen((Pawn) pawnToMove);
        pawnToMove.setPos(new Pos(toSquare.getPos().row(), toSquare.getPos().col()));
        newQueen.setPos(new Pos(toSquare.getPos().row(), toSquare.getPos().col()));

        toSquare.setPiece(newQueen);
        fromSquare.setPiece(null);
        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;

        //System.out.println(m_board);
        Move move = new Move(pawnToMove, pieceCaptured, fromSquare.getPos(), Move.MoveType.PROMOTION);
        if(!m_isPseudoMoving){
            m_moveHistory.add(move);
            changeRunningTimer();
            fiftyMoveRule(pawnToMove, pieceCaptured != null);
            createMoveNotation(move, false, pieceCaptured != null);
        }
        return move;
    }

    /**
     * Returns <code>movedPiece</code> and <code>capturedPiece</code> to its original squares.
     * Function is called only when calculating possible future moves in <code>minMax</code> algorithm function and
     * in <code>isChecked</code> function
     * If a previous move was castling, <code>movedPiece</code> contains a king and <code>capturedPiece</code>
     * contains a rook
     * Changes color of player to move and stops the current playing timer.
     *
     * @param move object that contains information about move(moved piece, captured piece, initial position, move type)
     */
    private Piece unmakeMove(Move move){
        Piece movedPiece = move.getMovedPiece(), capturedPiece = move.getCapturedPiece();
        Pos initialPos = move.getInitialPos();

        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;


        BoardSquare fromSquare = m_board.m_boardSquares[initialPos.row()][initialPos.col()],
                toSquare;

        // Check if move was castling
        if(move.getMoveType() == Move.MoveType.CASTLING){
            int diff = movedPiece.getPos().col() - initialPos.col();
            if(diff > 0) // short castle
                toSquare = m_board.m_boardSquares[initialPos.row()][7];
            else toSquare = m_board.m_boardSquares[initialPos.row()][0];

            Pos kingPos = movedPiece.getPos(), rookPos = capturedPiece.getPos();
            fromSquare.setPiece(movedPiece);
            toSquare.setPiece(capturedPiece);

            m_board.m_boardSquares[kingPos.row()][kingPos.col()].setPiece(null);
            m_board.m_boardSquares[rookPos.row()][rookPos.col()].setPiece(null);

            movedPiece.unmakeMove(fromSquare.getPos().row(), fromSquare.getPos().col());
            capturedPiece.unmakeMove(toSquare.getPos().row(), toSquare.getPos().col());
            return movedPiece;
        }

        toSquare = m_board.m_boardSquares[movedPiece.getPos().row()][movedPiece.getPos().col()];
        // Check if move was promotion
        if(move.getMoveType() == Move.MoveType.PROMOTION){
            //movedPiece.unmakeMove(fromSquare.getPos().row(), fromSquare.getPos().col());
            movedPiece.setPos(new Pos(fromSquare.getPos().row(), fromSquare.getPos().col()));
            fromSquare.setPiece(movedPiece);
            toSquare.setPiece(capturedPiece);
            return movedPiece;
        }

        // Check if move was enpassant
        if(move.getMoveType() == Move.MoveType.ENPASSANT){
            ((Pawn) capturedPiece).setEnpassant(true);
            m_board.m_boardSquares[capturedPiece.getPos().row()][capturedPiece.getPos().col()].setPiece(capturedPiece);
            fromSquare.setPiece(movedPiece);
            toSquare.setPiece(null);
            movedPiece.unmakeMove(fromSquare.getPos().row(), fromSquare.getPos().col());
            return movedPiece;
        }


        fromSquare.setPiece(movedPiece);
        toSquare.setPiece(capturedPiece);
        movedPiece.unmakeMove(fromSquare.getPos().row(), fromSquare.getPos().col());
        return movedPiece;
    }

    /**
     * Create algebraic notation of last move and call static member of ChessApp class to add that move to move
     * history panel. The function is called after the move has been performed so the piece that last moved is now on
     * <code>toSquare</code> square. Function calls <code>handleGameState</code>.
     * <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)"> Algebraic notation</a>
     * (kinda ugly but works B))
     *
     * @param move
     * @param isLongCastle true if the last move was long castling and isCastling is true, false otherwise
     * @param didCapture   true if toSquare contained a piece
     */
    private void createMoveNotation(Move move, boolean isLongCastle, boolean didCapture){
        BoardSquare fromSquare = m_board.m_boardSquares[move.getInitialPos().row()][move.getInitialPos().col()];
        BoardSquare toSquare =
                m_board.m_boardSquares[move.getMovedPiece().getPos().row()][move.getMovedPiece().getPos().col()];
        String notation;
        if(move.getMoveType() == Move.MoveType.CASTLING && isLongCastle){
            notation = "O-O-O";
        }else if(move.getMoveType() == Move.MoveType.CASTLING){
            notation = "o-o-o";
        }else{
            Piece pieceToMove = toSquare.getPiece();
            char fromSquareRowChar = 0, fromSquareColChar = 0;
            boolean hasSameType = hasSameTypeMove(pieceToMove, toSquare.getPos());
            if(hasSameType){
                fromSquareRowChar = Character.forDigit(8 - fromSquare.getPos().row(), 10);
                fromSquareColChar = (char) (fromSquare.getPos().col() + 97);
            }

            char pieceToMoveSquareSign = pieceToMove.getType().getCharacter();

            char toSquareRowChar = Character.forDigit(8 - toSquare.getPos().row(), 10);
            char toSquareColChar = (char) (toSquare.getPos().col() + 97);

            if(move.getMoveType() == Move.MoveType.PROMOTION){
                notation = String.format("%c%c=Q", toSquareColChar, toSquareRowChar);
            }else{

                if(pieceToMove.getType() == PieceAttributes.Type.PAWN){
                    if(didCapture)
                        notation = String.format("x%c%c", toSquareColChar, toSquareRowChar);
                    else
                        notation = String.format("%c%c", toSquareColChar, toSquareRowChar);
                }else{
                    if(didCapture){
                        if(fromSquareColChar != 0)
                            notation = String.format("%c%c%cx%c%c", pieceToMoveSquareSign, fromSquareColChar, fromSquareRowChar,
                                                     toSquareColChar, toSquareRowChar);
                        else
                            notation = String.format("%cx%c%c", pieceToMoveSquareSign,
                                                     toSquareColChar, toSquareRowChar);
                    }else
                        notation = String.format("%c%c%c", pieceToMoveSquareSign,
                                                 toSquareColChar, toSquareRowChar);
                }
            }
        }
        handleGameState();
        if(m_gameState == GameState.MATE){
            notation += "#";
        }else if(m_gameState == GameState.CHECK){
            notation += "+";
        }
        PieceAttributes.Color color = m_colorToMove == PieceAttributes.Color.WHITE ? PieceAttributes.Color.BLACK :
                PieceAttributes.Color.WHITE;
        ChessApp.addNewMove(notation, color);
        m_board.playSound(didCapture, move.getMoveType() == Move.MoveType.CASTLING);
        //System.out.println(m_board.generateFENotation());
        if(m_gameState == GameState.MATE) gameFinished();
    }

    /**
     * Check whether exists the same piece type and position it can move to in <code>m_piecesToMove</code>
     * Needed for creating algebraic notation
     *
     * @param pieceToMove piece that has just moved
     * @param pos         position to pieceToMove has moved to
     */
    private boolean hasSameTypeMove(Piece pieceToMove, Pos pos){
        int countTheSamePieceTypes = 0;
        for(Pair<Piece, Pos> pair : m_piecesToMove){
            if((pair.getFirst()).getType() == pieceToMove.getType() && pair.getSecond().equals(pos)){
                ++countTheSamePieceTypes;
            }
        }
        return countTheSamePieceTypes > 1;
    }

    protected void handleGameState(){
        m_piecesToMove.clear();
        Pos kingPos = m_board.findKingPos(m_colorToMove);
        m_gameState = checkForChecks(kingPos);
        // Make enpassant impossible for last moved pawn
        if(m_moveHistory.size() > 1 && m_moveHistory.get(m_moveHistory.size() - 2).getMovedPiece().getType() == PieceAttributes.Type.PAWN)
            ((Pawn) m_moveHistory.get(m_moveHistory.size() - 2).getMovedPiece()).setEnpassant(false);
    }

    /**
     * @param halfMoves moves with no capture or no pawn moves, needed for
     *                  <a href="https://en.wikipedia.org/wiki/Fifty-move_rule">Fifty-move rule</a>
     * @param fullMoves number of all moves
     */
    protected void setMoves(int halfMoves, int fullMoves){
        m_halfMoves = halfMoves;
        m_fullMoves = fullMoves;
    }

    private void makeRandomComputerMove(){
        m_canPlay = false;
        int size = m_piecesToMove.size();
        if(size == 0)
            return;
        int randomIndexMove = new Random().nextInt(size);
        Pos piecePos = m_piecesToMove.get(randomIndexMove).getFirst().getPos();
        Pos posToMove = m_piecesToMove.get(randomIndexMove).getSecond();
        movePiece(m_piecesToMove.get(randomIndexMove).getFirst(),
                  m_board.m_boardSquares[piecePos.row()][piecePos.col()],
                  m_board.m_boardSquares[posToMove.row()][posToMove.col()]);
        m_canPlay = true;
    }

    /**
     * Checks state of a game by checking possible moves of all <code>m_colorToMove</code> pieces.
     * Functions checks for possible moves of the king and all pieces that attack the king.
     *
     * @param kingPos position of m_colorToMove color king
     *
     * @return current state of the game (NONE, CHECK, MATE, STALEMATE)
     */
    private GameState checkForChecks(Pos kingPos){
        King king = (King) m_board.m_boardSquares[kingPos.row()][kingPos.col()].getPiece();
        GameState gameState = GameState.NONE;

        ArrayList<Pos> kingPossibleMoves = king.calculatePossibleMoves(m_board.m_boardSquares);
        int[][] coveredSquares = king.getAllCoveredSquares(m_board.m_boardSquares);
        int attackingPieces = coveredSquares[kingPos.row()][kingPos.col()]; // Number of pieces that attack the king

        if(attackingPieces > 1 && kingPossibleMoves.isEmpty())
            gameState = GameState.MATE;
        else if(attackingPieces > 1)
            gameState = GameState.CHECK;
        else{
            m_isPseudoMoving = true;
            int countPossibleMoves = 0;
            for(int i = 0; i < 8; ++i){
                for(int j = 0; j < 8; ++j){
                    Piece piece = m_board.m_boardSquares[i][j].getPiece();

                    if(piece == null || piece.getColor() != king.getColor())
                        continue;
                    if(!isChecked(piece, king)) ++countPossibleMoves;
                }
            }
            m_isPseudoMoving = false;
            if(countPossibleMoves == 0 && kingPossibleMoves.isEmpty() && attackingPieces == 1)
                gameState = GameState.MATE;
            else if(countPossibleMoves == 0 && kingPossibleMoves.isEmpty() && attackingPieces == 0)
                gameState = GameState.STALEMATE;
            else if(attackingPieces != 0)
                gameState = GameState.CHECK;

            return gameState;
        }

        ArrayList<Pos> possibleMoves = king.calculatePossibleMoves(m_board.m_boardSquares);
        for(Pos pos : possibleMoves){
            m_piecesToMove.add(new Pair<>(king, pos));
        }
        return gameState;
    }

    /**
     * Checks whether moving a given piece will put king in danger.
     *
     * @param piece piece to move
     * @param king  king that might become under attack
     *
     * @return true if moving a piece is possible, false otherwise
     */
    private boolean isChecked(Piece piece, King king){
        ArrayList<Pos> possibleMoves = piece.calculatePossibleMoves(m_board.m_boardSquares);
        int legalMovesCount = 0;
        for(Pos move : possibleMoves){
            BoardSquare fromSquare = m_board.m_boardSquares[piece.getPos().row()][piece.getPos().col()];
            BoardSquare toSquare = m_board.m_boardSquares[move.row()][move.col()];

            Move m = movePiece(piece, fromSquare, toSquare);
            boolean isKingSafe = king.isKingSafe(m_board.m_boardSquares);
            unmakeMove(m);

            if(isKingSafe){
                m_piecesToMove.add(new Pair<>(piece, move));
                ++legalMovesCount;
            }
        }
        return legalMovesCount == 0;
    }

    private void makeBestMove(){
        m_cache = new GameStateCache();
        ComputerMove bestMove = null;
        int depth = m_engineDepth;

        if(depth == 0){
            makeRandomComputerMove();
            return;
        }

        m_isPseudoMoving = true;
        boolean isMaximizing = m_playerColor != PieceAttributes.Color.WHITE;

        double bestScore = isMaximizing ? -Double.MAX_VALUE : Double.MAX_VALUE;

        handleGameState();
        ArrayList<Pair<Piece, Pos>> possibleMoves = new ArrayList<>(m_piecesToMove);

        for(Pair<Piece, Pos> move : possibleMoves){
            Piece pieceToMove = move.getFirst();
            Pos currentPos = pieceToMove.getPos(), nextPos = move.getSecond();

            BoardSquare fromSquare = m_board.m_boardSquares[currentPos.row()][currentPos.col()];
            BoardSquare toSquare = m_board.m_boardSquares[nextPos.row()][nextPos.col()];

            m_isPseudoMoving = true;
            Move m = movePiece(pieceToMove, fromSquare, toSquare);
            double score = minMax(depth - 1, -Double.MAX_VALUE, Double.MAX_VALUE, !isMaximizing);
            unmakeMove(m);

            if(isMaximizing){
                if(score > bestScore || bestMove == null){
                    bestScore = score;
                    bestMove = new ComputerMove(move.getFirst(), move.getSecond(), score);
                }
            }else{
                if(score < bestScore || bestMove == null){
                    bestScore = score;
                    bestMove = new ComputerMove(move.getFirst(), move.getSecond(), score);
                }
            }
        }

        if(bestMove == null){
            makeRandomComputerMove();
            return;
        }

        m_isPseudoMoving = false;

        Piece pieceToMove = bestMove.getPieceToMove();
        BoardSquare fromSquare = m_board.m_boardSquares[pieceToMove.getPos().row()][pieceToMove.getPos().col()];
        BoardSquare toSquare = m_board.m_boardSquares[bestMove.getPosToMove().row()][bestMove.getPosToMove().col()];
        movePiece(bestMove.getPieceToMove(), fromSquare, toSquare);
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Minimax">Minmax</a>
     * with <a href="https://www.youtube.com/watch?v=l-hh51ncgDI">alpha-beta pruning</a>
     * is implemented in this function.
     *
     * @param depth        number of moves to look
     * @param alpha        minimum score that maximizing player is assured to get, starts with -inf
     * @param beta         maximum score that minimazing player is assured to get, starts with inf
     * @param isMaximizing true if next move is white's, false otherwise
     *
     * @return the best value for called piece color
     */
    private double minMax(int depth, double alpha, double beta, boolean isMaximizing){
        if(depth == 0){
            double score = PositionEvaluationController.getRating(m_board, m_gameState, m_colorToMove);
            m_cache.add(m_board.hashCode(), score);
            return score;
        }

        handleGameState();
        m_isPseudoMoving = true;
        ArrayList<Pair<Piece, Pos>> possibleMoves = new ArrayList<>(m_piecesToMove);

        // Game is finished
        if(possibleMoves.size() == 0){
            if(m_gameState == GameState.MATE){
                if(isMaximizing)
                    return Double.MAX_VALUE;
                return -Double.MAX_VALUE;
            }
            return 0.0;
        }

        double best = (isMaximizing) ? -Double.MAX_VALUE : Double.MAX_VALUE;
        for(Pair<Piece, Pos> move : possibleMoves){
            Piece pieceToMove = move.getFirst();
            Pos currentPos = pieceToMove.getPos(), nextPos = move.getSecond();

            BoardSquare fromSquare = m_board.m_boardSquares[currentPos.row()][currentPos.col()];
            BoardSquare toSquare = m_board.m_boardSquares[nextPos.row()][nextPos.col()];

            Move m = movePiece(pieceToMove, fromSquare, toSquare);
            double ev;
            // Check if position was reached before in pseudo transposition table
            if(m_cache.containsPosition(m_board.hashCode())){
                ev = m_cache.getScore(m_board.hashCode());
            }else
                ev = minMax(depth - 1, alpha, beta, !isMaximizing);
            unmakeMove(m);

            // Check if current move is better that previous ones
            if(isMaximizing){
                best = Math.max(ev, best);
                alpha = Math.max(alpha, ev);
            }else{
                best = Math.min(best, ev);
                beta = Math.min(beta, ev);
            }
            if(beta <= alpha){
                break;
            }
        }

        return best;
    }


    /** Checks whether one of timers has stopped and stops the game if so **/
    private final javax.swing.Timer m_gameTimer = new javax.swing.Timer(100, e -> {
        if(m_blackTimer.hasFinished() || m_whiteTimer.hasFinished()){
            gameFinished();
        }
    });

    /**
     * Sets color of a human player.
     * Needed only in a game with computer
     */
    public void setPlayerColor(PieceAttributes.Color playerColor){
        m_playerColor = playerColor;
    }

    public void setEngineAttribs(int depth){
        m_engineDepth = depth;
    }

    public void setStartingColor(PieceAttributes.Color color){
        m_colorToMove = color;
    }

    public enum GameState{
        NONE, CHECK, MATE, STALEMATE
    }

    public enum GameType{
        NONE, SINGLE, MULTI
    }


    private record ComputerMove(Piece pieceToMove, Pos posToMove, double score){
        Piece getPieceToMove(){return pieceToMove;}

        Pos getPosToMove(){return posToMove;}

        double getScore(){return score;}
    }

}

