package sfs2x.client.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotTaunts {
	private static List<String> winTaunts=new ArrayList<String>();
	private static List<String> loseTaunts=new ArrayList<String>();
	private static List<String> raiseTaunts=new ArrayList<String>();
	private static List<String> foldTaunts=new ArrayList<String>();
	private static List<String> leaveTaunts=new ArrayList<String>();
	private static List<String> otherWinsTaunts=new ArrayList<String>();
	private static List<String> otherRaisesTaunts=new ArrayList<String>();
	private Random rand=new Random();
	//uploading taunts in lists.
	{		
		//when i win
		winTaunts.add("ha ha ha ha ha ha ha ha uh ha ha ha!!");
		winTaunts.add("Ha-ha-ha-ha-ha-ha");
		winTaunts.add("How can you challenge a perfect, immortal machine? :P");
		winTaunts.add("You've lost your edge buddy!!");
		winTaunts.add("Woooo Woooo Woooo-oooooooo");
		winTaunts.add("Nana nana nana!");
		winTaunts.add("I really am the greatest ever to play this game, aren't I?");
		winTaunts.add("I've won?! Oh my gosh - I never win! You must suck!!.");
		winTaunts.add("So tell me What's it like to lose so badly?");
		winTaunts.add("Do me a favor. Wake me when you're done losing.");
		winTaunts.add("Muhahahahaha");
		winTaunts.add("You cannot stop me");
		winTaunts.add("Common sense is not so common!");
	 
		
		//when i call/raise big amout
		raiseTaunts.add("Catch me if you can!");
		raiseTaunts.add("Don't make me hurt you");
		raiseTaunts.add("I ain't scared of you!");
		raiseTaunts.add("Don't be shy !");
		raiseTaunts.add("Come baby Come !!");
		raiseTaunts.add("Show some guts baby");
		raiseTaunts.add("Ice until you feel Nice !");
		raiseTaunts.add("Hold still this is gonna HURT BAD");
		raiseTaunts.add("So you think your tough... but are you tough enough ?");
		raiseTaunts.add("Any last requests!?.");
		raiseTaunts.add("You should give up.");
		raiseTaunts.add("");
		
		
		//when other Raises/Allin		 
		otherRaisesTaunts.add("Not good !! Not good !!");
		otherRaisesTaunts.add("Why are you throwing your money baby!!");
		otherRaisesTaunts.add("I appreciate your dumbness.");
		otherRaisesTaunts.add("Do you even know how to play the game named POKER?");
		otherRaisesTaunts.add("That was such a foolish move!!");
		otherRaisesTaunts.add("Better quit the game , if you cant play it properly :D");
		
		
		//when other wins
		otherWinsTaunts.add("Well played old man!");
		otherWinsTaunts.add("It's not that I'm losing - it's that I'm losing to someone like you!.");
		otherWinsTaunts.add("No No No No NO!.");
		otherWinsTaunts.add("Well I was just trying to build your confidence...its working!! :D");
		otherWinsTaunts.add("huh...Whatever");
		otherWinsTaunts.add("Lucky you.");
		otherWinsTaunts.add("Sad cards.");
		otherWinsTaunts.add("My Luck sucks like you.");
		otherWinsTaunts.add("Not my day :(");
		otherWinsTaunts.add("sad sad sad!!");
	}
	
	
	public BotTaunts()
	{

	}
	public String getTaunt(int action, int amount, int botId, int userTurnId)
	{
		String taunt="";
		
		return taunt;		
	}
	public boolean shouldBotTaunt()
	{
		boolean taunt=false;
		int i=rand.nextInt(10);
		if(i==5 || i==7)
		{
			taunt=true;
		}
		return taunt;
	}
	public String tauntOnWinner()
	{
		String taunt="";
		
		return taunt;
	}
	public String tauntOnWinner(Boolean iWon)
	{
		if(iWon)
		{
			//if i won then send Winning taunts
			return getWinTaunts();
		}
		else
		{
			//if other palyer won then send Losing taunts
			return getOtherWinsTaunts();
		}
	}
	public String tauntOnUserAction(int action, int amount, int botId, int userTurnId)
	{
		String taunt="";
		boolean tauntNeeded=false;
		boolean itsMe=false;
		if(botId==userTurnId)
		{
			itsMe=true;
		}
		
		if(itsMe)
		{//if its the BOT action
			switch (action) 
			{	 
				case 2:
					//Raise
					if(amount > 500)
					{	
						tauntNeeded=true;
						taunt=getRaiseTaunts();
					}
					break;
				case 0:
					//call
					if(amount > 1000)
					{	
						tauntNeeded=true;
						taunt=getRaiseTaunts();
					}
					break;
				case 13:
					//All-in
					if(amount > 500)
					{
						tauntNeeded=true;
						taunt=getRaiseTaunts();
					}						
					break;					
				default:
					tauntNeeded=false;
					taunt="";
					break;
			}
		}
		else
		{//if other performs some action.
			switch (action) 
			{
				case 2:
					//Raise
					if(amount > 500)
					{
						tauntNeeded=true;
						taunt=getOtherRaisesTaunts();
					}
					break;
				case 13:
					//All-in
					if(amount > 1000)
					{
						tauntNeeded=true;
						taunt=getOtherRaisesTaunts();
					}
					break;					
				default:
					tauntNeeded=false;
					taunt="";
					break;
			}
		}
		
		if(tauntNeeded)
		{
			return taunt;
		}
		else			
		{
			return "";
		}
	}
	public String getWinTaunts()
	{
		int index=rand.nextInt(winTaunts.size());
		
		return winTaunts.get(index);
	}
	public String getLoseTaunts()
	{
		int index=rand.nextInt(loseTaunts.size());
		
		return loseTaunts.get(index);
	}
	public String getRaiseTaunts()
	{
		int index=rand.nextInt(raiseTaunts.size());
		
		return raiseTaunts.get(index);
	}
	public String getFoldTaunts()
	{
		int index=rand.nextInt(foldTaunts.size());
		
		return foldTaunts.get(index);
	}
	public String getLeaveTaunts()
	{
		int index=rand.nextInt(leaveTaunts.size());
		
		return leaveTaunts.get(index);		
	}
	public String getOtherWinsTaunts()
	{
		int index=rand.nextInt(otherWinsTaunts.size());
		
		return otherWinsTaunts.get(index);		
	}
	public String getOtherRaisesTaunts()
	{
		int index=rand.nextInt(otherRaisesTaunts.size());
		
		return otherRaisesTaunts.get(index);		
	}
	
}
