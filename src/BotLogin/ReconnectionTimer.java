package BotLogin;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import sfs2x.client.SmartFox;

public class ReconnectionTimer extends Timer {

	int count = 0;
	SmartFox sfs = null;
	RequestIniatiator reqinit = null;
	
	public ReconnectionTimer(SmartFox sfs, RequestIniatiator reqinit)
	{
		this.reqinit = reqinit;
		this.sfs = sfs;
		init();
	}
	public void init()
	{
		scheduleAtFixedRate(new DetectDisconnection(), 1000, 12000);
	}
	
	class DetectDisconnection extends TimerTask
	{
		public void run()
		{
			count++;
			if(count<=15)
			{
//				if(sfs.isConnected())
//				{
				//	JOptionPane.showMessageDialog(null, "Recoonection Successfull");
					reqinit.initiateLogin(reqinit.getUserName(),"sitanshu");
					
//				}
			}
			else
			{
				int command = JOptionPane.showConfirmDialog(null, "Netowrk Connection lost. Terminating Application");
				switch(command)
				{
				case JOptionPane.YES_OPTION:
				case JOptionPane.DEFAULT_OPTION:
					System.exit(0);
				}
			}
			
		}
	}

}
