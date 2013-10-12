package sfs2x.client.example;

import common.Card;
import common.Face;
import common.Suit;

import java.util.List;
import java.util.ArrayList;

public class BotCard extends Card {

	public Face face;	
	public Suit suit;
	//private Card c = null;
	private List<Integer> chunkIdList = new ArrayList<Integer>();
	private float utilityValue;
	
	public BotCard()
	{}
	
	public BotCard(final Face face, final Suit suit) {
		super(face,suit);
		this.face = face;
		this.suit = suit;
	}	
	
//	public Card getC() {
//		return c;
//	}
//	public void setC(Card c) {
//		this.c = c;
//	}
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
	
	public Suit getSuit() {
		return suit;
	}

	public void setSuit(Suit suit) {
		this.suit = suit;
	}

	public String toString()
	{
		return suit.toString()+face.toString();
	}
}
