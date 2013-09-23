package NewClient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class CustomizedButton extends JButton implements ActionListener {

	NewJInternalFrame internalFrame = null;
	
	public CustomizedButton(NewJInternalFrame internalFrame)
	{
		this.setText(internalFrame.getRoomName());
		this.internalFrame = internalFrame;
		this.addActionListener(this);
	}
	
	public NewJInternalFrame getInternalFrame() {
		return internalFrame;
	}

	public void setInternalFrame(NewJInternalFrame internalFrame) {
		this.internalFrame = internalFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(internalFrame!=null && !internalFrame.isVisible())
		 internalFrame.setVisible(true);
	}
}
