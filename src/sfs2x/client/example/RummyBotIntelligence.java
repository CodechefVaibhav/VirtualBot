package sfs2x.client.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sun.org.apache.bcel.internal.generic.RETURN;


//import rummy.CardList;

import sfs2x.client.example.BotCard;
import common.Face;
import common.Suit;

public class RummyBotIntelligence extends RummyBotLogic {

	ChildRummyBot bot = null;
	
	private List<BotCard> cardlist = null;
	private List<List<BotCard>> groupedCards=new ArrayList<List<BotCard>>();
	private List<List<BotCard>> ungroupedCards=new ArrayList<List<BotCard>>();
	private List<BotCard> jokerList = new ArrayList<BotCard>();
	
	private SuitListClass suitClassObj = null;
	private SuitListClass[] suitTypeArray = null;
	private List<Chunk> chunkList = new ArrayList<Chunk>();
	private List<Chunk> readyChunks = new ArrayList<Chunk>();
	private HashMap<Integer,Opponent> pickedCardByNextPlayerMap = new HashMap<Integer,Opponent>();
	
	private BotCard cutJoker = null;
	private CardList cardListObj = null;
	
	private boolean isPickedMakesPure = false;
	private boolean isPickedMakesSequence = false;
	private boolean isPickedMakesSet = false;
	private boolean pickFromOpenDeck = false;
	private boolean hasPureSequence = false;
	private boolean hasJoker = false;
	private boolean canReadyCardBeReplaced = false;
	
	private int jokerCount = -1;
	private int clubCount = 0;
	private int spadeCount = 0;
	private int heartCount = 0;
	private int daimondCount = 0;
	private int pureSequenceCount = 0;
	private int readyGroups = 0;
	private int myUserTurnsPassed = 0;
	private int sequenceCountWithDiffTwoOrLess = 0;
	private int chunkincr = 0;
	private int openEndedSequenceCount = 0;
	private int chunkIdToBeUsed = -1;
	
	
	Chunk chunkTemp = null;
	
	private int usedSuit = -1;

	
	public RummyBotIntelligence(ChildRummyBot bot)
	{
		super(bot);
		this.bot = bot;
	}
	
