package App;

import board.Board;
import board.GameLogicController;
import pieces.PieceAttributes;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Objects;


public class ChessApp extends JPanel{
    private final static String SINGLE_PLAYER = "Play with a computer";
    private final static String MULTI_PLAYER = "Play with a friend";
    private final static String READ_FROM = "Read game state from file";
    private final static String SAVE_TO = "Save gamestate to fil";

    private static final ChessMoveOrderModel m_moveOrderModel = new ChessMoveOrderModel();
    //private static final String feNotation = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static final Board m_board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

    public static JButton m_playWithFriendButton;
    public static JButton m_singlePlayerMode;
    public static JButton m_saveGameButton;
    public static JButton m_readGameButton;

    public ChessApp(){
        this.setLayout(new GridBagLayout());
        createMainMenu();
        setVisible(true);
        this.setBackground(new Color(0x312e2b));
    }

    public void createMainMenu(){
        m_singlePlayerMode = createButton(SINGLE_PLAYER, new Color(0x2f2f2f));
        m_playWithFriendButton = createButton(MULTI_PLAYER, new Color(0x2f2f2f));
        m_saveGameButton = createButton(SAVE_TO, new Color(0x2f2f2f));
        m_readGameButton = createButton(READ_FROM, new Color(0x2f2f2f));

        JPanel menuPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        JPanel moveOrderPanel = new JPanel();

        buttonPanel.add(m_singlePlayerMode);
        buttonPanel.add(m_playWithFriendButton);
        buttonPanel.add(m_saveGameButton);
        buttonPanel.add(m_readGameButton);

        JTable moveOrderTable = new JTable();
        moveOrderTable.setTableHeader(null);
        moveOrderTable.setIntercellSpacing(new Dimension(0, 0));
        moveOrderTable.setShowGrid(false);
        moveOrderTable.setModel(m_moveOrderModel);

        TableColumn column;
        JScrollPane scrollPane = new JScrollPane(moveOrderTable);
        scrollPane.setPreferredSize(new Dimension(175, 500));
        scrollPane.getViewport().setBackground(new Color(0x312e2b));
        scrollPane.getViewport().setForeground(new Color(0x312e2b));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());


        for(int i = 0; i < 3; ++i){
            column = moveOrderTable.getColumnModel().getColumn(i);

            if(i == 0){
                column.setPreferredWidth(35);
            }else if(i == 1) column.setPreferredWidth(70);
        }

        moveOrderTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        moveOrderTable.setRowSelectionAllowed(false);

        moveOrderPanel.add(scrollPane);
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
                //case READ_FROM -> readFromFile();
                // return;
                case SAVE_TO -> {
                    saveToFile();
                    return;
                }
                default -> {
                    return;
                }
            }
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
                                                result.getPlayerColor(), result.getDepth());
                }
            }
        }
    };

    private static String addGameResult(){
        PieceAttributes.Color winningColor = m_board.getWinningColor();
        GameLogicController.GameState gameState = m_board.getResult();

        if(winningColor == PieceAttributes.Color.WHITE)
            return "1 0";
        else if(winningColor == PieceAttributes.Color.BLACK)
            return "0 1";
        else if(gameState == GameLogicController.GameState.STALEMATE)
            return "0.5 0.5";
        return "*";
    }

    private static void saveToFile(){
        try(PrintWriter writer = new PrintWriter(
                Objects.requireNonNull(ChessApp.class.getResource("/game_saved.txt")).getPath())){
            String notation = m_moveOrderModel.convertToPNG();
            notation += addGameResult();
            writer.print(notation);
            JOptionPane.showMessageDialog(null, "Game state saved to: " +
                                                ChessApp.class.getResource("/game_saved.txt").getPath(), "Save",
                                          JOptionPane.INFORMATION_MESSAGE);
        }catch(FileNotFoundException e){
            JOptionPane.showMessageDialog(null, "File not found", "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

}
