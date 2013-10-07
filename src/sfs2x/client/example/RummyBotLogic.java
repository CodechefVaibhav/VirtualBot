package sfs2x.client.example;


import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.omg.DynamicAny.DynValueOperations;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.sun.corba.se.impl.interceptors.PICurrent;
import com.sun.org.apache.bcel.internal.generic.IXOR;

import rummy.HandCards;
import sfs2x.client.entities.variables.UserVariable;
import sfs2x.client.requests.UnsubscribeRoomGroupRequest;
import sun.tools.jar.resources.jar_sv;

import common.Card;
import common.Deck;
import common.Face;
import common.Suit;

public class RummyBotLogic 
{
	private List<Card> cardlist = null;
	private Card cutJoker = null;	
	private Card discardedCard=null;
	private List<Card> cardsToBeMeld=null;
	private int points=0;
	private List<List<Card>> groupedCards=new ArrayList<List<Card>>();
	private List<List<Card>> ungroupedCards=new ArrayList<List<Card>>();
	private RummyBot bot;
	private List<Card> jokerCards=new ArrayList<Card>();
	private boolean groupOfFour=false;
	protected boolean isPickedFromDeck=false;
	protected boolean usePickedInGroup=false;
	protected boolean pureSequence=false;
	protected boolean iDeclared=false;
	protected boolean isValueGame=false;
	protected boolean isCutJokerPickable=false;
	public RummyBotLogic(RummyBot bot) {
		this.bot=bot;
		// TODO Auto-generated constructor stub
	}
	public void setCards(List<Card> cards)
	{
		cardlist = cards;		
	}
	public void addCard(Card card)
	{
		cardlist.add(card);
	}
	public List<Card> getCards()
	{
		return cardlist;
	}
	public void setJoker(Card c)
	{
		setJokerCard(c);
	}
	public int getPoints()
	{
		return points;
	}	
	public void gameStarted()
	{
		resetOnNewRound();
	}
	public void onHandCards(List<Card> handCards)
	{
		cardlist=handCards;
		checkForGroupedCards();
	}
	private void getCardsCount()
	{
		int ungroupedCardCount=0;
		int groupedCardCount=0;
		int jokerCardCount=0;
		
		for(List<Card> subCards:ungroupedCards)
		{
			ungroupedCardCount=ungroupedCardCount+subCards.size();
		}
		//System.out.println("Ungrouped cards count="+ungroupedCardCount);
		for(List<Card> subCards:groupedCards)
		{
			groupedCardCount=groupedCardCount+subCards.size();
		}
		//System.out.println("Grouped cards count="+groupedCardCount);
		
		jokerCardCount=jokerCards.size();
		//System.out.println("Joker cards count="+jokerCardCount);
		int totalHandCards=ungroupedCardCount+groupedCardCount+jokerCardCount;
		//System.out.println("**********Total handcards count="+totalHandCards);
	}
	private void checkForGroupedCards()
	{
		//sort cards
		List<Card> sortedCards=groupBySuit(cardlist);
		List<Card> club=new ArrayList<Card>();
		List<Card> diamond=new ArrayList<Card>();
		List<Card> heart=new ArrayList<Card>();
		List<Card> spade=new ArrayList<Card>();
		
		//identify groups and add them to grouped cards
		for(Card c: cardlist)
		{
			switch (c.suit.value) 
			{
				case 0:
					club.add(c);
					break;
				case 1:
					diamond.add(c);
					break;
				case 2:
					heart.add(c);	
					break;
				case 3:
					spade.add(c);
					break;
				default:
					break;
			}
		}
		
		//check if the suited cards can be grouped.
		List<List<Card>> allSuits=new ArrayList<List<Card>>();
		allSuits.add(allSuits.size(),groupBySuit(club));
		allSuits.add(allSuits.size(),groupBySuit(diamond));
		allSuits.add(allSuits.size(),groupBySuit(heart));
		allSuits.add(allSuits.size(),groupBySuit(spade));
		
		for(List<Card> suit:allSuits)
		{
			//System.out.println("*******************suited lists="+suit);
			for(int i=0; i < suit.size() && suit.size()>0;)
			{	
				i=0;
				Card c=suit.get(i);
				int j=i+1;
				if(suit.size()==1)
				{
					j=0;
				}
				List<Card> temp=new ArrayList<Card>();
				temp.add(c);
				while( j < suit.size() && ((temp.size()<=2 && groupOfFour) || (temp.size()<=3)) )
				{
					if(c.getFace().value + temp.size() == suit.get(j).getFace().value)
					{
						temp.add(suit.get(j));
					}
					j++;
				}
				suit=removeCommonValue(suit,temp);
				//System.out.println("*******temp="+temp);
				if(temp.size()>2)
				{
					//if a group of 3 or 4 cards is found then add it to grouped cards.
					
					if(groupOfFour && temp.size()==4)
					{
						//already have a group of 4, so remove a card from group and add as a new ungrouped list.
						Card extra=temp.get(0);
						temp.remove(extra);
						//add this extra card to ungrouped
						List<Card> newUngrouped=new ArrayList<Card>();
						newUngrouped.add(extra);
						ungroupedCards.add(newUngrouped);
						//System.out.println("Ungrouped="+newUngrouped);
					}	
					//add the 3group cards to grouped card
					groupedCards.add(temp);
					if(temp.size()==4)
					{
						//groupof4 found.
						groupOfFour=true;
					}
					//System.out.println("Grouped="+temp);
										
				}
				else if(temp.size()<=2)
				{
					//add to ungrouped cards
					ungroupedCards.add(temp);
					//System.out.println("Ungrouped="+temp);
				}				
			}
		}
		//remove joker cards from ungrouped list and add them to jokers list
		for(List<Card> subList:ungroupedCards)
		{
			List<Card> tempRemove=new ArrayList<Card>();
			for(Card c: subList)
			{
				if(isJoker(c))
				{
					tempRemove.add(c);
				}
			}
			//add to joker cards
			jokerCards.addAll(tempRemove);
			//remove joker cards from ungrouped cards.
			subList=removeCommonValue(subList, tempRemove);			
		}
	}
	public void onUserTurn(boolean isCutJokerPickable)
	{
		this.isCutJokerPickable=isCutJokerPickable;
		getCardsCount();
		pickACard();
	}
	private void pickACard()
	{
		//if isDiscardedUseful return a true then pick discarded card
		if(isDiscardedUseful())
		{
			//System.out.println("Picking discarded card="+discardedCard);
			//pick discarded card	
			isPickedFromDeck=false;
			bot.sendUserAction(21,discardedCard); 
		}
		else
		{
			//System.out.println("Picking from deck");
			//else pick from deck	
			isPickedFromDeck=true;
			bot.sendUserAction(20,null); 
		}			
	}
	private boolean isDiscardedUseful()
	{		
		boolean flag=false;
		if(discardedCard==null)
		{
			//no discarded card on the table so no need to pick discarded card.
			return flag;
		}
		if(isJoker(discardedCard) && !isCutJokerPickable)
		{
			//cutjoker can only be picked from discarded pile if its very first userturn.
			return flag;
		}
		//check if have a groupOfFour
		if(!groupOfFour)
		{
			//look in grouped cards only if a group of 4 is not formed.
			flag=isCardUsable(discardedCard ,groupedCards);			 
		}
		if(flag)
		{
			//card suitable for grouped cards
			usePickedInGroup=true;
			return flag;
		}
		//now look in ungrouped cards
		flag=isCardUsable(discardedCard,ungroupedCards);
		if(flag)
		{
			//card suitable for ungrouped cards
			usePickedInGroup=false;
			return flag;
		}
		
		if(isJoker(discardedCard))
		{	//card suitable for joker cards
			flag=true;
			return flag;
		} 
		
		//else return false pick from deck.
		return flag;
	}
	
