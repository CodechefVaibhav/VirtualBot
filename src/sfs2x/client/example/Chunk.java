package sfs2x.client.example;

import java.util.List;
import java.util.ArrayList;

import common.Card;


public class Chunk {

	private List<Card> chunkCards = new ArrayList<Card>();
	private boolean isArranged = false;
	private int id = -1 ;
	private int suiteTpe = -1;
	
	public Chunk(int id)
	{
		this.id = id;
	}
	
	public List<Card> getChunkCards() {
		return chunkCards;
	}
	public void setChunkCards(List<Card> chunkCards) {
		this.chunkCards = chunkCards;
	}
	public boolean isArranged() {
		return isArranged;
	}
	public void setArranged(boolean isArranged) {
		this.isArranged = isArranged;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getSuiteTpe() {
		return suiteTpe;
	}

	public void setSuiteTpe(int suiteTpe) {
		this.suiteTpe = suiteTpe;
	}
	
}