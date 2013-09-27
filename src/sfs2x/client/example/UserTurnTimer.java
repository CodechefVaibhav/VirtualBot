package sfs2x.client.example;

import java.util.Timer;
import java.util.TimerTask;

import NewClient.NewJInternalFrame;

public class UserTurnTimer extends Timer {

	public UserTurnTimer(NewJInternalFrame obj, int turnTime) {
		//this.cancel();
		scheduleAtFixedRate(new TurnTime(obj,turnTime), 1000, 1000);
	}
	
	class TurnTime extends TimerTask
	{
		int turnTime =0;
		NewJInternalFrame o;
		
		public TurnTime(NewJInternalFrame o, int turnTime)
		{
			this.turnTime = turnTime;
			this.o = o;
		}
		public void run()
		{
			o.updateTimerLabel(Integer.toString(turnTime--));
		}
	}
}
