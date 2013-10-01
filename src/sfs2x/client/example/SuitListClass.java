package sfs2x.client.example;

import java.util.ArrayList;
import java.util.List;

import common.Card;

public class SuitListClass {

	public List<List<Card>> openEnded = null;
	public List<List<Card>> groupedSequences = null;
	public List<List<Card>> ungroupedSequesnces = null;
	public List<List<Card>> middleOpen = null;
	int type = -1;

	public SuitListClass(int type)
	{
		this.type = type;
		openEnded = new ArrayList<List<Card>>();
		groupedSequences = new ArrayList<List<Card>>();
		ungroupedSequesnces = new ArrayList<List<Card>>();
		middleOpen = new ArrayList<List<Card>>();
	}
	
	public List<List<Card>> getOpenEnded() {
		return openEnded;
	}
	public void setOpenEnded(List<List<Card>> openEnded) {
		this.openEnded = openEnded;
	}
	public List<List<Card>> getGroupedSequences() {
		return groupedSequences;
	}
	public void setGroupedSequences(List<List<Card>> groupedSequences) {
		this.groupedSequences = groupedSequences;
	}
	public List<List<Card>> getUngroupedSequesnces() {
		return ungroupedSequesnces;
	}
	public void setUngroupedSequesnces(List<List<Card>> ungroupedSequesnces) {
		this.ungroupedSequesnces = ungroupedSequesnces;
	}
	public List<List<Card>> getMiddleOpen() {
		return middleOpen;
	}
	public void setMiddleOpen(List<List<Card>> middleOpen) {
		this.middleOpen = middleOpen;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
