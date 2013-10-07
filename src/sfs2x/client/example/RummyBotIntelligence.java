package sfs2x.client.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


//import rummy.CardList;

import common.Card;
import common.Face;
import common.Suit;

public class RummyBotIntelligence extends RummyBotLogic {

	ChildRummyBot bot = null;
	
	private List<Card> cardlist = null;
	private List<List<Card>> groupedCards=new ArrayList<List<Card>>();
	private List<List<Card>> ungroupedCards=new ArrayList<List<Card>>();
	private List<Card> jokerList = new ArrayList<Card>();
	
	private SuitListClass suitClassObj = null;
	private SuitListClass[] suitTypeArray = null;
	private List<Chunk> chunkList = new ArrayList<Chunk>();
	private List<Chunk> readyChunks = new ArrayList<Chunk>();
	
	private Card cutJoker = null;
	private CardList cardListObj = null;
	
	private boolean isPickedMakesPure = false;
	private boolean isPickedMakesSequence = false;
	private boolean isPickedMakesSet = false;
	int chunkincr = 0;
	
	Chunk chunkTemp = null;
	
	private int usedSuit = -1;
	//private List
	
	public RummyBotIntelligence(ChildRummyBot bot)
	{
		super(bot);
		this.bot = bot;
	}
	
	public void onHandCards(List<Card> handCards)
	{
		cardlist = handCards;
		//instantiateSuiteClass();
		//instantiateChunk();
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
	
//	public void instantiateChunk()
//	{
//		chunkList = new ArrayList<Chunk>();
//		Chunk chunkObj = new Chunk();
//		chunkObj.setChunkCards(cardlist);
//		chunkObj.setId(0);
//		chunkObj.setArranged(false);
//		chunkList.add(chunkObj);
//	}
	
	public SuitListClass getSuitListClassBySuitType(int type)
	{
		SuitListClass suitClassByType = null;
		if(suitTypeArray!=null && suitTypeArray.length==4)
		{
			suitClassByType = suitTypeArray[type];
		}
		return suitClassByType;
	}
	
	private List<List<Card>> groupByFace(List<Card> cards)
	{
		List<List<Card>> sortedCards = new ArrayList<List<Card>>();
		
		List<Card> two = new ArrayList<Card>();
		List<Card> three = new ArrayList<Card>();
		List<Card> four = new ArrayList<Card>();
		List<Card> five = new ArrayList<Card>();
		List<Card> six = new ArrayList<Card>();
		List<Card> seven = new ArrayList<Card>();
		List<Card> eight = new ArrayList<Card>();
		List<Card> nine = new ArrayList<Card>();
		List<Card> ten = new ArrayList<Card>();
		List<Card> eleven = new ArrayList<Card>();
		List<Card> twelve = new ArrayList<Card>();
		List<Card> thirteen = new ArrayList<Card>();
		List<Card> fourteen = new ArrayList<Card>();
		
		for(Card c : cards)
		{
			switch(c.face.value)
			{
				case 2:
					two.add(c);
					break;
				case 3:
					three.add(c);
					break;
				case 4:
					four.add(c);
					break;
				case 5:
					five.add(c);
					break;
				case 6:
					six.add(c);
					break;
				case 7:
					seven.add(c);
					break;
				case 8:
					eight.add(c);
					break;
				case 9:
					nine.add(c);
					break;
				case 10:
					ten.add(c);
					break;
				case 11:
					eleven.add(c);
					break;
				case 12:
					twelve.add(c);
					break;
				case 13:
					thirteen.add(c);
					break;
				case 14:
					fourteen.add(c);
					break;
					
			}
		}
		
		sortedCards.add(two);
		sortedCards.add(three);
		sortedCards.add(four);
		sortedCards.add(five);
		sortedCards.add(six);
		sortedCards.add(seven);
		sortedCards.add(eight);
		sortedCards.add(nine);
		sortedCards.add(ten);
		sortedCards.add(eleven);
		sortedCards.add(twelve);
		sortedCards.add(thirteen);
		sortedCards.add(fourteen);
		
		return sortedCards;
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
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(0),*/sortByFace(type0)/*)*/;
		//sortedCards.addAll(sortedCards.size(),sortByFace(type0));
		//sortedCards.addAll(sortedCards.size(),sortByFace(type1));
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(1),*/sortByFace(type1)/*)*/;
		//sortedCards.addAll(sortedCards.size(),sortByFace(type2));
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(2),*/sortByFace(type2)/*)*/;
		//sortedCards.addAll(sortedCards.size(),sortByFace(type3));
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(3),*/sortByFace(type3)/*)*/;
		
		//returns card sorted by suit
		return sortedCards;
	}
	
	private void prepareRawChunk(List<Card> typeNFaceSorted)
	{
		if(typeNFaceSorted.size()>0)
		{	
			Card temp = null;
			boolean firstFace = true;
			Chunk chunkObj = getNewChunkObject();
			for(int i=0 ; i<typeNFaceSorted.size(); i++)
			{
				if(firstFace)
				{
					Card c1 = typeNFaceSorted.get(i);
					chunkObj.getChunkCards().add(c1);
					chunkObj.setSuiteTpe(c1.suit.value);
					Card c2 = typeNFaceSorted.get(++i);
					temp = c2;
					int diff = c2.face.value - c1.face.value;
					if(diff==0)
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c2);
						chunkObj.setSuiteTpe(c2.suit.value);
					}
					else if(diff<=2)
					{
						chunkObj.getChunkCards().add(c2);
						chunkObj.setSuiteTpe(c2.suit.value);
					}
					else
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c2);
						chunkObj.setSuiteTpe(c2.suit.value);
					}
					firstFace = false;
				}
				else
				{
					Card c = typeNFaceSorted.get(i);
					int diff = c.face.value - temp.face.value;
					temp = c;
					if(diff<=2)
					{
						chunkObj.getChunkCards().add(c);
						chunkObj.setSuiteTpe(c.suit.value);
					}
					else if(diff==0)
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c);
						chunkObj.setSuiteTpe(c.suit.value);
					}
					else
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c);
						chunkObj.setSuiteTpe(c.suit.value);
					}
				}
			}
		}
	}
	private Chunk getNewChunkObject()
	{
		System.out.println("MAKING NEW CHUNK");
		chunkTemp = new Chunk(++chunkincr);
		chunkList.add(chunkTemp); 
		return chunkTemp;		
	}
	
	private void displayChunks()
	{
		for(int i=0; i<chunkList.size(); i++)
		{
			Chunk chunky = chunkList.get(i);
			System.out.println("CHUNK : "+"SUIT : "+chunky.getSuiteTpe()+ "   CHUNKID : "+chunky.getId());
			List<Card> c = chunky.getChunkCards();
			for(int j=0; j<c.size() ; j++)
			{
				System.out.println("CHUNK : "+c.get(j).face.value+ " # "+ c.get(j).suit.value);
			}
			if(chechForPure(c))
			{
				readyChunks.add(chunky);
				chunkList.remove(chunky);
			}
			
		}
		System.out.println("//////////////////////////////////////");
		displayReadyCards();
	}
	
	private void displayReadyCards()
	{
		for(int i=0; i<readyChunks.size(); i++)
		{
			Chunk chunky = readyChunks.get(i);
			System.out.println("CHUNK : "+"SUIT : "+chunky.getSuiteTpe()+ "   CHUNKID : "+chunky.getId());
			List<Card> c = chunky.getChunkCards();
			for(int j=0; j<c.size() ; j++)
			{
				System.out.println("CHUNK : "+c.get(j).face.value+ " # "+ c.get(j).suit.value);
			}
		}
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
				else
				{
					/**also covers case
					 *  else if(diff == 0)
						{
							suitClassObj.ungroupedSequesnces.add(cardTemp);
						}
					 */
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
		
		List<List<Card>> unGroupedListTemp = suitClassObj.getUngroupedSequesnces();
		for(int k=0; k<unGroupedListTemp.size(); k++)
		{  
			List<Card> jokerTempList = unGroupedListTemp.get(k);
			jokerloop : for(int y=0 ; jokerTempList.size()>0; )
			{
				System.out.println(y);
				Card jokerCheckCard = jokerTempList.get(y);
				if(isJoker(jokerCheckCard))
				{
					jokerList.add(jokerCheckCard);
					jokerTempList.remove(jokerCheckCard);
					//continue jokerloop;
				}
				else
				{
					List<List<Card>> groupedSeqTemp = suitClassObj.getGroupedSequences();
					for(int i=0 ;i<groupedSeqTemp.size(); i++)
					{
						List<Card> c = groupedSeqTemp.get(i);
						for(int j=0 ; j<c.size(); j++)
						{
							Card ctemp = c.get(j);
							int temp = ctemp.face.value;
							++temp;
							if(temp==jokerCheckCard.face.value)
							{
								c.add(jokerCheckCard);
								jokerTempList.remove(jokerCheckCard);
								sortByFace(c);
								continue jokerloop;
								
							}
							//System.out.println("open : "+ctemp.face.value +" # "+ctemp.suit.value);
						}
					}
					
					List<List<Card>> openEndedTemp = suitClassObj.getOpenEnded();
					for(int i=0; i<openEndedTemp.size() ;i++)
					{
						List<Card> c = openEndedTemp.get(i);
						for(int j=0 ; j<c.size(); j++)
						{
							Card ctemp = c.get(j);
							int temp = ctemp.face.value;
							++temp;
							if(temp==jokerCheckCard.face.value)
							{
								c.add(jokerCheckCard);
								sortByFace(c);
								jokerTempList.remove(jokerCheckCard);
								if(chechForPure(c))
								{
									suitClassObj.getGroupedSequences().add(c);
									suitClassObj.getOpenEnded().remove(c);
									continue jokerloop;
								}		
							}
							//System.out.println("open : "+ctemp.face.value +" # "+ctemp.suit.value);
						}
					}
					
					List<List<Card>> middleOpenTemp = suitClassObj.getMiddleOpen();
					for(int i=0; i<middleOpenTemp.size() ;i++)
					{
						List<Card> c = middleOpenTemp.get(i);
						for(int j=0; j<c.size(); j++)
						{
							Card ctemp = c.get(j);
							int temp = ctemp.face.value;
							++temp;
							if(temp==jokerCheckCard.face.value)
							{
								c.add(jokerCheckCard);
								jokerTempList.remove(jokerCheckCard);
								c = sortByFace(c);
								if(checkForPureImpure(c, true))
								{
									suitClassObj.getGroupedSequences().add(c);
									suitClassObj.getMiddleOpen().remove(c);
									continue jokerloop;
								}
								else
								{
									c.remove(jokerCheckCard);
									c = sortByFace(c);
								}
							}
						}
					}
				}
			}
			if(jokerTempList.isEmpty())
			{
				unGroupedListTemp.remove(jokerTempList);
			}
		}
		
		if(!jokerList.isEmpty())
		{
			for(int y=0 ; y<jokerList.size() ; y++)
			{
				List<List<Card>> middleOpenToClubedWithJokers = suitClassObj.getMiddleOpen();
				{
					for(int j=0; j<middleOpenToClubedWithJokers.size(); j++)
					{
						List<Card> middleOpenToClub = middleOpenToClubedWithJokers.get(j);
						middleOpenToClub.add(jokerList.get(y));
						middleOpenToClub = sortByFace(middleOpenToClub);
						if(checkForSequence(middleOpenToClub))
						{
							suitClassObj.getGroupedSequences().add(middleOpenToClub);
							suitClassObj.getMiddleOpen().remove(middleOpenToClub);
							jokerList.remove(jokerList.get(y));
						}
						else
						{
							middleOpenToClub.remove(jokerList.get(y));
							middleOpenToClub = sortByFace(middleOpenToClub);
						}
					}
				}
			}
		}
		
		//display(suitClassObj);
			
	}
		
	
	public void display(/*SuitListClass suitClassObj*/)
	{
		for(int i=0; i<suitTypeArray.length ; i++)
		{
			List<List<Card>> open = suitTypeArray[i].getOpenEnded();
			for(int z=0 ;z<open.size(); z++)
			{
				List<Card> c = open.get(z);
				for(int j=0 ; j<c.size(); j++)
				{
					Card ctemp = c.get(j);
					System.out.println("open : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			List<List<Card>> group = suitTypeArray[i].getGroupedSequences();
			for(int x=0 ;x<group.size(); x++)
			{
				List<Card> c = group.get(x);
				for(int j=0 ; j<c.size(); j++)
				{
					Card ctemp = c.get(j);
					System.out.println("group : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			List<List<Card>> middle = suitTypeArray[i].getMiddleOpen();
			for(int v=0 ;v<middle.size(); v++)
			{
				List<Card> c = middle.get(v);
				for(int j=0 ; j<c.size(); j++)
				{
					Card ctemp = c.get(j);
					System.out.println("middle : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			List<List<Card>> ungroup = suitTypeArray[i].getUngroupedSequesnces();
			for(int b=0 ;b<ungroup.size(); b++)
			{
				List<Card> c = ungroup.get(b);
				for(int j=0 ; j<c.size(); j++)
				{
					Card ctemp = c.get(j);
					System.out.println("ungroup : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			for(int n=0; n<jokerList.size(); n++)
			{
				Card ctemp = jokerList.get(n);
				System.out.println("jokerLIst : "+ctemp.face.value +" # "+ctemp.suit.value);
			}
			
			System.out.println("////////////////////////////////////////////////////////////////////////////////");
		}
	}
			
			
	private boolean checkDiscardedCardIfUseful(Card card)
	{
		
		boolean flag = false;
		if(isJoker(card) && !isCutJokerPickable)
		{
			//cutjoker can only be picked from discarded pile if its very first userturn.
			return flag;
		}
		int discardedCardSuit = card.suit.value;
		SuitListClass suitClassTempObj = suitTypeArray[discardedCardSuit];
		
		List<List<Card>> openEndedTemp = suitClassTempObj.getOpenEnded();
		open: for(int i=0; i<openEndedTemp.size() ;i++)
		{
			    List<Card> open = openEndedTemp.get(i);
				open.add(card);
				open = sortByFace(open);
				if(chechForPure(open))
				{
					usedSuit = card.suit.value;
					isPickedFromDeck = true;
					flag = true;
					open.remove(card);
					open = sortByFace(open);
					break open;
				}
				else if(checkForSequence(open))
				{
					usedSuit = card.suit.value;
					isPickedFromDeck = true;
					flag = true;
					open.remove(card);
					open = sortByFace(open);
					break open;
				}
		}
		
		if(flag)return flag;
		
		List<List<Card>> middleEndedTemp = suitClassTempObj.getMiddleOpen();
		middle: for(int j=0; j<middleEndedTemp.size() ;j++)
		{
			List<Card> middle = middleEndedTemp.get(j);
			middle.add(card);
			middle = sortByFace(middle);
			if(chechForPure(middle))
			{
				usedSuit = card.suit.value;
				isPickedFromDeck = true;
				flag = true;
				middle.remove(card);
				middle = sortByFace(middle);
				break middle;
			}
			else if(checkForSequence(middle))
			{
				usedSuit = card.suit.value;
				isPickedFromDeck = true;
				flag = true;
				middle.remove(card);
				middle = sortByFace(middle);
				break middle;
			}
		}
		
		if(flag)return flag;
		
		List<List<Card>> unGroupedSeqTemp = suitClassTempObj.getUngroupedSequesnces();
		ungroup: for(int k=0 ; k<unGroupedSeqTemp.size(); k++)
		{
			List<Card> ungrouped = unGroupedSeqTemp.get(k);
			ungrouped.add(card);
			ungrouped = sortByFace(ungrouped);
			//
		}
		
		return flag;
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
	
	private boolean chechForPure(List<Card> cards)
	{
		boolean flag= false;
		cardListObj = new CardList();
		cardListObj.list = cards;
		updateJkrCount(cardListObj);
		if(isPure(cardListObj))
		{
			flag = true;
		}
		
		return flag;
	}
	
	private boolean checkForSequence(List<Card> cards)
	{
		boolean flag= false;
		cardListObj = new CardList();
		cardListObj.list = cards;
		updateJkrCount(cardListObj);
		if(isSeq(cardListObj))
		{
			flag = true;
		}
		//System.out.println(flag);
		return flag;
	}
	
	private boolean checkForSet(List<Card> cards)
	{
		boolean flag= false;
		cardListObj = new CardList();
		cardListObj.list = cards;
		updateJkrCount(cardListObj);
		if(isSet(cardListObj))
		{
			flag = true;
		}
		
		return flag;
	}
	
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
		prepareRawChunk(cards);
		return cards;
	}
	
	public void setJokerCard(Card jokerCard) {
		this.cutJoker = jokerCard;
	}
	
	public Card getJokerCard() {
		
		return cutJoker;
	}
	
	
	private boolean duplicateCard(CardList cL, boolean isPure) {
		//System.out.println("In Duplicate Card "+cL.list.toString()+" ");
		
		for (int i = 0; i < cL.length; i++) {
			for (int j = 0; j < cL.length; j++) {
				if (i != j && ((isJoker(cL.list.get(i)) == false && isJoker(cL.list.get(j)) == false)||isPure)) 
				{
					if (cL.list.get(i).getFace().getValue() == cL.list.get(j).getFace().getValue() &&  
							cL.list.get(i).getSuit().getValue() == cL.list.get(j).getSuit().getValue() ) {
						//System.out.println("true");
						return true; 
					}
				}
			}
		}
		//System.out.println("false");
		return false;
	}
	
	private boolean isSeq(CardList cL) {
		//System.out.println("\nIn Is Seq");
		if (cL.list.size() <= 2) {cL.isSeq = false; return false;}
		if (cL.list.size()-cL.jkrCount <=1) {cL.isSeq = true; return true;}
		
		int index = 0;
		Suit s = cL.list.get(index).getSuit();
		while (isJoker(cL.list.get(index)) == true && index <cL.list.size()) index++;
		for (int i = index+1; i < cL.list.size(); i++)
			if (isJoker(cL.list.get(i)) == false && cL.list.get(i).getSuit().getValue() != s.getValue())
			{cL.isSeq = false; return false;}
		
		if (duplicateCard(cL,false) == true) {cL.isSeq = false; return false;}
		
		int[] array = new int[cL.list.size()-cL.jkrCount];
		int k = 0;
		for (index = 0; index < cL.list.size(); index++)
			if (isJoker(cL.list.get(index)) == false) {array[k] = cL.list.get(index).getFace().getValue(); k++;}
		Arrays.sort(array);//sort in increasing order
		if (array[array.length-1]-array[0] <= cL.list.size()-1) {cL.isSeq = true; return true;}

		if (array[array.length-1] == 14) {
			array[array.length-1] = 1;
			Arrays.sort(array);//sort in increasing order
			if (array[array.length-1]-array[0] <= cL.list.size()-1) {cL.isSeq = true; return true;}
		}
		
		cL.isSeq = false;
		return false;
	}
	
	private boolean isSet(CardList cL) {
		//System.out.println("\nIn Is Set");
		if (cL.list.size() <= 2) {cL.isSet = false; return false;}
		if (cL.jkrCount == cL.length) {cL.isSet = true; return true;}
		
		int index = 0;
		while (isJoker(cL.list.get(index)) == true && index <cL.list.size()) index++;
		Face f = cL.list.get(index).getFace();
		for (int i = index+1; i < cL.list.size(); i++)
			if (isJoker(cL.list.get(i)) == false && cL.list.get(i).getFace().getValue() != f.getValue())
			{cL.isSet = false; return false;}

		if (duplicateCard(cL,false) == true) {cL.isSet = false; return false;}
		
		cL.isSet = true;
		return true;
	}
	
	private boolean isPure(CardList cL) {
		//System.out.println("\nIn Is Pure");
		if (cL.list.size() <= 2) {cL.isPure = false; return false;}

		if (cL.paperJkrCount != 0) {cL.isPure = false; return false;}
		
		Suit s = cL.list.get(0).getSuit();
		for (int index = 1; index < cL.list.size(); index++)
			if (s.getValue() != cL.list.get(index).getSuit().getValue()) {cL.isPure = false; return false;}

		if (duplicateCard(cL,true) == true) {cL.isPure = false; return false;}
		
		int[] array = new int[cL.list.size()];
		for (int k = 0; k < cL.list.size(); k++)
			{array[k] = cL.list.get(k).getFace().getValue();}
		Arrays.sort(array);//sort in increasing order
		if (array[array.length-1]-array[0] == cL.list.size()-1) {cL.isPure = true; return true;}

		if (array[array.length-1] == 14) { 
			array[array.length-1] = 1;
			Arrays.sort(array);//sort in increasing order
			if (array[array.length-1]-array[0] == cL.list.size()-1) {cL.isPure = true; return true;}
		}
		
		cL.isPure = false;
		return false;
	}
	
	private void updateJkrCount(CardList cL) {
		//System.out.println("\nIn update jkr count");
		//cL.length = cL.list.size();
		for (int index = 0; index <cL.list.size(); index++) {
			if (cL.list.get(index).getFace().getValue() == getJokerCard().getFace().getValue() || 
					cL.list.get(index).getFace().getValue() == 21)
			{
				cL.jkrCount++; //both cut joker and paper joker
			}			
			
			// paper joker count
			if (cL.list.get(index).getFace().getValue() == 21) cL.paperJkrCount++;			
		}
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
			if(!checkForPure)
			{
				int nonJokerCardCount = 0;
				int jokerCardCount =0;
				for(int z=0 ; z<cardsTemp.size() ; z++)
				{
					Card checkNonJoker = cardsTemp.get(z);
					if(!isJoker(checkNonJoker))
					{
						++nonJokerCardCount;
					}
					else
					{
						++jokerCardCount;
					}
				}
				if(nonJokerCardCount<=2 && jokerCardCount<=1)
				{
					faceLoop :for(Card crd :   cardsTemp)
					{
						if(firstFace)
						{   
							if(isJoker(crd))
							{
								tempFaceValue = -2;
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
								if(tempFaceValue == -2)
								{
									tempFaceValue = -3;
								}
								else
								{
									tempFaceValue = ++tempFaceValue;
								}
							}
							else if(tempFaceValue == -3)
							{
								
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
				else
				{
					isImpurePure = true;
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
		f6.setValue(8);
		s6.setValue(2);
		
		Card c6=new Card();
		c6.setFace(f6);
		c6.setSuit(s6);
		list.add(c6);
		
		Face f7=new Face();
		Suit s7=new Suit();
		f7.setValue(6);
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
		
		
		Face f9=new Face();
		Suit s9=new Suit();
		f9.setValue(5);
		s9.setValue(0);
		
		Card c9=new Card();
		c9.setFace(f9);
		c9.setSuit(s9);
		list.add(c9);
		
		
		Face f10=new Face();
		Suit s10=new Suit();
		f10.setValue(2);
		s10.setValue(1);
		
		Card c10=new Card();
		c10.setFace(f10);
		c10.setSuit(s10);
		list.add(c10);
		
		List<List<Card>> sortedByFace = rc.groupByFace(list);
		for(int i=0 ; i<sortedByFace.size(); i++)
		{
			List<Card> faceList = sortedByFace.get(i);
			for(int j=0 ; j<faceList.size(); j++)
			{
				System.out.println(faceList.get(j).face.value +" ## "+faceList.get(j).suit.value);
			}
		}
		rc.onHandCards(list);
		rc.displayChunks();
//		//rc.display();
//		list=rc.sortByFace(list);
//		if(rc.isPureSequesnce(list))
//		{
//			System.out.println("PURE SEQUENCE");
//		}
//		else
//		{
//			System.out.println("IMPURE SEQUENCE");
//		}
//		
//		if(rc.isValidSet(list))
//		{
//			System.out.println("Valid ");
//		}
//		else
//		{
//			System.out.println("Invalid ");
//		}
	}
	
	class CardList {
		public List<Card> list = new ArrayList<Card>();
		public int length = 0;
		public int jkrCount = 0;
		public int paperJkrCount = 0;
		public boolean isSet = false;
		public boolean isSeq = false;
		public boolean isPure = false;
		public boolean isTrinala = false;
		public int pointsIfValid = 0;
		public int pointsIfInvalid = 0;
	}
}
