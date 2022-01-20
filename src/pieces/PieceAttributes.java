package pieces;

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
        KING(900, 'K'), QUEEN(90, 'Q'), ROOK(50, 'R'),
        BISHOP(30, 'B'), KNIGHT(30, 'N'), PAWN(10, 'p');

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
        WHITE(1), BLACK(-1);

        private final int m_value;

        Color(int value){this.m_value = value;}

        public int getValue(){return m_value;}

        public static Color randomColor(){
            int randValue = new Random().nextInt(2);
            return (randValue % 2 == 0) ? WHITE : BLACK;
        }
    }
}
