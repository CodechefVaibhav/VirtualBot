package sfs2x.client.example;

import java.awt.Point;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

import javax.swing.JTextArea;

import sfs2x.client.example.BotSpawner;

import NewClient.MyMainClient;
import NewClient.NewJInternalFrame;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.match.BoolMatch;
import sfs2x.client.entities.match.MatchExpression;
import sfs2x.client.entities.match.RoomProperties;
import sfs2x.client.entities.match.StringMatch;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LeaveRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.game.QuickGameJoinRequest;

import rummy.Seat;
import rummy.SeatInfo;
import common.Card;
import common.Converter;
import common.UserJoinedRoomsUtil;
import db.CpUser;
import db.dao.CpUserDAO;
import rummy.HandCards;
import rummy.Points;
import rummy.PointsInfo;
import rummy.Winner;
import rummy.WinnerInfo;
//Note : This class will be used to Test Random behaviour of Rummy BOTS . 
//This class is also used in BotSpawner Class for spawning BOTS on cardsplay.com server.
//To make this Class run for testing corner case scenarios of Rummy game, 
//set the value of test property to true in the file botProp.properties file in the config directory of Java Client
//this value should be set to False if the 
public class RummyBot  implements Runnable
{
	protected SmartFox sfs;
	private IEventListener evtListener;
	public  Thread runner;	
	protected int myPlayerId=-1;
	protected String userName= "";
	protected String password;
	
	protected static String LOBBY_ROOM_NAME = "Lobby";
	protected String roomToJoin = null;
	protected HandCards hcards = null;	
	
//	private Card pickedCard=null;
	protected int turnCount = 0;
	protected double dummyChips = 0;	
	protected Room gameRoom;
	protected SeatInfo seatinfo;
	protected Timer clientTimer; 
	protected ClientKeepAlive clientKeepAlive;
	protected Boolean gameStarted=false;
	protected Boolean gameEnded=false;
	protected boolean takenSeat=false;
	protected boolean roomJoined=false;
	protected boolean roomLeft=false;
	protected boolean requestedSeat=false;
	protected int requestedSeatId;
	protected int aliveCount=0;
	protected int entryFee=0;
	protected Boolean chipsCheck=false;
	public static boolean test=false;
	protected final int GAME_START_TIMER=15*1000;
	protected final int BOT_ACTION_DELAY=5*1000;
	protected final int TURN_TIME=60*1000;
	protected final int NETWORK_DELAY=3*1000;
	public final int KEEP_ALIVE_INTERVAL=5*1000;
	protected final static int INSTANTIATE_INTERVAL=5*1000;
	protected final int TIME_OUT_TIME= TURN_TIME - BOT_ACTION_DELAY-NETWORK_DELAY;
	public String BOT_RANK="mid";
	protected  int WAIT_FOR_GAMESTART = BotSpawner.THREAD_WAIT + GAME_START_TIMER + NETWORK_DELAY;
	protected int myChips=0;
	protected boolean isDestroyed=false;
	protected String chipType;
	protected double chips=0;
	protected boolean reloadChips=false;	
	protected int buyInLow=0;
	protected int buyInHigh=0;
	protected boolean isValueGame=false;
	boolean isCutJokerPickable=false;
	String gameType="";
	float pointMultiplier=-1;
	String pointsVariant="";
	int minPlayers=0;
	int maxPlayers=0;
	
	public MyMainClient mObj;
	
	
	protected RummyBotLogic rummyAI=new RummyBotLogic(this);
	
