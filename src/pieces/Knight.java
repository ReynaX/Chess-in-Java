package pieces;

import board.BoardSquare;

import java.util.ArrayList;
import java.util.Arrays;

public class Knight extends Piece{
    public Knight(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
    }

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        ArrayList<Pos> pairs = new ArrayList<>(Arrays.asList(new Pos(-2, -1), new Pos(-2, 1),
                                                             new Pos(2, -1), new Pos(2, 1),
                                                             new Pos(1, -2), new Pos(1, 2),
                                                             new Pos(-1, -2), new Pos(-1, 2)));

        int pairNumber = 0;
        while(pairNumber < 8){
            if(m_pos.row() + pairs.get(pairNumber).row() >= 0 && m_pos.row() + pairs.get(pairNumber).row() <= 7 &&
               m_pos.col() + pairs.get(pairNumber).col() >= 0 && m_pos.col() + pairs.get(pairNumber).col() <= 7){
                Piece piece = boardSquares[m_pos.row() + pairs.get(pairNumber).row()]
                        [m_pos.col() + pairs.get(pairNumber).col()].getPiece();
                if(piece == null || piece.getColor() != this.getColor()){
                    possibleMoves.add(new Pos(m_pos.row() + pairs.get(pairNumber).row(),
                                              m_pos.col() + pairs.get(pairNumber).col()));
                }
            }
            ++pairNumber;
        }
        return possibleMoves;
    }
}
