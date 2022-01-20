package board;

import pieces.Piece;
import pieces.Pos;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class BoardSquare extends JToggleButton{
    private final Color m_color;
    private final Pos m_pos;
    private Piece m_piece;

    public BoardSquare(boolean isWhite, int x, int y){
        if(isWhite)
            m_color = new Color(0xeeeed2);
        else
            m_color = new Color(0x769656);
        this.setBackground(m_color);

        setFocusPainted(false);
        m_piece = null;
        m_pos = new Pos(x, y);
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        if(Objects.equals(m_color, new Color(0xeeeed2)))
            g.setColor(new Color(0x769656));
        else g.setColor(new Color(0xeeeed2));

        // Print row and column ids
        if(this.m_pos.col() == 0){
            g.drawString(String.valueOf(8 - this.m_pos.row()), g.getFont().getSize() / 10,
                         g.getFont().getSize() - g.getFont().getSize() / 6);
        }
        if(this.m_pos.row() == 7){
            g.drawString(Character.toString((char) (this.m_pos.col() + 97)),
                         this.getWidth() - g.getFont().getSize(),
                         this.getHeight() - g.getFont().getSize());
        }
    }

    public Pos getPos(){return m_pos;}

    public Color getColor(){return m_color;}

    public Piece getPiece(){return m_piece;}

    public void setPiece(Piece piece){
        if(piece != null)
            this.setIcon(new ImageIcon(piece.getIcon()));
        else this.setIcon(null);
        m_piece = piece;
    }
}
