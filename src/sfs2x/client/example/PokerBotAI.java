package sfs2x.client.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import common.Card;
import common.Face;
import common.Suit;


public class PokerBotAI 
{
	private List<Card> holeCards=null;
	private List<Card> commCards=null;
	private ArrayList<Odds> odds=new ArrayList<Odds>();
	
	//private int outs;
	private enum DrawEnum { POCKET_PAIR , PAIR, SET, QUADS};
	public static enum Round { PREFLOP , FLOP, TURN, RIVER };
	public static enum Rank {None,HighCard,OnePair,TwoPair,ThreeOfAKind, Straight,Flush,FullHouse,FourOfAKind,StraightFlush,RoyalFlush};
	
	
	public void calcHandOuts( List<Card> hCards, List<Card> cCards)
	{
		//System.out.println("INSIDE CALCHANDOUTS");
		this.holeCards = hCards;
		this.commCards = cCards;		
		
		//h1_suit = substr($this->holeCards[0], 0, 1);
	    Suit h1Suit=this.holeCards.get(0).getSuit();
		//h1_face = substr($this->holeCards[0], 1);
		Face h1Face=this.holeCards.get(0).getFace();
		//h2_suit = substr($this->holeCards[1], 0, 1);
		Suit h2Suit=this.holeCards.get(1).getSuit();
		//h2_face = substr($this->holeCards[1], 1);
		Face h2Face=this.holeCards.get(1).getFace();
		
		
		if(commCards.size()==0)
		{
			//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP");
			Odds newOdd=new Odds();	
			newOdd.setHand(Rank.None);
			newOdd.setHasHand(false);
			
			if(h1Suit.value==h2Suit.value )
			{				
				//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: FLUSH FALSE");
				newOdd.setOuts(0);
				newOdd.setHand(Rank.Flush);
				newOdd.setHasHand(false);
				
				if( ( h1Face.value+4 < h2Face.value )  || ( h2Face.value+4 < h1Face.value ) )
				{			
					//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: StraightFlush FALSE");
					newOdd.setHand(Rank.StraightFlush);
					newOdd.setHasHand(false);
				}				
			}
			else if( ( h1Face.value+4 < h2Face.value )  || ( h2Face.value+4 < h1Face.value ) )
			{
				//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: Straight FALSE");
				newOdd.setHand(Rank.Straight);
				newOdd.setHasHand(false);
			}
			else if( h1Face.value == h2Face.value )
			{
				//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: ONEPAIR TRUE");
				newOdd.setHand(Rank.OnePair);
				newOdd.setHasHand(true);
			}
			else if( h1Face.value>10 && h2Face.value>10)
			{
				//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: HighCard true");
				newOdd.setHand(Rank.HighCard);
				newOdd.setHasHand(true);
			}
			else if(h1Face.value>13 || h2Face.value>13 )
			{
				//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: HighCard true");
				newOdd.setHand(Rank.HighCard);
				newOdd.setHasHand(true);				 
			}
			else
			{
				//System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: NONE FALSE");
				newOdd.setHand(Rank.None);
				newOdd.setHasHand(false);	
			}
			odds.add(newOdd);	
			return;
		}
		DrawEnum draw = DrawEnum.POCKET_PAIR;
		// check that hole cards is a pair
		if (h1Face.equals(h2Face) )
		{
			// none of the comm cards have same face
			for(Card card : this.commCards)
			{
				if (card.getFace().equals(h1Face))
				{
					if (draw.equals(DrawEnum.POCKET_PAIR))
					{
						// we have three of a kind
						draw = DrawEnum.SET;
					}
					else if (draw.equals(DrawEnum.SET))
					{
						draw = DrawEnum.QUADS;
					}
				}
			}
			
			switch (draw)
			{
				case POCKET_PAIR: // this means to make a set, we have 2 outs
					//this->odds[] = array('outs' => 2, 'desc' => "Pocket Pair to Set", 'has' => 'One Pair', 'goal' => 'Set');
					Odds newOdd=new Odds();
					newOdd.setOuts(2);
					newOdd.setDesc("Pocket Pair to Set");
					newOdd.setHas("One Pair");
					newOdd.setGoal("Set");
					newOdd.setHasHand(true);
					newOdd.setHand(Rank.OnePair);
					
					odds.add(newOdd);
					break;
					
				case SET: // can strive for quads or full house
					// if com cards has a pair, else outs = 7
					if (comContainsPair())
					{
						// we have full house already, can try for quads
						//$this->odds[] = array('outs' => 1, 'desc' => "Full House to Quads", 'has' => 'Full House', 'goal' => 'Quads');
						newOdd=new Odds();
						newOdd.setOuts(1);
						newOdd.setDesc("Full House to Quads");
						newOdd.setHas("Full House");
						newOdd.setGoal("Quads");
						newOdd.setHasHand(true);
						newOdd.setHand(Rank.FullHouse);
						odds.add(newOdd);
						
					}
					else 
					{
						//$this->odds[] = array('outs' => 7, 'desc' => "Set to Full House or Quads", 'has' => 'Set', 'goal' => 'Full House or Quads');
						newOdd=new Odds();
						newOdd.setOuts(7);
						newOdd.setDesc("Set to Full House or Quads");
						newOdd.setHas("Set");
						newOdd.setGoal("Full House or Quads");
						newOdd.setHasHand(true);
						newOdd.setHand(Rank.ThreeOfAKind);
						
						odds.add(newOdd);
					}
					break;	
					
				case QUADS:
					//$this->odds[] = array('outs' => 0, 'desc' => "Has Quads", 'has' => 'Quads', 'goal' => 'None');
					newOdd=new Odds();
					newOdd.setOuts(0);
					newOdd.setDesc("Has Quads");
					newOdd.setHas("Quads");
					newOdd.setGoal("None");
					newOdd.setHasHand(true);
					newOdd.setHand(Rank.FourOfAKind);
					
					odds.add(newOdd);
					break;
					
				default: break;
			}
		}
	    else 
	    {   // check for pairs, set, full house, quads
			//int numPairs =checkPairs();
	    	checkPairs(); 
	    }
		//print_r($this->odds);
		
		// check for straight		
	    int straight = checkStraight(false);
		
		// check for flush
		int flush = checkFlush();
		
		// check for straight flush and add to odds array if we can make it.
	    int straightFlush = checkStraightFlush();
	    
	    
	}
	
