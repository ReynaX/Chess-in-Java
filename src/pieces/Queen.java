package pieces;

import board.BoardSquare;

import java.util.ArrayList;
import java.util.Arrays;

public class Queen extends Piece{
    public Queen(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
    }

    @Override
    public ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares){
        ArrayList<Pos> possibleMoves = new ArrayList<>();

        // Check moves diagonally(same as in bishop implementation)
        ArrayList<Pos> pairs = new ArrayList<>(Arrays.asList(new Pos(-1, -1), new Pos(-1, 1),
                                                             new Pos(1, -1), new Pos(1, 1)));
        int pairNumber = 0;
        while(pairNumber < 4){
            int squareCount = 1;
            while(m_pos.row() + pairs.get(pairNumber).row() * squareCount >= 0 &&
                  m_pos.row() + pairs.get(pairNumber).row() * squareCount < 8 &&
                  m_pos.col() + pairs.get(pairNumber).col() * squareCount >= 0 &&
                  m_pos.col() + pairs.get(pairNumber).col() * squareCount < 8){

                Piece piece = boardSquares[m_pos.row() + pairs.get(pairNumber).row() * squareCount]
                        [m_pos.col() + pairs.get(pairNumber).col() * squareCount].getPiece();
                if(piece != null){
                    if(piece.getColor() != this.getColor()){
                        possibleMoves.add(new Pos(
                                m_pos.row() + pairs.get(pairNumber).row() * squareCount,
                                m_pos.col() + pairs.get(pairNumber).col() * squareCount));
                    }
                    break;
                }
                possibleMoves.add(new Pos(
                        m_pos.row() + pairs.get(pairNumber).row() * squareCount,
                        m_pos.col() + pairs.get(pairNumber).col() * squareCount));
                ++squareCount;
            }
            ++pairNumber;
        }
        // Check moves horizontally and vertically (same as in rook implementation)
        pairs = new ArrayList<>(Arrays.asList(
                new Pos(0, -1), new Pos(0, 1),
                new Pos(-1, 0), new Pos(1, 0)));

        pairNumber = 0;
        while(pairNumber < 4){
            int squareCount = 1;
            while(m_pos.row() + pairs.get(pairNumber).row() * squareCount >= 0 && m_pos.row() + pairs.get(pairNumber).row() * squareCount < 8 &&
                  m_pos.col() + pairs.get(pairNumber).col() * squareCount >= 0 && m_pos.col() + pairs.get(pairNumber).col() * squareCount < 8){
                Piece piece = boardSquares[m_pos.row() + pairs.get(pairNumber).row() * squareCount]
                        [m_pos.col() + pairs.get(pairNumber).col() * squareCount].getPiece();
                if(piece != null){
                    if(piece.getColor() != this.getColor()){
                        possibleMoves.add(new Pos(
                                m_pos.row() + pairs.get(pairNumber).row() * squareCount,
                                m_pos.col() + pairs.get(pairNumber).col() * squareCount
                        ));
                    }
                    break;
                }
                possibleMoves.add(new Pos(
                        m_pos.row() + pairs.get(pairNumber).row() * squareCount,
                        m_pos.col() + pairs.get(pairNumber).col() * squareCount
                ));
                ++squareCount;
            }
            ++pairNumber;
        }
        return possibleMoves;
    }
}
