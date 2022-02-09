package App;

import javax.swing.*;
import java.awt.event.*;

public class PlayWithFriendDialog extends JDialog{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel minutesPerSideLabel;
    private JSlider playTimeSlider;
    private JLabel incrementInSecondsLabel;
    private JSlider incrementSlider;

    private Result m_result;

    public PlayWithFriendDialog(){
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        playTimeSlider.addChangeListener(e -> {
            int value = playTimeSlider.getValue();
            minutesPerSideLabel.setText("Minutes per side: " + value);
        });

        incrementSlider.addChangeListener(e -> {
            int value = incrementSlider.getValue();
            incrementInSecondsLabel.setText("Increment in seconds: " + value);
        });
    }

    private void onOK(){
        // add your code here
        m_result = new Result(playTimeSlider.getValue(), incrementSlider.getValue());
        dispose();
    }

    private void onCancel(){
        // add your code here if necessary
        m_result = new Result(-1, -1);
        dispose();
    }

    public Result showDialog(){
        this.setVisible(true);
        return m_result;
    }

    public static class Result{
        private final int m_timePerSide;
        private final int m_incrementPerMove;

        public Result(int timePerSide, int incrementPerMove){
            m_timePerSide = timePerSide;
            m_incrementPerMove = incrementPerMove;
        }

        public int getTimePerSide(){return m_timePerSide;}

        public int getIncrementPerMove(){return m_incrementPerMove;}
    }
}