	static
	{			 	
		Properties p =new  Properties();
	    try 
	    {
	    	if(p.isEmpty())
	    		p.load(new FileInputStream("./config/botProp.properties"));
		}
	    catch (FileNotFoundException e)
	    {
			e.printStackTrace();
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
		}	   
	  
	    test=Boolean.parseBoolean(p.getProperty("BOT_TEST","false"));	       
	}
		
	
	class ClientKeepAlive extends TimerTask
	{
		//this timertask will be used to send keepAlives to server from this BOT.
		public void run()
		{
			if(!isDestroyed)
			{
				SFSObject data=new SFSObject();	
				System.out.println("KEEPALIVE SENDING******************PARENT");
				sendExtensionRequest("game.keepAlive",data,null);
				aliveCount++;
				//System.out.println("Sending keep Alive for Bot="+userName);
				if(!test) //these conditions need not to be checked, when running Bots for testing Corner cases.
				{				
					Room r=null;
					if(sfs!=null)
					{
						r=sfs.getRoomByName(roomToJoin);
					}
					//This room should not be searhed everytime.Must be done once-SITANSHU
					if(r== null)
					{
						//System.out.println("Returning from KeepAliveCheck");
						return;
					}
					
					if(isMidBOT())
					{						
						int nonBotUsers=BotSpawner.nonBotUsersCount(r);
						//System.out.println("NonBotUsers :"+nonBotUsers);
						//System.out.println("BOT="+userName+" takeSeat: "+takenSeat);
						
						if(!isNonTournamentGameType() &&  aliveCount==0)
						{
							WAIT_FOR_GAMESTART=(maxPlayers-1) * (BotSpawner.THREAD_WAIT ) + GAME_START_TIMER+  NETWORK_DELAY;
							//System.out.println("Max wait time of BOTS for Game start on Game table is "+WAIT_FOR_GAMESTART);
							//this calculation should not be done here , must be done once.-SITANSHU
						}
						if( aliveCount > ( WAIT_FOR_GAMESTART/ KEEP_ALIVE_INTERVAL ) && (gameStarted==false)  && takenSeat && ( r.getUserCount() < 2) ) // if a Bot didnt recieve game.started cmmnd in 90 secconds then he will do log out .
						{
							System.out.println("No Game Started recieved in last "+WAIT_FOR_GAMESTART+" seconds. So Bot "+userName+" is doing Logout.");
							botLogOut();							
						}
						if(nonBotUsers < 1) // if Bot taken seat and waiting for game start and non Bots users are less than 0.
						{
							System.out.println("******No  NON Bot User found in Room before Game Start. So Bot "+userName+" is doing Logout.");
							botLogOut();		
						}
					}
					if( gameStarted && !isNonTournamentGameType() && (r.getUserCount() < BotSpawner.roomBotDetails.get(r.getName()).size()) )
					{
						System.out.println("Room Usercount is less than BotCount, so doing Logout.");
						botLogOut();  // for removing inconsistency due to Late update of room variables.
					}
				}	
			}
										
		}
	}	
 	
	public RummyBot(String uName, String pwd,String roomname,String botRank,String chipType)
    {
		
		this.chipType=chipType;
		this.BOT_RANK=botRank;
		userName = uName;
		if(!test)
		{
			this.roomToJoin=roomname;
		}
		MessageDigest m;
		
		try 
		{
			m = MessageDigest.getInstance("SHA-256");
			byte[] data = pwd.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			password = String.format("%1$032X", i);
			password = password.toLowerCase();
			//System.out.println("\n SHA256 "+ password);
		} 
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Thread.sleep(500);
		runner = new Thread(this, uName); // (1) Create a new thread.
		//System.out.println(runner.getName());
		runner.start(); // (2) Start the thread.		
	}
	
	public void run()
	{
		//Display info about this particular thread
		//System.out.println(Thread.currentThread());
		init();
	}
	
	public void init()
	{
		// Instantiate SmartFox client 
		sfs = new SmartFox();
		
		// Add event listeners
		evtListener = new SFSEventHandler();
		sfs.addEventListener(SFSEvent.CONNECTION, evtListener);
		sfs.addEventListener(SFSEvent.CONNECTION_LOST, evtListener);
		sfs.addEventListener(SFSEvent.LOGIN, evtListener);
		sfs.addEventListener(SFSEvent.ROOM_JOIN, evtListener);
		sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, evtListener);	
		sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, evtListener);
		sfs.addEventListener(SFSEvent.LOGIN_ERROR, evtListener);
		sfs.addEventListener(SFSEvent.LOGOUT, evtListener);
		 
		
		// Load external configuration
		// The second parameter makes the client attempt the connection on configuration loaded
		sfs.loadConfig(getCfgPath(), true);	
		 System.out.println();
	}
		
	public String getCfgPath()
	{
		return "./config/sfs-config2.xml";
	}
	
	protected void finalize() throws Throwable {
//	    try {
//	        // close open files
//	    	fstream.close();	    	
//	    } finally {
//	        super.finalize();
//	    }
	}

	/**
	 * @param args
	 */
	