	private boolean isCardUsable(Card card, List<List<Card>> cards)
	{
		boolean flag=false;
		//check if a joker or cutjoker card.
		
		for(List<Card> subCards:cards)
		{
			List<Integer> temp=new ArrayList<Integer>();
			//check if forming a sequence or a set
			if(subCards.size() == 2)
			{
				if(subCards.get(0).getFace().value == subCards.get(1).getFace().value)
				{
					//if a pair is there and discarded card forms a set.
					if(subCards.get(0).getFace().value==card.getFace().value)
					{
						//set formed
						flag=true;
						temp.clear();
						break;
					}					
				}
				else
				{
					if(subCards.get(0).getSuit().getValue() != card.getSuit().getValue())
					{
						//if card is of different suit than the suit of current card group then skip, as cant be used in case of a sequence
						continue;
					}
					//if discarded card forms a sequence
					Card firstCard=subCards.get(0);
					Card secondCard=subCards.get(1);
					temp.add(firstCard.face.value);
					temp.add(secondCard.face.value);
					temp.add(card.face.value);
					Collections.sort(temp);				
					if(temp.get(2)-temp.get(1) == temp.get(1)-temp.get(0))
					{
						//sequence formed.
						flag=true;
						temp.clear();
						break;
					}
				}				
			}
			//check in grouped cards for a groupOfFour
			else if(subCards.size() == 3 && !groupOfFour)
			{	
				if(subCards.get(0).getSuit().getValue() != card.getSuit().getValue())
				{
					//if card is of different suit than the suit of current card group then skip, as cant be used in case of a sequence
					continue;
				}
				//if discarded card forms a sequence, not looking for quad. dunno y :)
				Card firstCard=subCards.get(0);
				Card secondCard=subCards.get(1);
				Card thirdCard=subCards.get(2);
				temp.clear();
				temp.add(firstCard.face.value);
				temp.add(secondCard.face.value);
				temp.add(thirdCard.face.value);
				temp.add(card.face.value);
				
				Collections.sort(temp);				
				
				if((temp.get(2)-temp.get(1) == temp.get(1)-temp.get(0)) && (temp.get(3)-temp.get(2) == temp.get(2)-temp.get(1)))
				{
					//forms  a sequence of 4
					flag=true;
					temp.clear();
					break;
				}
			}
		}		
		return flag;
	}
	
