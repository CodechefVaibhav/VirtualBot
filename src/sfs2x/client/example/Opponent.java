package sfs2x.client.example;

import common.Card;

public class Opponent {

	private int playerId;
	private Card card = null;
	private int pickCount = 0 ;
	
	public Opponent(int playerId, Card card, int pickCount)
	{
		this.playerId = playerId;
		this.card = card;
		this.pickCount = pickCount;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public int getPickCount() {
		return pickCount;
	}

	public void setPickCount(int pickCount) {
		this.pickCount = pickCount;
	}
	
	public void incrementPickCount()
	{
		++pickCount;
	}
	
	
}
