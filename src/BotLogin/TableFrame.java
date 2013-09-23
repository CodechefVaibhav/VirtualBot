package BotLogin;

import javax.swing.JFrame;

public class TableFrame extends JFrame {

	private static volatile TableFrame tableFrameObj = null;
	
	private TableFrame(String title)
	{
		super(title);
	}
	
	public static TableFrame getTabelFrameInstance(String title)
	{
		
		if(tableFrameObj == null)
		{
			tableFrameObj = new TableFrame(title);
		}
		
		return tableFrameObj;
	}
}
