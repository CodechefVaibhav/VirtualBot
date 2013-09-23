package sfs2x.client.example;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;

import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.sun.org.apache.bcel.internal.generic.FDIV;

import common.Card;

import poker.CommunityCard;
import poker.GameInfo;
import poker.HoleCard;
import poker.PlayerActionChoices;
import poker.PlayerRanking;
import poker.PlayersInfo;
import poker.Pot;
import poker.PotInfo;
import poker.Seat;
import poker.SeatInfo;
import poker.Winner;
import poker.WinnerInfo;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.entities.match.BoolMatch;
import sfs2x.client.entities.match.MatchExpression;
import sfs2x.client.entities.match.RoomProperties;
import sfs2x.client.example.PokerBotAI.Round;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LeaveRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.LogoutRequest;
import sfs2x.client.requests.game.QuickGameJoinRequest;
import sun.security.provider.PolicyParser.GrantEntry;

public class BasicClient implements Runnable
{
	private SmartFox sfs;
	private IEventListener evtListener;	
	private Thread runner;	
	private String userName = "";
	private String password;
	private String roomToJoin;
	private String BOT_RANK="mid";
	private String chipType;
	private int myPlayerId=-1;
	private int realChips = 0;
	private double chips=0;
	private double dummyChips = 0;	
	private int aliveCount;
	private int chipsLeft=0;
	private Room gameRoom;
	private SeatInfo seatinfo;
	private int roundId=0;	
	private Timer clientTimer;
	private ClientKeepAlive clientKeepAlive;	
	private List<Card> holeCards=new ArrayList<Card>();
	private List<Card> commCards=new ArrayList<Card>();	
	private static boolean test=false;
	private boolean gameStarted=false;
	private boolean gameEnded=false;
	private boolean takenSeat=false;
	private boolean roomJoined=false;
	private boolean roomLeft=false;
	private boolean requestedSeat=false;	
	private boolean chipsCheck=false;
	private boolean reloadChips=false;
	private boolean isRoomDataRecieved = false;
	private boolean granted = false;
	private int potAmount=0;
	private int requestedSeatId;
	private int turnCount;
	private int myChips=0;	
	private int buyInLow=0;	
	private int buyInHigh=0;	
	private final String LOBBY_ROOM_NAME = "Lobby";	
	private final int TURN_TIME=30*1000;	 
	private final int NETWORK_DELAY=3*1000;
	private final int KEEP_ALIVE_INTERVAL=5*1000;
	private final int BOT_ACTION_DELAY=3*1000;
	private final int GAME_START_TIMER=7*1000;
	private final static int INSTANTIATE_DELAY=5*1000;
	private final int TIME_OUT_TIME= TURN_TIME - BOT_ACTION_DELAY-NETWORK_DELAY;
	private  int WAIT_FOR_GAMESTART = BotSpawner.THREAD_WAIT + GAME_START_TIMER+NETWORK_DELAY;
	private boolean isDestroyed=false;
	private String gameType="";
	private int maxPlayers=0;
	private int minPlayers=0;
	
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
			//e.printStackTrace();
		} 
	    catch (IOException e) 
	    {
			//e.printStackTrace();
		}	  
	    
	    test=Boolean.parseBoolean(p.getProperty("BOT_TEST","false"));
	    	    
	}	
	
	public BasicClient(String uName, String pwd,String roomName,String botRank,String chipType)
    {
		this.roomToJoin=roomName;
		this.BOT_RANK=botRank;
		userName = uName;
		this.chipType=chipType;
		MessageDigest m;
		try 
		{
			m = MessageDigest.getInstance("SHA-256");
			byte[] data = pwd.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			password = String.format("%1$032X", i);
			password = password.toLowerCase();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			//e.printStackTrace();
		}
		//start the Bot thread.
		runner = new Thread(this, uName);
		
		runner.start();
	}
	
	private class ClientKeepAlive extends TimerTask
	{
		public void run()
		{
			if(!isDestroyed)
			{
				SFSObject data=new SFSObject();	 
				sendExtensionRequest("game.keepAlive", data, null);			 
				aliveCount++;
				if(!test) //these conditions need not to be checked, when running Bots for testing Corner cases.
				{
					if(isRoomDataRecieved)
					{
						if(!isNonTournamentGameType())
						{					
							if(isMidBOT() && aliveCount==0)
							{
								int maxUsers=sfs.getRoomByName(roomToJoin).getMaxUsers();
								WAIT_FOR_GAMESTART=(maxUsers-1) * (BotSpawner.THREAD_WAIT ) + GAME_START_TIMER + NETWORK_DELAY;
								//System.out.println("Max wait time of BOTS for Game start on Game table is "+WAIT_FOR_GAMESTART);
							}	
							//this check has been handeled in BotSpawner class.					
						}
						Room r=null;
						if(sfs!=null)
						  r= sfs.getRoomByName(roomToJoin);
						if(r== null)
						{
							//System.out.println("Returning from KeepAliveCheck");
							botLogOut();
							return;
						}
						if(roomJoined && !takenSeat && aliveCount==15)
						{
							// checking if Bot received Seat after joining game room, in 10 keep alives then do Logout. both in case of SNG and RING.
							//System.out.println("*****Seat not granted after joining room, so removing BOT ="+userName+" from Game table.");
							botLogOut();						
						}
						if(!isMidBOT() && BotSpawner.nonBotUsersCount(r) ==0 && roundId>5 && isNonTournamentGameType())	
						{//remove Front BOTS from RING table if Ring game has exceeded 50 games and No Nonbot users are present on the table.
							BotSpawner.frontBotsRoom.remove(roomToJoin);
							botLogOut();					
						}
						if(isMidBOT())
						{
							int nonBotUsers=BotSpawner.nonBotUsersCount(r);
							 
							if( nonBotUsers < 1) 
							{ // if during running game all Non bot users leave then Bot should do log out.
								botLogOut();						 
							}					
						}
						if( gameStarted && !isNonTournamentGameType() && BotSpawner.roomBotDetails.containsKey(r.getName()) && (r.getUserCount() <= BotSpawner.roomBotDetails.get(r.getName()).size()) )
						{
							//System.out.println("******Room Usercount is less than BotCount, so doing Logout.");
							botLogOut();  // for removing inconsistency due to Late update of room variables.
						}
				    }
				}			
			}
									
		}		
	}	
	private class SFSEventHandler implements IEventListener
	{	
		@Override
		public void dispatch(BaseEvent event) throws SFSException
		{			
		    if (event.getType().equals(SFSEvent.CONNECTION))
		    {		    	    	
		    	// Login in current zone

		    	sfs.send(new LoginRequest(userName, password, sfs.getCurrentZone()));
		    }		    
		    else if (event.getType().equals(SFSEvent.CONNECTION_LOST))
		    {
		    	if(test)
		    	{
		    		//System.out.println("BOT="+userName+" disconnected, now connecting again.");
		    		sfs.connect();
		    	}
		    	else
		    	{
		    		clearAllMemory();
		    		//System.out.println("BOT="+userName+" Logged Out.");
		    	}	    	
		    }		    
		    else if (event.getType().equals(SFSEvent.LOGIN))
		    {
		    	//start sending keep alive
		    	clientTimer=new Timer(userName);
		    	clientKeepAlive=new ClientKeepAlive();
				clientTimer.schedule(clientKeepAlive, 1, KEEP_ALIVE_INTERVAL); // Bot will start sending KeepAlive to server
				//get a Bot account detail
				String roomName = LOBBY_ROOM_NAME;		    	
		    	Map params = event.getArguments();
				BotSpawner.botAccountMap.get(userName).setStatus(false);
				// send room join request.
				if(test)
				{
					//System.out.println("Bot="+userName+" Joining Game Lobby.");						 
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
		    	//System.out.println("Bot="+userName+" failed to do Login . Doing disconnection from SFS.");
		    	
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
		    else if (event.getType().equals(SFSEvent.USER_EXIT_ROOM))
		    {
		    	User user = (User) event.getArguments().get("user");
		    	if(myPlayerId==user.getPlayerId())
		    	{
		    		System.out.println(userName+" left the room");
		    		if(isConnected())sfs.disconnect();						 
		    		BotSpawner.botAccountMap.get(userName).setStatus(true); //this has to be checked , vdr Bot actually gets disconnected, other vise set this true on disconnection event.
		    		//System.out.println("Bot :"+userName+" Logged out");
		    		clearAllMemory();
		    	}
		    }
		    else if (event.getType().equals(SFSEvent.ROOM_JOIN))
		    {	    	  
		    	Room room = sfs.getRoomByName(roomToJoin);
		    	myPlayerId=sfs.getMySelf().getVariable("PlayerId").getIntValue();
		    	
		    	//System.out.println("Bot has joined room="+room.getName());
				if(roomLeft)
				{
					//System.out.println("BOT="+userName+" has joined GAME LOBBY.");								
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
						//System.out.println("Bot :"+userName+" Logged out");
						clearAllMemory();
					}
				}
				else
				{				 
					//System.out.println("BOT="+userName+" Joined Room="+room.getName());
					gameRoom = room;
					roomJoined=true; // to identify, BOT has joined the Room or not.
					//getRoomChipDetails(gameRoom);//requesting server for game chip type.
					loadGameView();
				}
				//System.out.println("BOT-ROOM Details****"+ BotSpawner.botCount);
		    }
		    
		    else if (event.getType().equals(SFSEvent.ROOM_JOIN_ERROR))
		    {
		    	//System.out.println("Bot was Unable to Join the Room.Error="+event.getArguments().get("errorMessage"));
			 	if(test)
				{
					//still not joined in any room
					//join lobby
					joinRoom(LOBBY_ROOM_NAME);
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
		    		System.out.println(e);
		    	}
		    }
		    else if (event.getType().equals(SFSEvent.ROOM_VARIABLES_UPDATE))
		    {	
		    	//System.out.println("ROOM Variables Updated!!!");
		    }	  
		}
	} 
	 
	public void run() 
	{		
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
		
		sfs.addEventListener(SFSEvent.LOGIN_ERROR, evtListener);
		sfs.addEventListener(SFSEvent.LOGOUT, evtListener);
		sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, evtListener);
		
		// Load external configuration
		// The second parameter makes the client attempt the connection on configuration loaded
		try
		{
			sfs.loadConfig(getCfgPath(), true);	
		}
		catch (Exception e) {
			System.out.println("Unable to connect to host.");
		}		
    }
	
	public void onExtension(SFSEvent evt)
	{
		Map params = evt.getArguments();
    	ISFSObject data = (ISFSObject)params.get("params");	
      	
		if (params.get("cmd").equals("game.seatinfo"))
		{
			if(isConnected())
			{
				if(data==null)
				{
					//System.out.println("SFSObject was found Null on SEATINFO response. Bot="+userName+" doing LOGOUT.");
					botLogOut();
					return;
				} 
				seatinfo = (SeatInfo) data.getClass("seatinfo");
				System.out.println("**************************************SEAT INFO RECIEVED********************"+roomToJoin+" OF BOT"+userName);
				if(requestedSeat)
				{
				    granted=isSeatGranted(); 
					if(granted)
					{
						takenSeat=true;
						requestedSeat=false;
					}
					else
					{
						System.out.println("############################################GRANTED FOUND FALSE#######################################");
						//System.out.println("Seat was not granted to BOT="+userName+" . so Bot doing LOgout.");
						//botLogOut();
					}
				}
			}
		}			
		else if (params.get("cmd").equals("game.takeseat"))
		{
			// this is sent to the user who has to take a seat.
			
			if(data==null)
			{
				//System.out.println("SFSObject was found Null on TAKESEAT response. Bot="+userName+" doing LOGOUT.");
				//botLogOut();
				return;
			}
			System.out.println("BOT="+userName+" received TAKE-SEAT.");
			if(! test)
			{
				// just to show the Seat vacant for some time .
//				try
//				{
//					Thread.sleep(BOT_ACTION_DELAY);
//				} 
//				catch (InterruptedException e)
//				{					 
//					e.printStackTrace();
//				}
			}					 
				//check if BOT has sufficient account balance
			myChips=data.getInt("myChips");//saving myChips details
			
			if(!takenSeat)initiateTakeSeat();		
		}
		// game.started not needed as not GUI
		else if (params.get("cmd").equals("game.started")) // not needed really
		{
			turnCount = 0;			
			if(takenSeat)
			{
				gameStarted=true;
				roundId++;
			}
				
		}
		// game.started not needed as not GUI
		else if (params.get("cmd").equals("game.holecard")) // not needed really
		{
			HoleCard holecard = (HoleCard) data.getClass("HoleCard");
			if(holecard!=null)
			{				
				holeCards=holecard.getHoleCards();
			}		
		}
		else if (params.get("cmd").equals("game.communitycard")) // not needed really
		{
			CommunityCard commcard = (CommunityCard) data.getClass("CommunityCard");
			if(commcard!=null)
			{
				commCards=commcard.getCommunityCards();
			}	
			//updateMsg(commcard.toString());			
			////System.out.println("Bot="+userName+" Recieved Comm="+data.getClass("CommunityCard"));
		}
		else if (params.get("cmd").equals("game.roomdata"))
		{
			if(isConnected())
			{
				System.out.println("Roomdata recieved");
				buyInLow=data.getInt("buyInLow");
				buyInHigh=data.getInt("buyInHigh");
				gameType=data.getUtfString("gameType");
				maxPlayers=data.getInt("players");
				minPlayers=data.getInt("minPlayers");
				isRoomDataRecieved = true;
			}
		}
		else if(params.get("cmd").equals("game.gameinfo"))
		{
			// we are handling this Gameinfo event , but there is no use of this information in case of Bots.
			// so we have just initialized the values and havent use them anywhere in the code except Community cards and HoleCards.
			// As the idea of gameinfo is to view the gamedetails on GUI on game Watch or room join events, and thats not the cse of Bots
			// as they dont require View of Game on GUI.
			
			if(data==null)
			{
				return;
			}
			GameInfo gameInfo=(GameInfo)data.getClass("gameinfo");
			if(gameInfo==null)
			{
				return;
			}
			onGameInfo(gameInfo);			
		}
		else if (params.get("cmd").equals("game.userturn")) 
		{	try{			
			if(data==null)
			{
				//System.out.println("SFSObject was found Null on USERTURN response. Bot="+userName+" doing LOGOUT.");
				botLogOut();
				return;
			}
			int playerid = data.getInt("playerid");
			PlayerActionChoices choices = (PlayerActionChoices) data.getClass("PlayerActionChoices");
			if(choices==null)
			{
				return ;
			}
			if(playerid == myPlayerId)
			{
				botActionOnUserTurn(choices);				 
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}
		else if (params.get("cmd").equals("game.useraction"))  // not needed, just logging
		{
			if(data==null)
			{
				//System.out.println("SFSObject was found Null on USERACTION response. Bot="+userName+" doing LOGOUT.");
				botLogOut();
				return;
			}
			int playerid = data.getInt("playerid");
			int action = data.getInt("action");
			int amt	= data.getInt("amt");
			BotTaunts taunts=new BotTaunts();
			String say=taunts.tauntOnUserAction(action, amt, myPlayerId, playerid);
			
			if(say!="" && taunts.shouldBotTaunt())
			{
				sendChat(say);
			}
			String msg = "Player Id " + playerid + " did " + action + " amount " + amt; 
			//System.out.println("**********BOT="+userName+" did Action="+ action+" with amount="+ amt);
		}
	
		else if (params.get("cmd").equals("game.pot")) // just logging, not needed
		{
			if(data==null)
			{
				//System.out.println("SFSObject was found Null on POTINFO response. Bot="+userName+" doing LOGOUT.");
				botLogOut();
				return;
			}
			PotInfo potinfo = (PotInfo) data.getClass("potinfo");
			if(potinfo==null)
			{
				return;
			}
			int amount =0;
			for (Pot pot: potinfo.getPotList())
			{
				amount=amount+pot.getAmount();				
			}
			potAmount=amount;
		}
		
		else if (params.get("cmd").equals("game.winner")) 
		{
			if(data==null)
			{
				//System.out.println("SFSObject was found Null on WINNERINFO response. Bot="+userName+" doing LOGOUT.");
				botLogOut();
				return;
			}
			try
			{
				BotTaunts taunts=new BotTaunts();
				String say="";
				boolean iWon=false;
				WinnerInfo winnerInfo = (WinnerInfo) data.getClass("WinnerInfo");
				if(winnerInfo==null)
				{
					return;
				}
				String str = "";
				
				for (Winner winner: winnerInfo.getWinnerList())
				{		
					
					str += "[ Player Id "+ winner.getPlayerId() + " won $"+ winner.getAmount() +" ]";
					
					for (PlayerRanking playerRank: winnerInfo.getPlayerRankingList())
					{				
						if(playerRank.getPlayerId() == winner.getPlayerId())
							str += " with "+ playerRank.getRank().toString();					
					}
					//checking to send chat message in case someone wins.
					if(winner.getPlayerId()==myPlayerId)
					{//checking whether i won or not?
						iWon=true;												
						break;
					}
				}
				say=taunts.tauntOnWinner(iWon); 
				if(say!="" && taunts.shouldBotTaunt())
				{
				   sendChat(say);
				}
			}
			catch(Exception e)
			{
				
			}
		}		 
		else if(params.get("cmd").equals("game.ended"))
		{	//Game ended event comes ..			 
			if(test)
			{				
				joinRoom(LOBBY_ROOM_NAME);	
			}
			else
			{	
				gameEnded=true;
				// Bot should not do Logout or Leave Seat in this case because ,
			    //if this happens then BOT will leave even if he Wins or loses.
				//Bot will automatically take Seat , when he will Lose. So no need to make him Log out or Leave Seat. 
				botLogOut();     
			}		
		}
		else if(params.get("cmd").equals("game.account"))
		{
			
			System.out.println("Acount response recieved for BOT="+userName);
			if(data==null)
			{
				
				//botLogOut();
				return;
			}
			if(test)
			{
				//quickGameJoin();
				joinRoom(LOBBY_ROOM_NAME);
			}
			else
			{
				//System.out.println("data.getUtfString()="+data.getUtfString("chipType"));
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

				//System.out.println("game.account");
				if(chipsCheck)
				{//on initiateTakeSeat responce
					//System.out.println(userName+" Dummychips="+data.getInt("dummyChips"));					
					if(chips< 1000)
					{
						reloadChips=true;
						SFSObject sfsob = new SFSObject();
						//sfsob.putUtfString("chipType",chipType);
						System.out.println("Account is insufficient so sending Chip reload request. BOT="+userName);
						sendExtensionRequest("game.reloadchips", sfsob, null);
					}
					else
					{
						//System.out.println(userName+" Chips="+chips);	
						reloadChips=false;						
						requestedSeatId = takeRandomSeat();						
						if(requestedSeatId == -1)
						{
							System.out.println("BOT="+userName+" is doing Logout as no more Seats are vacant in the Room="+roomToJoin);
							botLogOut();
						}
						else
						{
							reserveSeat(requestedSeatId, true);
							try
							{
								Thread.sleep(BOT_ACTION_DELAY);
							} 
							catch (InterruptedException e)
							{					 
								e.printStackTrace();
							}
							if(!requestedSeat)requestSeat();
						}					
					}					
				}				 
			}
		}		 
	}
	public void removeSfsEventListener()
	{
		if(sfs!=null)
		{
			sfs.removeAllEventListeners();
		}		
	}
	
	private void joinRoom(String roomname)
	{
		Room room =sfs.getRoomByName(roomname);
		if(room== null)
		{
			//System.out.println("Room="+roomname+" was not found , BOT="+userName+ " is doing LOGOUT");			 
			return;
		}	
		JoinRoomRequest request = new JoinRoomRequest(roomname);		
		sfs.send(request);	
 
	}
	private void leaveRoom()
	{		
		LeaveRoomRequest request = new LeaveRoomRequest();		
		sfs.send(request);	
		isRoomDataRecieved = false;
		clearAllMemory();
	}
	private void loadLobbyView()
	{					
		quickGameJoin();		
	}
	
	private void loadGameView()
	{
		try 
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		if(!test)
		{
			BotSpawner.updateBotCount(roomToJoin, this);
		}		
		ISFSObject sfso  = new SFSObject();	
		sfso.putBool("reCon", false);
		sfso.putBool("watch", false);		 
		sendExtensionRequest("game.clientready", sfso, gameRoom);		
	}
	private void getRoomChipDetails(Room r)
	{
		SFSObject sfso=new SFSObject();
		sendExtensionRequest("game.account",sfso,null);		 
	}
	private void quickGameJoin()
	{	
		// Prepare a match expresison
		MatchExpression expr = new MatchExpression(RoomProperties.IS_GAME, BoolMatch.EQUALS, true).and(RoomProperties.HAS_FREE_PLAYER_SLOTS, BoolMatch.EQUALS, true);
		//.and("isGameStarted", BoolMatch.EQUALS, false);
		
		// An array of Room Groups where we want the search to take place
		List roomNames = (List) new ArrayList();
		roomNames.add("default");
		
		//updateMsg("\n In quick game join before request");
		// Fire the request and jump into the game!
		QuickGameJoinRequest aReq = new QuickGameJoinRequest(expr, roomNames, sfs.getLastJoinedRoom());
		sfs.send(aReq);	
		//updateMsg("\n In quick game join after sending request");
	}
	
	private void sendUserTurn(int act, int amt)
	{		
		if(!isDestroyed)
		{
			chipsLeft=chipsLeft-amt;
			try
			{
				Thread.currentThread().sleep(BOT_ACTION_DELAY);
			} catch (InterruptedException e)
			{
				//e.printStackTrace();
			}
			ISFSObject sfso  = new SFSObject();	
			sfso.putInt("action",act);
			sfso.putInt("amount",amt);
			sendExtensionRequest("game.userturn",sfso,gameRoom);	
		}
				
	}
	
	private void sendTakeSeat(int id, int myChips)
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
				reserveSeat(requestedSeatId, false);
				botLogOut();
			}
		}
		
			 
		
	}	
	private void sendLeaveSeat()
	{
		ISFSObject sfso  = new SFSObject();	
		sendExtensionRequest("game.leaveseat",sfso,gameRoom);	 
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
	public void sendLeaveRoom()
	{
		ISFSObject sfso  = new SFSObject();
//		joinRoom(LOBBY_ROOM_NAME);	
		leaveRoom();
	}
	
	public Boolean isSeatGranted()
	{
		boolean grant=false;
		for(int i=0 ; i< seatinfo.getSeats().size() ; i++ )
		{
			Seat seat=seatinfo.getSeats().get(i);
			boolean flag1 = seat.isOccupied();
			int flag2 =  seat.getPlayerId();
			if(flag1 && seat.getPlayerId() == myPlayerId)
			{
				chipsLeft=seat.getChipsLeft();// update BOT chips left on table.
				grant=true;
				break;
			}			
		}
		return grant;	 
	}
	private void initiateTakeSeat()
	{
		chipsCheck=true;
		takenSeat=false;
		SFSObject sfsob = new SFSObject();	
		System.out.println("initiateTakeSeat");
		sendExtensionRequest("game.account",sfsob,null);
	}
	
	private void botActionOnUserTurn(PlayerActionChoices choices)
	{
		PokerBotAI botAi=new PokerBotAI();		
		Round round=Round.PREFLOP;
		int callAmount=choices.getCallAmount();
		
		if(callAmount > 0)
		{
			//System.out.println("commCards.size() "+ commCards.size());
			
			switch(commCards.size())
			{
			case 0:
				//System.out.println("PREFLOP commCards.size() "+ commCards.size());
				round=Round.PREFLOP;
				break;
			case 3:
				//System.out.println("FLOP commCards.size() "+ commCards.size());
				round=Round.FLOP;
				break;
			case 4:
				//System.out.println("TURN commCards.size() "+ commCards.size());
				round=Round.TURN;
				break;
			case 5:
				//System.out.println("RIVER commCards.size() "+ commCards.size());
				round=Round.RIVER;
				break;
			default:
				//System.out.println("Improper size of Community cards on USERTURN.");
				break;
			}
			
			ArrayList<String> decision=botAi.botDecide(callAmount, round, potAmount, holeCards, commCards, chipsLeft);
			String action=decision.get(0);
			Integer amount=Integer.parseInt(decision.get(1));
			//System.out.println("*******action "+ action+" amount "+amount);
			if(action.equals("FOLD"))
			{
				if(choices.isRaiseAllowed())
				{
					Random randTurn=new Random();
				 	int randChoice=randTurn.nextInt(3); // 0=call, 1=fold, 2=raise
					switch(randChoice)
					{
						case 0:
							sendUserTurn(0, choices.getCallAmount());//call
							break;
						case 1:
							sendUserTurn(1, 0);//fold
							break;
						case 2:							 
							sendUserTurn(0, choices.getCallAmount());//Call
							break;
						default:
							break;
					}
				}
				else
				{
					sendUserTurn(1, 0);//fold
				}				
			}
			else if(action.equals("CALL"))
			{
				if(choices.isRaiseAllowed())
				{
					Random randTurn=new Random();
				 	int randChoice=randTurn.nextInt(3); // 0=call, 1=raise, 2=call
					switch(randChoice)
					{
						case 0:
							sendUserTurn(0, choices.getCallAmount());//call
							break;
						case 1:
							sendUserTurn(2, choices.getRaiseLow());//raise
							break;
						case 2:							 
							sendUserTurn(0, choices.getCallAmount());//Call
							break;
						default:
							break;
					}
				}
				else
				{
					sendUserTurn(0, choices.getCallAmount());//call
				}								 
			}
			else if(action.equals("RAISE"))
			{
				if(choices.isRaiseAllowed())
				{
					sendUserTurn(2, choices.getRaiseLow());//raise
				}
				else
				{
					sendUserTurn(0, amount); //call
				}						
			}
			else if(action.equals("ALLIN"))
			{
				sendUserTurn(2, choices.getRaiseHigh());//allin
			}					
		}
		else
		{
			sendUserTurn(0, 0);//call
		}				
	}
	
	
	private void sendChat(String taunt)
	{
		if(BotSpawner.BOT_CHAT_ALLOWED && takenSeat )
		{
			try
			{
				Thread.sleep(BOT_ACTION_DELAY);
			} 
			catch (InterruptedException e)
			{
			}
			//System.out.println("BOT="+userName+" Taunting="+taunt);
			SFSObject chatObj=new SFSObject();
			chatObj.putUtfString("msg", taunt);
			sendExtensionRequest("game.chat",chatObj,gameRoom);	
		}
		
	}
	private void onGameInfo(GameInfo gameInfo)
	{
		if(gameInfo.getGameState()!=null)
		{
			String gameState=gameInfo.getGameState();
		}	
		if(gameInfo.getCommunityCard()!=null)
		{
			commCards=gameInfo.getCommunityCard().getCommunityCards();
		}
		if(gameInfo.getHoleCard()!=null)
		{
			holeCards=gameInfo.getHoleCard().getHoleCards();
		}
		if(gameInfo.getPotInfo()!=null)
		{
			int amount =0;
			for (Pot pot: gameInfo.getPotInfo().getPotList())
			{
				amount=amount+pot.getAmount();				
			}
			potAmount=amount;
		}
		if(gameInfo.getWhoseTurnId()!= -1)
		{
			int whoseTurnId=gameInfo.getWhoseTurnId();
		}
		if(gameInfo.getRoundId()!=-1)
		{
			// roundId=gameInfo.getRoundId();
		}
		if(gameInfo.getTurnTime()!= -1)
		{
			int turnTime=gameInfo.getTurnTime();
		}
		if(gameInfo.getDealerSeatId()!= -1)
		{
			int dealerSeatId=gameInfo.getDealerSeatId();
		}
		if(gameInfo.getPlayersInfo()!=null)
		{
			PlayersInfo playersInfo=gameInfo.getPlayersInfo();
		}			
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
		if(roomJoined && !takenSeat)
		{
			users++;		
		}
		return users;		
	}
	private int takeRandomSeat()
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
		if(seatinfo !=null)seatLeft = seatinfo.getSeats();
		if(seatLeft != null)
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
			System.out.println("$$$$$$$$$$$$$$$$$$$$ SEAT INFO FOUND NULL $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		}
		if(!seatLeft.isEmpty())seatLeft.clear();
        if(!concurrentSeatLeftObj.isEmpty())
        {	
			Iterator setLeftIterator = concurrentSeatLeftObj.iterator();
			while(setLeftIterator.hasNext())
			{
				Seat seatObj = (Seat)setLeftIterator.next();
				if(seatObj.isOccupied() == false && seatObj.isReserved() == false)
				{
					seatLeft.add(seatObj);
				}
			}
	    }
//		for (Seat aSeat: seatinfo.getSeats())
//		{
//			if(aSeat.isOccupied() == false && aSeat.isReserved()==false)
//			{
//				seatLeft.add(aSeat);				
//			}
//		}
		Random randSeat=new Random();
		if(seatLeft.size()>0)
		{
			int seatIndex=randSeat.nextInt(seatLeft.size()); // return some random Seat index from the list of vacant Seats.
			int returnRequestedSeatId = seatLeft.get(seatIndex).getSeatId();
			return 	returnRequestedSeatId;
		}
		else
		{
			// No more vacant Seats Left !!!! BOT doing LOGOUT			
			return  -1;
		}			 
	}
	 
	private String getCfgPath() 
	{
		return "./config/sfs-config.xml";
	}
 	public boolean hasRequestedReload()
	{
		return reloadChips;
	}
	public void requestSeat()
	{
		System.out.println("BOT="+userName+" taking Seat no=" + requestedSeatId);
		chipsCheck=false;
		requestedSeat=true;		
		sendTakeSeat(requestedSeatId, myChips);		 
		System.out.println("requested seat "+ requestedSeat);
	}
	public synchronized void botLogOut()
	{
		if(!isDestroyed)
		{
			System.out.println("*******************BOT="+userName+" [ Bot-RAnk="+this.getRank()+" ]  doing Logout.");
			requestedSeat = false;
			gameStarted=false;
			takenSeat=false;
			roomJoined=false;
			roomLeft=true;
			granted =false;
			if(clientTimer!=null)
			{
				clientTimer.cancel();
			}	
			if(clientKeepAlive!=null)
			{
				clientKeepAlive.cancel();
			}
			aliveCount=0;
			roundId=0;
			sendLeaveRoom();
			//clearAllMemory();
		}
				
	}
	public void clearAllMemory()
	{
		if(!isDestroyed)
		{
			BotSpawner.botAccountMap.get(userName).setStatus(true);
			BotSpawner.botCountOnLogout(roomToJoin,this);
			System.out.println("Cleared memory for Thread="+userName);
			
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
			//dereference all instance variables.
			sfs=null;
			evtListener=null;			
			gameRoom=null;
			seatinfo=null;
			
			if(holeCards!=null)
		    {
				holeCards.clear();
				holeCards =null;
		    }
			if(commCards!=null)
		    {
				commCards.clear();
				commCards =null;
		    }
			
			//set the flag that the meory has been deallocated successfully
			isDestroyed=true;
			//run the Garbage collector
			System.gc();
		}		
	}
	private void reserveSeat(int seatId, boolean toReserve)
	{
		SFSObject sfso=new  SFSObject();
		sfso.putInt("seatId",seatId);
		sfso.putBool("reserve",toReserve);
		sfso.putBool("isWaiting",false);
		
		sendExtensionRequest("game.reserveSeat", sfso, gameRoom);
	}
	private void sendExtensionRequest(String command, ISFSObject data, Room room)
	{
		if(sfs!=null)
		{
			if(isConnected())
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
//		BotSpawner.loadBotNames();
//		for(int i=0; i< BotSpawner.botAccountMap.size() ;i++)
//	    {
//	        String mainsfsId=BotSpawner.getBotId();   	    
//	   
//	        BasicClient bot=new BasicClient(mainsfsId,BotSpawner.botAccountMap.get(mainsfsId).getPassword(),null,"forward","dummychips");
//		    try 
//		    {
//				Thread.sleep(2000);
//		    }
//		    catch(InterruptedException e)
//		    {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//		    }	    
//			try 
//			{
//				bot.runner.join();
//			}
//			catch (InterruptedException e) 
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	    }
	}
}
