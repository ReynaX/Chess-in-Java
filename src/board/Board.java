package board;

import App.SoundPlayer;
import pieces.*;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class Board extends JPanel{
    /** 8x8 matrix that scores board squares */
    protected final BoardSquare[][] m_boardSquares;
    /** Handles game state, moves and position evaluation */
    private final GameLogicController m_logicController;
    /** Position that will be loaded if given position was invalid */
    private final String m_standardFeNotation = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    //private final String m_standardFeNotation = "8/8/8/7k/5P2/7K/8/R7 w - - 0 1";
    private SoundPlayer m_moveSound;
    private SoundPlayer m_captureSound;
    private SoundPlayer m_checkSound;
    private SoundPlayer m_checkMateSound;
    private SoundPlayer m_castlingSound;
    private SoundPlayer m_gameStartSound;

    /**
     * Creates a board with given <code>FE notation</code>.
     * Adds timers and logic controller to a board
     */
    public Board(String feNotation){
        JPanel m_boardPanel = new JPanel();

        m_boardPanel.setLayout(new GridLayout(8, 8));

        this.setLayout(new GridBagLayout());
        m_boardSquares = new BoardSquare[8][8];
        // Create a board
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                boolean isWhite = ((i + j) % 2) != 1;
                m_boardSquares[i][j] = new BoardSquare(isWhite, i, j);
                m_boardPanel.add(m_boardSquares[i][j]);
            }
        }
        m_logicController = new GameLogicController(this);
        if(!readFromFENotation(feNotation)){
            readFromFENotation(m_standardFeNotation);
        }

        // Add action listener to each board square
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                m_boardSquares[i][j].addActionListener(m_logicController);
            }
        }
        addComponent(m_logicController.getBlackTimer().getTimeLabel(), 0, 0, 1, 1, 0, 0,
                     GridBagConstraints.LINE_END, GridBagConstraints.NONE);
        addComponent(m_boardPanel, 0, 1, 1, 1, 1.0, 0.9,
                     GridBagConstraints.CENTER, GridBagConstraints.BOTH);
        addComponent(m_logicController.getWhiteTimer().getTimeLabel(), 0, 2, 1, 1, 0, 0,
                     GridBagConstraints.LINE_END, GridBagConstraints.NONE);

        m_boardPanel.setBackground(new Color(0x312e2b));
        this.setBackground(new Color(0x312e2b));
        readSoundFiles();
    }

    private void incorrectFENotation(){
        System.out.println("Incorrect FE Notation. Default FENotation has been loaded");
        JOptionPane.showMessageDialog(this, "Incorrect FE Notation", "Error",
                                      JOptionPane.ERROR_MESSAGE);
    }

    public String generateFENotation(){
        StringBuilder notation = new StringBuilder();
        // Position
        for(int i = 0; i < 8; ++i){
            int emptySquares = 0;
            for(int j = 0; j < 8; ++j){
                Piece piece = m_boardSquares[i][j].getPiece();
                if(piece != null){
                    Character ch = piece.getColor() == PieceAttributes.Color.BLACK ?
                            Character.toLowerCase(piece.getType().getCharacter()) :
                            piece.getType().getCharacter();
                    if(emptySquares != 0)
                        notation.append(emptySquares);
                    emptySquares = 0;
                    notation.append(ch);
                }else ++emptySquares;
            }

            if(emptySquares != 0)
                notation.append(emptySquares);
            if(i == 7)
                notation.append(" ");
            else notation.append("/");
        }

        if(m_logicController.m_colorToMove == PieceAttributes.Color.WHITE)
            notation.append("w ");
        else
            notation.append("b ");

        // That's a lot of ifs
        Piece whiteKing = m_boardSquares[7][4].getPiece();
        Piece whiteLeftRook = m_boardSquares[7][0].getPiece(), whiteRightRook = m_boardSquares[7][7].getPiece();
        boolean hasAdded = false;
        // white short castling
        if(whiteKing != null && whiteRightRook != null){
            if(whiteKing.getType() == PieceAttributes.Type.KING && whiteKing.getColor() == PieceAttributes.Color.WHITE
               && whiteRightRook.getType() == PieceAttributes.Type.ROOK && whiteRightRook.getColor() == PieceAttributes.Color.WHITE){
                if(whiteKing.getFirstMove() && whiteRightRook.getFirstMove()){
                    notation.append("K");
                    hasAdded = true;
                }
            }
        }

        // white long castle
        if(whiteKing != null && whiteLeftRook != null){
            if(whiteKing.getType() == PieceAttributes.Type.KING && whiteKing.getColor() == PieceAttributes.Color.WHITE
               && whiteLeftRook.getType() == PieceAttributes.Type.ROOK && whiteLeftRook.getColor() == PieceAttributes.Color.WHITE){
                if(whiteKing.getFirstMove() && whiteLeftRook.getFirstMove()){
                    notation.append("Q");
                    hasAdded = true;
                }
            }
        }


        Piece blackKing = m_boardSquares[0][4].getPiece();
        Piece blackLeftRook = m_boardSquares[0][0].getPiece(), blackRightRook = m_boardSquares[0][7].getPiece();
        // black short castling
        if(blackKing != null && blackRightRook != null){
            if(blackKing.getType() == PieceAttributes.Type.KING && blackKing.getColor() == PieceAttributes.Color.BLACK
               && blackRightRook.getType() == PieceAttributes.Type.ROOK && blackRightRook.getColor() == PieceAttributes.Color.BLACK){
                if(blackKing.getFirstMove() && blackRightRook.getFirstMove()){
                    notation.append("k");
                    hasAdded = true;
                }
            }
        }
        // black long castling
        if(blackKing != null && blackLeftRook != null){
            if(blackKing.getType() == PieceAttributes.Type.KING && blackKing.getColor() == PieceAttributes.Color.BLACK
               && blackLeftRook.getType() == PieceAttributes.Type.ROOK && blackLeftRook.getColor() == PieceAttributes.Color.BLACK){
                if(blackKing.getFirstMove() && blackLeftRook.getFirstMove()){
                    notation.append("q");
                    hasAdded = true;
                }
            }
        }

        if(!hasAdded)
            notation.append("-");
        notation.append(" ");

        int enpassantRow = -1, enpassantCol = -1;
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = m_boardSquares[i][j].getPiece();
                if(piece == null)
                    continue;
                if(piece.getType() == PieceAttributes.Type.PAWN && ((Pawn) piece).isEnpassantPossible()){
                    enpassantRow = i;
                    enpassantCol = j;
                    break;
                }
            }
        }

        if(enpassantRow != -1){
            char colChar = (char) (enpassantCol + 97);
            notation.append(colChar);
            notation.append(8 - enpassantRow + m_logicController.m_colorToMove.getValue());
        }else notation.append("-");
        notation.append(" ");

        notation.append(m_logicController.m_halfMoves).append(" ").append(m_logicController.m_fullMoves);

        return notation.toString();
    }

    private boolean readFromFENotation(String feNotation){
        feNotation = feNotation.trim();
        String[] values = feNotation.split(" "); // #0 position, #1 colorToMove, #2 castling right #3 enpassant
        // #4 half-moves #5 fullmoves

        if(values.length != 6){
            incorrectFENotation();
            return false;
        }

        PieceAttributes.Color colorToPlay;

        // Check if board position is valid
        if(!positionIsValid(values[0])){
            incorrectFENotation();
            return false;
        }

        // Color to play
        if(values[1].equals("w"))
            colorToPlay = PieceAttributes.Color.WHITE;
        else if(values[1].equals("b"))
            colorToPlay = PieceAttributes.Color.BLACK;
        else{
            incorrectFENotation();
            return false;
        }

        // Castling rights
        boolean whiteShortCastle = values[2].contains("K"), whiteLongCastle = values[2].contains("Q"),
                blackShortCastle = values[2].contains("k"), blackLongCastle = values[2].contains("q");

        if(!assignCastlingRights(whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle)){
            incorrectFENotation();
            return false;
        }
        if(!checkEnpassantRights(values[3], colorToPlay)){
            incorrectFENotation();
            return false;
        }

        return checkMoveValues(values[4], values[5]);
    }

    /** Returns position of king of m_colorToMove color **/
    public Pos findKingPos(PieceAttributes.Color kingColor){
        Pos kingPos = new Pos(-1, -1);
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                if(m_boardSquares[i][j].getPiece() != null && m_boardSquares[i][j].getPiece().getType() == PieceAttributes.Type.KING
                   && m_boardSquares[i][j].getPiece().getColor() == kingColor){
                    kingPos.setPos(i, j);
                    break;
                }
            }
        }
        return kingPos;
    }

    /**
     * Iterate through given FE Notation and create corresponding pieces
     *
     * @param position {<a href="https://www.chess.com/terms/fen-chess">FEN</a>}
     *
     * @return true if given notation is correct, false otherwise
     */
    private boolean positionIsValid(String position){
        int currentRow = 0, currentCol = 0;

        for(int i = 0, length = position.length(); i < length; ++i){
            if(currentCol > 7 && currentRow == 7) return false;
            char ch = position.charAt(i);

            if(Character.isDigit(ch)){
                currentCol += Character.getNumericValue(ch);
            }else if("rbnqkp".indexOf(Character.toLowerCase(ch)) != -1){
                PieceAttributes.Color color = Character.isLowerCase(ch) ? PieceAttributes.Color.BLACK : PieceAttributes.Color.WHITE;
                Piece piece = null;

                switch(ch){
                    case 'R', 'r' -> piece = new Rook(new PieceAttributes(color, PieceAttributes.Type.ROOK), new Pos(currentRow, currentCol));
                    case 'B', 'b' -> piece = new Bishop(new PieceAttributes(color, PieceAttributes.Type.BISHOP), new Pos(currentRow, currentCol));
                    case 'N', 'n' -> piece = new Knight(new PieceAttributes(color, PieceAttributes.Type.KNIGHT), new Pos(currentRow, currentCol));
                    case 'Q', 'q' -> piece = new Queen(new PieceAttributes(color, PieceAttributes.Type.QUEEN), new Pos(currentRow, currentCol));
                    case 'K', 'k' -> piece = new King(new PieceAttributes(color, PieceAttributes.Type.KING), new Pos(currentRow, currentCol));
                    case 'P', 'p' -> piece = new Pawn(new PieceAttributes(color, PieceAttributes.Type.PAWN), new Pos(currentRow, currentCol));
                    default -> {
                    }
                }
                m_boardSquares[currentRow][currentCol].setPiece(piece);
                currentCol += 1;
            }else if(ch == '/'){
                if(currentCol != 8) return false;
                currentCol = 0;
                ++currentRow;
            }else return false;
        }

        return currentCol == 8 && currentRow == 7;
    }

    private boolean assignCastlingRights(boolean whiteShortCastle, boolean whiteLongCastle, boolean blackShortCastle,
                                         boolean blackLongCastle){
        Pos whiteKing = findKingPos(PieceAttributes.Color.WHITE), blackKing = findKingPos(PieceAttributes.Color.BLACK);


        if(whiteShortCastle){
            if(whiteKing.row() != 7 || whiteKing.col() != 4)
                return false;
            Piece piece = m_boardSquares[7][7].getPiece();
            if(piece == null || piece.getType() != PieceAttributes.Type.ROOK || piece.getColor() != PieceAttributes.Color.WHITE)
                return false;
        }
        if(whiteLongCastle){
            if(whiteKing.row() != 7 || whiteKing.col() != 4)
                return false;
            Piece piece = m_boardSquares[7][0].getPiece();
            if(piece == null || piece.getType() != PieceAttributes.Type.ROOK || piece.getColor() != PieceAttributes.Color.WHITE)
                return false;
        }

        if(blackShortCastle){
            if(blackKing.row() != 0 || blackKing.col() != 4)
                return false;
            Piece piece = m_boardSquares[0][7].getPiece();
            if(piece == null || piece.getType() != PieceAttributes.Type.ROOK || piece.getColor() != PieceAttributes.Color.BLACK)
                return false;
        }

        if(blackLongCastle){
            if(blackKing.row() != 0 || blackKing.col() != 4)
                return false;
            Piece piece = m_boardSquares[0][0].getPiece();
            return piece != null && piece.getType() == PieceAttributes.Type.ROOK && piece.getColor() == PieceAttributes.Color.BLACK;
        }
        return true;
    }

    private boolean checkEnpassantRights(String piecePos, PieceAttributes.Color colorToMove){
        if(Objects.equals(piecePos, "-")) return true;

        // Pawn that can be captured with enpassant can only have 2 characters in its move notation
        if(piecePos.length() != 2)
            return false;

        char rowChar = piecePos.charAt(0), colChar = piecePos.charAt(1);

        if(!Character.isAlphabetic(rowChar) || !Character.isDigit(colChar))
            return false;
        int direction = colorToMove.getValue();

        int row = rowChar - 97 + direction, col = 8 - Character.getNumericValue(colChar) - colorToMove.getValue();

        if(row < 0 || row > 7)
            return false;

        Piece piece = m_boardSquares[row][col].getPiece();
        if(piece == null || piece.getType() != PieceAttributes.Type.PAWN || piece.getColor() == colorToMove)
            return false;
        ((Pawn) piece).setFirstMove(false);
        ((Pawn) piece).setEnpassant(true);
        return true;
    }


    private boolean checkMoveValues(String halfMoves, String fullMoves){
        int half, full;
        try{
            half = Integer.parseInt(halfMoves);
            full = Integer.parseInt(fullMoves);
        }catch(NumberFormatException e){
            return false;
        }
        m_logicController.setMoves(half, full);
        return true;
    }


    private void addComponent(Component component, int gridX, int gridY, int gridWidth, int gridHeight, double weightX,
                              double weightY, int anchor, int fill){
        GridBagConstraints gbc = new GridBagConstraints(gridX, gridY, gridWidth, gridHeight, weightX, weightY, anchor
                , fill, new Insets(0, 0, 0, 0), 0, 0);
        this.add(component, gbc);
    }

    public GameLogicController.GameState getResult(){
        return m_logicController.m_gameState;
    }

    public PieceAttributes.Color getWinningColor(){
        return m_logicController.m_winningColor;
    }

    public void createTwoPlayerGame(int timePerSide, int incrementPerMove){
        // Remove pieces from the previous game
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                m_boardSquares[i][j].setPiece(null);
            }
        }
        readFromFENotation(m_standardFeNotation);
        m_logicController.startLogic(timePerSide, incrementPerMove, GameLogicController.GameType.MULTI);
        m_gameStartSound.play();
    }

    public void createOnePlayerGame(int timePerSide, int incrementPerMove,
                                    PieceAttributes.Color playerColor,
                                    int depth){
        // Remove pieces from the previous game
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                m_boardSquares[i][j].setPiece(null);
            }
        }
        readFromFENotation(m_standardFeNotation);
        m_logicController.setPlayerColor(playerColor);
        m_logicController.setEngineAttribs(depth);
        m_logicController.startLogic(timePerSide, incrementPerMove, GameLogicController.GameType.SINGLE);
        m_gameStartSound.play();
    }


    /**
     * @param didCapture true if piece that just moved has captured
     * @param isCastling true if method has been called from <code>castlingMove</code> method
     */
    protected void playSound(boolean didCapture, boolean isCastling){
        if(m_logicController.m_gameState == GameLogicController.GameState.MATE || m_logicController.m_gameState == GameLogicController.GameState.STALEMATE)
            m_checkMateSound.play();
        else if(m_logicController.m_gameState == GameLogicController.GameState.CHECK) m_checkSound.play();
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

    @Override
    public String toString(){
        StringBuilder notation = new StringBuilder();
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = m_boardSquares[i][j].getPiece();
                if(piece != null){
                    char ch = piece.getType().getCharacter();
                    if(piece.getColor() == PieceAttributes.Color.BLACK)
                        ch = Character.toLowerCase(ch);
                    notation.append(ch).append(" ");
                }else
                    notation.append("- ");
            }
            notation.append('\n');
        }
        return notation.toString();
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Arrays.deepEquals(m_boardSquares, board.m_boardSquares);
    }

    @Override
    public int hashCode(){
        return Arrays.deepHashCode(m_boardSquares);
    }
}
