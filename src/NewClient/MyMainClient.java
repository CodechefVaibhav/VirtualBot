package NewClient;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MyMainClient.java
 *
 * Created on Aug 16, 2013, 11:47:52 AM
 */


import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import BotLogin.RequestIniatiator;

import sfs2x.client.entities.Room;
import sfs2x.client.example.ChildRummyBot;
import sfs2x.client.example.RummyBot;

/**
 *
 * @author vaibhav
 */
public class MyMainClient extends javax.swing.JFrame {
	
	// Making this object of this class volatile so that every other thread gets same copy of this object.
	private static volatile MyMainClient mainClientObj = null;
	private MyDesktopPane desktopPane = null;

	private CopyOnWriteArrayList<NewJInternalFrame> internalFramesArrayList = null;
	
	NewJInternalFrame intnlClient;
    MinimizingPanel jPanelObj = null;
    CopyOnWriteArrayList<CustomizedButton> CustomizedButtonArrayList = null;
    CustomizedButton buttonObj = null;

	private MyMainClient()
    {
    	  initComponents();
    	 // desktopPane = MyDesktopPane.getMyDesktopPaneInstance();
    	  //this.add(desktopPane);
    	  CustomizedButtonArrayList = new CopyOnWriteArrayList<CustomizedButton>();
    	  internalFramesArrayList = new CopyOnWriteArrayList<NewJInternalFrame>();
    	  jPanelObj = new MinimizingPanel(new JPanel());
    	  //desktopPane.add(jPanelObj);
    	  this.add(jPanelObj);
    	  
    	  this.setTitle("Game Rooms");
    	  this.setLayout(new FlowLayout());
    	  //desktopPane.setLayout(new FlowLayout());
    	  this.setVisible(true);
    }
    
    public static MyMainClient getInstance()
    {
    	if(mainClientObj == null)
    	{
    		mainClientObj = new MyMainClient();
    	}
    	
    	return mainClientObj;
    }
    
    /** Creates new form MyMainClient */
    public NewJInternalFrame createInternalFrame(String roomName, String botName, int botId, Room roomObj, RummyBot rummyGlobalObj)
    {
    	setInternalFrame(roomName, botName, botId, roomObj, rummyGlobalObj);
    	this.add(getInternalFrame());
    	return getInternalFrame();
    }
    public NewJInternalFrame createInternalFrame(String roomName, String botName, int botId, Room roomObj, RequestIniatiator reqinitGlobalObj)
    {
    	setInternalFrame(roomName, botName, botId, roomObj, reqinitGlobalObj);
    	this.add(getInternalFrame());
    	return getInternalFrame();
    }
    public NewJInternalFrame createInternalFrame(String roomName, String botName, int botId, Room roomObj, ChildRummyBot childRummyGlobalObj)
    {
    	
    	setInternalFrame(roomName, botName, botId, roomObj, childRummyGlobalObj);
    	this.add(getInternalFrame());
    	internalFramesArrayList.add(getInternalFrame());
    	//desktopPane.add(getInternalFrame());
    	buttonObj = new CustomizedButton(getInternalFrame());
    	setButtonObj(buttonObj);
    	CustomizedButtonArrayList.add(buttonObj);
    	JPanel temp = (JPanel)jPanelObj.getViewport().getView();
    	temp.add(buttonObj);
    	temp.revalidate();
    	jPanelObj.getViewport().revalidate();
    	return getInternalFrame();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(MyMainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(MyMainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(MyMainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(MyMainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//              //  new MyMainClient().setVisible(true);
//            }
//        });
//    }
    //setter getter methods
    
    public NewJInternalFrame getInternalFrame()
    {
    	return intnlClient;
    }
    
    public void setInternalFrame(String roomName, String botName, int botId, Room roomObj, RummyBot rummyGlobalObj)
    {
    	intnlClient = new NewJInternalFrame();
    	intnlClient.setRoomName(roomName, botName);
    	intnlClient.setBotId(botId);
    	intnlClient.setRoomObject(roomObj);
    	intnlClient.setRummyGlobalObj(rummyGlobalObj);
    } 
    public void setInternalFrame(String roomName, String botName, int botId, Room roomObj, RequestIniatiator reqinitGlobalObj)
    {
    	intnlClient = new NewJInternalFrame();
    	intnlClient.setRoomName(roomName, botName);
    	intnlClient.setBotId(botId);
    	intnlClient.setRoomObject(roomObj);
    	intnlClient.setReqinitGlobalObj(reqinitGlobalObj);
    } 
    public void setInternalFrame(String roomName, String botName, int botId, Room roomObj, ChildRummyBot childRummyGlobalObj)
    {
    	intnlClient = new NewJInternalFrame();
    	intnlClient.setRoomName(roomName, botName);
    	intnlClient.setBotId(botId);
    	intnlClient.setRoomObject(roomObj);
    	intnlClient.setChildRummyGlobalObj(childRummyGlobalObj);
    } 
    public void updateString(String msg)
    {
    	intnlClient.updateString(msg);
    }
    
    public void updateDealerChat(String dealerChat)
    {
    	intnlClient.updateDealerChat(dealerChat);
    }
    
    public MinimizingPanel getJPanelObj() {
		return jPanelObj;
	}

	public void setJPanelObj(MinimizingPanel panelObj) {
		jPanelObj = panelObj;
	}
	
	
	  public CopyOnWriteArrayList<CustomizedButton> getButtonObj() {
			return CustomizedButtonArrayList;
		}

		public void setButtonObj(CustomizedButton buttonObj) {
			CustomizedButtonArrayList.add(buttonObj);
		}
		
		public MyDesktopPane getDesktopPane() {
			return desktopPane;
		}

		public void setDesktopPane(MyDesktopPane desktopPane) {
			this.desktopPane = desktopPane;
		}

		public CopyOnWriteArrayList<NewJInternalFrame> getInternalFramesArrayList() {
			return internalFramesArrayList;
		}

		public void setInternalFramesArrayList(
				CopyOnWriteArrayList<NewJInternalFrame> internalFramesArrayList) {
			this.internalFramesArrayList = internalFramesArrayList;
		}
    // Variables declaration - do not modify
    // End of variables declaration
}