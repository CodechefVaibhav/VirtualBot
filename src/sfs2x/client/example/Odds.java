package sfs2x.client.example;

import sfs2x.client.example.PokerBotAI.Rank;

public class Odds 
{
	private int outs;
	private String desc;
	private String has;
	private String goal;
	private Rank hand;
	private boolean hasHand=false;
	
	public void setOuts(int outs) {
		this.outs = outs;
	}
	public int getOuts() {
		return outs;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
	}
	public void setHas(String has) {
		this.has = has;
	}
	public String getHas() {
		return has;
	}
	public void setGoal(String goal) {
		this.goal = goal;
	}
	public String getGoal() {
		return goal;
	}
	public void setHand(Rank hand) {
		this.hand = hand;
	}
	public Rank getHand() {
		return hand;
	}
	public void setHasHand(boolean hasHand) {
		this.hasHand = hasHand;
	}
	public boolean isHasHand() {
		return hasHand;
	}
	
}
