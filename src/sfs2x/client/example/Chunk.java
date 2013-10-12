package sfs2x.client.example;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import sfs2x.client.example.BotCard;


public class Chunk implements Comparator<Chunk> {

	private List<BotCard> chunkCards = new ArrayList<BotCard>();
	private boolean isSet = false;
	private boolean isPure = false;
	private boolean isSequence = false;
	private int id = -1 ;
	private int suiteTpe = -1;
	private int chunkCardsSum =0;
	
	public Chunk()
	{
		
	}
	
	public Chunk(int id)
	{
		this.id = id;
	}
	
	public List<BotCard> getChunkCards() {
		return chunkCards;
	}
	
	public void setChunkCards(List<BotCard> chunkCards) {
		this.chunkCards = chunkCards;
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

	public int getChunkCardsSum() {
		return chunkCardsSum;
	}

	public boolean isSet() {
		return isSet;
	}
	
	public void setSet(boolean isSet) {
		this.isSet = isSet;
	}
	
	public boolean isPure() {
		return isPure;
	}
	
	public void setPure(boolean isPure) {
		this.isPure = isPure;
	}
	
	public boolean isSequence() {
		return isSequence;
	}
	
	public void setSequence(boolean isSequence) {
		this.isSequence = isSequence;
	}
	
	public void setChunkCardsSum(int chunkCardsSum) {
		this.chunkCardsSum = chunkCardsSum;
	}

	@Override
	public int compare(Chunk o1, Chunk o2) {
		return o1.getChunkCardsSum() > o2.getChunkCardsSum()? -1 : 
			o1.getChunkCardsSum() == o2.getChunkCardsSum()?0:1;
	}
	
}