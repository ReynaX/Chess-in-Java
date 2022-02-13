package pieces;

import board.BoardSquare;

import java.util.ArrayList;
import java.util.Objects;

public class Knight extends Piece{
    public Knight(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
    }

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        int[][] offsets = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {1, -2}, {1, 2}, {-1, -2}, {-1, 2}};
        for(int[] offset : offsets){
            // Check if move isn't outside of the board
            if(m_pos.row() + offset[0] >= 0 && m_pos.row() + offset[0] <= 7 &&
               m_pos.col() + offset[1] >= 0 && m_pos.col() + offset[1] <= 7){
                Piece piece = boardSquares[m_pos.row() + offset[0]][m_pos.col() + offset[1]].getPiece();
                // Check if encountered piece can be captured
                if(piece == null || piece.getColor() != this.getColor()){
                    possibleMoves.add(new Pos(m_pos.row() + offset[0],
                                              m_pos.col() + offset[1]));
                }
            }
        }
        return possibleMoves;
    }
}