	private boolean comContainsPair()
	{
		boolean hasPair=false;
		
//		$faces = array(0,0,0,0,0,0,0,0,0,0,0,0,0);
//		foreach ($this->commCards as $card)
//		{
//			$faces[getFace($card)] += 1;
//		}
//		foreach ($faces as $k => $v)
//		{
//			if ($v > 1) $hasPair = TRUE;
//		}
		
		for(int i=0;i< commCards.size();i++)
		{
			Card c=commCards.get(i);
			if( !(c.getSuit().equals( commCards.get(i).getSuit() )) && c.getFace().equals(commCards.get(i).getFace()) )
			{
				hasPair=true;
				break;
			}
		}
		
		return hasPair;
	}
	
	private int checkStraight(boolean flush)
	{
		int outs=0;
		
		//$h1_face = getNumeric(getFace($this->holeCards[0]));
		int h1Face=holeCards.get(0).getFace().getValue();
		//$h2_face = getNumeric(getFace($this->holeCards[1]));
		int h2Face=holeCards.get(1).getFace().getValue();
		//print "faces ".$h1_face.$h2_face;
		int greater = h2Face;
		int lesser = h1Face;
		
		if (h2Face != 1  && ( h1Face == 1 || h1Face > h2Face) )
		{
			greater = h1Face;
			lesser = h2Face;
		}
		
		if (greater == 1)
		{
			greater = 14;
		}
		
		if (flush)
		{
			outs = checkSuitedSequence(lesser, greater);
		}
		else 
		{
			outs =checkSequence(lesser, greater);
		}
		return outs;		 
	}
	
	private int checkSequence(int min, int max)
	{
		//print "min ".$min." max ".$max;
		int outs = 0;
//		$faces = array();
//		$comFaces = array();
		ArrayList<Integer> faces=new ArrayList<Integer>();
		ArrayList<Integer> comFaces=new ArrayList<Integer>();
		//print_r($this->commCards);
		for(Card card:commCards)
		{
			int val = card.getFace().getValue();
//			$faces[] = $val;
//			$comFaces[] = $val;
			faces.add(val);
			comFaces.add(val);
			if (val == 1)
				comFaces.add(14);
		}
//		$faces[] = getNumeric(getFace($this->holeCards[0]));
//		$faces[] = getNumeric(getFace($this->holeCards[1]));
		faces.add(holeCards.get(0).getFace().getValue());
		faces.add(holeCards.get(1).getFace().getValue());
		
		// if ace is found, add 14 number card
//		for(Integer f:faces)
//		{
//		    if (f == 1)
//		    {
//		       $faces[] = 14;
//		       break;
//		    }	
//		}
		for(int i=0;i<faces.size();i++)
		{
			int f=faces.get(i);
			 if (f == 1)
		     {
		        faces.set(i, 14);
		        break;
		     }	
		}
		
		boolean overcard = false;
		// either the lesser hole card is max or greater is min or anything in between
		for (int i = min-4; i <= max; i++)
		{
		   //print " i ".$i;
//		   $seq = array($i, $i+1, $i+2, $i+3, $i+4);
		   ArrayList<Integer> seq= new ArrayList<Integer>();
		   seq.add(i);
		   seq.add(i+1);
		   seq.add(i+2);
		   seq.add(i+3);
		   seq.add(i+4);
		   
			
			//print_r($seq);
			//print_r($faces);
			//$diff = array_diff($seq, $faces); // tells the numbers in seq not in faces
			ArrayList<Integer> diff=(ArrayList<Integer>) arrayDiff(seq, faces);
			//print_r($diff);
			if (diff.size() == 0) // we have a sequence
			{
				outs = 0;
				// check if we have an overcard
				//if ((i+4 == min || i+4 == max ) && !in_array((i+4), comFaces) )
				if ((i+4 == min || i+4 == max ) && !comFaces.contains(i+4) )
				{
					// we have a straight with overcard
					//$this->odds[] = array('outs' => 0, 'desc' => "Has Straight with Overcard", 'has' => 'Straight with Overcard', 'goal' => 'None');
					Odds newOdd=new Odds();
					newOdd.setOuts(0);
					newOdd.setDesc("Has Straight with Overcard");
					newOdd.setHas("Straight with Overcard");
					newOdd.setGoal("None");
					newOdd.setHasHand(false);
					newOdd.setHand(Rank.None);
					
					odds.add(newOdd);
				}
				else 
				{
					//$this->odds[] = array('outs' => 0, 'desc' => "Has Straight", 'has' => 'Straight', 'goal' => 'None');
					Odds newOdd=new Odds();
					newOdd.setOuts(0);
					newOdd.setDesc("Has Straight");
					newOdd.setHas("Straight");
					newOdd.setGoal("None");
					newOdd.setHasHand(true);
					newOdd.setHand(Rank.Straight);
					
					odds.add(newOdd);
				}
				return outs;
			}
			else if (diff.size() == 1) // we can try for a sequence
			{
				//$only = array_shift($diff);				 
				int only= diff.get(0);
				//print $onlyId;
				if (only != 15)
				{
					//print "diff 1";
					outs += 4;
					// check if we have an overcard
					//if ((i+4 == min || i+4 == max) && !in_array((i+4), comFaces))
					if ((i+4 == min || i+4 == max) && ! comFaces.contains(i+4))
					{
						overcard = true; // this might be true only for one of the two ways in which we can make a straight, thats ok
					}
				}
			}
		}
		
		if (outs > 0)
		{
			//print "outs ".$outs;
			if (overcard)
			{
				//$this->odds[] = array('outs' => $outs, 'desc' => "Straight with Overcard", 'has' => 'None', 'goal' => 'Straight with Overcard');
				Odds newOdd=new Odds();
				newOdd.setOuts(outs);
				newOdd.setDesc("Straight with Overcard");
				newOdd.setHas("None");
				newOdd.setGoal("Straight with Overcard");
				newOdd.setHasHand(false);
				newOdd.setHand(Rank.None);
				
				odds.add(newOdd);
			}
			else
			{
				//$this->odds[] = array('outs' => $outs, 'desc' => "Straight", 'has' => 'None', 'goal' => 'Straight');
				Odds newOdd=new Odds();
				newOdd.setOuts(outs);
				newOdd.setDesc("Straight");
				newOdd.setHas("None");
				newOdd.setGoal("Straight");
				newOdd.setHasHand(false);
				newOdd.setHand(Rank.None);
				
				odds.add(newOdd);
			}
		}
		return outs;
	}
	private ArrayList arrayDiff( ArrayList s1, ArrayList s2 )
	{
		ArrayList diff=new ArrayList();
		
		for(int i=0; i < s1.size() ; i++)
		{
			for(int j=0; j < s2.size() ; j++)
			{
				if( ! s2.contains(s1.get(i)))
				{
					diff.add(s1.get(i));
				}
			}
		}
		return diff;
	}
	
