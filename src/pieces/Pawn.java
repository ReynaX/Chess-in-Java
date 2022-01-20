package pieces;

import board.BoardSquare;

import java.util.ArrayList;

public class Pawn extends Piece{
    public Pawn(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
        m_isFirstMove = true;
    }

    /** Used when pawn gets to the edge of a board and promotes **/
    public void setNewType(pieces.PieceAttributes.Type type){m_attrib.setType(type);}

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        int m_y = -m_attrib.getColor().getValue();
        int moveForwardCount = m_isFirstMove ? 2 : 1;
        // Check if pawn can move forward
        for(int i = 1; i <= moveForwardCount; ++i){
            int possibleMove = m_y * i + getPos().row();
            if(possibleMove >= 0 && possibleMove < 8){
                if(boardSquares[possibleMove][getPos().col()].getPiece() == null){
                    possibleMoves.add(new Pos(possibleMove, getPos().col()));
                }else break;
            }
        }

        // Check if pawn can capture
        if(m_pos.row() + m_y >= 0 && m_pos.row() + m_y < 8){
            if(m_pos.col() - 1 >= 0 && boardSquares[m_pos.row() + m_y][m_pos.col() - 1].getPiece() != null &&
               boardSquares[m_pos.row() + m_y][m_pos.col() - 1].getPiece().getColor() != getColor()){
                possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() - 1));
            }

            if(m_pos.col() + 1 < 8 && boardSquares[m_pos.row() + m_y][m_pos.col() + 1].getPiece() != null &&
               boardSquares[m_pos.row() + m_y][m_pos.col() + 1].getPiece().getColor() != getColor()){
                possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() + 1));
            }
        }
        return possibleMoves;
    }

    /** Returns diagonally forward one square to the left and right **/
    public ArrayList<Pos> calculateAttackingMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();
        int m_y = m_attrib.getColor().getValue();
        if(m_pos.row() + m_y >= 0 && m_pos.row() + m_y < 8 && m_pos.col() - 1 >= 0)
            possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() - 1));

        if(m_pos.row() + m_y >= 0 && m_pos.row() + m_y < 8 && m_pos.col() + 1 < 8)
            possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() + 1));
        return possibleMoves;
    }
}
