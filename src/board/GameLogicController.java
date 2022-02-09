package board;

import App.ChessApp;
import App.SoundPlayer;
import pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class GameLogicController implements ActionListener{
    /** Contains game board given as a parameter in constructor */
    private final Board m_board;
    /** Stores pieces that can move and positions to move to */
    protected ArrayList<Pair<Piece, Pos>> m_piecesToMove;
    /** Indicates color of player to move */
    private PieceAttributes.Color m_colorToMove = PieceAttributes.Color.WHITE;
    /** Indicates which color player is(needed only in game against computer) */
    private PieceAttributes.Color m_playerColor;
    /** Current clicked square(always has a piece) */
    private BoardSquare m_boardSquareClicked;

    private SoundPlayer m_moveSound;
    private SoundPlayer m_captureSound;
    private SoundPlayer m_checkSound;
    private SoundPlayer m_checkMateSound;
    private SoundPlayer m_castlingSound;
    private SoundPlayer m_gameStartSound;

    /**
     * Indicates whether current move is a castle.
     * Needed for printing move history
     */
    private boolean m_isCastling = false;
    /** Indicates the state of a current chess game (NONE, CHECK, MATE, STALEMATE) */
    protected GameState m_gameState = GameState.NONE;
    /** Indicates the type of a current chess game (NONE, SINGLE, MULTI, ANALYZE) */
    private GameType m_gameType = GameType.NONE;
    /** Timer for white player */
    private final App.Timer m_whiteTimer = new App.Timer(0, 0);
    /** Timer for black player */
    private final App.Timer m_blackTimer = new App.Timer(0, 0);
    /** True if game is still in progress or it's player's move */
    private boolean m_canPlay = false;
    private boolean m_isPseudoMoving = false;
    private int m_engineDepth;
    private int m_maxThinkingTime;


    /**
     * @param board object of class Board that contains a current chess board position
     */
    public GameLogicController(Board board){
        m_piecesToMove = new ArrayList<>();
        m_board = board;

        readSoundFiles();
    }

    /**
     * Start game of given type and setups the timers
     *
     * @param timePerSide      timer for each player
     * @param incrementPerMove time added to player's timer every move
     * @param gameType         game type (none, single, multi, analyze)
     */
    public void startLogic(int timePerSide, int incrementPerMove, GameType gameType){
        m_whiteTimer.setNewValues(timePerSide, incrementPerMove);
        m_blackTimer.setNewValues(timePerSide, incrementPerMove);

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

        //handleGameState();
        //System.out.println(MoveGenerationTest(3, true));
        if(m_gameType == GameType.SINGLE && m_colorToMove != m_playerColor){
            makeRandomComputerMove();
        }
        m_gameStartSound.play();
    }


    /** Stops the timer of both players and shows dialog with game result **/
    private void gameFinished(){
        m_canPlay = false;
        m_blackTimer.stop();
        m_whiteTimer.stop();
        m_gameTimer.stop();
        unselectPossibleMoves();

        String message = null;
        if(m_gameState != GameState.STALEMATE)
            message = m_whiteTimer.hasFinished() ?
                    "BLACK WON!" : "WHITE WON!";
        else message = "DRAW!";
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
                    movePiece(m_boardSquareClicked.getPiece(), m_boardSquareClicked, squareClicked, false);
                System.out.println(m_board);
            }
            unselectPossibleMoves();
            m_boardSquareClicked.setSelected(false);
            squareClicked.setSelected(false);
            m_boardSquareClicked = null;
        }
        if(m_gameType == GameType.SINGLE && m_colorToMove != m_playerColor){
            m_colorToMove = m_playerColor == PieceAttributes.Color.WHITE ? PieceAttributes.Color.BLACK :
                    PieceAttributes.Color.WHITE;
            //handleGameState();
            makeRandomComputerMove();
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

    /**
     * Check if selected move is castling or enpassant
     * If so, function calls either <code>castlingMove</code> or <code>enpassantMove</code> function
     *
     * @return true if special move has been called, false otherwise
     */
    private boolean isSpecialMove(Piece pieceToMove, BoardSquare fromSquare, BoardSquare toSquare, boolean isReversingMove){
        Piece toSquarePiece = toSquare.getPiece();
        // Check for castling
        if(toSquarePiece != null && pieceToMove.getType() == PieceAttributes.Type.KING
           && toSquarePiece.getType() == PieceAttributes.Type.ROOK
           && pieceToMove.getColor() == toSquarePiece.getColor()){
            castlingMove(pieceToMove, toSquarePiece, isReversingMove);
            return true;
        }

        // Check for enpassant
        if(toSquarePiece == null && pieceToMove.getType() == PieceAttributes.Type.PAWN
           && Math.abs(pieceToMove.getPos().col() - toSquare.getPos().col()) == 1){
            enpassantMove(pieceToMove, fromSquare, toSquare, isReversingMove);
            return true;
        }
        return false;
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
    private void movePiece(Piece pieceToMove, BoardSquare fromSquare, BoardSquare toSquare, boolean isReversingMove){
        if(fromSquare == toSquare) return;

        if(isSpecialMove(pieceToMove, fromSquare, toSquare, isReversingMove)){
            return;
        }

        boolean didCapture = toSquare.getPiece() != null;

        if(!isReversingMove){
            pieceToMove.movePiece(toSquare.getPos().row(), toSquare.getPos().col());
            toSquare.setPiece(pieceToMove);
            fromSquare.setPiece(null);
        }else{
            pieceToMove.unmakeMove(fromSquare.getPos().row(), fromSquare.getPos().col());
            fromSquare.setPiece(pieceToMove);
            toSquare.setPiece(null);
        }


        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;


        if(!m_isCastling && !m_isPseudoMoving){
            changeRunningTimer();
            createMoveNotation(fromSquare, toSquare, false, false, didCapture);
        }
    }

    /**
     * Moves king and a rook if both pieces haven't moved yet and new position of both pieces will not be covered by
     * opposite color pieces.
     * Changes color of player to move and stops the current playing timer.
     * Calls <code>createMoveNotation</code> method.
     **/
    private void enpassantMove(Piece pawn, BoardSquare fromSquare, BoardSquare toSquare, boolean isReversingMove){
        int direction = -pawn.getColor().getValue();
        if(!isReversingMove){
            pawn.movePiece(toSquare.getPos().row(), toSquare.getPos().col());
            toSquare.setPiece(pawn);
            fromSquare.setPiece(null);
        }else{
            pawn.unmakeMove(fromSquare.getPos().row(), toSquare.getPos().col());
            toSquare.setPiece(null);
            fromSquare.setPiece(pawn);
        }

        m_board.m_boardSquares[pawn.getPos().row() - direction][pawn.getPos().col()].setPiece(null);

        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;


        if(!m_isPseudoMoving){
            changeRunningTimer();
            createMoveNotation(fromSquare, toSquare, false, false, true);
        }
    }

    /**
     * Moves king and a rook if both pieces haven't moved yet and new position of both pieces will not be covered by
     * opposite color pieces.
     * Changes color of player to move and stops the current playing timer.
     * Calls <code>createMoveNotation</code> method.
     **/
    private void castlingMove(Piece king, Piece rook, boolean isReversingMove){
        int diff = king.getPos().col() - rook.getPos().col();
        m_isCastling = true;
        if(diff > 0){
            // Castling king side
            movePiece(king, m_board.m_boardSquares[king.getPos().row()][king.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() - 2], isReversingMove);
            movePiece(rook, m_board.m_boardSquares[rook.getPos().row()][rook.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() + 1], isReversingMove);
        }else{
            // Castling queen side
            movePiece(king, m_board.m_boardSquares[king.getPos().row()][king.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() + 2], isReversingMove);
            movePiece(rook, m_board.m_boardSquares[rook.getPos().row()][rook.getPos().col()],
                      m_board.m_boardSquares[king.getPos().row()][king.getPos().col() - 1], isReversingMove);
        }
        m_isCastling = false;

        if(m_colorToMove == PieceAttributes.Color.WHITE) m_colorToMove = PieceAttributes.Color.BLACK;
        else m_colorToMove = PieceAttributes.Color.WHITE;

        if(!m_isPseudoMoving){
            changeRunningTimer();
            createMoveNotation(null, null, true, diff > 0, false);
        }
    }

    /**
     * Create algebraic notation of last move and call static member of ChessApp class to add that move to move
     * history panel. The function is called after the move has been performed so the piece that last moved is now on
     * <code>toSquare</code> square. Function calls <code>handleGameState</code>.
     * <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)"> Algebraic notation</a>
     *
     * @param fromSquare   square with a piece that moves, can be null if isCastling is true
     * @param toSquare     square where a piece moves to, can be null if isCastling is true
     * @param isCastling   true whether the last move was castling, false otherwise
     * @param isLongCastle true if the last move was long castling and isCastling is true, false otherwise
     * @param didCapture   true if toSquare contained a piece
     */
    private void createMoveNotation(BoardSquare fromSquare, BoardSquare toSquare, boolean isCastling,
                                    boolean isLongCastle, boolean didCapture){
        String notation;
        if(isCastling && isLongCastle){
            notation = "O-O-O";
        }else if(isCastling){
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

            if(pieceToMove.getType() == PieceAttributes.Type.PAWN){
                if(didCapture)
                    notation = String.format("%cx%c%c", fromSquareColChar, toSquareColChar, toSquareRowChar);
                else
                    notation = String.format("%c%c", toSquareColChar, toSquareRowChar);
            }else{
                if(didCapture)
                    notation = String.format("%c%c%cx%c%c", pieceToMoveSquareSign, fromSquareColChar, fromSquareRowChar,
                                             toSquareColChar, toSquareRowChar);
                else
                    notation = String.format("%c%c%c%c%c", pieceToMoveSquareSign, fromSquareColChar, fromSquareRowChar,
                                             toSquareColChar, toSquareRowChar);
            }
        }
        handleGameState();
        if(m_gameState == GameState.MATE)
            notation += "#";
        else if(m_gameState == GameState.CHECK)
            notation += "+";
        PieceAttributes.Color color = m_colorToMove == PieceAttributes.Color.WHITE ? PieceAttributes.Color.BLACK :
                PieceAttributes.Color.WHITE;
        ChessApp.addNewMove(notation, color);
        playSound(didCapture, isCastling);
        if(m_gameState == GameState.MATE) gameFinished();
    }

    /**
     * Check whether exists the same piece type and position it can move to in m_piecesToMove
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

        // Make enpassant impossible for all moves
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = m_board.m_boardSquares[i][j].getPiece();
                if(piece != null && piece.getColor() == m_colorToMove && piece.getType() == PieceAttributes.Type.PAWN){
                    ((Pawn) piece).setEnpassantImpossible();
                }
            }
        }

        //long start = System.nanoTime();
        //long end = System.nanoTime();
        //long duration = (end - start);
        // System.out.println("Time taken to calculate game state: " + duration);
        // System.out.println("Evaluation score: " + PositionEvaluationController.getRating(m_board));
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
                  m_board.m_boardSquares[posToMove.row()][posToMove.col()], false);
        m_canPlay = true;
    }

    /**
     * Checks state of a game by checking possible moves of all m_colorToMove pieces
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
        int attackingPieces = coveredSquares[kingPos.row()][kingPos.col()];

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

                    if(piece == null || piece.getColor() != king.getColor() || piece.equals(king))
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
        }

        ArrayList<Pos> possibleMoves = king.calculatePossibleMoves(m_board.m_boardSquares);
        for(Pos pos : possibleMoves){
            m_piecesToMove.add(new Pair<>(king, pos));
        }

        return gameState;
    }

    /**
     * Checks whether moving a given piece will put king in danger
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
            // Pseudo move piece to given position to check if king will be attacked
            BoardSquare fromSquare = m_board.m_boardSquares[piece.getPos().row()][piece.getPos().col()];
            BoardSquare toSquare = m_board.m_boardSquares[move.row()][move.col()];
            Piece tmpPiece = toSquare.getPiece();

            boolean isEnpassant = tmpPiece == null && piece.getType() == PieceAttributes.Type.PAWN &&
                                  Math.abs(move.col() - piece.getPos().col()) == 1;

            if(isEnpassant){
                int direction = piece.getColor().getValue();
                tmpPiece = m_board.m_boardSquares[move.row() + direction][move.col()].getPiece();
                m_board.m_boardSquares[move.row() + direction][move.col()].setPiece(null);
            }

            movePiece(piece, fromSquare, toSquare, false);
            boolean isKingSafe = king.isKingSafe(m_board.m_boardSquares);
            movePiece(piece, fromSquare, toSquare, true);

            if(isEnpassant)
                m_board.m_boardSquares[tmpPiece.getPos().row()][tmpPiece.getPos().col()].setPiece(tmpPiece);
            else toSquare.setPiece(tmpPiece);

            if(isKingSafe){
                m_piecesToMove.add(new Pair<>(piece, move));
                ++legalMovesCount;
            }
        }
        return legalMovesCount == 0;
    }

    /**
     * @param didCapture true if piece that just moved has capture
     * @param isCastling true if method has been called from <code>castlingMove</code> method
     */
    private void playSound(boolean didCapture, boolean isCastling){
        if(m_gameState == GameState.MATE || m_gameState == GameState.STALEMATE) m_checkMateSound.play();
        else if(m_gameState == GameState.CHECK) m_checkSound.play();
        else if(isCastling) m_castlingSound.play();
        else if(didCapture) m_captureSound.play();
        else m_moveSound.play();
    }

    private void readSoundFiles(){
        try{
            m_moveSound = new SoundPlayer("/sound_standard_Move.wav");
            m_captureSound = new SoundPlayer("/sound_standard_Capture.wav");
            m_castlingSound = new SoundPlayer("/sound_standard_Castling.wav");
            m_checkSound = new SoundPlayer("/sound_standard_Check.wav");
            m_checkMateSound = new SoundPlayer("/sound_standard_CheckMate.wav");
            m_gameStartSound = new SoundPlayer("/sound_standard_GameStart.wav");
        }catch(Exception e){
            System.out.println("Error playing sound");
            JOptionPane.showMessageDialog(null, "Can't read sound files from resources", "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
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

    public void setEngineAttribs(int depth, int maxThinkingTime){
        m_engineDepth = depth;
        m_maxThinkingTime = maxThinkingTime;
    }

    enum GameState{
        NONE, CHECK, MATE, STALEMATE
    }

    public enum GameType{
        NONE, SINGLE, MULTI, ANALYZE
    }

}

