package pieces;

import board.BoardSquare;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;


public abstract class Piece{
    protected boolean m_isFirstMove;
    protected int m_moveCount = 0;
    protected Pos m_pos;
    protected PieceAttributes m_attrib;
    protected Image m_icon;


    public Piece(PieceAttributes attrib, Pos pos){
        m_attrib = attrib;
        m_pos = pos;
        m_isFirstMove = isFirstMove();

        if(!assignIcon()){
            System.exit(1);
        }
    }

    public Piece(Piece other){
        this.m_isFirstMove = other.m_isFirstMove;
        this.m_moveCount = other.m_moveCount;
        this.m_pos = new Pos(other.m_pos.row(), other.m_pos.col());
    }

    private boolean isFirstMove(){
        switch(m_attrib.getType()){
            case PAWN -> {
                if(m_attrib.getColor() == PieceAttributes.Color.WHITE)
                    return this.m_pos.row() == 6;
                else return this.m_pos.row() == 1;
            }
            case ROOK -> {
                if(m_attrib.getColor() == PieceAttributes.Color.WHITE)
                    return this.m_pos.row() == 7 && (this.m_pos.col() == 0 || this.m_pos.col() == 7);
                else return this.m_pos.row() == 0 && (this.m_pos.col() == 0 || this.m_pos.col() == 7);
            }

            case KING -> {
                if(m_attrib.getColor() == PieceAttributes.Color.WHITE)
                    return this.m_pos.row() == 7 && this.m_pos.col() == 4;
                else return this.m_pos.row() == 0 && this.m_pos.col() == 4;
            }
            default -> {
                return true;
            }
        }
    }

    /**
     * Calculate possible moves of this piece
     * Function returns moves that might be illegal due to possible checks
     *
     * @param boardSquares current board position
     *
     * @return return list of possible moves
     */
    public abstract ArrayList<Pos> calculatePossibleMoves(BoardSquare[][] boardSquares);


    public void movePiece(int row, int col){
        this.m_pos = new Pos(row, col);
        m_isFirstMove = false;
        ++m_moveCount;
    }

    public void unmakeMove(int row, int col){
        this.m_pos = new Pos(row, col);
        --m_moveCount;
        if(m_moveCount == 0)
            m_isFirstMove = true;
    }

    public boolean getFirstMove(){return m_isFirstMove;}

    public Pos getPos(){return m_pos;}

    public void setPos(Pos pos){m_pos = pos;}

    public PieceAttributes.Type getType(){return m_attrib.getType();}

    public Image getIcon(){return m_icon;}

    public PieceAttributes.Color getColor(){return m_attrib.getColor();}

    public void setColor(PieceAttributes.Color color){m_attrib.setColor(color);}

    public boolean assignIcon(){
        String iconName = "/" + m_attrib.getColor().name().toLowerCase() +
                          "_" + m_attrib.getType().name().toLowerCase() + "_icon.png";
        try{
            m_icon = ImageIO.read(Objects.requireNonNull(getClass().getResource(iconName)));
        }catch(Exception e){
            System.out.println(iconName);
            System.out.println("Failed to load " + m_attrib.getType().name() + " icon");
        }

        return true;
    }

    @Override
    public String toString(){
        return "Piece{" + "m_pos=" + m_pos + '}';
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return Objects.equals(m_pos, piece.m_pos) && Objects.equals(m_attrib, piece.m_attrib);
    }

    @Override
    public int hashCode(){
        return Objects.hash(m_pos, m_attrib);
    }
}
