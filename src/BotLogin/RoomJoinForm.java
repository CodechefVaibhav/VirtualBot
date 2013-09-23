/*
 * RoomJoinForm.java
 *
 * Created on __DATE__, __TIME__
 */

package BotLogin;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 *
 * @author  vaibhav
 */
public class RoomJoinForm extends javax.swing.JFrame {

	RequestIniatiator reqInitObj = null;
	String roomName = null;
	private static volatile RoomJoinForm rjfObj = null;

	/** Creates new form RoomJoinForm */
	//	public RoomJoinForm(RequestIniatiator reqInitObj) {
	//		
	//		this.reqInitObj = reqInitObj;
	//		
	//	}
	private RoomJoinForm() {
		initComponents();
		this.getRootPane().setDefaultButton(jButton1);
		this.setTitle("Bot Login Form");
		this.setVisible(true);
		jLabel5.setVisible(false);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
			}
		});
	}

	public static RoomJoinForm getRoomJoinFormInstance() {
		if (rjfObj == null) {
			rjfObj = new RoomJoinForm();
		}
		return rjfObj;
	}

	public void prepareRequestInitiator(String roomName) {
		jTextField2.setText(roomName);
		this.roomName = roomName;
		reqInitObj = new RequestIniatiator();
		reqInitObj.setLoginnum(2);
	}

	public boolean amIVisible() {
		Container c = getParent();
		while (c != null)
			if (!c.isVisible())
				return false;
			else
				c = c.getParent();
		return true;
	}

	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jButton1 = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		jPasswordField1 = new javax.swing.JPasswordField();
		jLabel4 = new javax.swing.JLabel();
		jTextField2 = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		jButton1.setText("Join");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		jButton1.addKeyListener(new java.awt.event.KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent evt)
			{
				if(evt.getKeyCode() == KeyEvent.VK_ENTER)
				{
					jButton1KeyActionPerformed(evt);
				}
			}
		});

		jLabel1.setText("Bot Login Panel");

		jLabel2.setText("UserName");

		jLabel3.setText("Password");

		jLabel4.setText("RoomName");

		jLabel5.setForeground(new java.awt.Color(255, 0, 51));
		jLabel5.setText("Username or Password Left Blank!!!");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addGap(
																				77,
																				77,
																				77)
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel3)
																						.addComponent(
																								jLabel4)
																						.addComponent(
																								jLabel2))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								jTextField1,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								139,
																								Short.MAX_VALUE)
																						.addComponent(
																								jPasswordField1)
																						.addComponent(
																								jTextField2)
																						.addComponent(
																								jButton1,
																								javax.swing.GroupLayout.Alignment.TRAILING)))
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addGap(
																				160,
																				160,
																				160)
																		.addComponent(
																				jLabel1)))
										.addContainerGap(104, Short.MAX_VALUE))
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addContainerGap(
										119, Short.MAX_VALUE).addComponent(
										jLabel5).addGap(86, 86, 86)));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addGap(27, 27, 27)
										.addComponent(jLabel1)
										.addGap(33, 33, 33)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jTextField1,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																19,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel2))
										.addGap(27, 27, 27)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jPasswordField1,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jLabel3))
										.addGap(26, 26, 26)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel4)
														.addComponent(
																jTextField2,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												13, Short.MAX_VALUE)
										.addComponent(jLabel5).addGap(18, 18,
												18).addComponent(jButton1)
										.addGap(38, 38, 38)));

		pack();
	}// </editor-fold>
	//GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		String userName = jTextField1.getText();
		String password = jPasswordField1.getText();
		if ((userName != null && !userName.equals(""))
				&& (password != null && !password.equals(""))
				&& (roomName != null && !roomName.equals(""))) {
			jLabel5.setVisible(false);
			reqInitObj.setRoomName(roomName);
			reqInitObj.setChipType("dummy");
			reqInitObj.setRoomName(jTextField2.getText());
			//reqInitObj.initiateLogin(userName, password);
			//reqInitObj.joinRoom(reqInitObj.getRoomName());
			reqInitObj.startBot(userName, password);

			this.setVisible(false);
			jTextField1.setText("");
			jPasswordField1.setText("");
		}
		else
		{
			jLabel5.setVisible(true);
		}
	}
	
	private void jButton1KeyActionPerformed(KeyEvent evt)
	{
		String userName = jTextField1.getText();
		String password = jPasswordField1.getText();
		if ((userName != null && !userName.equals(""))
				&& (password != null && !password.equals(""))
				&& (roomName != null && !roomName.equals(""))) {
			jLabel5.setVisible(false);
			reqInitObj.setRoomName(roomName);
			reqInitObj.setChipType("dummy");
			reqInitObj.setRoomName(jTextField2.getText());
			//reqInitObj.initiateLogin(userName, password);
			//reqInitObj.joinRoom(reqInitObj.getRoomName());
			reqInitObj.startBot(userName, password);

			this.setVisible(false);
			jTextField1.setText("");
			jPasswordField1.setText("");
		}
		else
		{
			jLabel5.setVisible(true);
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		//		java.awt.EventQueue.invokeLater(new Runnable() {
		//			public void run() {
		//				new RoomJoinForm().setVisible(true);
		//			}
		//		});
	}

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JPasswordField jPasswordField1;
	private javax.swing.JTextField jTextField1;
	private javax.swing.JTextField jTextField2;
	// End of variables declaration//GEN-END:variables

}