	private int checkFlush()
	{
		int outs = -1;
//		$h1_suit = getSuit($this->holeCards[0]);
//		$h2_suit = getSuit($this->holeCards[1]);
		String h1Suit=holeCards.get(0).getSuit().getText();
		String h2Suit=holeCards.get(1).getSuit().getText();
		
//		$suits = array('c' => 0, 'd' => 0, 'h' => 0, 's' => 0);
//		$maxFaces = array('c' => 2, 'd' => 2, 'h' => 2, 's' => 2);
		HashMap<String, Integer> suits=new HashMap<String, Integer>();		
		HashMap<String, Integer> maxFaces=new HashMap<String, Integer>();
		suits.put("c", 0);
		suits.put("d", 0);
		suits.put("h", 0);
		suits.put("s", 0);
		maxFaces.put("c", 2);
		maxFaces.put("d", 2);
		maxFaces.put("h", 2);
		maxFaces.put("s", 2);
		
		for(Card card: commCards)
		{
			//$suits[getSuit(card)] += 1; 
			int co=suits.get(card.getSuit().getText());
			suits.put(card.getSuit().getText(), ++co);
			
			if (card.getFace().getValue() == 1)
			{
				//$maxFaces[getSuit(card)] = 1;
				maxFaces.put(card.getSuit().getText(),1);
			}
			//else if ($maxFaces[getSuit(card)] != 1 && card.getFace().getValue() > $maxFaces[getSuit(card)])
			else if (maxFaces.get(card.getSuit().getText()) != 1 && card.getFace().getValue() > maxFaces.get(card.getSuit().getText()))
			{
				//$maxFaces[getSuit(card)] =card.getFace().getValue();
				maxFaces.put(card.getSuit().getText(), card.getFace().getValue());
			}
		}
		
		// get com max face card and hole cards
		int cMax = getMaxFaceValue(commCards);
		int hMax = getMaxFaceValue(holeCards);
		
		boolean overcard = false;
		Set<String> kSet = suits.keySet();
//		for($suits as $k => $v)
		for(String k : kSet)
		{
			int v = suits.get(k);
			// need this suit in both holecards to get 4 of same suit
			if (v == 2 && k == h1Suit && k == h2Suit)
			{
				outs = 9;
				if (hMax == 1)
				{
					overcard = true;
				}
				else
				{
					//if ($maxFaces[$k] != 1 && $maxFaces[$k] < hMax)
					if (maxFaces.get(k)!= 1 && maxFaces.get(k) < hMax)
					{
						overcard = true;
					}
				}
			}
			else if (v == 3) // 3 in com cards, 1 in hole card of $k suit
			{
				if (k == h1Suit && k == h2Suit) // both hole cards are $k suit
				{
					outs = 0; // has a flush
					if (hMax == 1)
					{
						overcard = true;
					}
					else
					{
						if (maxFaces.get(k) != 1 && maxFaces.get(k) < hMax)
						{
							overcard = true;
						}
					}
				}
				else if (k == h1Suit || k == h2Suit) // only one hole card is $k
				{
					outs = 9;
					// do we have an overcard of this suit
					if (k == h1Suit)
					{
						int h1Face = holeCards.get(0).getFace().getValue();
						if (h1Face == 1)
						{
							overcard = true;
						}
						else 
						{
							if ( maxFaces.get(k) != 1 && maxFaces.get(k) < h1Face)
							{
								overcard = true;
							}
						}
					}
					if (k == h2Suit)
					{
						int h2Face = holeCards.get(1).getFace().getValue();
						if (h2Face == 1)
						{
							overcard = true;
						}
						else 
						{
							if (maxFaces.get(k) != 1 && maxFaces.get(k) < h2Face)
							{
								overcard = true;
							}
						}
					}
				}
			}
			else if (v == 4 && (k == h1Suit || k == h2Suit)) // 4 in com cards, atleast one in hole card of $k suit
			{
				outs = 0; // has a flush already
				if (k == h1Suit)
				{
					int h1Face = holeCards.get(0).getFace().getValue();
					if (h1Face == 1)
					{
						overcard = true;
					}
					else 
					{
						if (maxFaces.get(k) != 1 && maxFaces.get(k) < h1Face)
						{
							overcard = true;
						}
					}
				}
				if (k == h2Suit)
				{
					int h2Face = holeCards.get(1).getFace().getValue();
					if (h2Face == 1)
					{
						overcard = true;
					}
					else 
					{
						if (maxFaces.get(k) != 1 && maxFaces.get(k) < h2Face)
						{
							overcard = true;
						}
					}
				}
			}
			
			if (outs == 0)
			{
				if(overcard)
				{
					//$this->odds[] = array('outs' => $outs, 'desc' => "Has Flush with Overcard", 'has' => 'Flush with Overcard', 'goal' => 'None');
					Odds newOdd=new Odds();
					newOdd.setOuts(outs);
					newOdd.setDesc("Has Flush with Overcard");
					newOdd.setHas("Flush with Overcard");
					newOdd.setGoal("None");
					newOdd.setHasHand(false);
					newOdd.setHand(Rank.None);
					
					odds.add(newOdd);
				}
				else
				{
					//$this->odds[] = array('outs' => $outs, 'desc' => "Has Flush", 'has' => 'Flush', 'goal' => 'None');
					Odds newOdd=new Odds();
					newOdd.setOuts(outs);
					newOdd.setDesc("Has Flush");
					newOdd.setHas("Flush");
					newOdd.setGoal("None");
					newOdd.setHasHand(true);
					newOdd.setHand(Rank.Flush);
					
					odds.add(newOdd);
				}
				return outs;
			}
			else if (outs > 0)
			{
				if (overcard)
				{
					//$this->odds[] = array('outs' => $outs, 'desc' => "Flush with Overcard", 'has' => 'None', 'goal' => 'Flush with Overcard');
					Odds newOdd=new Odds();
					newOdd.setOuts(outs);
					newOdd.setDesc("Flush with Overcard");
					newOdd.setHas("None");
					newOdd.setGoal("Flush with Overcard");
					newOdd.setHasHand(false);
					newOdd.setHand(Rank.None);
					
					odds.add(newOdd);
				}
				else
				{
					//$this->odds[] = array('outs' => $outs, 'desc' => "Flush", 'has' => 'None', 'goal' => 'Flush');
					Odds newOdd=new Odds();
					newOdd.setOuts(outs);
					newOdd.setDesc("Flush");
					newOdd.setHas("None");
					newOdd.setGoal("Flush");
					newOdd.setHasHand(false);
					newOdd.setHand(Rank.None);
					
					odds.add(newOdd);
				}
				return outs;
			}
		}
		
		return outs;
	}
	private int checkStraightFlush()
	{
		int outs = checkStraight(true);
		return outs;
	}
	private int checkSuitedSequence(int min, int max)
	{	
		int outs = 0;
		//$total = array_merge($this->commCards, $this->holeCards);
		ArrayList<Card> totalCards= new ArrayList<Card>();
		totalCards.addAll(commCards);
		totalCards.addAll(holeCards);
		
		//$cards = array();
		ArrayList<String> cards =new ArrayList<String>();
		
		for(Card card : totalCards)
		{
			int val = card.getFace().getValue();
			//$cards[] = getSuit($card).$val;
			cards.add(card.getSuit().getText()+val);
			if (val == 1)
			{
				// add a card with face = 14
				//$cards[] = getSuit($card).'14';
				cards.add(card.getSuit().getText()+"14");
				
			}
		}
		  //print_r($cards);
		 //print "min ".$min." max ".$max;
		 boolean overcard = false;
		 //$suits = array("c", "d", "h", "s");
		 ArrayList<String> suits= new ArrayList<String>();
		 suits.add("c");
		 suits.add("d");
		 suits.add("h");
		 suits.add("s");
		 // either the lesser hole card is max or greater is min or anything in between
		 for (int i = min-4; i <= max; i++)
		 {
			for(int j=0;j<suits.size();j++)
			{
				String suit=suits.get(j);
				//$seq = array();
				ArrayList<String> seq=new ArrayList<String>();
//				$seq[] = $suit.$i;
//				$seq[] = $suit.($i+1);
//				$seq[] = $suit.($i+2);
//				$seq[] = $suit.($i+3);
//				$seq[] = $suit.($i+4);
				seq.add(suit+i);
				seq.add(suit+(i+1));
				seq.add(suit+(i+2));
				seq.add(suit+(i+3));
				seq.add(suit+(i+4));
				
//				$diff = array_diff($seq, $cards); // tells the numbers in seq not in cards
				ArrayList<String> diff= arrayDiff(seq, cards);
				//print "<br>";
				//print_r($seq);
				if (diff.size() == 0) // we have a straight flush
				{
					outs = 0;
					// check if we have an overcard
					//if (in_array($suit.($i+4), $this->holeCards) || ($i+4 == 14 && in_array($suit.'1', $this->holeCards)) )
					if (holeCards.contains(suit+(i+4)) || (i+4 == 14 && holeCards.contains(suit+1)) )
					{
						// we have a straight flush with overcard
						//$this->odds[] = array('outs' => 0, 'desc' => "Has Straight Flush with Overcard", 'has' => 'Straight Flush with Overcard', 'goal' => 'None');
						Odds newodd=new Odds();
						newodd.setOuts(0);
						newodd.setDesc("Has Straight Flush with Overcard");
						newodd.setHas("Straight Flush with Overcard");
						newodd.setGoal("None");
						newodd.setHasHand(false);
						newodd.setHand(Rank.None);
						
						odds.add(newodd);
					
					}
					else 
					{
						//$this->odds[] = array('outs' => 0, 'desc' => "Has Straight Flush", 'has' => 'Straight Flush', 'goal' => 'None');
						Odds newodd=new Odds();
						newodd.setOuts(0);
						newodd.setDesc("Has Straight Flush");
						newodd.setHas("Straight Flush");
						newodd.setGoal("None");
						newodd.setHasHand(true);
						newodd.setHand(Rank.StraightFlush);
						
						
						odds.add(newodd);
					}
					return outs;
				}
				else if (diff.size() == 1) // we can try for a straight flush
				{
					//$only = array_shift($diff);
					String only=diff.get(0);
					//print $onlyId;
					//if ($only != $suit.'15')
					if (! only.equals(suit+"15"))
					{
						//print "<br> diff is 1";
						//print_r($diff);
						outs += 1;
						//if (in_array($suit.($i+4), $this->holeCards) || ($i+4 == 14 && in_array($suit.'1', $this->holeCards)) )
						if ( holeCards.contains(suit+(i+4)) || (i+4 == 14 && holeCards.contains(suit+"1")) )
						{
							overcard = true;
						}
					}
				}
			}
		}
		
		if (outs > 0)
		{
			if (overcard)
			{
				//$this->odds[] = array('outs' => $outs, 'desc' => "Straight Flush with Overcard", 'has' => 'None', 'goal' => 'Straight Flush with Overcard');
				Odds newodd=new Odds();
				newodd.setOuts(outs);
				newodd.setDesc("Straight Flush with Overcard");
				newodd.setHas("None");
				newodd.setGoal("Straight Flush with Overcard");
				newodd.setHasHand(false);
				newodd.setHand(Rank.None);
				
				odds.add(newodd);
			}
			else
			{
				//$this->odds[] = array('outs' => $outs, 'desc' => "Straight Flush", 'has' => 'None', 'goal' => 'Straight Flush');
				Odds newodd=new Odds();
				newodd.setOuts(outs);
				newodd.setDesc("Straight Flush");
				newodd.setHas("None");
				newodd.setGoal("Straight Flush");
				newodd.setHasHand(false);
				newodd.setHand(Rank.None);
				
				odds.add(newodd);
			}
		}
		return outs;		 
	}
	