	private boolean isJoker(Card c) {
		//returns true if the card is a joker or cutjoker.
		if (getJokerCard() == null) return false;
		if (c.getFace().getValue() == getJokerCard().getFace().getValue())
			return true;
		if(isValueGame)
		{
			if(c.getSuit().getValue() == getJokerCard().getSuit().getValue())
			{
				if (c.getFace().getValue() - 1 == getJokerCard().getFace().getValue())
				{
					//papplu
					return true;
				}
				
				if (c.getFace().getValue() +1 == getJokerCard().getFace().getValue())
				{
					//nichlu
					return true;
				}
			}
			
		}
		//paper joker
		if (c.getFace().getValue() == 21) return true;
		return false;
	}
	private boolean isPaperJoker(Card c) {
		//returns true if the card is a joker or cutjoker.
		if (c.getFace().getValue() == 21) return true;
		return false;
	}
	private boolean canIMeld()
	{
		boolean flag=false;
		
		//using temporary lists to calculate whether we can meld or not !!
		List<List<Card>> ungroupedTemp=new ArrayList<List<Card>>();
		List<List<Card>> groupedTemp=new ArrayList<List<Card>>();
		List<Card> jokerTemp=new ArrayList<Card>(jokerCards);
			
		for(List<Card> list:ungroupedCards)
		{
			ungroupedTemp.add( new ArrayList<Card>(list));
		}
		for(List<Card> list:groupedCards)
		{
			ungroupedTemp.add( new ArrayList<Card>(list));
		}
		//remove one extra card
		Card discard=getCardToDiscard(ungroupedTemp,jokerTemp);
		
		//use joker cards to complete ungrouped card pairs first and then ungrouped single cards
		if(jokerTemp.size()>0)
		{
			for(List<Card> subCards:ungroupedTemp)
			{
				if((subCards.size()==2 && groupOfFour) || (subCards.size()==3 && !groupOfFour))
				{					
					Card jCard=useJokerCard(jokerTemp);
					if(jCard==null)
					{
						//no joker cards left
						break;
					}	
					subCards.add(jCard);
					if(subCards.size()==4)
					{
						groupOfFour=true;
					}
				}											
			}
		}
		
		if(jokerTemp.size()>0)
		{
			outer:for(List<Card> subCards:ungroupedTemp)
			{
				inner:while(subCards.size()<=2)
				{					
					Card jCard=useJokerCard(jokerTemp);
					if(jCard==null)
					{
						//no joker cards left
						break outer;
					}	
					subCards.add(jCard);
				}											
			}
		}
			
		List<Card> leftCards=new ArrayList<Card>();
		
		//remove all the group of 3/4 cards from ungrouped and add list to grouped lists
		//and other pending ungrouped cards will be added to leftcards
		
		for(List<Card> subCards:ungroupedTemp)
		{
			if(subCards.size()>=3)
			{
				groupedTemp.add(subCards);
			}
			else
			{
				leftCards.addAll(leftCards.size(), subCards);
			}			
		}
		
		//now all ungrouped cards have been added to grouped and other ungrouped are added to left cards so we can clear them.
		ungroupedTemp.clear();
		
		//now check if sets can be made from the leftcards
		makeSetsFromLeftCards(leftCards, groupedTemp, ungroupedTemp, jokerTemp);
		
		//now check if more hands can be formed from the joker cards alone.
		if(jokerTemp.size()>0)
		{
			List<Card> toRemoveLater=null;
			int i=0;
			
			while(jokerTemp.size()%3==0)
			{
				List<Card> jokerCardSets=new ArrayList<Card>();
				if(toRemoveLater==null)
				{
					toRemoveLater=new ArrayList<Card>();
				}
				
				jokerCardSets.add(jokerTemp.get(i));
				toRemoveLater.add(jokerTemp.get(i));
				i++;
				
				jokerCardSets.add(jokerTemp.get(i));
				toRemoveLater.add(jokerTemp.get(1));
				i++;
				
				jokerCardSets.add(jokerTemp.get(i));
				toRemoveLater.add(jokerTemp.get(i));			
				i++;
				
				//add the grouped jokers to the grouped list
				groupedTemp.add(jokerCardSets);
			}
			
			if(toRemoveLater!=null)
			{
				//remove grouped jokers from the joker list
				for(Card removeCard:toRemoveLater)
				{
					jokerTemp.remove(removeCard);
				}
			}
		}
		
		//System.out.println("Grouped size="+groupedTemp.size()+" leftCaRDS size="+leftCards.size()+" JokerCards="+jokerTemp.size());
				
		List<Card> finalMeldCards=new ArrayList<Card>();
		if(groupOfFour)
		{
			//add the group of four at the end of final meld card list
			List<Card> groupOfFourCards=null;
			for(List<Card> groups:groupedTemp)
			{
				if(groups.size()==4)
				{
					groupOfFourCards=groups;
					continue;
				}
				finalMeldCards.addAll(finalMeldCards.size(),groups);
			}
			finalMeldCards.addAll(finalMeldCards.size(), leftCards);
			if(groupOfFourCards!=null)
			{
				finalMeldCards.addAll(finalMeldCards.size(), groupOfFourCards);
			}
		}
		else
		{
			//add the grouped cards at the starting of list, leave other cards as it is at the end of list.
			for(List<Card> groups:groupedTemp )
			{
				finalMeldCards.addAll(finalMeldCards.size(),groups);
			}
			finalMeldCards.addAll(finalMeldCards.size(), leftCards);
		}	
		
		if(validateHands(finalMeldCards))
		{
			flag=true;
			iDeclared=true;
			cardsToBeMeld=finalMeldCards;
			bot.sendUserAction(23, null);
		}		
		return flag;
	}
	