	public void onnHandCards(List<BotCard> handCards)
	{
		cardlist = handCards;
		List<List<BotCard>> suitSortedLists = sortBySuit(cardlist);
		for(int i=0 ; i<suitSortedLists.size();i++)
		{
			List<BotCard> typeN = suitSortedLists.get(i);
			if(!typeN.isEmpty())
			{
				startChunkPrepration(typeN);
			}
		}
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
	
	public void saveOpponentPlayersPickedCards(int playerId, BotCard c, boolean clear)
	{
		if(!clear)
		{
			if(!pickedCardByNextPlayerMap.containsKey(new Integer(playerId)))
			{
				int turnCount = 0;
				pickedCardByNextPlayerMap.put(new Integer(playerId), new Opponent(playerId,c,++turnCount));
			}
			else
			{
				Opponent opponentObj = pickedCardByNextPlayerMap.get(new Integer(playerId));
				opponentObj.setCard(c);
				opponentObj.incrementPickCount();
			}
		}
		else
		{
			pickedCardByNextPlayerMap.clear();
		}
	}
	
	public void updateNumberOfMyTurnsHavePassed(int turnCount)
	{
		myUserTurnsPassed = turnCount;
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
	
	private List<List<BotCard>> groupByFace(List<BotCard> cards)
	{
		System.out.println("?????????????????????????????????????");
		List<List<BotCard>> sortedCards = new ArrayList<List<BotCard>>();
		
		List<BotCard> two = new ArrayList<BotCard>();
		List<BotCard> three = new ArrayList<BotCard>();
		List<BotCard> four = new ArrayList<BotCard>();
		List<BotCard> five = new ArrayList<BotCard>();
		List<BotCard> six = new ArrayList<BotCard>();
		List<BotCard> seven = new ArrayList<BotCard>();
		List<BotCard> eight = new ArrayList<BotCard>();
		List<BotCard> nine = new ArrayList<BotCard>();
		List<BotCard> ten = new ArrayList<BotCard>();
		List<BotCard> eleven = new ArrayList<BotCard>();
		List<BotCard> twelve = new ArrayList<BotCard>();
		List<BotCard> thirteen = new ArrayList<BotCard>();
		List<BotCard> fourteen = new ArrayList<BotCard>();
		
		for(BotCard c : cards)
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
		prepareChunksOfGroupByFace(two);
		sortedCards.add(three);
		prepareChunksOfGroupByFace(three);
		sortedCards.add(four);
		prepareChunksOfGroupByFace(four);
		sortedCards.add(five);
		prepareChunksOfGroupByFace(five);
		sortedCards.add(six);
		prepareChunksOfGroupByFace(six);
		sortedCards.add(seven);
		prepareChunksOfGroupByFace(seven);
		sortedCards.add(eight);
		prepareChunksOfGroupByFace(eight);
		sortedCards.add(nine);
		prepareChunksOfGroupByFace(nine);
		sortedCards.add(ten);
		prepareChunksOfGroupByFace(ten);
		sortedCards.add(eleven);
		prepareChunksOfGroupByFace(eleven);
		sortedCards.add(twelve);
		prepareChunksOfGroupByFace(twelve);
		sortedCards.add(thirteen);
		prepareChunksOfGroupByFace(thirteen);
		sortedCards.add(fourteen);
		prepareChunksOfGroupByFace(fourteen);
		displayReadyCards();
		
		return sortedCards;
	}
	
	private void prepareChunksOfGroupByFace(List<BotCard> sortedCards)
	{
		if(!sortedCards.isEmpty())
		{
			while(!sortedCards.isEmpty())
			{
				List<BotCard> myCards =  new ArrayList<BotCard>();
				List<BotCard> temp = new ArrayList<BotCard>();
				outter : for(int i=0 ; i<4 ; i++)
				{
					inner : for(int j=0 ; j<sortedCards.size() ; j++)
					{
						
							if(i==sortedCards.get(j).suit.value)
							{
								if(!isJoker(sortedCards.get(j)))
								{
									myCards.add(sortedCards.get(j));
									temp.add(sortedCards.get(j));
									break inner;
								}
								else
								{
									temp.add(sortedCards.get(j));
								}
							}
						
					}
				}
				
				if(!myCards.isEmpty())
				{
					Chunk mySetChunk = getNewChunkObject();
					mySetChunk.getChunkCards().addAll(myCards);
					
					for(int l=0; l<myCards.size(); l++)
					{
						myCards.get(l).getChunkIdList().add(new Integer(mySetChunk.getId()));
					}
					
					if(myCards.size()>=3)
					{
						if(checkForSet(myCards))
						{
							mySetChunk.setSet(true);
						}
					}
					
				}
				if(!temp.isEmpty())
				{
					for(int z=0; z<temp.size();z++)
					{
						sortedCards.remove(temp.get(z));
					}
				}
			}
		}
	}
	
	private List<List<BotCard>> sortBySuit(List<BotCard> cards)
	{
		//Sorts them by suit.
		List<List<BotCard>> sortedCards=new ArrayList<List<BotCard>>();
		
		List<BotCard> type0=new ArrayList<BotCard>();
		List<BotCard> type1=new ArrayList<BotCard>();
		List<BotCard> type2=new ArrayList<BotCard>();
		List<BotCard> type3=new ArrayList<BotCard>();
		
		for(BotCard c : cards)
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
		sortedCards.add(type0);
		sortedCards.add(type1);
		sortedCards.add(type2);
		sortedCards.add(type3);
		//List<BotCard> type0FaceSorted = sortBySuit(type0);
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(0),*//*prepareRawChunk(sortByFace(type0))*//*)*/;
		//sortedCards.addAll(sortedCards.size(),sortByFace(type0));
		//sortedCards.addAll(sortedCards.size(),sortByFace(type1));
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(1),*//*prepareRawChunk(sortByFace(type1))*//*)*/;
		//sortedCards.addAll(sortedCards.size(),sortByFace(type2));
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(2),*//*prepareRawChunk(sortByFace(type2))*//*)*/;
		//sortedCards.addAll(sortedCards.size(),sortByFace(type3));
		/*distributeCardsInVariousLists(getSuitListClassBySuitType(3),*//*prepareRawChunk(sortByFace(type3))*//*)*/;
		
		//returns card sorted by suit
		return sortedCards;
	}
	
	private void startChunkPrepration(List<BotCard> typeN)
	{
		prepareRawChunk(sortByFace(typeN));
	}
	
	private boolean CheckConditionsToDrop()
	{
		boolean flag = false;
		if(!hasPureSequence && (jokerCount<=0))
			return true;
		if(jokerCount<=1 && !hasPureSequence && openEndedSequenceCount<=1)
			return true;
		if(jokerCount>=2 && openEndedSequenceCount>=2)
			return false;
		if(jokerCount <=3)
			return false;
		
		return flag;
	}
	private void prepareStatsToDrop()
	{
		
		
		// loop traversal for recording no. of pure sequence & ready group cards
		for(int j=0; j<chunkList.size(); j++)
		{
			Chunk myChunkTemp = chunkList.get(j);
			if(!myChunkTemp.getChunkCards().isEmpty())
			{
				if(/*chechForPure(myChunkTemp.getChunkCards())*/ myChunkTemp.isPure())
				{
					++pureSequenceCount;
					hasPureSequence = true;
				}
				else if(myChunkTemp.isSequence() || myChunkTemp.isSet())
				{
					++readyGroups;
				}
			}
		}
		
		//loop traversal for recording no. of cards of each suit
		for(int j=0 ; j<chunkList.size() ; j++)
		{
			Chunk mychunkTemp = chunkList.get(j);
			calculateSumofCardsBySuit(mychunkTemp);
			
			//record no. of open ended sequences
			isOpenEndedSequenceAndUpdateCount(mychunkTemp.getChunkCards());
			
		}
		
		// recording info about joker count
		if(!jokerList.isEmpty())
		{
			jokerCount = jokerList.size();
		}
		else
		{
			hasJoker = false;
		}
		
//		if(hasJoker && hasPureSequence)
//		{
//			flag = true;
//		}
//		
//		return flag;
		System.out.println(CheckConditionsToDrop());
	}
	
	private void calculateSumofCardsBySuit(Chunk myChunkTemp)
	{
		int type = myChunkTemp.getSuiteTpe();
		suitcount: switch (type) {
		case 0:
			clubCount = clubCount + myChunkTemp.getChunkCards().size(); 
			break suitcount;
		case 1:
			daimondCount = daimondCount + myChunkTemp.getChunkCards().size();
			break suitcount;
		case 2:
			heartCount = heartCount + myChunkTemp.getChunkCards().size();
			break suitcount;
		case 3:
			spadeCount = spadeCount + myChunkTemp.getChunkCards().size();
			break suitcount;
		default:
			break suitcount;
		}
	}
	
	private void isOpenEndedSequenceAndUpdateCount(List<BotCard> c)
	{
		if(c.size()==2)
		{
			BotCard c1 = c.get(0);
			BotCard c2 = c.get(1);
			int diff = c2.face.value - c1.face.value;
			if(diff==1)
			{
				++openEndedSequenceCount;
			}
		}
	}
	
	private void checkToDeclare()
	{
		
	}
	
	private void onPickedCardRecievedFromServer(BotCard c)
	{
		for(int i=0 ; i<chunkList.size(); i++)
		{
			Chunk myChunkTemp = chunkList.get(i);
			if(myChunkTemp.getId() == chunkIdToBeUsed)
			{
				if(canReadyCardBeReplaced)
				{
					BotCard tempCard = null;
					List<BotCard> myChunkCards = myChunkTemp.getChunkCards();
					checkJoker : for(int j=0; j<myChunkCards.size(); j++)
					{
						if(isJoker(myChunkCards.get(j)))
						{
							tempCard = myChunkCards.get(j);
							break checkJoker;
						}
					}
					
					jokerList.add(tempCard);
					myChunkCards.add(c);
					myChunkCards.remove(tempCard);
					if(isPickedMakesPure)
					{
						canReadyCardBeReplaced = false;
						myChunkTemp.setPure(true);
						isPickedMakesPure = false;
						break;
					}
					else if(isPickedMakesSequence)
					{
						canReadyCardBeReplaced = false;
						myChunkTemp.setSequence(true);
						isPickedMakesSequence = false;
						break;
					}
					else if(isPickedMakesSet)
					{
						canReadyCardBeReplaced = false;
						myChunkTemp.setSet(true);
						isPickedMakesSet = false;
						break;
					}
				}
				else if(pickFromOpenDeck)
				{
					List<BotCard> myChunkCards = myChunkTemp.getChunkCards();
					myChunkCards.add(c);
					if(isPickedMakesPure)
					{
						pickFromOpenDeck = false;
						myChunkTemp.setPure(true);
						isPickedMakesPure = false;
						break;
					}
					else if(isPickedMakesSequence)
					{
						pickFromOpenDeck = false;
						myChunkTemp.setSequence(true);
						isPickedMakesSequence = false;
						break;
					}
					else if(isPickedMakesSet)
					{
						pickFromOpenDeck = false;
						myChunkTemp.setSet(true);
						isPickedMakesSet = false;
						break;
					}
					
				}
				else
				{
					Chunk chunkObj = getNewChunkObject();
					chunkObj.getChunkCards().add(c);
					chunkObj.setSuiteTpe(c.suit.value);
					break;
				}
			}
		}
	}
	
	private void onUserturnCheckDiscardIfUseful()
	{
		
		/**Below readyLoop is responsible for checking if joker can be replaced with
		 * discarded card by other user */
		
		readyLoop : for(int i=0; i<chunkList.size(); i++)
		{
			BotCard tempCard = null;
			List<BotCard> cardsToCHeck = new ArrayList<BotCard>();
			Chunk myReadyChunkTemp = chunkList.get(i);
			boolean cardsHasJoker = false;
			List<BotCard> myReadyChunkCards = myReadyChunkTemp.getChunkCards();
			checkJoker : for(int j=0; j<myReadyChunkCards.size(); j++)
			{
				if(isJoker(myReadyChunkCards.get(j)))
				{
					cardsHasJoker = true;
					tempCard = myReadyChunkCards.get(j);
					break checkJoker;
				}
			}
			
			if(cardsHasJoker)
			{
				myReadyChunkCards.remove(tempCard);
				cardsToCHeck.addAll(myReadyChunkCards);
				cardsToCHeck.add((BotCard)getDiscardedCard());
				cardsToCHeck = sortByFace(cardsToCHeck);
				if(chechForPure(cardsToCHeck))
				{
					myReadyChunkCards.add(tempCard);
					canReadyCardBeReplaced = true;
					isPickedMakesPure = true;
					hasPureSequence = true;
					++pureSequenceCount;
					chunkIdToBeUsed = myReadyChunkTemp.getId();
					break readyLoop;
					
				}
				
				/**if user has at least one pure sequence then only set or sequence 
				 * creation should proceed.*/ 
				if(pureSequenceCount>=1)
				{
					if(checkForSequence(cardsToCHeck))
					{
						myReadyChunkCards.add(tempCard);
						canReadyCardBeReplaced = true;
						isPickedMakesSequence = true;
						chunkIdToBeUsed = myReadyChunkTemp.getId();
						break readyLoop;
					}
					else if(checkForSet(cardsToCHeck))
					{
						myReadyChunkCards.add(tempCard);
						canReadyCardBeReplaced = true;
						isPickedMakesSet = true;
						chunkIdToBeUsed = myReadyChunkTemp.getId();
						break readyLoop;
					}
					else
					{
						myReadyChunkCards.add(tempCard);
					}
				}
				else
				{
					myReadyChunkCards.add(tempCard);
				}
			}
				
		}
		
		if(canReadyCardBeReplaced)
		{
			sendActionToPickCard();
			return;
		}
		
		
		/**Below checkToUse loop tries to check if discarded card can be utilized 
		 * with any of the other cards*/
		
		List<Chunk> chunkToRemove = new ArrayList<Chunk>();
		checkToUse : for(int i=0 ; i<chunkList.size() ; i++)
		{
			
			if(chunkList.get(i).getChunkCards().size()>0)
			{
				List<BotCard> cardsToCHeck = new ArrayList<BotCard>();
				Chunk chunkTEmp = chunkList.get(i);
				List<BotCard> chunkCards = chunkTEmp.getChunkCards();
				cardsToCHeck.addAll(chunkCards);
				cardsToCHeck.add((BotCard)getDiscardedCard());
				if(chechForPure(cardsToCHeck))
				{
					isPickedMakesPure = true;
					hasPureSequence = true;
					++pureSequenceCount;
					chunkIdToBeUsed = chunkTEmp.getId();
					break checkToUse;
				}
				
				/**if user has at least one pure sequence then only set or sequence 
				 * creation should proceed.*/ 
				if(pureSequenceCount>=1)
				{
					if(checkForSequence(cardsToCHeck))
					{
						isPickedMakesSequence = true;
						chunkIdToBeUsed = chunkTEmp.getId();
						break checkToUse;
					}
					if(checkForSet(cardsToCHeck))
					{
						isPickedMakesSet = true;
						chunkIdToBeUsed = chunkTEmp.getId();
						break checkToUse;
					}
				}
			}
			
		}
		
		if(isPickedMakesPure||isPickedMakesSequence||isPickedMakesSet)
		{
			pickFromOpenDeck = true;
		}
		
		sendActionToPickCard();
	}
	
	private void sendActionToPickCard()
	{
		if(pickFromOpenDeck || canReadyCardBeReplaced)
		{
			bot.sendUserAction(21,getDiscardedCard());
		}
		else
		{
			bot.sendUserAction(20,null); 
		}
	}
	
	private void prepareRawChunk(List<BotCard> typeNFaceSorted)
	{
		if(typeNFaceSorted.size()>0)
		{	
			BotCard temp = null;
			boolean firstFace = true;
			Chunk chunkObj = getNewChunkObject();
			for(int i=0 ; i<typeNFaceSorted.size(); i++)
			{
				if(firstFace)
				{
					BotCard c1 = typeNFaceSorted.get(i);
					chunkObj.getChunkCards().add(c1);
					chunkObj.setSuiteTpe(c1.suit.value);
					c1.getChunkIdList().add(new Integer(chunkObj.getId()));
					BotCard c2 = typeNFaceSorted.get(++i);
					temp = c2;
					int diff = c2.face.value - c1.face.value;
					if(diff==0)
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c2);
						c2.getChunkIdList().add(new Integer(chunkObj.getId()));
						chunkObj.setSuiteTpe(c2.suit.value);
					}
					else if(diff<=2)
					{
						chunkObj.getChunkCards().add(c2);
						c2.getChunkIdList().add(new Integer(chunkObj.getId()));
						chunkObj.setSuiteTpe(c2.suit.value);
					}
					else
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c2);
						c2.getChunkIdList().add(new Integer(chunkObj.getId()));
						chunkObj.setSuiteTpe(c2.suit.value);
					}
					firstFace = false;
				}
				else
				{
					BotCard c = typeNFaceSorted.get(i);
					int diff = c.face.value - temp.face.value;
					temp = c;
					if(diff<=2)
					{
						++sequenceCountWithDiffTwoOrLess;
						chunkObj.getChunkCards().add(c);
						c.getChunkIdList().add(new Integer(chunkObj.getId()));
						chunkObj.setSuiteTpe(c.suit.value);
					}
					else if(diff==0)
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c);
						c.getChunkIdList().add(new Integer(chunkObj.getId()));
						chunkObj.setSuiteTpe(c.suit.value);
					}
					else
					{
						chunkObj = getNewChunkObject();
						chunkObj.getChunkCards().add(c);
						c.getChunkIdList().add(new Integer(chunkObj.getId()));
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
	
	private void manageJokerListFromChunks()
	{
		List<Chunk> chunkToRemove = new ArrayList<Chunk>();
		for(int i=0 ; i<chunkList.size() ; i++)
		{
			Chunk myChunkTemp = chunkList.get(i);
			if(myChunkTemp.getId()!=-1)
			{
				List<BotCard> tempCards = myChunkTemp.getChunkCards();
				if(!tempCards.isEmpty() && (tempCards.size()<=2) && !myChunkTemp.isPure())
				{
					for(int j=0 ; j<tempCards.size() ;j++)
					{
						BotCard c = tempCards.get(j);
						if(isJoker(c))
						{
							jokerList.add(c);
							if(tempCards.size()==1)
							{
								chunkToRemove.add(myChunkTemp);
							}
							else
							{
								tempCards.remove(c);
							}
						}
					}
				}
			}
		}
		
		if(!chunkToRemove.isEmpty())
		{
			for(int j=0 ;j<chunkToRemove.size();j++)
			{
				chunkList.remove(chunkToRemove.get(j));
			}
		}
		
		insertJokersAtRelevantPlace();
	}
	
	private void insertJokersAtRelevantPlace()
	{
		//open ended or middle open hardly matters by this time since pure and grouped sequences are already separated 
		
		//calculating sum of cards in each chunk
		for(int i=0; i<chunkList.size(); i++)
		{
			int sum = 0;
			Chunk myChunkTemp = chunkList.get(i);
			List<BotCard> c = myChunkTemp.getChunkCards();
			for(int j=0; j<c.size(); j++)
			{
				sum = sum + c.get(j).face.value;
			}
			
			myChunkTemp.setChunkCardsSum(sum);
		}
		
		//sorting chunks on the basis of sum of cards in each chunk
		Collections.sort(chunkList, new Chunk());
		
//		List<Chunk> chunkToRemove = new ArrayList<Chunk>();
		List<BotCard> jokerToRemove = new ArrayList<BotCard>();
		for(int k=0; k<jokerList.size() ; k++)
		{
		    fillJoker : for(int i=0; i<chunkList.size() ; i++)
			{
				
				List<BotCard> listToTest = new ArrayList<BotCard>();
				Chunk myChunkTemp = chunkList.get(i);
				if(!myChunkTemp.isPure() && !myChunkTemp.isSequence())
				{
					List<BotCard> c = myChunkTemp.getChunkCards();
					listToTest.addAll(c);
					listToTest.add(jokerList.get(k));
					if(checkForSequence(listToTest))
					{
						myChunkTemp.getChunkCards().add(jokerList.get(k));
						//readyChunks.add(myChunkTemp);
						myChunkTemp.setSequence(true);
						//chunkToRemove.add(myChunkTemp);
						jokerToRemove.add(jokerList.get(k));
						break fillJoker;
					}
					else if(chechForPure(listToTest))
					{
						myChunkTemp.getChunkCards().add(jokerList.get(k));
						//readyChunks.add(myChunkTemp);
						myChunkTemp.setPure(true);
						//chunkToRemove.add(myChunkTemp);
						jokerToRemove.add(jokerList.get(k));
						break fillJoker;
					}
				}
				
		    }
			
		}
		
		
//		if(!chunkToRemove.isEmpty())
//		{
//			for(int j=0 ;j<chunkToRemove.size();j++)
//			{
//				chunkList.remove(chunkToRemove.get(j));
//			}
//		}
		
		if(!jokerToRemove.isEmpty())
		{
			for(int p=0; p<jokerToRemove.size(); p++)
			{
				jokerList.remove(jokerToRemove.get(p));
			}
		}
		
		if(!jokerList.isEmpty())
		{
			jokerCount = jokerList.size();
		}
		else
		{
			hasJoker = false;
			jokerCount =0;
		}
		
		prepareStatsToDrop();
		//displayReadyCards();
	}
	private void seperateReadyChunks()
	{
		List<Chunk> chunkToRemove = new ArrayList<Chunk>();
		for(int i=0; i<chunkList.size(); i++)
		{
			Chunk chunky = chunkList.get(i);
			if(chunky.getId()!= -1)
			{
				System.out.println("CHUNK : "+"SUIT : "+chunky.getSuiteTpe()+ "   CHUNKID : "+chunky.getId());
				List<BotCard> c = chunky.getChunkCards();
				for(int j=0; j<c.size() ; j++)
				{
					System.out.println("CHUNK : "+c.get(j).face.value+ " # "+ c.get(j).suit.value);
				}
				if(chechForPure(c))
				{
					chunky.setPure(true);
					//readyChunks.add(chunky);
					//chunkToRemove.add(chunky);
				}
			}
			
		}
		
//		if(!chunkToRemove.isEmpty())
//		{
//			for(int j=0 ;j<chunkToRemove.size();j++)
//			{
//				chunkList.remove(chunkToRemove.get(j));
//			}
//		}
		
		System.out.println("//////////////////////////////////////");
		manageJokerListFromChunks();
		//displayReadyCards();
	}
	
	private void displayReadyCards()
	{
		System.out.println("Chunk CARDS :-");
		for(int i=0; i<chunkList.size(); i++)
		{
			Chunk chunky = chunkList.get(i);
			System.out.println("CHUNK : "+"SUIT : "+chunky.getSuiteTpe()+ " CHUNKID : "+chunky.getId()
					+" ispure: "+chunky.isPure()
					+" isSet : "+chunky.isSet()
					+" isSeq : "+chunky.isSequence());
			List<BotCard> c = chunky.getChunkCards();
			for(int j=0; j<c.size() ; j++)
			{
				System.out.println("CHUNK : "+c.get(j).face.value+ " # "+ c.get(j).suit.value);
			}
		}
		
		System.out.println("//////////////////////////////////////////////////////////////");
	}
	
	
	private void distributeCardsInVariousLists( SuitListClass suitClassObj, List<BotCard> typeNFaceSorted)
	{
		
		for(int i=0 ; i<typeNFaceSorted.size(); i++)
		{
			List<BotCard> cardTemp = new  ArrayList<BotCard>();
			
			if((typeNFaceSorted.size()-1)== i)
			{
				BotCard c1 = typeNFaceSorted.get(i);
				cardTemp.add(c1);
				suitClassObj.ungroupedSequesnces.add(cardTemp);
			}
			else
			{
				BotCard c1 = typeNFaceSorted.get(i);
				BotCard c2 = typeNFaceSorted.get(++i);
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
		
		List<List<BotCard>> openended = suitClassObj.getOpenEnded();
		for(int j=0; j<openended.size(); j++)
		{   if(!(openended.size()-1 == j))
			{
				List<BotCard> one = openended.get(j);
				List<BotCard> two = openended.get(++j);
				BotCard c1 = one.get((one.size()-1));
				BotCard c2 = two.get(0);
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
		
		List<List<BotCard>> unGroupedListTemp = suitClassObj.getUngroupedSequesnces();
		for(int k=0; k<unGroupedListTemp.size(); k++)
		{  
			List<BotCard> jokerTempList = unGroupedListTemp.get(k);
			jokerloop : for(int y=0 ; jokerTempList.size()>0; )
			{
				System.out.println(y);
				BotCard jokerCheckCard = jokerTempList.get(y);
				if(isJoker(jokerCheckCard))
				{
					jokerList.add(jokerCheckCard);
					jokerTempList.remove(jokerCheckCard);
					//continue jokerloop;
				}
				else
				{
					List<List<BotCard>> groupedSeqTemp = suitClassObj.getGroupedSequences();
					for(int i=0 ;i<groupedSeqTemp.size(); i++)
					{
						List<BotCard> c = groupedSeqTemp.get(i);
						for(int j=0 ; j<c.size(); j++)
						{
							BotCard ctemp = c.get(j);
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
					
					List<List<BotCard>> openEndedTemp = suitClassObj.getOpenEnded();
					for(int i=0; i<openEndedTemp.size() ;i++)
					{
						List<BotCard> c = openEndedTemp.get(i);
						for(int j=0 ; j<c.size(); j++)
						{
							BotCard ctemp = c.get(j);
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
					
					List<List<BotCard>> middleOpenTemp = suitClassObj.getMiddleOpen();
					for(int i=0; i<middleOpenTemp.size() ;i++)
					{
						List<BotCard> c = middleOpenTemp.get(i);
						for(int j=0; j<c.size(); j++)
						{
							BotCard ctemp = c.get(j);
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
				List<List<BotCard>> middleOpenToClubedWithJokers = suitClassObj.getMiddleOpen();
				{
					for(int j=0; j<middleOpenToClubedWithJokers.size(); j++)
					{
						List<BotCard> middleOpenToClub = middleOpenToClubedWithJokers.get(j);
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
			List<List<BotCard>> open = suitTypeArray[i].getOpenEnded();
			for(int z=0 ;z<open.size(); z++)
			{
				List<BotCard> c = open.get(z);
				for(int j=0 ; j<c.size(); j++)
				{
					BotCard ctemp = c.get(j);
					System.out.println("open : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			List<List<BotCard>> group = suitTypeArray[i].getGroupedSequences();
			for(int x=0 ;x<group.size(); x++)
			{
				List<BotCard> c = group.get(x);
				for(int j=0 ; j<c.size(); j++)
				{
					BotCard ctemp = c.get(j);
					System.out.println("group : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			List<List<BotCard>> middle = suitTypeArray[i].getMiddleOpen();
			for(int v=0 ;v<middle.size(); v++)
			{
				List<BotCard> c = middle.get(v);
				for(int j=0 ; j<c.size(); j++)
				{
					BotCard ctemp = c.get(j);
					System.out.println("middle : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			List<List<BotCard>> ungroup = suitTypeArray[i].getUngroupedSequesnces();
			for(int b=0 ;b<ungroup.size(); b++)
			{
				List<BotCard> c = ungroup.get(b);
				for(int j=0 ; j<c.size(); j++)
				{
					BotCard ctemp = c.get(j);
					System.out.println("ungroup : "+ctemp.face.value +" # "+ctemp.suit.value);
				}
			}
			
			for(int n=0; n<jokerList.size(); n++)
			{
				BotCard ctemp = jokerList.get(n);
				System.out.println("jokerLIst : "+ctemp.face.value +" # "+ctemp.suit.value);
			}
			
			System.out.println("////////////////////////////////////////////////////////////////////////////////");
		}
	}
			
			
	private boolean checkDiscardedCardIfUseful(BotCard card)
	{
		
		boolean flag = false;
		if(isJoker(card) && !isCutJokerPickable)
		{
			//cutjoker can only be picked from discarded pile if its very first userturn.
			return flag;
		}
		int discardedCardSuit = card.suit.value;
		SuitListClass suitClassTempObj = suitTypeArray[discardedCardSuit];
		
		List<List<BotCard>> openEndedTemp = suitClassTempObj.getOpenEnded();
		open: for(int i=0; i<openEndedTemp.size() ;i++)
		{
			    List<BotCard> open = openEndedTemp.get(i);
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
		
		List<List<BotCard>> middleEndedTemp = suitClassTempObj.getMiddleOpen();
		middle: for(int j=0; j<middleEndedTemp.size() ;j++)
		{
			List<BotCard> middle = middleEndedTemp.get(j);
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
		
		List<List<BotCard>> unGroupedSeqTemp = suitClassTempObj.getUngroupedSequesnces();
		ungroup: for(int k=0 ; k<unGroupedSeqTemp.size(); k++)
		{
			List<BotCard> ungrouped = unGroupedSeqTemp.get(k);
			ungrouped.add(card);
			ungrouped = sortByFace(ungrouped);
			//
		}
		
		return flag;
	}
		
	
	private boolean chechForPure(List<BotCard> cards)
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
	
	private boolean checkForSequence(List<BotCard> cards)
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
	
	private boolean checkForSet(List<BotCard> cards)
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
	
	private boolean checkForMiddleDrop()
	{
		boolean flag = false;
		if(!hasPureSequence && pureSequenceCount<=0)
		{
			if(myUserTurnsPassed<=7)
			{
				for(int i=0; i<chunkList.size(); i++)
				{
					Chunk myChunkTemp = chunkList.get(i);
					calculateSumofCardsBySuit(myChunkTemp);
				}
				
				int totalPointsToLoose = clubCount + daimondCount + heartCount + spadeCount;
				
				int pickedCountRange =0;
				loop : for(Integer key : pickedCardByNextPlayerMap.keySet()) {
			         Opponent value = pickedCardByNextPlayerMap.get(key);
			         if(value.getPickCount()==3)
			         {
		            	pickedCountRange = value.getPickCount();
		            	break loop;
			         }
			         else
			         {
			        	 pickedCountRange = value.getPickCount();
			         }
		        	}
				
				if(pickedCountRange<=2 && totalPointsToLoose<=40)
				{
					flag = false;
				}
				else if(pickedCountRange >=3 && totalPointsToLoose<=50)
				{
					flag = true;
				}
			}
			else
			{
				flag = true;
			}
		}
		
		return flag;
	}
	
	private List<BotCard> sortByFace(List<BotCard> cards)
	{
		//Sorts them by Face.
		List<BotCard> sortedCards=new ArrayList<BotCard>();
		
		for(BotCard c :cards)
		{
			if(sortedCards.size() == 0)
			{
				sortedCards.add(c);
			}
			else
			{
				for(int i=0;i<sortedCards.size(); i++)
				{
					BotCard cardObj=sortedCards.get(i);
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
	
	public void setJokerCard(BotCard jokerCard) {
		this.cutJoker = jokerCard;
	}
	
	public BotCard getJokerCard() {
		
		return cutJoker;
	}
	
	
	private boolean duplicateCard(CardList cL, boolean isPure) {
		//System.out.println("In Duplicate BotCard "+cL.list.toString()+" ");
		
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
	
	private boolean checkForPureImpure(List<BotCard> cards, boolean checkForPure)
	{
		boolean flag = false;
		int tempFaceValue = -1;
		boolean firstFace = true;
		boolean isPure = true;
		List<BotCard> cardsTemp = sortByFace(cards);
		if(checkForPure)
		{
			faceLoop :for(BotCard crd :  cardsTemp)
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
					BotCard checkNonJoker = cardsTemp.get(z);
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
					faceLoop :for(BotCard crd :   cardsTemp)
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
	
	private boolean hasSameSuit(List<BotCard> cards)
	{
		boolean first = true;
		boolean isAllSuitSame = true;
		int suitValue = -1;
		suitloop : for(BotCard c :cards)
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
	
	
	private boolean isPureSequesnce(List<BotCard> cards)
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
	
	private boolean isJoker(BotCard c) {
		//returns true if the card is a joker or cutjoker.
		if (getJokerCard() == null) return false;
		if (c.getFace().getValue() == getJokerCard().getFace().getValue())
			return true;
		
		//paper joker
		if (c.getFace().getValue() == 21) return true;
		return false;
	}
	
	private boolean isPaperJoker(BotCard c) {
		//returns true if the card is a joker or cutjoker.
		if (c.getFace().getValue() == 21) return true;
		return false;
	}
	
	private boolean isValidSet(List<BotCard> cards)
	{
		boolean flag=true;	
		 
		for(int i=0;i<cards.size();i++)
		{
			BotCard compCard=cards.get(i);
			if(! isJoker(compCard))
			{
				for(int j=0;j<cards.size();j++)
				{
					if(i != j)
					{
						
						BotCard card=cards.get(j);
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
		List<BotCard> list=new ArrayList<BotCard>();
		
		Face f4=new Face();
		Suit s4=new Suit();
		f4.setValue(2);
		s4.setValue(1);
		
		BotCard c4=new BotCard();
		c4.setFace(f4);
		c4.setSuit(s4);
		list.add(c4);
		
		Face f5=new Face();
		Suit s5=new Suit();
		f5.setValue(4);
		s5.setValue(1);
		
		BotCard c5=new BotCard();
		c5.setFace(f5);
		c5.setSuit(s5);
		list.add(c5);
		
		Face f6=new Face();
		Suit s6=new Suit();
		f6.setValue(8);
		s6.setValue(2);
		
		BotCard c6=new BotCard();
		c6.setFace(f6);
		c6.setSuit(s6);
		list.add(c6);
		
		Face f7=new Face();
		Suit s7=new Suit();
		f7.setValue(6);
		s7.setValue(2);
		
		BotCard c7=new BotCard();
		c7.setFace(f7);
		c7.setSuit(s7);
		list.add(c7);
		
		Face f1=new Face();
		Suit s1=new Suit();
		f1.setValue(4);
		s1.setValue(0);
		
		BotCard c1=new BotCard();
		c1.setFace(f1);
		c1.setSuit(s1);
		list.add(c1);
		
		Face f2=new Face();
		Suit s2=new Suit();
		f2.setValue(10);
		s2.setValue(0);
		
		BotCard c2=new BotCard();
		c2.setFace(f2);
		c2.setSuit(s2);
		list.add(c2);
		
		rc.setJokerCard(c2);
		
		Face f3=new Face();
		Suit s3=new Suit();
		f3.setValue(3);
		s3.setValue(0);
		
		BotCard c3=new BotCard();
		c3.setFace(f3);
		c3.setSuit(s3);
		list.add(c3);
		

		Face f8=new Face();
		Suit s8=new Suit();
		f8.setValue(2);
		s8.setValue(0);
		
		BotCard c8=new BotCard();
		c8.setFace(f8);
		c8.setSuit(s8);
		list.add(c8);
		
		
		Face f9=new Face();
		Suit s9=new Suit();
		f9.setValue(5);
		s9.setValue(0);
		
		BotCard c9=new BotCard();
		c9.setFace(f9);
		c9.setSuit(s9);
		list.add(c9);
		
		
		Face f10=new Face();
		Suit s10=new Suit();
		f10.setValue(2);
		s10.setValue(1);
		
		BotCard c10=new BotCard();
		c10.setFace(f10);
		c10.setSuit(s10);
		list.add(c10);
		
//		List<List<BotCard>> sortedByFace = rc.groupByFace(list);
//		for(int i=0 ; i<sortedByFace.size(); i++)
//		{
//			List<BotCard> faceList = sortedByFace.get(i);
//			for(int j=0 ; j<faceList.size(); j++)
//			{
//				System.out.println(faceList.get(j).face.value +" ## "+faceList.get(j).suit.value);
//			}
//		}
		
		rc.onnHandCards(list);
		rc.seperateReadyChunks();
		rc.groupByFace(list);
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
		public List<BotCard> list = new ArrayList<BotCard>();
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
