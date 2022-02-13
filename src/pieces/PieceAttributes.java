package pieces;

import java.util.Objects;
import java.util.Random;

public class PieceAttributes{
    private Color m_color;
    private Type m_type;

    public PieceAttributes(Color color, Type type){
        m_color = color;
        m_type = type;
    }

    public Type getType(){return m_type;}

    public void setType(Type newType){m_type = newType;}

    public Color getColor(){return m_color;}

    public void setColor(Color color){m_color = color;}

    public enum Type{
        KING(9000, 'K'), QUEEN(900, 'Q'), ROOK(500, 'R'),
        BISHOP(300, 'B'), KNIGHT(300, 'N'), PAWN(100, 'P');

        private final int m_value;
        private final char m_charValue;

        Type(int value, char charValue){
            this.m_value = value;
            this.m_charValue = charValue;
        }

        public int getValue(){return m_value;}

        public char getCharacter(){return m_charValue;}
    }

    public enum Color{
        NONE(0), WHITE(1), BLACK(-1);

        private final int m_value;

        Color(int value){this.m_value = value;}

        public int getValue(){return m_value;}

        public static Color randomColor(){
            int randValue = new Random().nextInt(2);
            return (randValue % 2 == 0) ? WHITE : BLACK;
        }
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        PieceAttributes that = (PieceAttributes) o;
        return m_color == that.m_color && m_type == that.m_type;
    }

    @Override
    public int hashCode(){
        return Objects.hash(m_color, m_type);
    }
}