	private boolean validateHands(List<Card> hands)
	{
		boolean flag=false;
		int noOfSeq=0;
		int noOfHands=0;
		pureSequence=false;
		if(hands.size()==13)
		{
			List<Card> testList=new ArrayList<Card>();
			
			testList.add(0,hands.get(0));
			testList.add(1,hands.get(1));
			testList.add(2,hands.get(2));
			
			if(isValidSequence(testList))
			{				
				if(isPure(testList))
				{
					//no jokers in the sequence, so we got a pure joker
					pureSequence=true;
				}
				noOfHands++;
				noOfSeq++;
			}
			else if (isValidSet(testList))
			{
				noOfHands++;				
			}
			 
			testList.clear();
			
			testList.add(0,hands.get(3));
			testList.add(1,hands.get(4));
			testList.add(2,hands.get(5));
			
			if(isValidSequence(testList))
			{
				if(isPure(testList))
				{
					//no jokers in the sequence, so we got a pure joker
					pureSequence=true;
				}
				noOfHands++;
				noOfSeq++;
			}
			else if (isValidSet(testList))
			{
				noOfHands++;				
			}
			
			
			testList.clear();
			testList.add(0,hands.get(6));
			testList.add(1,hands.get(7));
			testList.add(2,hands.get(8));
			
			if(isValidSequence(testList))
			{
				if(isPure(testList))
				{
					//no jokers in the sequence, so we got a pure joker
					pureSequence=true;
				}
				noOfHands++;
				noOfSeq++;
			}
			else if (isValidSet(testList))
			{
				noOfHands++;			
			}
			 
			testList.clear();
			testList.add(0,hands.get(9));
			testList.add(1,hands.get(10));
			testList.add(2,hands.get(11));
			testList.add(3,hands.get(12));
			
			if(isValidSequence(testList))
			{				
				if(isPure(testList))
				{
					//no jokers in the sequence, so we got a pure joker
					pureSequence=true;
				}
				noOfHands++;
				noOfSeq++;
			}
			else if (isValidSet(testList))
			{
				noOfHands++;				
			}
			 
			
			if(noOfHands==4 && noOfSeq>=2 && pureSequence)
			{
				//can meld cards
				flag=true;
			}
		}
		return flag;
	}
	private boolean meldCards()
	{
		boolean flag=false;
		//System.out.println("Melding cards ");
		//if joker cards are 0 then manage ungrouped cards accordingly and meld. //try looking for sets then
		getCardsCount();	
		//use joker cards to complete ungrouped card pairs first and then ungrouped single cards
		if(jokerCards.size()>0)
		{
			for(List<Card> subCards:ungroupedCards)
			{
				if((subCards.size()==2 && groupOfFour) || (subCards.size()==3 && !groupOfFour))
				{					
					Card jCard=useJokerCard(jokerCards);
					if(jCard==null)
					{
						//no joker cards left
						break;
					}	
					subCards.add(jCard);
					if(subCards.size()==4)
					{
						groupOfFour=true;
					}
				}											
			}
		}
		
		if(jokerCards.size()>0)
		{
			outer:for(List<Card> subCards:ungroupedCards)
			{
				inner:while(subCards.size()<=2)
				{					
					Card jCard=useJokerCard(jokerCards);
					if(jCard==null)
					{
						//no joker cards left
						break outer;
					}	
					subCards.add(jCard);
				}											
			}
		}
		
		//now till here all the joker cards have been added to ungrouped cards.
		
		//check if some group cards have been formed yet, if yes then add to grouped cards.
		//now group the  left cards in the ungrouped list by face a send them to meld as it is.
		List<Card> leftCards=new ArrayList<Card>();
		for(List<Card> subCards:ungroupedCards)
		{
			if(subCards.size()>=3)
			{
				groupedCards.add(subCards);
			}
			else
			{
				leftCards.addAll(leftCards.size(), subCards);
			}			
		}
		//now all ungrouped cards have been added to grouped and left cards so we can clear them.
		ungroupedCards.clear();
//		leftCards=sortByFace(leftCards);
		
		makeSetsFromLeftCards(leftCards, groupedCards, ungroupedCards, jokerCards);
		
		//Check if Joker cards can be added as a Hand of jokers

		if(jokerCards.size()>0)
		{
			List<Card> toRemoveLater=null;
			int i=0;
			
			while(jokerCards.size()%3==0)
			{
				List<Card> jokerCardSets=new ArrayList<Card>();
				if(toRemoveLater==null)
				{
					toRemoveLater=new ArrayList<Card>();
				}
				
				jokerCardSets.add(jokerCards.get(i));
				toRemoveLater.add(jokerCards.get(i));
				i++;
				
				jokerCardSets.add(jokerCards.get(i));
				toRemoveLater.add(jokerCards.get(1));
				i++;
				
				jokerCardSets.add(jokerCards.get(i));
				toRemoveLater.add(jokerCards.get(i));			
				i++;
				
				//add the grouped jokers to the grouped list
				groupedCards.add(jokerCardSets);
			}
			
			if(toRemoveLater!=null)
			{
				//remove grouped jokers from the joker list
				for(Card removeCard:toRemoveLater)
				{
					jokerCards.remove(removeCard);
				}
			}
		}

		List<Card> finalMeldCards=new ArrayList<Card>();
		
		//order the final cards as 3,3,3,4
		if(groupOfFour)
		{
			//add the group of four at the end of final meld card list
			List<Card> groupOfFourCards=null;
			for(List<Card> groups:groupedCards)
			{
				if(groups.size()==4)
				{
					groupOfFourCards=groups;
					continue;
				}
				finalMeldCards.addAll(finalMeldCards.size(),groups);
			}
			finalMeldCards.addAll(finalMeldCards.size(), leftCards);
			if(groupOfFourCards!=null)
			{
				finalMeldCards.addAll(finalMeldCards.size(), groupOfFourCards);
			}
		}
		else
		{
			//add the grouped cards at the starting of list, leave other cards as it is at the end of list.
			for(List<Card> groups:groupedCards )
			{
				finalMeldCards.addAll(finalMeldCards.size(),groups);
			}
			finalMeldCards.addAll(finalMeldCards.size(), leftCards);
		}		
		//System.out.println("***********Melding cards ="+finalMeldCards);
		//can clear groupedcards and jokercards list here or use reset method . on points info event
		bot.meldCards(finalMeldCards);	
		return flag;
	}
	private boolean makeSetsFromLeftCards(List<Card> leftCards ,List<List<Card>> groupedTemp ,List<List<Card>> ungroupedTemp ,List<Card> jokersTemp)
	{
		//utilising the left cards to check if  sets can b made
		boolean flag=false;
		leftCards=sortByFace(leftCards);
		
		List<Card> tempRemove=new ArrayList<Card>();
		
		for(int i=0; i<leftCards.size() ; )
		{
			Card leftCard=leftCards.get(i);
			//look if next card is of same face
			if((i+1) >=leftCards.size())
			{
				break;
			}			
			else
			{
				Card tempNext=leftCards.get(i+1);
				if(leftCard.getFace().getValue() == tempNext.getFace().getValue())
				{
					if((i+2) >=leftCards.size())
					{
						break;
					}				
					else
					{
						Card tempNext2=leftCards.get(i+2);
						//look if next card is of same face
						if(leftCard.getFace().getValue() == tempNext2.getFace().getValue())
						{
							i=i+3;	
							//add this set to grouped
							List<Card> newGrouped=new ArrayList<Card>();
							newGrouped.add(leftCard);
							newGrouped.add(tempNext);
							newGrouped.add(tempNext2);
							tempRemove.add(leftCard);
							tempRemove.add(tempNext);
							tempRemove.add(tempNext2);
							
							groupedTemp.add(newGrouped);
							flag=true;
							continue;
						}
						else
						{
							Card joker=useJokerCard(jokersTemp);
							if(joker!=null)
							{
								i=i+2;
								//add this set to grouped
								List<Card> newGrouped=new ArrayList<Card>();
								newGrouped.add(leftCard);
								newGrouped.add(tempNext);
								newGrouped.add(joker);
								tempRemove.add(leftCard);
								tempRemove.add(tempNext);
								flag=true;
							}
							else
							{
								i++;
							}
							continue;
						}
					}					
				}				
				else
				{
					i++;
					continue;
				}		
			}
				
		}
		//remove all sets from leftCards that have been added to grouped cardss
		for(Card removeCard:tempRemove)
		{
			if(leftCards.contains(removeCard))
			{
				leftCards.remove(removeCard);
			}
		}
		//System.out.println("*********** makeSetsFromLeftCards LeftCards="+leftCards.size()+" GroupedTemp="+groupedTemp+" UngroupedTemp="+ungroupedTemp);
		return flag;
	}
	private int calculateJokersNeeded()
	{
		int count=0;
		for(List<Card> subCards:ungroupedCards)
		{
			int needJokers=3-subCards.size();
			count=count+needJokers;
		}
		return count;
	}
	
