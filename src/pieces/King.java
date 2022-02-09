package pieces;

import board.BoardSquare;

import java.util.ArrayList;

public class King extends Piece{
    public King(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
    }

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        int[][] coveredSquares = getAllCoveredSquares(boardSquares);
        for(int i = -1; i <= 1; ++i){
            for(int j = -1; j <= 1; ++j){
                if(m_pos.row() + i >= 0 && m_pos.row() + i < 8 && m_pos.col() + j >= 0 && m_pos.col() + j < 8){
                    Piece tmpPiece = boardSquares[m_pos.row() + i][m_pos.col() + j].getPiece();
                    if((tmpPiece != null && tmpPiece.getColor() == this.getColor())
                       || coveredSquares[m_pos.row() + i][m_pos.col() + j] > 0)
                        continue;

                    possibleMoves.add(new Pos(m_pos.row() + i, m_pos.col() + j));
                }
            }
        }

        // Check if castling is possible
        if(m_isFirstMove && (m_pos.row() == 0 || m_pos.row() == 7)){
            Piece rook = boardSquares[m_pos.row()][0].getPiece();
            if(rook != null && rook.getColor() == this.getColor()
               && rook.getType() == PieceAttributes.Type.ROOK && rook.m_isFirstMove){
                int col = m_pos.col() - 1;
                while(col > 0 && boardSquares[m_pos.row()][col].getPiece() == null) --col;
                if(col == 0 && coveredSquares[m_pos.row()][m_pos.col() - 2] == 0
                   && coveredSquares[m_pos.row()][m_pos.col() - 1] == 0)
                    possibleMoves.add(new Pos(m_pos.row(), 0));
            }
            rook = boardSquares[m_pos.row()][7].getPiece();
            if(rook != null && rook.getColor() == this.getColor() && rook.getType() == PieceAttributes.Type.ROOK
               && rook.m_isFirstMove){
                int col = m_pos.col() + 1;
                while(col < 7 && boardSquares[m_pos.row()][col].getPiece() == null) ++col;
                if(col == 7 && coveredSquares[m_pos.row()][m_pos.col() + 2] == 0
                   && coveredSquares[m_pos.row()][m_pos.col() + 1] == 0)
                    possibleMoves.add(new Pos(m_pos.row(), 7));
            }
        }

