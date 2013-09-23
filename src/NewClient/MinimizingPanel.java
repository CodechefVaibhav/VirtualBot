package NewClient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

public class MinimizingPanel extends JScrollPane{
	JPanel jpObj = null;
	public MinimizingPanel(JPanel jpObj)
	{
		super(jpObj);
		this.jpObj = jpObj;
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		//this.setvi
//		this.setBackground(Color.BLACK);
//		jpObj.setBackground(Color.BLACK);
		this.setPreferredSize(new Dimension( 1000,70));
//		JScrollPane scrollFrame = new JScrollPane(this);
//		this.setAutoscrolls(true);
//		scrollFrame.setPreferredSize(new Dimension( 800,300));
//		this.getRootPane().getcon
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createLineBorder(Color.RED)));
		//this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		//this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Minized Rooms"));
		//this.setLayout(new FlowLayout());
		this.getViewport().setLayout(new FlowLayout());
   	    this.setVisible(true);
		new ComponentMover().registerComponent(this);
	}


	public void removeButtonByName(CustomizedButton btn)
	{
		jpObj.remove(btn);
		//this.getViewport().remove(btn);
	}

}