	private Card useJokerCard(List<Card> jokersTemp)
	{
		Card jCard=null;
		if(jokersTemp.size()>0)
		{
			jCard=jokersTemp.get(0);
			jokersTemp.remove(jCard);
		}
		return jCard;
	}
	public Card getCardToDiscard(List<List<Card>> ungroupedTemp, List<Card> jokersTemp)
	{
		Card discard=null;
		List<Card> discardedFromList=null;
		//if ungrouped cards contains a list of single card with higher rank.
		for(List<Card> subCards: ungroupedTemp)
		{
			if(subCards.size()==1)
			{
				Card compCard=subCards.get(0);
				if(isJoker(compCard))
				{
					continue;
				}
				if(discard==null)
				{
					if(!isJoker(subCards.get(0)))
					{
						discard=subCards.get(0);
						discardedFromList=subCards;
						continue;
					}				
				}
				if(discard.getFace().getValue() < subCards.get(0).getFace().getValue())
				{
					discard=subCards.get(0);
					discardedFromList=subCards;
				}
			}
		}
		if(discard!=null)
		{
			removeCardWithFace(discardedFromList, discard.getFace().getValue());
			return discard;
		}
		//if no list with single card is found then look for list with a card of highest rank.
		for(List<Card> subCards: ungroupedTemp)
		{
			if(subCards.size()==2)
			{				
				for(Card c:subCards)
				{
					if(isJoker(c))
					{
						continue;
					}
					if(discard==null)
					{
						discard=c;
						discardedFromList=subCards;
						continue;
					}
					if(discard.getFace().getValue() < c.getFace().getValue())
					{
						discard=c;
						discardedFromList=subCards;
					}
				}				
			}
		}
		if(discard!=null)
		{
			removeCardWithFace(discardedFromList, discard.getFace().getValue());
			return discard;
		}
		//System.out.println("***********No card in ungrouped/grouped cards to discard so discarding joker ");
		//still if no cards found to discard ,select any from jokers list
		for(Card joker:jokersTemp)
		{
			discard=joker;
			removeCardWithFace(jokersTemp, discard.getFace().getValue());
			return discard;
		}	
		
		//return null
		return discard;
	}
	
	
	
