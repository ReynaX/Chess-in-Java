package App;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ChessMoveOrderModel extends AbstractTableModel{
    private static class RowData{
        private Integer m_rowIndex;
        private String m_whiteMove;
        private String m_blackMove;

        public RowData(Integer rowIndex, String whiteMove, String blackMove){
            m_rowIndex = rowIndex;
            m_whiteMove = whiteMove;
            m_blackMove = blackMove;
        }

        @Override
        public String toString(){
            //System.out.println(m_rowIndex + ". " + m_whiteMove + " " + m_blackMove + " ");
            return m_rowIndex + ". " + m_whiteMove + " " + m_blackMove + " ";
        }
    }

    private final ArrayList<RowData> m_rowData = new ArrayList<>();

    @Override
    public int getRowCount(){
        return m_rowData.size();
    }

    @Override
    public int getColumnCount(){
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        RowData data = m_rowData.get(rowIndex);

        return switch(columnIndex){
            case 0 -> data.m_rowIndex;
            case 1 -> data.m_whiteMove;
            case 2 -> data.m_blackMove;
            default -> null;
        };
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex){
        if(rowIndex < 0 || rowIndex > m_rowData.size())
            return;
        //System.out.println(value);
        switch(columnIndex){
            case 0 -> m_rowData.get(rowIndex - 1).m_rowIndex = (Integer) value;
            case 1 -> m_rowData.get(rowIndex - 1).m_whiteMove = (String) value;
            case 2 -> m_rowData.get(rowIndex - 1).m_blackMove = (String) value;
        }

        fireTableCellUpdated(rowIndex - 1, columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int colIndex){
        return false;
    }

    public void addRow(){
        m_rowData.add(new RowData(m_rowData.size() + 1, "", ""));
        fireTableRowsInserted(m_rowData.size(), m_rowData.size());
    }

    public void setRowCount(int rowCount){
        if(m_rowData.size() > rowCount){
            m_rowData.subList(rowCount, m_rowData.size()).clear();
            fireTableDataChanged();
        }
    }

    private String rowToPGN(int row){
        if(row >= getRowCount())
            return "";
        return m_rowData.get(row).toString();
    }

    /** Retruns PGN from current game state */
    public String convertToPGN(){
        StringBuilder pgn = new StringBuilder();
        int rows = this.getRowCount();
        for(int row = 0; row < rows; ++row){
            pgn.append(rowToPGN(row));
        }
        return pgn.toString();
    }
}