class SFSEventHandler implements IEventListener
{	
	@Override
	public void dispatch(BaseEvent event) throws SFSException
	{
	    if (event.getType().equals(SFSEvent.CONNECTION))
	    {	
	    	System.out.println("Bot="+userName+" connected successfully. Now doing Login.");
	    	// Login in current zone
	    	System.out.println("#####################Username "+userName);
			System.out.println("#####################Password "+password);
	    	sfs.send(new LoginRequest(userName, password));
	    }	    
	    else if (event.getType().equals(SFSEvent.CONNECTION_LOST))
	    {
	    	System.out.println("Bot="+userName + " Disconnected!");
	    	if(test)
	        {
	    		System.out.println("BOT="+userName+" disconnected, now connecting again.");
	    		sfs.connect();	    		
	        }
	    	else
	    	{
	    		clearAllMemory();    		 
	    	}	    	
	    }
	    
	    else if (event.getType().equals(SFSEvent.LOGIN))
	    {	
	    	System.out.println("Bot="+userName + " Logged in successfully.");
	    	//start keep alive timer
	    	clientTimer= new Timer();
	    	clientKeepAlive=new ClientKeepAlive();
		    clientTimer.scheduleAtFixedRate(clientKeepAlive,1,KEEP_ALIVE_INTERVAL);
		    
		    BotSpawner.botAccountMap.get(userName).setStatus(false);
			//realChips = sfso.getInt("realChips");
			if(test)
			{
				System.out.println("Bot="+userName+" Joining Game Lobby.");						 
				joinRoom(LOBBY_ROOM_NAME);
			}
			else
			{
				if(roomToJoin !=null && !roomToJoin.isEmpty())
			    	joinRoom(roomToJoin);						
			}
			     	 		
	    }
	    else if (event.getType().equals(SFSEvent.LOGIN_ERROR))
	    {	     
	    	System.out.println("Bot="+userName+" failed to do Login . Doing disconnection from SFS.");
	    	
	    	if(!test)
	    	{
	    		if(BotSpawner.botAccountMap.containsKey(userName))
	    		{		    			
	    			botLogOut();
	    		}
	    	}  			     
	    }
	    else if (event.getType().equals(SFSEvent.LOGOUT))
	    {
	    }	    
	    else if (event.getType().equals(SFSEvent.ROOM_JOIN))
	    {  	    	
	    	Room room = sfs.getRoomByName(roomToJoin);
	    	
	    	myPlayerId=sfs.getMySelf().getVariable("PlayerId").getIntValue(); 	 
	    	
			if(roomLeft)
			{
				System.out.println("BOT="+userName+" has joined GAME LOBBY.");
				//updateMsg(sfs.getMySelf().getName() + " joined room:" + room.getName());					
				if(test)
				{
					gameRoom = null;
					loadLobbyView();
				}
				else
				{
					//Bot has left the room , now coming to Lobby for logout.
					if(isConnected())sfs.disconnect();
					BotSpawner.botAccountMap.get(userName).setStatus(true); //this has to be checked , vdr Bot actually gets disconnected, other vise set this true on disconnection event.
					System.out.println("Bot :"+userName+" Logged out");
					clearAllMemory();
				}
			}
			else
			{
				
				System.out.println("BOT="+userName+" Joined Room="+room.getName());
				gameRoom = room;
				roomJoined=true; // to identify, BOT has joined the Room or not.
				loadGameView();
			}
		}	    
	    else if (event.getType().equals(SFSEvent.ROOM_JOIN_ERROR))
	    {
	    	System.out.println("Bot was Unable to Join the Room.Error="+event.getArguments().get("errorMessage"));
		 	if(test)
			{
				//still not joined in any room
				//join lobby
				joinRoom(LOBBY_ROOM_NAME);
				//sfs.disconnect();
			}
			else
			{
				botLogOut();
			}		
	    }
	    else if (event.getType().equals(SFSEvent.EXTENSION_RESPONSE))
	    {	
	    	//System.out.println("Received Response from server for BOT="+userName);
	    	try
	    	{
	    		onExtension((SFSEvent) event);
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    		//System.out.println("**********************Error");
	    		//e.printStackTrace();
	    	}
	    }	    
	    else if (event.getType().equals(SFSEvent.ROOM_VARIABLES_UPDATE))
	    {	
	    	//System.out.println("ROOM Variables Updated!!! for Bot="+userName);
	    }	  
	}	
		
} //event handler class ends
protected void leaveRoom()
{		
	if(!isDestroyed)
	{
		LeaveRoomRequest request = new LeaveRoomRequest();		
		sfs.send(request);	
		System.out.println(userName+" left the room");
		//if(isConnected())sfs.disconnect();						 
		 //this has to be checked , vdr Bot actually gets disconnected, other vise set this true on disconnection event.
		//System.out.println("Bot :"+userName+" Logged out");
		clearAllMemory();
	}
}
protected void joinRoom(String roomname)
{		
	Room room =sfs.getRoomByName(roomname);
	if(room == null)
	{
		System.out.println("Room="+roomname+" was not found , BOT="+userName+ " is doing LOGOUT");			 
		return;
	}
	System.out.println("Bot="+userName+" is joining Room "+roomname);
	
	JoinRoomRequest request = new JoinRoomRequest(roomname);		
	System.out.println("ROOMM JOIN SENT*****************************");
	sfs.send(request);	
	

}


