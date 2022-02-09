package App;

import board.Board;
import pieces.PieceAttributes;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;


public class ChessApp extends JPanel{
    private final static String SINGLE_PLAYER = "Play with a computer";
    private final static String MULTI_PLAYER = "Play with a friend";
    private final static String ANALYZE_GAME = "Analyze game";
    private static final ChessMoveOrderModel m_moveOrderModel = new ChessMoveOrderModel();

    private static final Board m_board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

    public static JButton m_analyzeButton;
    public static JButton m_playWithFriendButton;
    public static JButton m_singlePlayerMode;

    public ChessApp(){
        this.setLayout(new GridBagLayout());
        createMainMenu();
        setVisible(true);
        this.setMaximumSize(this.getPreferredSize());
        this.setBackground(new Color(0x312e2b));
    }

    public void createMainMenu(){
        m_analyzeButton = createButton(ANALYZE_GAME, new Color(0x2f2f2f));
        m_singlePlayerMode = createButton(SINGLE_PLAYER, new Color(0x2f2f2f));
        m_playWithFriendButton = createButton(MULTI_PLAYER, new Color(0x2f2f2f));

        JPanel menuPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        JPanel moveOrderPanel = new JPanel();

        buttonPanel.add(m_singlePlayerMode);
        buttonPanel.add(m_playWithFriendButton);
        buttonPanel.add(m_analyzeButton);

        JTable moveOrderTable = new JTable();
        TableColumn column = null;

        moveOrderTable.setIntercellSpacing(new Dimension(0, 0));
        moveOrderTable.setShowGrid(false);
        moveOrderTable.setModel(m_moveOrderModel);

        for(int i = 0; i < 3; ++i){
            column = moveOrderTable.getColumnModel().getColumn(i);

            if(i == 0){
                column.setPreferredWidth(35);
            }else if(i == 1) column.setPreferredWidth(70);
        }

        moveOrderTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        moveOrderTable.setRowSelectionAllowed(false);

        moveOrderPanel.add(moveOrderTable);
        menuPanel.add(buttonPanel, BorderLayout.PAGE_START);
        menuPanel.add(moveOrderPanel, BorderLayout.WEST);
        menuPanel.setBackground(new Color(0x312e2b));
        moveOrderPanel.setBackground(new Color(0x312e2b));
        moveOrderTable.setForeground(new Color(0xffffff));
        moveOrderTable.setBackground(new Color(0x2f2f2f));

        addComponent(this, m_board, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH);
        addComponent(this, menuPanel, 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH);
    }

    private static JButton createButton(String text, Color bgColor){
        JButton newButton = new JButton(text);
        newButton.setBackground(bgColor);
        newButton.setForeground(Color.white);
        newButton.setFont(new Font("Calibri", Font.PLAIN, 14));
        newButton.setUI(new CustomMenuButton());
        newButton.setFocusPainted(false);
        newButton.addActionListener(actionListener);
        return newButton;
    }

    public static void addNewMove(String moveNotation, PieceAttributes.Color colorMoved){
        //System.out.println(moveNotation);
        switch(colorMoved){
            case WHITE -> {
                m_moveOrderModel.addRow();
                m_moveOrderModel.setValueAt(moveNotation,
                                            m_moveOrderModel.getRowCount(), 1);
            }
            case BLACK -> {
                m_moveOrderModel.setValueAt(moveNotation,
                                            m_moveOrderModel.getRowCount(), 2);
            }
        }
    }

    public static void addComponent(Container container, Component component, int gridX, int gridY,
                                    int gridWidth, int gridHeight, int anchor, int fill){
        GridBagConstraints gbc = new GridBagConstraints(gridX, gridY, gridWidth, gridHeight, 1.0, 1.0,
                                                        anchor, fill, new Insets(0, 0, 0, 0), 0, 0);
        container.add(component, gbc);
    }


    public Board getBoard(){return m_board;}

    private static final ActionListener actionListener = e -> {

        if(e.getSource() instanceof JButton){
            JDialog newDialog = null;
            switch(((JButton) e.getSource()).getText()){
                case MULTI_PLAYER -> newDialog = new PlayWithFriendDialog();
                case SINGLE_PLAYER -> newDialog = new PlayWithComputerDialog();
            }
            assert newDialog != null;
            newDialog.pack();
            if(newDialog instanceof PlayWithFriendDialog){
                PlayWithFriendDialog.Result result = ((PlayWithFriendDialog) newDialog).showDialog();
                if(result.getTimePerSide() != -1){
                    m_moveOrderModel.setRowCount(0);
                    m_board.createTwoPlayerGame(result.getTimePerSide(), result.getIncrementPerMove());
                }
            }else{
                PlayWithComputerDialog.Result result = ((PlayWithComputerDialog) newDialog).showDialog();
                if(result.getTimePerSide() != -1){
                    m_moveOrderModel.setRowCount(0);
                    m_board.createOnePlayerGame(result.getTimePerSide(), result.getIncrementPerMove(),
                                                result.getPlayerColor(), result.getDepth(), result.getMaxThinkingTime());
                }
            }
        }
    };
}
