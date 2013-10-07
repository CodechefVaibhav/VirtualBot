package sfs2x.client.example;

import common.Card;
import java.util.List;
import java.util.ArrayList;

public class BotCard {

	private Card c = null;
	private List<Integer> chunkIdList = new ArrayList<Integer>();
	private float utilityValue;
	
	public BotCard()
	{
		
	}
	
	public Card getC() {
		return c;
	}
	public void setC(Card c) {
		this.c = c;
	}
	public List<Integer> getChunkIdList() {
		return chunkIdList;
	}
	public void setChunkIdList(List<Integer> chunkIdList) {
		this.chunkIdList = chunkIdList;
	}
	public float getUtilityValue() {
		return utilityValue;
	}
	public void setUtilityValue(float utilityValue) {
		this.utilityValue = utilityValue;
	}
}