	private void checkPairs()
	{
		int pair = 0;
		int set = 0;
		int quad = 0;
		int overCard=0;
		//$faces = array(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
		int[] faces= new int[15];
		for(int i=0;i< faces.length;i++)
		{
			faces[i]=0;
		}
		
//		foreach ($this->commCards as $card)
//		{
//			$faces[getFace($card)] += 1;
//		}
		for(Card c: commCards)
		{
			faces[c.getFace().getValue()] += 1;			
		}
		// at least one hole card should be used for the pair
		for(Card c: holeCards)
		{
			if (faces[c.getFace().getValue()] == 1)
			{
				pair += 1;
			}
			else if (faces[c.getFace().getValue()] == 2)
			{
				set += 1;
			}
			else if (faces[c.getFace().getValue()]== 3)
			{
				quad += 1;
			}
		}
		
		if (pair == 1)
		{
			if (set == 1)
			{
				// full house
				//$this->odds[] = array('outs' => 3, 'desc' => "Full House to Quads", 'has' => 'Full House', 'goal' => 'Quads');
				Odds newodd=new Odds();
				newodd.setOuts(3);
				newodd.setDesc("Full House to Quads");
				newodd.setHas("Full House");
				newodd.setGoal("Quads");
				newodd.setHasHand(true);
				newodd.setHand(Rank.FullHouse);
				
				odds.add(newodd);
			}
			else if (quad == 1)
			{
				//$this->odds[] = array('outs' => 0, 'desc' => "Quads", 'has' => 'Quads', 'goal' => 'None');
				Odds newodd=new Odds();
				newodd.setOuts(0);
				newodd.setDesc("Quads");
				newodd.setHas("Quads");
				newodd.setGoal("None");
				newodd.setHasHand(true);
				newodd.setHand(Rank.FourOfAKind);
				
				odds.add(newodd);
			}
			else 
			{
				//$this->odds[] = array('outs' => 5, 'desc' => "One Pair to Two Pairs or Set", 'has' => 'One Pair', 'goal' => 'Two Pairs or Set');
				Odds newodd=new Odds();
				newodd.setOuts(5);
				newodd.setDesc("One Pair to Two Pairs or Set");
				newodd.setHas("One Pair");
				newodd.setGoal("Two Pairs or Set");
				newodd.setHasHand(true);
				newodd.setHand(Rank.OnePair);
				
				odds.add(newodd);
			}
		}
		else if (pair == 2)
		{
			//$this->odds[] = array('outs' => 4, 'desc' => "Two Pairs to Full House", 'has' => 'Two Pairs', 'goal' => 'Full House');
			Odds newodd=new Odds();
			newodd.setOuts(4);
			newodd.setDesc("Two Pairs to Full House");
			newodd.setHas("Two Pairs");
			newodd.setGoal("Full House");
			newodd.setHasHand(true);
			newodd.setHand(Rank.TwoPair);
			
			odds.add(newodd);
		}
		else if (set >= 1)
		{
			//$this->odds[] = array('outs' => 7, 'desc' => "Set to Full House or Quads", 'has' => 'Set', 'goal' => 'Full House or Quads');
			Odds newodd=new Odds();
			newodd.setOuts(7);
			newodd.setDesc("Set to Full House or Quads");
			newodd.setHas("Set");
			newodd.setGoal("Full House or Quads");
			newodd.setHasHand(true);
			newodd.setHand(Rank.ThreeOfAKind);
			
			odds.add(newodd);
		}
		else if (quad == 1)
		{
			//$this->odds[] = array('outs' => 0, 'desc' => "Quads", 'has' => 'Quads', 'goal' => 'None');
			Odds newodd=new Odds();
			newodd.setOuts(0);
			newodd.setDesc("Quads");
			newodd.setHas("Quads");
			newodd.setGoal("None");
			newodd.setHasHand(true);
			newodd.setHand(Rank.FourOfAKind);
			
			odds.add(newodd);
		}
		else if (pair == 0 && set == 0 && quad == 0) // no pair
		{
			// no pair to pair, one overcard to overpair, two overcards to overpair
			//print "<br> Checking overcard in check pairs";
			
			overCard = numOvercards();
			
			if (overCard == 2)
			{
				//$this->odds[] = array('outs' => 6, 'desc' => "Two Overcards to Over Pair", 'has' => 'Two Overcards', 'goal' => 'Over Pair');
				Odds newodd=new Odds();
				newodd.setOuts(6);
				newodd.setDesc("Two Overcards to Over Pair");
				newodd.setHas("Two Overcards");
				newodd.setGoal("Over Pair");
				newodd.setHasHand(false);
				newodd.setHand(Rank.None);
				
				odds.add(newodd);
			}
			else if (overCard == 1)
			{
				//$this->odds[] = array('outs' => 3, 'desc' => "One Overcard to Over Pair", 'has' => 'One Overcard', 'goal' => 'Over Pair');
				
				Odds newodd=new Odds();
				newodd.setOuts(3);
				newodd.setDesc("One Overcard to Over Pair");
				newodd.setHas("One Overcard");
				newodd.setGoal("Over Pair");
				newodd.setHasHand(false);
				newodd.setHand(Rank.None);
				
				odds.add(newodd);
				//$this->odds[] = array('outs' => 6, 'desc' => "No Pair to Pair", 'has' => 'None', 'goal' => 'Pair');
				newodd=new Odds();
				newodd.setOuts(6);
				newodd.setDesc("No Pair to Pair");
				newodd.setHas("None");
				newodd.setGoal("Pair");
				newodd.setHasHand(false);
				newodd.setHand(Rank.None);
				
				odds.add(newodd);
			}
			else
			{
				//$this->odds[] = array('outs' => 6, 'desc' => "No Pair to Pair", 'has' => 'None', 'goal' => 'Pair');
				Odds newodd=new Odds();
				newodd=new Odds();
				newodd.setOuts(6);
				newodd.setDesc("No Pair to Pair");
				newodd.setHas("None");
				newodd.setGoal("Pair");
				newodd.setHasHand(false);
				newodd.setHand(Rank.None);
				
				odds.add(newodd);
			}
		}
		
		//return straight;
	}
	
