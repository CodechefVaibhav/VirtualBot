package sfs2x.client.example;

import java.util.ArrayList;
import java.util.List;

import sfs2x.client.example.BotCard;

public class SuitListClass {

	public List<List<BotCard>> openEnded = null;
	public List<List<BotCard>> groupedSequences = null;
	public List<List<BotCard>> ungroupedSequesnces = null;
	public List<List<BotCard>> middleOpen = null;
	int type = -1;

	public SuitListClass(int type)
	{
		this.type = type;
		openEnded = new ArrayList<List<BotCard>>();
		groupedSequences = new ArrayList<List<BotCard>>();
		ungroupedSequesnces = new ArrayList<List<BotCard>>();
		middleOpen = new ArrayList<List<BotCard>>();
	}
	
	public List<List<BotCard>> getOpenEnded() {
		return openEnded;
	}
	public void setOpenEnded(List<List<BotCard>> openEnded) {
		this.openEnded = openEnded;
	}
	public List<List<BotCard>> getGroupedSequences() {
		return groupedSequences;
	}
	public void setGroupedSequences(List<List<BotCard>> groupedSequences) {
		this.groupedSequences = groupedSequences;
	}
	public List<List<BotCard>> getUngroupedSequesnces() {
		return ungroupedSequesnces;
	}
	public void setUngroupedSequesnces(List<List<BotCard>> ungroupedSequesnces) {
		this.ungroupedSequesnces = ungroupedSequesnces;
	}
	public List<List<BotCard>> getMiddleOpen() {
		return middleOpen;
	}
	public void setMiddleOpen(List<List<BotCard>> middleOpen) {
		this.middleOpen = middleOpen;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
