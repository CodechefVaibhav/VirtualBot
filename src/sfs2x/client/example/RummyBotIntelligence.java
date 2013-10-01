package sfs2x.client.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import common.Card;
import common.Face;
import common.Suit;

public class RummyBotIntelligence {

	private List<Card> cardlist = null;
	private List<List<Card>> groupedCards=new ArrayList<List<Card>>();
	private List<List<Card>> ungroupedCards=new ArrayList<List<Card>>();
		private List<Card> jokerList = new ArrayList<Card>();
	private SuitListClass suitClassObj = null;
	private SuitListClass[] suitTypeArray = null; 
	private Card cutJoker = null;
	//private List
	
	public RummyBotIntelligence(RummyBot bot)
	{
		
	}
	
	public void onHandCards(List<Card> handCards)
	{
		cardlist = handCards;
		instantiateSuiteClass();
		 /*List<Card>suitSortedCards =*/ sortBySuit(cardlist);
		// List<Card>faceSortedCards = sortByFace(suitSortedCards);
/*		 for(int i=0 ; i<suitSortedCards.size() ; i++)
		 {
			 Card c = suitSortedCards.get(i);
			 System.out.println(i + " "+ c.face.value +"   "+c.suit.value);
		 }*/
	}
	
	public void instantiateSuiteClass()
	{
		suitTypeArray = new SuitListClass[4];
		for(int i=0; i<suitTypeArray.length ;i++)
		{
			suitClassObj = new SuitListClass(i);
			suitTypeArray[i] = suitClassObj;
		}
	}
	
	public SuitListClass getSuitListClassBySuitType(int type)
	{
		SuitListClass suitClassByType = null;
		if(suitTypeArray!=null && suitTypeArray.length==4)
		{
			suitClassByType = suitTypeArray[type];
		}
		return suitClassByType;
	}
	
	private List<Card> sortBySuit(List<Card> cards)
	{
		//Sorts them by suit.
		List<Card> sortedCards=new ArrayList<Card>();
		
		List<Card> type0=new ArrayList<Card>();
		List<Card> type1=new ArrayList<Card>();
		List<Card> type2=new ArrayList<Card>();
		List<Card> type3=new ArrayList<Card>();
		
		for(Card c : cards)
		{
			switch(c.suit.value)
			{
				case 0:
					type0.add(c);
					break;
				case 1:
					type1.add(c);
					break;
				case 2:
					type2.add(c);
					break;
				case 3:
					type3.add(c);
					break;
			}
		}
		//List<Card> type0FaceSorted = sortBySuit(type0);
		distributeCardsInVariousLists(getSuitListClassBySuitType(0),sortByFace(type0));
		//sortedCards.addAll(sortedCards.size(),sortByFace(type0));
		//sortedCards.addAll(sortedCards.size(),sortByFace(type1));
		distributeCardsInVariousLists(getSuitListClassBySuitType(1),sortByFace(type1));
		//sortedCards.addAll(sortedCards.size(),sortByFace(type2));
		distributeCardsInVariousLists(getSuitListClassBySuitType(2),sortByFace(type2));
		//sortedCards.addAll(sortedCards.size(),sortByFace(type3));
		distributeCardsInVariousLists(getSuitListClassBySuitType(3),sortByFace(type3));
		
		//returns card sorted by suit
		return sortedCards;
	}
	
	
	private void distributeCardsInVariousLists( SuitListClass suitClassObj, List<Card> typeNFaceSorted)
	{
		
		for(int i=0 ; i<typeNFaceSorted.size(); i++)
		{
			List<Card> cardTemp = new  ArrayList<Card>();
			
			if((typeNFaceSorted.size()-1)== i)
			{
				Card c1 = typeNFaceSorted.get(i);
				cardTemp.add(c1);
				suitClassObj.ungroupedSequesnces.add(cardTemp);
			}
			else
			{
				Card c1 = typeNFaceSorted.get(i);
				Card c2 = typeNFaceSorted.get(++i);
				int diff = c2.face.value - c1.face.value;
				cardTemp.add(c1);
				cardTemp.add(c2);
				if(diff==1)
				{
					suitClassObj.openEnded.add(cardTemp);
				}
				else if(diff ==2)
				{
					suitClassObj.middleOpen.add(cardTemp);
				}
				else if(diff == 0)
				{
					suitClassObj.ungroupedSequesnces.add(cardTemp);
				}	
			}
		}
		
		List<List<Card>> openended = suitClassObj.getOpenEnded();
		for(int j=0; j<openended.size(); j++)
		{   if(!(openended.size()-1 == j))
			{
				List<Card> one = openended.get(j);
				List<Card> two = openended.get(++j);
				Card c1 = one.get((one.size()-1));
				Card c2 = two.get(0);
				int temp = c1.face.value;
				++temp;
				if(temp==c2.face.value)
				{
					one.addAll(two);
					suitClassObj.getOpenEnded().remove(two);
					suitClassObj.getGroupedSequences().add(one);
					suitClassObj.getOpenEnded().remove(one);
				}
			}
		}
		
		display(suitClassObj);
			
	}
		
	
	public void display(SuitListClass suitClassObj)
	{
		List<List<Card>> open = suitClassObj.getOpenEnded();
		for(int i=0 ;i<open.size(); i++)
		{
			List<Card> c = open.get(i);
			for(int j=0 ; j<c.size(); j++)
			{
				Card ctemp = c.get(j);
				System.out.println(ctemp.face.value +" # "+ctemp.suit.value);
			}
		}
		
		List<List<Card>> group = suitClassObj.getGroupedSequences();
		for(int i=0 ;i<group.size(); i++)
		{
			List<Card> c = group.get(i);
			for(int j=0 ; j<c.size(); j++)
			{
				Card ctemp = c.get(j);
				System.out.println(ctemp.face.value +" # "+ctemp.suit.value);
			}
		}
		
		List<List<Card>> middle = suitClassObj.getMiddleOpen();
		for(int i=0 ;i<middle.size(); i++)
		{
			List<Card> c = middle.get(i);
			for(int j=0 ; j<c.size(); j++)
			{
				Card ctemp = c.get(j);
				System.out.println(ctemp.face.value +" # "+ctemp.suit.value);
			}
		}
		
		List<List<Card>> ungroup = suitClassObj.getUngroupedSequesnces();
		for(int i=0 ;i<ungroup.size(); i++)
		{
			List<Card> c = ungroup.get(i);
			for(int j=0 ; j<c.size(); j++)
			{
				Card ctemp = c.get(j);
				System.out.println(ctemp.face.value +" # "+ctemp.suit.value);
			}
		}
	}
			
			
		
		
		
//		boolean first = true;
//		int firstNum =-1;
//		int temp =-1;
//		for(int i=0 ; i<typeNFaceSorted.size(); i++)
//		{
//			List<Card> myList = new ArrayList<Card>();
//			if(first)
//			{
//				Card c = typeNFaceSorted.get(i);
//				firstNum = c.getFace().value;
//				myList.add(c);
//				++temp;
//				first = false;
//			}
//			
//			if(temp == typeNFaceSorted.get(i).face.value)
//			{
//				myList.add(typeNFaceSorted.get(i));
//				++temp;
//			}
//			else
//			{
//				myList.add(null);
//				myList.add(typeNFaceSorted.get(i));
//				++temp;
//			}
	
