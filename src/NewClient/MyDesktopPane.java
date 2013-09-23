package NewClient;

import javax.swing.JDesktopPane;

public class MyDesktopPane extends JDesktopPane{

	private static volatile MyDesktopPane deskTopPaneObj = null; 
	private NewJInternalFrame internalFrame = null;
	
	private MyDesktopPane()
	{
		super();	
		this.setVisible(true);
	}
	
	public static MyDesktopPane getMyDesktopPaneInstance()
	{
		if(deskTopPaneObj == null)
		{
			deskTopPaneObj = new MyDesktopPane();
		}
		
		return deskTopPaneObj;
	}
	
	public NewJInternalFrame getMainClientObj() {
		return internalFrame;
	}

	public void NewJInternalFrame(NewJInternalFrame internalFrame) {
		this.internalFrame = internalFrame;
	}
}
