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

    /** Returns matrix where elements indicate how many opposite color pieces cover given square **/
    public int[][] getAllCoveredSquares(BoardSquare[][] boardSquares){
        int[][] coveredSquares = new int[8][8];
        Piece attackingPiece = null;

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
                    possibleMoves = ((Pawn) tmpPiece).calculateAttackingMoves(boardSquares);
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