	private List<Card> sortByFace(List<Card> cards)
	{
		//Sorts them by Face.
		List<Card> sortedCards=new ArrayList<Card>();
		
		for(Card c :cards)
		{
			if(sortedCards.size() == 0)
			{
				sortedCards.add(c);
			}
			else
			{
				for(int i=0;i<sortedCards.size(); i++)
				{
					Card cardObj=sortedCards.get(i);
					if(cardObj.getFace().getValue() >= c.getFace().getValue())
					{
						sortedCards.add(i,c);
						break;
					}
					else
					{
						//find next larger face index
						int j=i+1;
						while( j<sortedCards.size() && sortedCards.get(j).face.value < c.face.value )
						{
							j++;
						}
						sortedCards.add(j,c);
						break;
					}
				}
			}
		}
		//returns card sorted by face value and grouped by suit.
		cards.clear();
		cards.addAll(sortedCards);
		return cards;
	}
	
	public void setJokerCard(Card jokerCard) {
		this.cutJoker = jokerCard;
	}
	
	public Card getJokerCard() {
		
		return cutJoker;
	}
	
	
	
	
	private boolean checkForPureImpure(List<Card> cards, boolean checkForPure)
	{
		boolean flag = false;
		int tempFaceValue = -1;
		boolean firstFace = true;
		boolean isPure = true;
		List<Card> cardsTemp = sortByFace(cards);
		if(checkForPure)
		{
			faceLoop :for(Card crd :  cardsTemp)
			{
				if(firstFace)
				{
					tempFaceValue = crd.face.value;
					tempFaceValue++;
					firstFace = false;
				}
				else
				{
					if(tempFaceValue == crd.face.value)
					{
						System.out.println("isPure");
						tempFaceValue = ++tempFaceValue;
					}
					else
					{
						isPure =  false;
						break faceLoop;
					}
				}
			}
			
		}
		
		boolean isImpurePure = false;
			if(!checkForPure){
			faceLoop :for(Card crd :   cardsTemp)
			{
				if(firstFace)
				{   
					if(isJoker(crd))
					{
						
					}
					else
					{
						tempFaceValue = crd.face.value;
						tempFaceValue++;
					}
					firstFace = false;
				}
				else
				{
					if(isJoker(crd))
					{
						tempFaceValue = ++tempFaceValue;
					}
					else if(tempFaceValue == crd.face.value)
					{
						System.out.println("isImPure");
						tempFaceValue = ++tempFaceValue;
					}
					else
					{
						isImpurePure =  true;
						break faceLoop;
					}
				}
			}
		}
		
			if(checkForPure)
			{
				flag = isPure;
			}
			else
			{
				flag = isImpurePure;
			}
		return flag;
	}
	
