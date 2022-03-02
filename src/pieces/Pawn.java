package pieces;

import board.BoardSquare;

import java.util.ArrayList;

public class Pawn extends Piece{
    private boolean m_hasDoubleMoved = false;

    public Pawn(PieceAttributes attrib, Pos pos){
        super(attrib, pos);
        // Check if pawn returns to its original position
        if(attrib.getColor() == PieceAttributes.Color.WHITE && pos.row() == 6)
            m_isFirstMove = true;
        else m_isFirstMove = attrib.getColor() == PieceAttributes.Color.BLACK && pos.row() == 1;

        if(!assignIcon()){
            System.exit(1);
        }
    }

    public Pawn(Piece other){
        super(other);
        this.m_attrib = new PieceAttributes(other.getColor(), PieceAttributes.Type.PAWN);
    }

    public boolean isEnpassantPossible(){return m_hasDoubleMoved;}

    public void setEnpassant(boolean b){m_hasDoubleMoved = b;}

    public void setFirstMove(boolean firstMove){
        m_isFirstMove = firstMove;
        m_hasDoubleMoved = false;
    }

    @Override
    public void movePiece(int row, int col){
        m_hasDoubleMoved = Math.abs(row - m_pos.row()) == 2;
        this.m_pos = new Pos(row, col);
        ++m_moveCount;
        m_isFirstMove = false;
    }

    @Override
    public void unmakeMove(int row, int col){
        this.m_pos = new Pos(row, col);
        --m_moveCount;
        if(m_moveCount == 0){
            // Check if pawn returns to its original position
            if(getColor() == PieceAttributes.Color.WHITE && m_pos.row() == 6)
                m_isFirstMove = true;
            else m_isFirstMove = getColor() == PieceAttributes.Color.BLACK && m_pos.row() == 1;

            m_hasDoubleMoved = false;
        }else if(m_moveCount == 1){
            m_isFirstMove = false;
        }
    }

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

        // Check if enpassant is possible
        if(m_pos.row() + m_y >= 0 && m_pos.row() + m_y < 8){
            if(m_pos.col() - 1 >= 0){
                Piece piece = boardSquares[m_pos.row()][m_pos.col() - 1].getPiece();
                // Check if there is opposite color pawn on the same row and column - 1 that has just moved
                if(piece != null && piece.getColor() != getColor() && piece.getType() == PieceAttributes.Type.PAWN &&
                   ((Pawn) piece).isEnpassantPossible()){

                    possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() - 1));
                }
            }
            // Check if there is opposite color pawn on the same row and column + 1 that has just moved
            if(m_pos.col() + 1 < 8){
                Piece piece = boardSquares[m_pos.row()][m_pos.col() + 1].getPiece();
                // Check if there is opposite color pawn on the same row and column - 1 that has just moved
                if(piece != null && piece.getColor() != getColor() && piece.getType() == PieceAttributes.Type.PAWN &&
                   ((Pawn) piece).isEnpassantPossible()){

                    possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() + 1));
                }
            }
        }
        return possibleMoves;
    }

    /** Returns diagonally forward one square to the left and right **/
    public ArrayList<Pos> calculateAttackingMoves(){
        ArrayList<Pos> possibleMoves = new ArrayList<>();
        int m_y = m_attrib.getColor().getValue();
        if(m_pos.row() + m_y >= 0 && m_pos.row() + m_y < 8 && m_pos.col() - 1 >= 0)
            possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() - 1));

        if(m_pos.row() + m_y >= 0 && m_pos.row() + m_y < 8 && m_pos.col() + 1 < 8)
            possibleMoves.add(new Pos(m_pos.row() + m_y, m_pos.col() + 1));

        return possibleMoves;
    }
}