	public Double calcHandOdds(Integer outs, String round)
	{
		// turn to river
		Double turn =  ( 46 - Double.parseDouble(outs.toString()) ) /46;
		Double flop =  ( 47 - Double.parseDouble(outs.toString()) ) /47;
		//adding for rounds PreFlop and River.
		
		Double percent = 0.0;
		Double odds = 0.0;
		
		if (round.equals("FLOP"))
		{
			percent = (1 - flop * turn);
		}
		else if (round.equals("TURN"))
		{
			percent = (1 - turn);
		}
		if (percent != 0) 
		{ 
			odds = 1/percent - 1;
		}
		//return array('percent' => $percent, 'odds' => $odds);
		return odds;
	}
	
	int numOvercards()
	{
		int overcard = 0;
    	int maxCom = getMaxFaceValue(commCards);
			
    	//print "<br> max com card is ". $maxCom;
		if (maxCom != 1) // no point in checking overcard is comm card has an ace
		{
			for(Card hole : holeCards)
			{
				//$val = getNumeric(getFace($hole));
				int val= hole.getFace().getValue();
				
				if (val == 1 || val > maxCom)
				{
					overcard += 1;
				}
			}
		}
		return overcard;
	}
	
//	boolean getSuit($card)
//	{
//		boolean straight=false;
//		
//		return straight;	
//	}
	
//	boolean getFace($card)
//	{
//		boolean straight=false;
//		
//		return straight;
//	}
	
//	boolean getNumeric($face)
//	{
//		boolean straight=false;
//		
//		return straight;	
//	}
	int getMaxFaceValue(List<Card> cards)
	{
		int high = 2;
		for(Card card : cards)
		{
			int val = card.getFace().getValue();
			if (val == 1)
				return 1;
			
			if (val > high)
			{
				high = val;
			}
		}		
		//print $high;
		return high;
	}
	public ArrayList<String> botDecide(Integer callAmount, Round round, Integer potAmount, List<Card> hCards, List<Card> cCards, Integer myChips)
	{	
		//System.out.println("INSIDE BOT DECIDE");
		ArrayList<String> instruct= new ArrayList<String>();
		String action="";
		String amount="";
		
		this.holeCards = hCards;
		this.commCards = cCards;
		
		calcHandOuts(holeCards, commCards);		
		
		if(commCards.size()== 0)   
		{
			//System.out.println("Before Comm-Cards Size: "+commCards.size());
			// preflop Round
			
			int act= preFlopLogic(callAmount,myChips);
			//System.out.println("After Comm-Cards Size: "+commCards.size());
			if(act==0)
			{
				//Fold
				action="FOLD";
	    		amount="0";
			}
			else if(act==1)
			{
				//Call
				action="CALL";
				amount=""+callAmount;
			}
			else if(act==2)
			{
				//Raise
				action="RAISE";
				amount=""+(callAmount*2);
			}
			else
			{
				//All-in
				action="ALLIN";
				amount=""+myChips;
			}
			instruct.add(action);
	    	instruct.add(amount);
	    	//System.out.println("Instruct "+instruct);
			return instruct;
		}
		
		//calculate hand Odds and outs for FLOP and TURN round.
		
		Double outs=0.0;
		Rank currRank=Rank.None;
	    for(int i=0;i < odds.size();i++)
	    {
	    	outs=outs+odds.get(i).getOuts();
	    	if(currRank.compareTo(odds.get(i).getHand()) < 0 )
	    	{
	    		currRank= odds.get(i).getHand();
	    	}
	    	//System.out.println("Odds ="+odds.get(i).getHas());
	    	//System.out.println("Odds ="+odds.get(i).getGoal());
	    }
	    //System.out.println("oUts="+outs);
	    
	    
	    
	    if(commCards.size()==5)  
	    {
	    	// River Round
	    	int act=riverLogic(currRank, callAmount, myChips); 	 
	    	if(act==0)
			{
				//Fold
	    		action="FOLD";
	    		amount="0";
			}
			else if(act==1)
			{
				//Call
				action="CALL";
				amount=""+callAmount;
			}
			else if(act==2)
			{
				//Raise
				action="RAISE";
				amount=""+(callAmount*2);
			}
			else
			{
				//All in
				action="ALLIN";
				amount=""+myChips;
			}	    	
	    	
	    	instruct.add(action);
	    	instruct.add(amount);
	    	//System.out.println("Instruct "+instruct);
	    	return instruct;
	    }
	    
	    
	    //for FLOP , TURN rounds.
		Double potOdd=Double.parseDouble(potAmount.toString())/Double.parseDouble(callAmount.toString());
		Double handOdd=0.0;		
		
		switch(round)
		{
			case PREFLOP :
				System.out.println("PREFLOP");
			    handOdd = (50-outs)/outs;
				break;
			case FLOP:
				System.out.println("FLOP");
			    handOdd = (47-outs)/outs;
				break;
			case TURN:
				System.out.println("TURN");
			    handOdd = (46-outs)/outs;
				break;
			case RIVER:
				System.out.println("RIVER");
			    handOdd = (45-outs)/outs;
				break;
		    default:
				System.out.println("Invalid Round Name");
				break;	
		}
		
		//System.out.println("Outs="+outs+" potOdd"+potOdd+ "handOdd="+handOdd);
		double callRatio=0.0;
		if(handOdd <= potOdd)
		{
			//call or raise
			if((callAmount*handOdd*2) <= myChips)
		    {
			    //Raise
				action="RAISE";
				amount=""+callAmount * 2;
				//System.out.println("Round:"+round+" ,  RAISE. HAND-ODD="+handOdd+" POT-ODD="+potOdd); 
		    }
			else
			{
				//Call		
				action="CALL";
				amount=""+callAmount;
				//System.out.println("Round:"+round+" ,  CALL. HAND-ODD="+handOdd+" POT-ODD="+potOdd);
			}
		}
		else
		{
			//Fold
			action="FOLD";
			amount=""+0;
			//System.out.println("Round:"+round+" ,  FOLD. HAND-ODD="+handOdd+" POT-ODD="+potOdd);
		}
		
		instruct.add(action);
		instruct.add(amount);
		return instruct;
		
	}
	
