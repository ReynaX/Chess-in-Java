package pieces;

import board.BoardSquare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Rook extends Piece{
    public Rook(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
    }

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        int[][] offsets = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for(int[] offset : offsets){
            int squareCount = 1;
            // Traverse through possible squares until piece is encountered or there is no more squares
            while(m_pos.row() + offset[0] * squareCount >= 0 && m_pos.row() + offset[0] * squareCount < 8 &&
                  m_pos.col() + offset[1] * squareCount >= 0 && m_pos.col() + offset[1] * squareCount < 8){
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
}
