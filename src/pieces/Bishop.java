package pieces;

import board.BoardSquare;

import java.util.ArrayList;
import java.util.Objects;

public class Bishop extends Piece{
    public Bishop(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
    }

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        int[][] offsets = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for(int[] offset : offsets){
            int squareCount = 1;
            // Traverse through possible squares until piece is encountered or there is no more squares
            while(m_pos.row() + offset[0] * squareCount >= 0 && m_pos.row() + offset[0] * squareCount <= 7 &&
                  m_pos.col() + offset[1] * squareCount >= 0 && m_pos.col() + offset[1] * squareCount <= 7){

                Piece piece = boardSquares[m_pos.row() + offset[0] * squareCount]
                        [m_pos.col() + offset[1] * squareCount].getPiece();
                // Check if encountered piece can be captured
                if(piece != null){
                    if(piece.getColor() != this.getColor()){
                        possibleMoves.add(new Pos(m_pos.row() + offset[0] * squareCount,
                                                  m_pos.col() + offset[1] * squareCount));
                    }
                    break;
                }
                possibleMoves.add(new Pos(m_pos.row() + offset[0] * squareCount,
                                          m_pos.col() + offset[1] * squareCount));
                ++squareCount;
            }
        }

        return possibleMoves;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Bishop)) return false;
        Piece piece = (Piece) o;
        return Objects.equals(m_pos, piece.m_pos) && Objects.equals(m_attrib, piece.m_attrib);
    }

    @Override
    public int hashCode(){
        return Objects.hash(m_pos, m_attrib.getType(), m_attrib.getColor());
    }
}