package generator;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
 
    
	private static final long serialVersionUID = -5753448662578711897L;
	private ArrayList<String[]> myData; 
                                        
    private boolean isEditable;

    public MyModel(ArrayList<String[]> myData, boolean isEditable){
    	this.myData = myData;
    	this.isEditable = isEditable;
    }
 
    @Override
    public int getRowCount(){
    	return myData.size();
    }
 
    @Override
    public int getColumnCount(){
        return myData.get(0).length;
    }
 
    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        return myData.get(rowIndex)[columnIndex];
    }
 
    public void setValueAt(String[] newData, int rowIndex, int columnIndex){   
    	
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
    	return isEditable;
    	}
}