	private boolean hasSameSuit(List<Card> cards)
	{
		boolean first = true;
		boolean isAllSuitSame = true;
		int suitValue = -1;
		suitloop : for(Card c :cards)
		{
			if(first)
			{
				suitValue = c.suit.value;
				first = false;
			}
			else
			{
				if(c.suit.value == suitValue)
				{
					System.out.println("SuitSame");
				}
				else
				{
					isAllSuitSame = false;
					break suitloop;
				}
			}
			
		}
		return isAllSuitSame;
	}
	
	
	private boolean isPureSequesnce(List<Card> cards)
	{
		
		boolean isPure = false;
		boolean isImpure = false;
		boolean flag = false;
		boolean isAllSuitSame = hasSameSuit(cards);
		
		if(isAllSuitSame){
			isPure = checkForPureImpure(cards,true);
			if(!isPure)
			isImpure = checkForPureImpure(cards, false);
		}
		
		if(isPure && isAllSuitSame)
		{
			flag = true;
		}
		else if(!isImpure && isAllSuitSame)
		{
			flag = false;
		}
		
		return flag;
	}
	
	private boolean isJoker(Card c) {
		//returns true if the card is a joker or cutjoker.
		if (getJokerCard() == null) return false;
		if (c.getFace().getValue() == getJokerCard().getFace().getValue())
			return true;
		
		//paper joker
		if (c.getFace().getValue() == 21) return true;
		return false;
	}
	
	private boolean isPaperJoker(Card c) {
		//returns true if the card is a joker or cutjoker.
		if (c.getFace().getValue() == 21) return true;
		return false;
	}
	
	private boolean isValidSet(List<Card> cards)
	{
		boolean flag=true;	
		 
		for(int i=0;i<cards.size();i++)
		{
			Card compCard=cards.get(i);
			if(! isJoker(compCard))
			{
				for(int j=0;j<cards.size();j++)
				{
					if(i != j)
					{
						
						Card card=cards.get(j);
						if(!isJoker(card))
						{
							if(compCard.getSuit().getValue() == card.getSuit().getValue())
							{//check if cards have different suits
								flag=false;
								return flag;
							}
						}
						else
						{
							continue;
						}
						
						
						if(compCard.getFace().getValue()!=card.getFace().getValue() )
						{//check if cards are of different face values
							flag=false;
							return flag;
						}
						
					}					
				}
			}	
			else
			{
				//if a joker card then continue;
				continue;
			}
		}		
		return flag;
	}


	
	
	
	public SuitListClass getSuitClassObj() {
		return suitClassObj;
	}

	public void setSuitClassObj(SuitListClass suitClassObj) {
		this.suitClassObj = suitClassObj;
	}

	public static void main(String... crd)
	{
		RummyBotIntelligence rc = new RummyBotIntelligence(null);
		List<Card> list=new ArrayList<Card>();
		
		Face f4=new Face();
		Suit s4=new Suit();
		f4.setValue(2);
		s4.setValue(1);
		
		Card c4=new Card();
		c4.setFace(f4);
		c4.setSuit(s4);
		list.add(c4);
		
		Face f5=new Face();
		Suit s5=new Suit();
		f5.setValue(4);
		s5.setValue(1);
		
		Card c5=new Card();
		c5.setFace(f5);
		c5.setSuit(s5);
		list.add(c5);
		
		Face f6=new Face();
		Suit s6=new Suit();
		f6.setValue(6);
		s6.setValue(2);
		
		Card c6=new Card();
		c6.setFace(f6);
		c6.setSuit(s6);
		list.add(c6);
		
		Face f7=new Face();
		Suit s7=new Suit();
		f7.setValue(8);
		s7.setValue(2);
		
		Card c7=new Card();
		c7.setFace(f7);
		c7.setSuit(s7);
		list.add(c7);
		
		Face f1=new Face();
		Suit s1=new Suit();
		f1.setValue(4);
		s1.setValue(0);
		
		Card c1=new Card();
		c1.setFace(f1);
		c1.setSuit(s1);
		list.add(c1);
		
		Face f2=new Face();
		Suit s2=new Suit();
		f2.setValue(10);
		s2.setValue(0);
		
		Card c2=new Card();
		c2.setFace(f2);
		c2.setSuit(s2);
		list.add(c2);
		
		rc.setJokerCard(c2);
		
		Face f3=new Face();
		Suit s3=new Suit();
		f3.setValue(3);
		s3.setValue(0);
		
		Card c3=new Card();
		c3.setFace(f3);
		c3.setSuit(s3);
		list.add(c3);
		

		Face f8=new Face();
		Suit s8=new Suit();
		f8.setValue(2);
		s8.setValue(0);
		
		Card c8=new Card();
		c8.setFace(f8);
		c8.setSuit(s8);
		list.add(c8);
		
		rc.onHandCards(list);
		
		list=rc.sortByFace(list);
		if(rc.isPureSequesnce(list))
		{
			System.out.println("PURE SEQUENCE");
		}
		else
		{
			System.out.println("IMPURE SEQUENCE");
		}
		
		if(rc.isValidSet(list))
		{
			System.out.println("Valid ");
		}
		else
		{
			System.out.println("Invalid ");
		}
	}
}