	public void usePickedCardAndDiscardCard(Card c)
	{
		List<Card> meldCards=new ArrayList<Card>();
		//System.out.println("Picked card="+c);
		if(usingCard(c))
		{
			//System.out.println("***********Used card "+c);
			getCardsCount();
			//used picked successfully
		}
		if(canIMeld())
		{
			//System.out.println("***********Declaring  ");
			return;
		}		
		Card discard=getCardToDiscard(ungroupedCards,jokerCards);
		getCardsCount();
		//check if i can meld
		
		
		//if not then discard a card
		if(discard!=null)
		{
			//System.out.println("***********Discarding card "+discard);
			bot.sendUserAction(22,discard);
		}
		else
		{
			//System.out.println("No card available to discard");
		}
		getCardsCount();
	}
	private boolean usingCard(Card c)
	{
		boolean flag=false;
		if(isPickedFromDeck)
		{			
			if(!groupOfFour)
			{
				flag=useCardPicked(c, groupedCards);
			}
			if(!flag)
			{
				flag=useCardPicked(c, ungroupedCards);
			}			
		}
		else
		{
			if(usePickedInGroup)
			{
				flag=useCardPicked(c, groupedCards);
			}
			else
			{
				flag=useCardPicked(c, ungroupedCards);
			}			
		}
		if(!flag)
		{
			if(isJoker(c))
			{
				jokerCards.add(c);
				flag=true;
			}
		}
		if(!flag)
		{
			//add it as a new list to ungrouped
			List<Card> newUngrouped=new ArrayList<Card>();
			newUngrouped.add(c);
			ungroupedCards.add(newUngrouped);
			flag=true;
		}
		
		return flag;		
	}
	private boolean useCardPicked(Card c, List<List<Card>> cards)
	{		
		for(List<Card> subCards:cards)
		{
			List<Integer> temp=new ArrayList<Integer>();
			//check if forming a sequence or a set
			
			if(subCards.size() == 2)
			{//checking in ungrouped cards
				if(subCards.get(0).getFace().value == subCards.get(1).getFace().value)
				{
					//if a pair is there and discarded card forms a set.
					if(subCards.get(0).getFace().value==c.getFace().value)
					{
						//add the card to this group and remove this group from ungrouped cards and to grouped cards.
						cards.remove(subCards);
						subCards.add(c);						
						groupedCards.add(sortByFace(subCards));
						return true;
					}					
				}
				else
				{
					if(subCards.get(0).getSuit().getValue() != c.getSuit().getValue())
					{
						//if card is of different suit than the suit of current card group then skip, as cant be used in case of a sequence
						continue;
					}
					//if discarded card forms a sequence
					temp.clear();
					Card firstCard=subCards.get(0);
					Card secondCard=subCards.get(1);
					temp.add(firstCard.face.value);
					temp.add(secondCard.face.value);
					temp.add(c.face.value);
					Collections.sort(temp);				
					if(temp.get(2)-temp.get(1) == temp.get(1)-temp.get(0))
					{//use iterator
						//add the card to this group and remove this group from ungrouped cards and to grouped cards.
						ungroupedCards.remove(subCards);
						subCards.add(c);						
						groupedCards.add(sortByFace(subCards));						
						temp.clear();
						return true;
					}
				}				
			}
			
			//check in grouped cards for a groupOfFour
			else if( subCards.size() == 3 && groupOfFour == false )
			{	
				if(subCards.get(0).getSuit().getValue() != c.getSuit().getValue())
				{
					//if card is of different suit than the suit of current card group then skip, as cant be used in case of a sequence
					continue;
				}
				//if picked card forms a sequence, not looking for quad. dunno y :)
				Card firstCard=subCards.get(0);
				Card secondCard=subCards.get(1);
				Card thirdCard=subCards.get(2);
				temp.clear();
				temp.add(firstCard.face.value);
				temp.add(secondCard.face.value);
				temp.add(thirdCard.face.value);
				temp.add(c.face.value);
				
				Collections.sort(temp);				
				if((temp.get(2)-temp.get(1) == temp.get(1)-temp.get(0)) && (temp.get(3)-temp.get(2) == temp.get(2)-temp.get(1)))
				{
					//forms  a sequence of 4
					groupOfFour=true;
					subCards.add(c);
					subCards=sortByFace(subCards);
					
					return true;
				}
				temp.clear();
			}
		}
		return false;
	}

	
			   
	
	
	

