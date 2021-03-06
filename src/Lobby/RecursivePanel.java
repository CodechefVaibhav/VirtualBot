/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RecursivePanel.java
 *
 * Created on Sep 4, 2013, 10:51:20 AM
 */
package Lobby;

import java.util.Iterator;
import java.util.List;

import BotLogin.RequestIniatiator;
import BotLogin.RoomJoinForm;

/**
 *
 * @author vaibhav
 */
public class RecursivePanel extends javax.swing.JPanel {

	private String roomName;
	private List playerNames;
	private RoomJoinForm rjfObj = null;
	//RequestIniatiator loginObj = null;
	/** Creates new form RecursivePanel */
	public RecursivePanel() {
		initComponents();
		jButton2.setVisible(false);
		this.setVisible(true);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jSeparator1 = new javax.swing.JSeparator();

		setEnabled(false);

		jLabel1.setText("Room ID:");

//		jLabel2.setText("RING6#456852");

//		jLabel3.setText("0 Players");

		jButton1.setText("Join");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jButton2.setFont(new java.awt.Font("Tahoma", 0, 10));
		jButton2.setLabel("Wait");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addComponent(
												jLabel1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												52,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jLabel2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												115,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								layout
										.createSequentialGroup()
										.addComponent(
												jLabel3,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												121,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jButton1)
														.addComponent(
																jButton2,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																54,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()).addComponent(
								jSeparator1,
								javax.swing.GroupLayout.DEFAULT_SIZE, 193,
								Short.MAX_VALUE));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel1)
														.addComponent(jLabel2))
										.addGap(18, 18, 18)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel3,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				134,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED))
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addComponent(
																				jButton1)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addComponent(
																				jButton2)
																		.addGap(
																				43,
																				43,
																				43)))
										.addComponent(
												jSeparator1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												10,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));

		jLabel3.getAccessibleContext().setAccessibleName("jLabel3");
	}// </editor-fold>
	//GEN-END:initComponents

	public void setRoomName(String roomName) {
		this.roomName = roomName;
		jLabel2.setText(roomName);
	}

	public void displayButtons(boolean flag) {
		if (!flag) {
			jButton2.setVisible(true);
		}
	}

	public void renderPlayersNames(List playerNames) {
		String names="<html><body>";
		Iterator itr = playerNames.iterator();
		if(itr!=null)
		{
			while(itr.hasNext())
			{
				names= names+"<br>"+(String)itr.next();
			}
		}
		else
		{
			names="0 Players";
		}
		names = names+"</body></html>";
		jLabel3.setText(names);
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		//loginObj = new RequestIniatiator();
		//loginObj.setLoginnum(2);
		RoomJoinForm rjfObj = RoomJoinForm.getRoomJoinFormInstance();
		String roomName = jLabel2.getText();
		if(roomName!=null && roomName!="")
		{
//			if(rjfObj!=null && !rjfObj.amIVisible())
//			{
//				rjfObj.setVisible(true);
//			}
			rjfObj.setVisible(true);
			rjfObj.prepareRequestInitiator(roomName);
		}
		//rjfObj = new RoomJoinForm(loginObj);
	}

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
	// TODO add your handling code here:
	}//GEN-LAST:event_jButton2ActionPerformed

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JSeparator jSeparator1;
	// End of variables declaration//GEN-END:variables
}
