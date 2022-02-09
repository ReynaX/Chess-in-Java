package board;

import pieces.*;

import javax.swing.*;
import java.awt.*;

public class Board extends JPanel{
    /** 8x8 matrix that scores board squares */
    protected final BoardSquare[][] m_boardSquares;
    /** Handles game state, moves and position evaluation */
    private final GameLogicController m_logicController;
    /** Position that will be */
    //    private final String m_standardFeNotation = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    private final String m_standardFeNotation = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8";

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

        if(!readFromFENotation(feNotation)){
            readFromFENotation(m_standardFeNotation);
        }

        // Add action listener to each board square
        m_logicController = new GameLogicController(this);
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
    }

    private boolean readFromFENotation(String feNotation){
        if(!positionIsValid(feNotation)){
            System.out.println("Incorrect FE Notation");
            JOptionPane.showMessageDialog(this, "Incorrect FE Notation", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
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
     * @param feNotation {<a href="https://www.chess.com/terms/fen-chess">FEN</a>}
     *
     * @return true if given notation is correct, false otherwise
     */
    private boolean positionIsValid(String feNotation){
        int currentRow = 0, currentCol = 0;

        for(int i = 0, length = feNotation.length(); i < length; ++i){
            if(currentCol > 7 && currentRow == 7) return false;

            char ch = feNotation.charAt(i);
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

    private void addComponent(Component component, int gridX, int gridY, int gridWidth, int gridHeight, double weightX,
                              double weightY, int anchor, int fill){
        GridBagConstraints gbc = new GridBagConstraints(gridX, gridY, gridWidth, gridHeight, weightX, weightY, anchor
                , fill, new Insets(0, 0, 0, 0), 0, 0);
        this.add(component, gbc);
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
    }

    public void createOnePlayerGame(int timePerSide, int incrementPerMove, PieceAttributes.Color playerColor,
                                    int depth, int maxThinkingTime){
        // Remove pieces from the previous game
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                m_boardSquares[i][j].setPiece(null);
            }
        }
        readFromFENotation(m_standardFeNotation);
        m_logicController.setPlayerColor(playerColor);
        m_logicController.setEngineAttribs(depth, maxThinkingTime);
        m_logicController.startLogic(timePerSide, incrementPerMove, GameLogicController.GameType.SINGLE);
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
}