	private int preFlopLogic(Integer callAmount, Integer myChips)
	{
		
//		if(odds.size()<=0)
//		{
//			System.out.println("bhak sala");
//		}
		//System.out.println("ODDS***************** "+odds.get(0).getHand());
//		if(1==1){
//			return 1;
//		}
		double percentage=0.0;
		//System.out.println("ODDS***************** ");
		if(odds.get(0).isHasHand())
		{
			//System.out.println("ONE PAIR.");
			percentage=100.0;
			//one pair.				
		}
		else
		{
			//System.out.println("NO ONE PAIR.");
			switch(odds.get(0).getHand())
			{
				case RoyalFlush:
					//System.out.println("RoyalFlush");
					percentage=50.0;
					break;
				
				case StraightFlush:
					//System.out.println("StraightFlush");
					percentage=20.0;
					break;
					
				case Flush:
					//System.out.println("Flush");
					percentage=15.0;
					break;
					
				case Straight:
					//System.out.println("Straight");
					percentage=10.0;
					break;
					
				case HighCard:
					//System.out.println("HighCard");
					percentage=5.0;
					break;
				case None:
					//System.out.println("None");
					percentage=0.0;
					break;
				default:
					//System.out.println("Wrong Rank");
					break;
			}		   
		}
	   
	   
	   double myPercentage = Double.parseDouble(callAmount.toString()) * 100 / Double.parseDouble(myChips.toString()) ;
	  // System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: myPercentage "+myPercentage);
	  // System.out.println("INSIDE CALC-HAND-OUTS  PREFLOP: myPercentage"+myPercentage+" percentage"+percentage);
	   if(myPercentage <= percentage)
	   {
		   if(percentage==100)
		   {
			  //System.out.println("Round: PREFLOP ,  RAISE"); 
			  return 2;//raise
		   }
		   //System.out.println("Round: PREFLOP ,  CALL"); 
		   return 1; // call		  
	   }
	   else
	   {
		  //System.out.println("Round: PREFLOP ,  FOLD"); 
		  return 0; //fold
	   }
	   
	}
	