        return possibleMoves;
    }

    public boolean isKingSafe(BoardSquare[][] boardSquares){
        int row = m_pos.row(), col = m_pos.col();
        // Search for diagonal attacks
        for(int r = -1; r <= 1; r += 2){
            for(int c = -1; c <= 1; c += 2){
                int squareCount = 1;
                while(row + squareCount * r >= 0 && row + squareCount * r < 8 &&
                      col + squareCount * c >= 0 && col + squareCount * c < 8){
                    Piece piece = boardSquares[row + squareCount * r][col + squareCount * c].getPiece();
                    if(piece != null){
                        if(piece.getColor() != this.getColor() &&
                           (piece.getType() == PieceAttributes.Type.BISHOP || piece.getType() == PieceAttributes.Type.QUEEN)){
                            return false;
                        }
                        break;
                    }
                    ++squareCount;
                }
            }
        }

        // Search for horizontal and vertical attacks
        for(int r = -1; r <= 1; r += 2){
            int squareCount = 1;
            // Check vertically
            while(row + squareCount * r >= 0 && row + squareCount * r < 8){
                Piece piece = boardSquares[row + squareCount * r][col].getPiece();
                if(piece != null){
                    if(piece.getColor() != m_attrib.getColor() &&
                       (piece.getType() == PieceAttributes.Type.ROOK || piece.getType() == PieceAttributes.Type.QUEEN)){
                        return false;
                    }
                    break;
                }
                ++squareCount;
            }
            squareCount = 1;
            // Check horizontally
            while(col + squareCount * r >= 0 && col + squareCount * r < 8){
                Piece piece = boardSquares[row][col + squareCount * r].getPiece();
                if(piece != null){
                    if(piece.getColor() != m_attrib.getColor() &&
                       (piece.getType() == PieceAttributes.Type.ROOK || piece.getType() == PieceAttributes.Type.QUEEN)){
                        return false;
                    }
                    break;
                }
                ++squareCount;
            }
        }

        // Search for knight attacks
        for(int r = -1; r <= 1; r += 2){
            for(int c = -1; c <= 1; c += 2){
                if(row + r * 2 >= 0 && row + r * 2 < 8 && col + c >= 0 && col + c < 8){
                    Piece piece = boardSquares[row + r * 2][col + c].getPiece();
                    if(piece != null && piece.getColor() != m_attrib.getColor() &&
                       piece.getType() == PieceAttributes.Type.KNIGHT){
                        return false;
                    }
                }

                if(row + r >= 0 && row + r < 8 && col + c * 2 >= 0 && col + c * 2 < 8){
                    Piece piece = boardSquares[row + r][col + c * 2].getPiece();
                    if(piece != null && piece.getColor() != m_attrib.getColor() &&
                       piece.getType() == PieceAttributes.Type.KNIGHT){
                        return false;
                    }
                }
            }
        }

        // Search for pawn attacks
        int pawnAttackDirection = getColor().getValue() * (-1);
        if(row + pawnAttackDirection >= 0 && row + pawnAttackDirection < 8){
            if(col - 1 >= 0){
                Piece piece = boardSquares[row + pawnAttackDirection][col - 1].getPiece();
                if(piece != null && piece.getColor() != m_attrib.getColor() &&
                   piece.getType() == PieceAttributes.Type.PAWN){
                    return false;
                }
            }

            if(col + 1 < 8){
                Piece piece = boardSquares[row + pawnAttackDirection][col + 1].getPiece();
                if(piece != null && piece.getColor() != m_attrib.getColor() &&
                   piece.getType() == PieceAttributes.Type.PAWN){
                    return false;
                }
            }
        }

        for(int r = -1; r <= 1; ++r){
            for(int c = -1; c <= 1; ++c){
                if(row + r >= 0 && row + r < 8 && col + c >= 0 && col + c < 8){
                    Piece piece = boardSquares[row + r][col + c].getPiece();
                    if(piece != null && piece.getColor() != m_attrib.getColor() &&
                       piece.getType() == PieceAttributes.Type.KING)
                        return false;
                }
            }
        }
        return true;
    }

    /** Returns matrix where elements indicate how many opposite color pieces cover given square **/
    public int[][] getAllCoveredSquares(BoardSquare[][] boardSquares){
        int[][] coveredSquares = new int[8][8];

        boardSquares[m_pos.row()][m_pos.col()].setPiece(null);

        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece tmpPiece = boardSquares[i][j].getPiece();
                if(tmpPiece == null || tmpPiece.getColor() == this.getColor())
                    continue;

                if(tmpPiece.getColor() == PieceAttributes.Color.BLACK) tmpPiece.setColor(PieceAttributes.Color.WHITE);
                else tmpPiece.setColor(PieceAttributes.Color.BLACK);

                ArrayList<Pos> possibleMoves;
                if(tmpPiece.getType() == PieceAttributes.Type.PAWN)
                    possibleMoves = ((Pawn) tmpPiece).calculateAttackingMoves();
                else if(tmpPiece.getType() == PieceAttributes.Type.KING)
                    possibleMoves = ((King) tmpPiece).calculateAttackingMoves(boardSquares);
                else possibleMoves = tmpPiece.calculatePossibleMoves(boardSquares);

                for(Pos pos : possibleMoves){
                    ++coveredSquares[pos.row()][pos.col()];
                }

                if(tmpPiece.getColor() == PieceAttributes.Color.BLACK) tmpPiece.setColor(PieceAttributes.Color.WHITE);
                else tmpPiece.setColor(PieceAttributes.Color.BLACK);
            }
        }

        boardSquares[m_pos.row()][m_pos.col()].setPiece(this);
        return coveredSquares;
    }

    /** Returns all moves around the king (needed only when opposite color king wants to move) **/
    public ArrayList<Pos> calculateAttackingMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();
        for(int i = -1; i <= 1; ++i){
            for(int j = -1; j <= 1; ++j){
                if(m_pos.row() + i >= 0 && m_pos.row() + i < 8 &&
                   m_pos.col() + j >= 0 && m_pos.col() + j < 8){
                    possibleMoves.add(new Pos(m_pos.row() + i, m_pos.col() + j));
                }
            }
        }
        return possibleMoves;
    }
}
