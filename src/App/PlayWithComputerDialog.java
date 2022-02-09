package App;

import pieces.PieceAttributes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;

public class PlayWithComputerDialog extends JDialog{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel timePanel;
    private JLabel minutesPerSideLabel;
    private JSlider playTimeSlider;
    private JLabel incrementInSecondsLabel;
    private JSlider incrementSlider;
    private JComboBox timeControlComboBox;
    private JPanel colorPanel;
    private JSlider depthSlider;
    private JSlider maxTimeSlider;
    private JLabel maxTimeLabel;
    private JLabel depthLabel;
    private JToggleButton whiteColorButton;
    private JToggleButton blackColorButton;
    private JToggleButton randomColorButton;
    private Result m_result;

    public PlayWithComputerDialog(){
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        addColorButtonsToMenu();

        setResizable(false);
        setTitle("Create a game");

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
        timeControlComboBox.setFocusable(false);

        timeControlComboBox.addActionListener(e -> {
            int selectedIndex = timeControlComboBox.getSelectedIndex();
            timePanel.setVisible(selectedIndex != 0);
            this.pack();
        });

        playTimeSlider.addChangeListener(e -> {
            int value = playTimeSlider.getValue();
            minutesPerSideLabel.setText("Minutes per side: " + value);
        });

        incrementSlider.addChangeListener(e -> {
            int value = incrementSlider.getValue();
            incrementInSecondsLabel.setText("Increment in seconds: " + value);
        });

        depthSlider.addChangeListener(e -> {
            int value = depthSlider.getValue();
            depthLabel.setText("Depth: " + value);
        });

        maxTimeSlider.addChangeListener(e -> {
            int value = maxTimeSlider.getValue();
            maxTimeLabel.setText("Max thinking time in seconds: " + value);
        });

        timePanel.setVisible(false);
    }

    private void addColorButtonsToMenu(){
        whiteColorButton = new JToggleButton();
        randomColorButton = new JToggleButton();
        blackColorButton = new JToggleButton();
        ButtonGroup group = new ButtonGroup();

        group.add(whiteColorButton);
        group.add(randomColorButton);
        group.add(blackColorButton);

        try{
            whiteColorButton.setIcon(new ImageIcon(
                    ImageIO.read(Objects.requireNonNull(getClass().getResource("/white_king_icon.png")))));
            randomColorButton.setIcon(new ImageIcon(
                    ImageIO.read(Objects.requireNonNull(getClass().getResource("/white_black_color.png")))));
            blackColorButton.setIcon(new ImageIcon(
                    ImageIO.read(Objects.requireNonNull(getClass().getResource("/black_king_icon.png")))));
        }catch(Exception ignored){

        }
        whiteColorButton.setBackground(new Color(0xcccccc));
        randomColorButton.setBackground(new Color(0xcccccc));
        blackColorButton.setBackground(new Color(0xcccccc));

        colorPanel.add(whiteColorButton);
        colorPanel.add(randomColorButton);
        colorPanel.add(blackColorButton);

        whiteColorButton.setContentAreaFilled(true);
        randomColorButton.setContentAreaFilled(true);
        blackColorButton.setContentAreaFilled(true);

        whiteColorButton.setFocusPainted(false);
        randomColorButton.setFocusPainted(false);
        blackColorButton.setFocusPainted(false);

        randomColorButton.setSelected(true);
    }


    private void onOK(){
        // Check if there is a time control
        int playTimeValue = 0, incrementValue = 0;
        if(timeControlComboBox.getSelectedIndex() == 1){
            playTimeValue = playTimeSlider.getValue();
            incrementValue = incrementSlider.getValue();
        }

        int depth = depthSlider.getValue();
        int maxThinkingTime = maxTimeSlider.getValue();
        // Get selected color value, if no color is selected choose randomly
        if(whiteColorButton.isSelected()){
            m_result = new Result(playTimeValue, incrementValue, PieceAttributes.Color.WHITE,
                                  depth, maxThinkingTime);
        }else if(blackColorButton.isSelected())
            m_result = new Result(playTimeValue, incrementValue, PieceAttributes.Color.BLACK,
                                  depth, maxThinkingTime);
        else
            m_result = new Result(playTimeValue, incrementValue, PieceAttributes.Color.randomColor(),
                                  depth, maxThinkingTime);
        dispose();
    }

    private void onCancel(){
        String stringNumber = null;

        // add your code here if necessary
        m_result = new Result(-1, -1, PieceAttributes.Color.BLACK,
                              -1, -1);
        dispose();
    }

    public Result showDialog(){
        this.setVisible(true);
        return m_result;
    }


    public static class Result{
        private final int m_timePerSide;
        private final int m_incrementPerMove;
        private final PieceAttributes.Color m_playerColor;
        private final int m_depth;
        private final int m_maxThinkingTime;

        public Result(int timePerSide, int incrementPerMove, PieceAttributes.Color playerColor, int depth,
                      int maxThinkingTime){
            m_timePerSide = timePerSide;
            m_incrementPerMove = incrementPerMove;
            m_playerColor = playerColor;
            m_depth = depth;
            m_maxThinkingTime = maxThinkingTime;
        }


        public int getTimePerSide(){return m_timePerSide;}

        public int getIncrementPerMove(){return m_incrementPerMove;}

        public PieceAttributes.Color getPlayerColor(){return m_playerColor;}

        public int getDepth(){return m_depth;}

        public int getMaxThinkingTime(){return m_maxThinkingTime;}

    }

}