	public int riverLogic(Rank currRank, int callAmount, int myChips)
	{
		 double percentage=0.0;
	   	 switch(currRank)
	   	 {
	    	 case RoyalFlush:
	    		 percentage=100.0;
	    		 break;
	    	 case StraightFlush:
	    		 percentage=100.0;
	    		 break;
	    	 case FourOfAKind:
	    		 percentage=60.0;
	    		 break;
	    	 case FullHouse:
	    		 percentage=50.0;
	    		 break;
	    	 case Flush:
	    		 percentage=40.0;
	    		 break;
	    	 case Straight:
	    		 percentage=40.0;
	    		 break;
	    	 case ThreeOfAKind:
	    		 percentage=40.0;
	    		 break;
	    	 case TwoPair:
	    		 percentage=30.0;
	    		 break;
	    	 case OnePair:
	    		 percentage=20.0;
	    		 break;
	    	 default:	
	    		 //System.out.println("Wrong Hand : riverLogic.");
	    		 break;	    	 
	   	 }
	   
	   	 
	   	 double myPercentage = 0.0;
	   	 
	   	 if(callAmount< myChips)
	   	 {
	   		  myPercentage=callAmount * 100 / myChips ;
	   		  if( myPercentage < percentage )
			  {
	   			  if( (callAmount*2)< myChips )
	   			  {
	   				  //System.out.println("Round: River , " + "Rank="+currRank+ " RAISE");
	   				  return 2;// Raise
	   			  }
	   			//System.out.println("Round: River , " + "Rank="+currRank+ " CALL");
				  return 1; // call				   
			  }
			  else
			  {
				  //System.out.println("Round: River , " + "Rank="+currRank+ " FOLD");
				  return 0; // fold
			  }
	   	 }
	   	 else
	   	 {
	   		 if( percentage==100 )
	   		 {
	   			//System.out.println("Round: River , " + "Rank="+currRank+ " ALLIN");
	   			 return 3; // all in 
	   		 }
	   		 else
	   		 {
	   			//System.out.println("Round: River , " + "Rank="+currRank+ " FOLD");
	   			 return 0; //fold
	   		 }
	   	 }
	}
	
	public static void main(String args[])
	{
		PokerBotAI ai=new PokerBotAI();
		List<Card> holecards=new ArrayList<Card>();
		List<Card> commcards=new ArrayList<Card>();
		ArrayList<String> result=new ArrayList<String>();
		for(int i=2;i<4;i++)
		{
			Face h1f= new Face();
			h1f.setText(""+i);
			h1f.setValue(i);
			Suit h1s=new Suit();
			h1s.setText("c");
			h1s.setValue(0);
			Card c=new Card();
			c.setFace(h1f);
			c.setSuit(h1s);
			holecards.add(c);			
		}
		for(int i=4;i<7;i++)
		{
			Face h1f= new Face();
			h1f.setText(""+i);
			h1f.setValue(i);
			Suit h1s=new Suit();
			h1s.setText("d");
			h1s.setValue(1);
			Card c=new Card();
			c.setFace(h1f);
			c.setSuit(h1s);
			commcards.add(c);			
		}
		
		result=ai.botDecide(5, Round.FLOP, 600, holecards, commcards,950);
		//System.out.println("Results "+result);
	}
}