	private List<Card> removeCommonValue(List<Card> list, List<Card> subList)
	{		
		for(Card c: subList)
		{
			list= removeCardWithFace(list , c.getFace().value);
		}
		return list;
	}
	private List<Card> removeCardWithFace(List<Card> cards,int val)
	{
		Card toBeRemoved=null;
		for(int i=0;i< cards.size() ; i++)
		{			
			Card card=cards.get(i);
			if(card.getFace().value==val)
			{
				toBeRemoved=card;
				break;
			}
		}
		if(toBeRemoved!=null)
		{
			cards.remove(toBeRemoved);
		}
		return cards;
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
		sortedCards.addAll(sortedCards.size(),type0);
		sortedCards.addAll(sortedCards.size(),type1);
		sortedCards.addAll(sortedCards.size(),type2);
		sortedCards.addAll(sortedCards.size(),type3);
		
		//returns card sorted by suit
		return sortedCards;
	}
	private List<Card> groupBySuit(List<Card> cards)
	{
		//sorts them by face and groups them by suit
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
		sortedCards.addAll(sortedCards.size(),sortByFace(type0));
		sortedCards.addAll(sortedCards.size(),sortByFace(type1));
		sortedCards.addAll(sortedCards.size(),sortByFace(type2));
		sortedCards.addAll(sortedCards.size(),sortByFace(type3));
		//this will return sorted face value cards grouped by suit.
		return sortedCards;
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
		return cards;
	}
	private void resetOnNewRound()
	{
		iDeclared=false;
		ungroupedCards.clear();
		groupedCards.clear();
		jokerCards.clear();
		cutJoker=null;
		discardedCard=null;
		cardsToBeMeld=null;
	}
	public static void main(String[] args)
	{
 
//		//System.out.println(list);
//		List<Card> list=new ArrayList<Card>();
//		//generate cards
		RummyBotLogic rc = new RummyBotLogic(null);
//		Random randSuit=new Random();
//		Random randFace=new Random();
//		int count=0;
//		while(count<13)
//		{
//			count++;
//			int suitVal=randSuit.nextInt(4);
//			int faceVal=randFace.nextInt(14);
//			if(suitVal< 1)
//			{
//				suitVal=1;
//			}
//			if(faceVal< 2)
//			{
//				faceVal=2;
//			}
//			Face f=new Face();
//			Suit s=new Suit();
//			f.setValue(faceVal);
//			s.setValue(suitVal);
//			
//			Card c=new Card();
//			c.setFace(f);
//			c.setSuit(s);
//			list.add(c);
//		}
//		rc.setCards(list);
//		
//	
//		
//		//create a discarded card
//		
//		int suitVal=randSuit.nextInt(4);
//		int faceVal=randFace.nextInt(14);
//		if(suitVal< 1)
//		{
//			suitVal=1;
//		}
//		if(faceVal< 2)
//		{
//			faceVal=2;
//		}
//		Face f=new Face();
//		Suit s=new Suit();
//		f.setValue(faceVal);
//		s.setValue(suitVal);
//		
//		Card discardedC=new Card();
//		discardedC.setFace(f);
//		discardedC.setSuit(s);
//		
//		rc.setDiscardedCard(discardedC);
//		rc.setJoker(discardedC);
//		rc.checkForGroupedCards();
//		rc.onUserTurn();
//		
//		rc.usePickedCardAndDiscardCard(discardedC);
		List<Card> list=new ArrayList<Card>();
		
		Face f1=new Face();
		Suit s1=new Suit();
		f1.setValue(21);
		s1.setValue(0);
		
		Card c1=new Card();
		c1.setFace(f1);
		c1.setSuit(s1);
		list.add(c1);
		
		Face f2=new Face();
		Suit s2=new Suit();
		f2.setValue(8);
		s2.setValue(0);
		
		Card c2=new Card();
		c2.setFace(f2);
		c2.setSuit(s2);
		list.add(c2);
		
		rc.setJokerCard(c2);
		
		Face f3=new Face();
		Suit s3=new Suit();
		f3.setValue(21);
		s3.setValue(0);
		
		Card c3=new Card();
		c3.setFace(f3);
		c3.setSuit(s3);
		list.add(c3);
		

		Face f4=new Face();
		Suit s4=new Suit();
		f4.setValue(2);
		s4.setValue(0);
		
		Card c4=new Card();
		c4.setFace(f4);
		c4.setSuit(s4);
		list.add(c4);
		
		list=rc.sortByFace(list);
		
		if(rc.isValidSet(list))
		{
			//System.out.println("Valid ");
		}
		else
		{
			//System.out.println("Invalid ");
		}
	}
	public static void msg(String msg)
	{
		//System.out.println(msg);
	}
	public void setJokerCard(Card jokerCard) {
		this.cutJoker = jokerCard;
	}
	public Card getJokerCard() {
	
		return cutJoker;
	}
	public void setDiscardedCard(Card discardedCard) {
		this.discardedCard = discardedCard;
	}
	public Card getDiscardedCard() {
		return discardedCard;
	}
	public void onMeld()
	{
		//System.out.println("Received meld");
		//Card discard=getDiscardCard();
		if(iDeclared)
		{
			//if i have declared then use stored hand cards
			if(cardsToBeMeld!=null)
			{
				bot.meldCards(cardsToBeMeld);
			}			
		}
		else
		{
			//create handcards to meld from current cards
			meldCards();
		}		
	}
	public void updatePoints(int points) {
		this.points += points;
		//resetOnPoints();
	}
	public void isValueGame()
	{
		isValueGame=true;
	}
	public void isCutJokerPickable()
	{
		isCutJokerPickable=true;
	}
	private boolean isValidSequence(List<Card> cards)
	{
		boolean flag=true;
		
		int faceVal=-1;
		int suitVal=-1;
			
		for(int i=0 ; i<cards.size() ;i++)
		{	
			Card current=cards.get(i);
			//joker card is always a valid sequence card
			if(!isJoker(current))
			{
				//if not a joker card
				if(faceVal == -1)
				{
					faceVal=current.getFace().getValue();
				}
				if(suitVal == -1)
				{
					suitVal=current.getSuit().getValue();
				}
				
				if( suitVal != current.getSuit().getValue() )
				{
					//suit is same as previous cards
					flag=false;
					break;
				}
				if( current.getFace().getValue() - faceVal == 0 )
				{
					//current card has the expected consecutive faceval
					faceVal++;
					continue;
				}
				else
				{
					flag=false;
					break;
				}
			}
			else
			{		
				if(faceVal!=-1)
				{
					//dont increment the face val if intial card is joker card
					faceVal++;
					continue;
				}				
			}	
		}
		return flag;
	}
	private boolean isPure(List<Card> cards)
	{
		boolean flag=false;
		for(int i=0; i < cards.size(); i++)
		{
			Card card=cards.get(i);
			Card prev=null;
			Card next=null;
			if(i != 0)
			{
				prev=cards.get(i-1);
			}
			if(i < cards.size()-1)
			{
				next=cards.get(i+1);
			}
			if(card.getFace().getValue() == cutJoker.getFace().getValue())
			{
				if(prev!=null  && card.getFace().getValue()-prev.getFace().getValue()!=  1)
				{
					flag=true;
					return flag;
				}				
				if(next!=null  && next.getFace().getValue()-card.getFace().getValue()!=  1)
				{
					flag=true;
					return flag;
				}				
			}
		}
		return flag;
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
}
