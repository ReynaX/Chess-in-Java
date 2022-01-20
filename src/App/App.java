package App;

import javax.swing.*;
import java.awt.*;

public class App{
    private static final ChessApp chessMenu = new ChessApp();

    public static void main(String[] args){
        JFrame frame = new JFrame("Chess");
        frame.setSize(new Dimension(900, 640));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(0x808080));
        frame.setLayout(new GridBagLayout());
        frame.setContentPane(chessMenu);

        frame.setVisible(true);
    }

}
