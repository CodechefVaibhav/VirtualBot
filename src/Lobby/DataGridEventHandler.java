/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lobby;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;

import BotLogin.RequestIniatiator;

/**
*
* @author  vaibhav
*/
public class DataGridEventHandler extends MouseAdapter {

    JTable table;
    MyTableModel myTableObj;
    RoomListPopUp recursivePanelObj =null;
    RequestIniatiator reqInitObj = null;
    int configId =-1;
    public DataGridEventHandler(JTable table, RequestIniatiator reqInitObj) {
        this.table = table;
        this.reqInitObj = reqInitObj;
        //recursivePanelObj = RoomListPopUp.getRoomListPopupObj();
    }
    
    @Override
    public void mouseClicked(MouseEvent me)
    {
    	boolean requestSentFlag = false;
      if (me.getClickCount() == 1) 
      {
          JTable target = (JTable)me.getSource();
          target.getSelectedRow();
          int row = target.getSelectedRow();
          int column = target.getSelectedColumn();
          myTableObj = (MyTableModel)target.getModel();
          Data dt = myTableObj.getDataObject(row, column);
          System.out.println(row+" && "+column+" ** "+dt.getConfigId());
          configId = dt.getConfigId();
          if(configId>0)
          {
        	 requestSentFlag =  reqInitObj.sendConfigIdForRoomListPopOut(configId);
          }
         
//          if(recursivePanelObj!=null)
//          {
//              if(recursivePanelObj.isEnabled())
//              {
//                  recursivePanelObj.dispose();
//                  recursivePanelObj = new RoomListPopUp();
//              }
//          }
//          else
//          {
//              recursivePanelObj = new RoomListPopUp();
//          }
         // recursivePanelObj.prepareRoomList();
      }
    }
    
    
}