protected void loadLobbyView()
{			
	System.out.println("Bot="+userName+"  is Joining a Game Room");
	quickGameJoin();		
}

protected void loadGameView()
{
	try 
	{
		Thread.sleep(1000);
	}
	catch (InterruptedException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if(!test)
	{
		BotSpawner.updateBotCount(roomToJoin,this);
	}
	//System.out.println("Bot="+userName+"  has Joined Game Room");
	ISFSObject sfso  = new SFSObject();	
	sfso.putBool("reCon", false);
	sfso.putBool("watch", false);
	sendExtensionRequest("game.clientready", sfso, gameRoom);		
}

private void quickGameJoin()
{		
	// Prepare a match expresison
	MatchExpression expr = new MatchExpression(	RoomProperties.IS_GAME, BoolMatch.EQUALS, true)
				.and (RoomProperties.HAS_FREE_PLAYER_SLOTS, BoolMatch.EQUALS, true)
				.and("status",StringMatch.EQUALS,"new")
				;
	
	// An array of Room Groups where we want the search to take place
	List<String> roomNames = new ArrayList<String>();
	roomNames.add("default");
	
	
	// Fire the request and jump into the game!
	QuickGameJoinRequest aReq = new QuickGameJoinRequest(expr, roomNames, sfs.getLastJoinedRoom());
	sfs.send(aReq);		
}

public void sendUserAction(int act, Card card)
{	
	if(!isDestroyed)
	{
		try 
		{
			Thread.currentThread().sleep(BOT_ACTION_DELAY);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ISFSObject sfso  = new SFSObject();	
		sfso.putInt("action",act);
		if(card != null)
			sfso.putUtfString("Card", Converter.CardToString(card));	
		sendExtensionRequest("game.rummyuserturn",sfso,gameRoom);	
	}
		
}

public void sendUserChat(String msg,Room roomObj)
{
	ISFSObject sfso = new SFSObject();
	sfso.putUtfString("msg", msg);
	sendExtensionRequest("game.chat", sfso, roomObj);
}

public void meldCards(List<Card> handCardsList)
{
	//get hand cards from rummyAi. and send them after some delay
	try 
	{
		Thread.sleep(BOT_ACTION_DELAY);
	} 
	catch (InterruptedException e) 
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//System.out.println("Melding cards");
	//System.out.println("Hand Cards Size: "+handCardsList.size());
	//System.out.println("Hand Cards melded: "+handCardsList);
	HandCards handCards= new HandCards();
	handCards.setHandCards(handCardsList);
	
	ISFSObject sfso=new SFSObject();	
	sfso.putUtfString("HandCards",Converter.HandCardsToString(handCards));	
	
	sendExtensionRequest("game.meld",sfso,gameRoom);
}
public void sendTakeSeat(int id,int myChips)
{	
	if(!isDestroyed)
	{
		ISFSObject sfso  = new SFSObject();
		sfso.putInt("seatid",id);		
		int amtNeededHigh = buyInHigh - myChips;
		if (amtNeededHigh < 0) amtNeededHigh = 0;
		
		int amtNeededLow = buyInLow - myChips;
		if (amtNeededLow < 0) amtNeededLow = 0;
		
		int amt = -1;

		if (chips < amtNeededLow) amt = -1;
		else if (chips >= amtNeededLow && chips <= amtNeededHigh) amt = (int)chips;
		else if (chips > amtNeededHigh) amt = amtNeededHigh;

		if (amt != -1) {
			sfso.putInt("amt",amt);			
			sfso.putInt("playerid",myPlayerId);
			sendExtensionRequest("game.takeseat",sfso,gameRoom);
		}
		else
		{
			System.out.println("Not enough funds to request for take seat");
			botLogOut();
		}
	}
	
		 
}



public void leaveSeat()
{
	//SFSObject sfsob=new SFSObject();
	joinRoom(LOBBY_ROOM_NAME);
	System.out.println("BOT "+userName+" has left Seat and Joined Game Lobby.");
}
public void sendLeaveSeat()
{
	ISFSObject sfso  = new SFSObject();	
	sendExtensionRequest("game.leaveseat",sfso,gameRoom);	
	System.out.println("Bot="+userName+" Leaving seat.*****");
	//showTakeSeat();
}

protected void sendLeaveRoom()
{
	//ISFSObject sfso  = new SFSObject();	
	leaveRoom();
	System.out.println("Bot="+userName+" Leaving Room and Joining Lobby. *****");
}
public Boolean isSeatGranted()
{
	boolean grant=false;
	for(int i=0 ; i< seatinfo.getSeats().size() ; i++ )
	{
		Seat seat=seatinfo.getSeats().get(i);
		if( seat.seatId == requestedSeatId )
		{
			if( seat.isOccupied() && (seat.getPlayerId() == myPlayerId))
			{
				//chipsLeft=seat.getChipsLeft();// update BOT chips left on table.Commented as chipsleft is not available in Rummy Seat class.--Sitanshu 
				grant=true;
				break;
			}			
		}
	}
	return grant;	 
}
public boolean isConnected()
{
	boolean flag=false;
	if(sfs!=null)
	{
		flag=sfs.isConnected();
	}
	return flag;
}
public boolean hasJoinedRoom()
{
	return roomJoined;
}
public boolean hasGameStarted()
{
	return gameStarted;
}
public boolean hasGameEnded()
{
	return gameEnded;
}
public int getPlayerId()
{
	return myPlayerId;
}
public boolean hasRequestedSeat()
{
	return requestedSeat;
}
public String getRank()
{
	return BOT_RANK;
}
protected void onExtension(SFSEvent evt)
{
	Random rand= new  Random();
	Map params = evt.getArguments();
	ISFSObject data = (ISFSObject)params.get("params");	
	
	if (params.get("cmd").equals("game.seatinfo"))
	{
		if(data==null)
		{
			System.out.println("SFSObject was found Null on SEATINFO response. Bot="+userName+" doing LOGOUT.");
			botLogOut();
			return;
		} 
		try
		{
			seatinfo = Converter.StringToSeatInfo(data.getUtfString(("seatinfo")));
		}
		catch (Exception e) {
			System.out.println("************Exception in seatinfo");
		}
		
		//System.out.println("SeatInfo recieved");
		if(requestedSeat)
		{ 
			boolean grant=false;
			for(int i=0 ; i< seatinfo.getSeats().size() ; i++ )
			{					
				if( seatinfo.getSeats().get(i).seatId == requestedSeatId )
				{							
					if( ( seatinfo.getSeats().get(i).isOccupied()) && ( seatinfo.getSeats().get(i).getPlayerId() ==myPlayerId) )
					{
						grant=true;
						
						break;
					}				 
				}
			}
			if(grant)
			{
				takenSeat=true;
				requestedSeat=false;				
			}
			else
			{
				System.out.println("Seat was not granted to BOT="+userName+" . so Bot doing LOgout.");
				botLogOut();
			}
		}
	}
	else if (params.get("cmd").equals("game.roomdata"))
	{
		//System.out.println("Roomdata recieved");
		buyInLow=data.getInt("buyInLow");
		buyInHigh=data.getInt("buyInHigh");
		isValueGame=data.getBool("valueGame");
		gameType=data.getUtfString("gameType");
		pointsVariant=data.getUtfString("pointsVariant");
		pointMultiplier=data.getFloat("multiplier");
		maxPlayers=data.getInt("players");
		minPlayers=data.getInt("minPlayers");
		//resp.data.amount=sfso.getUtfString("amount");
		//chipType =sfso.getUtfString("chipType");
		if(isValueGame)
		{
			rummyAI.isValueGame();
		}
	}
	else if (params.get("cmd").equals("game.takeseat"))
	{
		if(data==null)
		{
			System.out.println("SFSObject was found Null on TAKESEAT response. Bot="+userName+" doing LOGOUT.");
			//botLogOut();
			return;
		}
		//System.out.println("BOT="+userName+" received TAKE-SEAT.");
		if(! test)
		{
			
			try 
			{
				Thread.sleep(BOT_ACTION_DELAY);
			} 
			catch (InterruptedException e)
			{			
				e.printStackTrace();
			}
		}					 
			
		initiateTakeSeat();		  
	}
	
	else if (params.get("cmd").equals("game.started")) // not needed really
	{	
		turnCount = 0;
		if(takenSeat)
		{
			gameStarted=true;
		}
		rummyAI.gameStarted();
	}
	else if (params.get("cmd").equals("game.ended")) // not needed really
	{	
		if(test)
		{
			joinRoom(LOBBY_ROOM_NAME);	
		}
		else
		{
			gameEnded=true;
			botLogOut();  // bot should do Logout on game end.
		}		
	}
	else if (params.get("cmd").equals("game.handcard")) // not needed really
	{	
		try
		{
			hcards =  Converter.StringToHandCards(data.getUtfString("HandCards"));
		}
		catch (Exception e) {
			System.out.println("************Exception in handcards");
		}
		
		if(hcards==null)
		{
			System.out.println("BOT="+userName+" didnt receive HandCards, ISSUE!!!!!!!!!!!!");
			botLogOut();
		}
		rummyAI.onHandCards(hcards.handCards);
		//updateMsg("Received HandCards" + hcards.toString());
	}		
	else if (params.get("cmd").equals("game.cutjoker")) // not needed really
	{	
		Card cutJoker=null;
		try
		{
			cutJoker = Converter.StringToCard(data.getUtfString("Card"));
		}
		catch (Exception e) {
			System.out.println("************Exception in cutjoker");
		}
		
		if(cutJoker==null)
		{
			System.out.println("BOT="+userName+" didnt receive CutJoker, ISSUE!!!!!!!!!!!!");
			botLogOut();
		}
		rummyAI.setJoker(cutJoker);
	}
	else if (params.get("cmd").equals("game.discard")) // not needed really			
	{			
		//initial discarded card
		Card discardCard=null;
		try
		{
			discardCard =Converter.StringToCard(data.getUtfString("Card"));
		}
		catch (Exception e) {
			System.out.println("************Exception in discard");
		}		
		if(discardCard==null)
		{
			System.out.println("BOT="+userName+" didnt receive Discarded Card, ISSUE!!!!!!!!!!!!");
			botLogOut();
		}
		rummyAI.setDiscardedCard(discardCard);
	}
	
	else if (params.get("cmd").equals("game.rummyuserturn")) 
	{
		int playerid = data.getInt("playerId");	
		int timer  =  data.getInt("turntime");
		
		//Noow i  wil pick a card
		if(playerid ==myPlayerId)
		{	
			
			try
			{
				isCutJokerPickable  = data.getBool("isCutJokerPickable");
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			
			turnCount++;
			// automatically do user turn
			rummyAI.onUserTurn(isCutJokerPickable);
		}
	}
	else if (params.get("cmd").equals("game.pickedcard")) // not needed really
	{	
		//picked card from either from deck or discarded card
		Card card = null;
		try
		{
			card = Converter.StringToCard(data.getUtfString("Card"));
		}
		catch (Exception e) {
			System.out.println("************Exception in pickedcard");
		}		
		
		rummyAI.usePickedCardAndDiscardCard(card);
	}
	else if (params.get("cmd").equals("game.useraction"))  // not needed, just logging
	{
		int playerid = data.getInt("playerId");
		int action = data.getInt("action");
		 
		Card card =null;
		try
		{
			card =Converter.StringToCard(data.getUtfString("Card"));
		}
		catch (Exception e) {
			System.out.println("************Exception in useraction");
		}		
		if(action == 22) //user dropped a card
		{
			//save this card as discared card
			rummyAI.setDiscardedCard(card);
			String msg = "Player Id " + playerid + " did " + action  + " card " + card;
		}
	}	
	else if (params.get("cmd").equals("game.points")) // not needed really
	{	
		
		PointsInfo pointInfo=null;
		try
		{
			//pointInfo= Converter.StringToPointsInfo(data.getUtfString("PointsInfo"));
		}
		catch (Exception e) {
			System.out.println("************Exception in pointinfo");
		}		
		
		if(pointInfo==null)
		{
			System.out.println("BOT="+userName+" didnt receive PointsInfo, ISSUE!!!!!!!!!!!!");		
			return;
		}
		int myPoints=0;
		for(int i=0;i<pointInfo.pointsList.size();i++)
		{
			Points pointDetail=pointInfo.pointsList.get(i);
			if(myPlayerId==pointDetail.playerId)
			{
				myPoints=pointDetail.getPoints();
				break;
			}			
		}
		//update my points
		rummyAI.updatePoints(myPoints);
	}
	else if (params.get("cmd").equals("game.meld")) // not needed really
	{	
		int timer  =  data.getInt("turntime");
		rummyAI.onMeld();
		//updateMsg("Received meld");		
	}	
	else if (params.get("cmd").equals("game.PlayerAway")) // not needed really
	{	
		//updateMsg("Received PlayerAway");
	}	
	else if (params.get("cmd").equals("game.winner")) 
	{
		WinnerInfo winnerInfo=null;
		try
		{
			winnerInfo = Converter.StringToWinnerInfo(data.getUtfString("WinnerInfo"));
		}
		catch (Exception e) {
			System.out.println("************Exception in winnerInfo");
		}		
		
		if(winnerInfo==null)
		{
			System.out.println("BOT="+userName+" didnt receive winnerInfo, ISSUE!!!!!!!!!!!!");	
			return;
		}
	}	
	else if (params.get("cmd").equals("game.keepAlive")) 
	{

	}
	else if(params.get("cmd").equals("game.account"))
	{
		//System.out.println("Acount response recieved for BOT="+userName);
		if(data==null)
		{
			System.out.println("SFSObject was found Null on ACCOUNT response. Bot="+userName+" doing LOGOUT.");
			//botLogOut();
			return;
		}
	
		if(test)
		{
			System.out.println("Bot="+userName+" Joining Game Lobby");
			//quickGameJoin();
			joinRoom(LOBBY_ROOM_NAME);
		}
		else
		{
			if(chipType.equals("real"))
			{
				//System.out.println("game.dummyChips="+dummyChips);
				chips=data.getDouble("realChips");
				dummyChips=chips;
				//realChips=data.getInt("realChips");
			}
			if(chipType.equals("Freeroll"))
			{//free roll chips
				//System.out.println("chipType="+chipType);
				chips=data.getDouble("Freeroll");
			}		
			if(chipType.equals("VIP"))
			{//free roll chips
				//System.out.println("chipType="+chipType);
				chips=data.getDouble("vipChips");
			}	
			if(chipType.equals("dummy"))
			{//free roll chips
				//System.out.println("chipType="+chipType);
				chips=data.getDouble("dummyChips");
			}	
			if(chipsCheck)
			{
				if(chips < 1000)
				{
					reloadChips=true;
					SFSObject sfsob = new SFSObject();			
					System.out.println("Account is insufficient so sending Chip reload request. BOT="+userName);
					sendExtensionRequest("game.reloadchips",sfsob,null); 
				}
				else
				{
					reloadChips=false;
					//chipsCheck=false;
					requestedSeatId= takeRandomSeat();
					if(requestedSeatId == -1)
					{
						System.out.println("BOT="+userName+" is doing Logout as no more Seats are vacant in the Room="+roomToJoin);
						botLogOut();
					}
					else
					{
						requestSeat();
					}						
				}					
			}
		}
		
	}
	else if(params.get("cmd").equals("game.rowclickdata"))
	{
		
	}
	else if(params.get("cmd").equals("game.chat"))
	{
		
	}
	else if(params.get("cmd").equals("game.Message"))
	{
		
	}
 
}
	public void requestSeat()
	{
		System.out.println("BOT="+userName+" taking Seat no=" + requestedSeatId);
		chipsCheck=false;
		sendTakeSeat(requestedSeatId, myChips);		 
		requestedSeat=true;		
	}
		
	private int usersOnTableInRoom()
	{
		int users=0;
		for(int i=0;i< seatinfo.getSeats().size();i++)
		{
			if(seatinfo.getSeats().get(i).isOccupied())
			{
				users++;
			}
		}
		return users;		
	}
	public void botLogOut()
	{
		if(!isDestroyed)
		{
			gameStarted=false;
			takenSeat=false;
			roomJoined=false;
			roomLeft=true;
			if(clientTimer!=null)
			{
				clientTimer.cancel();				
			}
			if(clientKeepAlive!=null)
			{
				clientKeepAlive.cancel();
			}
			aliveCount=0;
			//sendLeaveSeat();
			sendLeaveRoom();
			
			//clearAllMemory();
		}
	}
	public void clearAllMemory()
	{
		if(!isDestroyed)
		{
//			BotSpawner.botAccountMap.get(userName).setStatus(true);
//			BotSpawner.botCountOnLogout(roomToJoin,this);
			//terminate the Thread.
			if(runner!=null)
			{
				runner.interrupt();
				runner=null;
			}	
			//Stop all timers and tasks
			if(clientTimer!=null)
			{
				clientTimer.cancel();
				clientTimer=null;
			}
			if(clientKeepAlive!=null)
			{
			   clientKeepAlive.cancel();
			   clientKeepAlive=null;
			}
			
			if(sfs!=null)
			{
				sfs.removeAllEventListeners();
				if(isConnected())sfs.disconnect();//check if needs to be disconnected if already done before
			}
			
			sfs=null;
			evtListener=null;	
			gameRoom=null;
			seatinfo=null;
			
			if(hcards!=null)
			{
				hcards.getHandCards().clear();
				hcards = null;
			}		 
			
			isDestroyed=true;
		}	
	}
	protected int takeRandomSeat()
	{
		/*
		 * @author vaibhav
		 * @param concurrentSeatLeftObj
		 * Due to ConcurrentThreadModification Exception normal ArrayList
		 * is replaced with CopyOnWriteArrayList which actually copies
		 * data into it before performing opreations over data.
		 * */
		CopyOnWriteArrayList<Seat> concurrentSeatLeftObj = new CopyOnWriteArrayList<Seat>();
		System.out.println("Bot="+userName+" taking Random Seat.");
		List<Seat> seatLeft=new ArrayList<Seat>();
		if(seatinfo!=null)seatLeft = seatinfo.getSeats();
		if(seatLeft!=null && !seatLeft.isEmpty())
		{
			Iterator<Seat> seatLeftObj = seatLeft.iterator();
			while(seatLeftObj.hasNext())
			{
				Seat seatObjectTeamp = (Seat)seatLeftObj.next();
				concurrentSeatLeftObj.add(seatObjectTeamp);
			}
		}
		else
		{
			System.out.println("$$$$$$$$$$$$$$$$$$$$ Something Wrong With Seat Info $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		}
		if(seatLeft!=null && !seatLeft.isEmpty())seatLeft.clear();
		if(!concurrentSeatLeftObj.isEmpty())
        {	
			Iterator setLeftIterator = concurrentSeatLeftObj.iterator();
			while(setLeftIterator.hasNext())
			{
				Seat seatObj = (Seat)setLeftIterator.next();
				if(seatObj.isOccupied() == false)
				{
					seatLeft.add(seatObj);
				}
			}
	    }
//		for (Seat aSeat: seatinfo.getSeats())
//		{
//			if(aSeat.isOccupied() == false)
//			{					
//				seatLeft.add(aSeat);				
//			}
//		}
		Random randSeat=new Random();
		if(seatLeft.size()>0)
		{
			int seatIndex=randSeat.nextInt(seatLeft.size()); // return some random Seat index from the list of vacant Seats.
			return seatLeft.get(seatIndex).getSeatId();	
		}
		else
		{
			System.out.println("No more vacant Seats Left !!!!");
			return  -1;
		}		 
	}
	
	public void removeSfsEventListener()
	{
		if(sfs!=null)
		{
			sfs.removeAllEventListeners();
		}		
	}
	public void sendExtensionRequest(String command, ISFSObject data, Room room)
	{
		if(!isDestroyed)
		{
			if(sfs != null)
			{
				if(command != null && data != null)
				{
					if((sfs.getMySelf()!=null) &&  sfs.isConnected())
					{
						if(room!=null)
						{
							sfs.send(new ExtensionRequest(command,data,room)); 
						}
						else
						{
							sfs.send(new ExtensionRequest(command,data,null)); 
						}
					}	
					else
					{
						//bot is not connected so log him out.
						botLogOut();
						clearAllMemory();
					}
				}
			}
		}
	}
	protected void initiateTakeSeat()
	{
		chipsCheck=true;
		takenSeat=false;
		SFSObject sfsob = new SFSObject();
		//System.out.println("initiateTakeSeat");
		sendExtensionRequest("game.account",sfsob,null);
	}
	private boolean isNonTournamentGameType()
	{
		boolean flag=false;
		if(gameType.equalsIgnoreCase("RING") || gameType.equalsIgnoreCase("POINTS"))
		{
			flag=true;
		}
		return flag;
	}
	private boolean isMidBOT()
	{
		boolean flag=false;
		if(BOT_RANK.equalsIgnoreCase("mid"))
		{
			flag=true;
		}
		return flag;
	}
	
	public static void main(String[] args)
	{	
//		List<Integer> list=new ArrayList<Integer>();
//		list.add(3);
//		list.add(4);
//		list.add(2);
//		list.add(3);
//		 
//		
//		List<Integer> club=new ArrayList<Integer>();
//		club.add(0);
//		club.add(2);
//		club.add(3);
//		club.add(7);
//		club.add(8);
//		club.add(11);
//		club.add(12);
//		club.add(12);
//		club.add(16);
//		club.add(17);
//		club.add(18);
//		club.add(21);
//		club.add(22);
//		
////		list.remove(3);
////		System.out.println("Sorted "+list);
//		for(int i=0 ; i < club.size() && club.size()>0; i++)
//		{	
//			i=0;
//			int c=club.get(i);
//			int j=i+1;
//			List<Integer> temp=new ArrayList<Integer>();
//			temp.add(c);
//			while( j < club.size())
//			{
//				if(c + temp.size() ==club.get(j))
//				{
//					temp.add(club.get(j));
//				}
//				j++;
//			}
//			club=removeCommonValues(club,temp);
//			System.out.println("List="+club);
//			System.out.println("Temp List="+temp);
//		}
		for1: for(int i=0;i<5;i++)
		{
			for2:for(int j=0;j<5;j++)
			{
				System.out.println("j="+j);
				if(j==2)
				{
					break for1;
				}
			}
			System.out.println("i="+i);
		}
	}
 	
}
