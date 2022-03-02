package board;

import pieces.Piece;
import pieces.Pos;

public class Move{

    public Move(Piece pieceToMove, Piece pieceCatured, Pos initialPos, MoveType moveType){
        m_pieceMoved = pieceToMove;
        m_pieceCaptured = pieceCatured;
        m_moveType = moveType;
        m_initialPos = initialPos;
    }

    public Piece getMovedPiece(){return m_pieceMoved;}

    public Piece getCapturedPiece(){return m_pieceCaptured;}

    public Pos getInitialPos(){return m_initialPos;}

    public MoveType getMoveType(){return m_moveType;}

    private final Piece m_pieceMoved;
    private final Piece m_pieceCaptured;
    private final MoveType m_moveType;
    private final Pos m_initialPos;

    protected enum MoveType{
        NORMAL, ENPASSANT, PROMOTION, CASTLING
    }
}
