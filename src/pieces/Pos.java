package pieces;

import java.util.Objects;

public class Pos{
    public Pos(int x, int y){
        m_row = x;
        m_col = y;
    }

    public void setPos(int x, int y){
        m_row = x;
        m_col = y;
    }

    public int row(){return m_row;}

    public int col(){return m_col;}

    @Override
    public boolean equals(Object obj){
        if(getClass() != obj.getClass())
            return false;

        Pos pos = (Pos) obj;
        return this.row() == pos.row() && this.col() == pos.col();
    }

    @Override
    public int hashCode(){
        return Objects.hash(m_row, m_col);
    }

    @Override
    public String toString(){
        return "Pos(" + m_row + ", " + m_col + '}';
    }

    private int m_row;
    private int m_col;
}
