/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lobby;

/**
 *
 * @author vaibhav
 */
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel {
  
	private static volatile MyTableModel table = null;
	
    public static final int remarks = 0;
    public static final int buyin_high = 1;
    public static final int buyin_low = 2;
    public static final int small_blind = 3;
    public static final int big_blind =4;
    
    public Vector dataVector;
    static JTable tableObj = null;
    

	public String[] columnNames = {"Game Name","Buyin-High","Buyin-Low","Small-Blind","Big-Blind"};
    
    public Class[]  colTypes = {String.class,Integer.class,Integer.class,Integer.class,Integer.class};
    
    private MyTableModel(Vector dataVector)
    {
        super();
        this.dataVector = dataVector;
    }
    
    public static MyTableModel getMyTableModelInstance(Vector dataVector)
    {
    	if(table == null)
    	{
    		table = new MyTableModel(dataVector);
    		tableObj = new JTable(table);
    	}
    	
    	return table;
    }

    @Override
    public int getColumnCount() {
    return columnNames.length;
    }

    @Override
    public int getRowCount() {
    return dataVector.size();
    }

    @Override
    public Object getValueAt(int row, int col) 
    {
        Data eData = (Data)(dataVector.elementAt(row));
        switch(col)
        {
            case remarks : return eData.getGameName();
            case buyin_high : return eData.getBuyin_high();
            case buyin_low : return eData.getBuyin_low();
            case small_blind : return eData.getSmall_blind();
            case big_blind : return eData.getBig_blind();
                
        }
        return new String();
    }

    @Override
    public String getColumnName(int col){
    return columnNames[col];
    }

    @Override
    public Class getColumnClass(int c){
    return colTypes[c];
    }


    @Override
    public boolean isCellEditable(int row, int col){
    return false;
    }

    
    public void setValueAt(Object value, int row, int col)
    {
        Data eData = (Data)(dataVector.elementAt(row));

        switch(col)
        {
        case remarks : eData.setGameName((String) value); break;
        case buyin_high : eData.setBuyin_high((Integer) value); break;
        case buyin_low : eData.setBuyin_low((Integer) value); break;
        case small_blind : eData.setSmall_blind((Integer) value); break;
        case big_blind : eData.setBig_blind((Integer) value); break;
        }
    }
    
    public Data getDataObject(int row, int col)
    {
         Data eData = (Data)(dataVector.elementAt(row));
         return eData;
    }
    
    public static JTable getTableObj() {
		return tableObj;
	}

	public static void setTableObj(JTable tableObj) {
		MyTableModel.tableObj = tableObj;
	}
}