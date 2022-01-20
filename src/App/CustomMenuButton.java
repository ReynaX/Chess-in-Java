package App;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class CustomMenuButton extends BasicButtonUI{

    @Override
    public void installUI(JComponent c){
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        button.setOpaque(false);
    }

    @Override
    public void paint(Graphics g, JComponent c){
        Dimension size = c.getSize();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c.getBackground().darker());
        g.fillRoundRect(0, 0, size.width, size.height, 10, 10);
        g.setColor(c.getBackground());
        g.fillRoundRect(0, 0, size.width, size.height, 10, 10);
        super.paint(g, c);
    }
}
