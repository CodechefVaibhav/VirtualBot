package sfs2x.client.example;

import sfs2x.client.SmartFox; 
import sfs2x.client.core.*; 
import sfs2x.client.entities.*;
import sfs2x.client.requests.*;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException; 
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//Note: This class is used to spawn Poker and Rummy BOTS on adda52.com server. These BOTS are supposed to play with Room Users.
// To run the BOT , the sfs-config of this BOT client should contain the IP of the server and the Zone file name.
// This program requires a user.txt.csv file to upload the Usernames kept reserved for BOTS, that should be in the config 
//folder of SFS.
//BOTS will join the game room only if the room contains Users(Non BOT players).
//This class cant be used to test Random behaviour of BOTS.

public class BotSpawner  implements Runnable 
{
	private TimerTask alive;
	public SmartFox sfs=null;		
	public Thread runner=null;
	
	private Timer timer;
	private Timer reconnectTimer;
	
	private static File botIdFile=null;
	private static BufferedReader reader=null;
	
	public static HashMap<String,BotAccount> botAccountMap =new HashMap<String, BotAccount>();
	public static ArrayList<String> frontBotsRoom=new ArrayList<String>();//contains the list of room names that have been alloted front bots.
	public static HashMap<String, ArrayList> roomBotDetails=new HashMap<String, ArrayList>();//contains the list of bot instances corresponding to room names in which they have been spawned.
	public static ArrayList<String> roomsAllowedForBots=new ArrayList<String>();//contains the details of room names that are based on dummy chips.
	private static HashMap<String,String> roomChipType=new HashMap<String,String>();//contains the chiptype of rooms.
	private static boolean reconnecting=false;	 
	public static boolean FRONT_BOTS_ALLOWED=false;
	public static boolean BOT_CHAT_ALLOWED=false;
	
	public int removalCount=0;
	private int cleanCount=0;
	public static final int THREAD_WAIT=45*1000; // these are kept static to access them from BOT classes.	
	public final int MAX_POKER_BOTS_IN_RING=3;
	private final int RECONNECTION_INTERVAL=60*1000;
	
	private static final String RUMMY_BOT_USER_ID_FILE_PATH="./config/user.txt.csv";
	private static final String POKER_BOT_USER_ID_FILE_PATH="./config/usersNew.txt.csv";
	private static final int MAX_PAST_BOTNAMES=10;
	public static String superBotId=null;
	private static  String BOT_TYPE="Rummy";
	private static List<String> pastBotNames=new ArrayList<String>();
	private boolean runAllowed=true;
	private int reconnectionAttempts=0;	
	//Limits the Reconnection attempts of Bot Spawner to server to MAX_RECONNECTION_ATTEMPT times.
	private static boolean limitedReconnection=false;
	private static int MAX_RECONNECTION_ATTEMPT=5;
	public static ThreadGroup threadPool=null;
	private final int RUMMY_FRONT_BOT_CONFIGID=32;
	private final int POKER_FRONT_BOT_CONFIGID=2544;
	//this flag tells whether to use client side logic for identifying frontbot loading or the server side.
	private final boolean clientSideFrontLogic=true;
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
	    
	    BOT_TYPE=p.getProperty("BOT_TYPE","Poker");	    	
	    FRONT_BOTS_ALLOWED=Boolean.parseBoolean(p.getProperty("ADD_FRONT_BOTS","false"));
	    BOT_CHAT_ALLOWED=Boolean.parseBoolean(p.getProperty("BOT_CHAT","true"));
	    limitedReconnection=Boolean.parseBoolean(p.getProperty("LIMITED_RECONNECTION","true"));
	    MAX_RECONNECTION_ATTEMPT=Integer.parseInt(p.getProperty("RECONNECTION_ATTEMPT","5"));
	    loadBotNames();	// always call this method at the end of this static block.
	}
	
	
	

	private class KeepAlive extends TimerTask
	{	 
		public void run()
		{
			SFSObject data=new SFSObject();	 
			//System.out.println("Sending KEEP ALIVE for Super Bot.");
			sendExtensionRequest("game.keepAlive",data,null); 
		}
		
	}
	
	
	private class Reconnect extends TimerTask
	{
		public void run()
		{			
			if(reconnecting)
			{				
				
				try
				{					
					reconnectionAttempts++;
					System.out.println("Reconnection attempt count="+reconnectionAttempts);
					if(reconnectionAttempts > MAX_RECONNECTION_ATTEMPT && limitedReconnection)
					{
						//if exceeds the MAX RECONNECTION ATTEMPTS then stop BOT Spawner.
						System.out.println("Reconnection attempts exceeded MAX_RECONNECTION_ATTEMPT so stoping Bot-Spawner");
						runAllowed=false;
						return;
					}
					// free all botIds that were running at the time of diconnection.
					refreshBotAccounts();  
					//reconnect
					init();
					if(sfs != null )sfs.connect();					
				}
				catch(Exception e)
				{
					//System.out.println("Unable to connect to the server, Server isn't reachable.");
				}	
			}
		}
	}
	
	
	
	public BotSpawner(ThreadGroup parentPool)
	{ 		
		this.threadPool=parentPool;
	    superBotId=getBotId();
	    System.out.println("Initiating Super Bot with userName="+superBotId);
	    init();		   
	    runner =new Thread(this,superBotId);			   
	}	
	public static void loadBotNames()
	{
		//System.out.println("Loading  Bot Names from Username File.");
		String data="";
		if(BOT_TYPE.equalsIgnoreCase("rummy"))
			botIdFile=new File(RUMMY_BOT_USER_ID_FILE_PATH);
		else
			botIdFile=new File(POKER_BOT_USER_ID_FILE_PATH);
		//System.out.println("botIdFile : "+botIdFile);
		try 
		{
			reader=new BufferedReader(new FileReader(botIdFile));
		} 
		catch (FileNotFoundException e)
		{			
			e.printStackTrace();
		}
		
		try 
		{
			if(reader!=null)
			{				 
				reader.readLine();//skipping the column names row in the file.				
				while((data=reader.readLine())!=null)
				{
					String[] userAcc=data.split(",");
					BotAccount bot=new BotAccount();
					bot.setUsername(userAcc[0]);
					bot.setPassword(userAcc[1]);
					bot.setStatus(true);					 
					fillBotIdMap(bot); 					
				}				
			}			 
		} 
		catch (IOException e) 
		{		 
			e.printStackTrace();
		}		 	 
	}	
	public static void fillBotIdMap(BotAccount bot)	
	{
		if(! botAccountMap.containsKey(bot.getUsername()))
		{
			botAccountMap.put(bot.getUsername(), bot);
		}
	}
	
	
	public void run()
	{		
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e1) 
		{			 
			e1.printStackTrace();
		}	
		
		while(runAllowed)
		{
			System.out.println("CURRENT THREAD COUNT : "+Thread.activeCount());
			cleanCount++;		
			//request front bot rooms
			if(clientSideFrontLogic)
			{
				requestRoomsForFrontbots();
			}
			for(int i=0;i< sfs.getRoomList().size();i++)
			{
				Room r=sfs.getRoomList().get(i);	
				//initialize hashmaps
 				if(! roomBotDetails.containsKey(r.getName()))
				{
					roomBotDetails.put(r.getName(), new ArrayList());					
				}
				 
				//chk for if One BOT in all rooms needed
				int minUserNeeded=0; // minimum no. of players required in a room , to spawn Bot in that room.
				
				if(FRONT_BOTS_ALLOWED &&  getLeftBotsCount() > 0)
				{
					//minUserNeeded=0;
					minUserNeeded=1; // will add MIN type BOTS when room count will be more than this value.
					//if(r.isGame() && r.getUserCount()< minUserNeeded)
					if(r.isGame())
					{
						//add front bots if the front Bot count in  room is 0
						if(( frontBotsRoom.contains(r.getName()) && getFrontBotCountInRoom(r)==0 ))
						{
							try 
							{
								Thread.sleep(500);
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
//							frontBotsRoom.add(r.getName());
							checkAndAddBot(r,"front");						
						}
					}
				}
				else
				{
					minUserNeeded=0;  
				}
				
				 
				//System.out.println("*********Current Thread count="+Thread.currentThread().getThreadGroup().activeCount());
				//troubleshootThreads();
				/* commenting this portion of code.
				//check for inconsistency in Bot Counts and improper Bots.
				if(cleanCount==2)
				{//remove inconsistency of Bots in room.
					if(isPokerBot())
					{
						cleanBotsInconsistency(r);
						cleanCount=0;
					}
					
					printRunningBotDetails();
				}
				 */
				if( r.isGame() && (r.getMaxUsers() > r.getUserCount()))
				{				
					//System.out.println("RoomName="+r.getName()+" containsFrontBot="+BotSpawner.frontBotsRoom.contains(r.getName())+" usercount="+r.getUserCount());
//					if((frontBotsRoom.contains(r.getName()) && (r.getUserCount() > 1)) || (r.getUserCount() > 0) )
//					{						 
						//System.out.println("ROOM="+r.getName()+" Non BOT Users :"+ nonBotUsersCount(r));	
						//System.out.println("ROOM="+r.getName()+" Total Users :"+ r.getUserCount());
						if( getLeftBotsCount() > 0 )	//checking if Bot Id are available
						{					 
							if(nonBotUsersCount(r) > 0)  // Room contains atleast 1 Non-Bot player ?
							{
								if(r.getName().contains("RING") || r.getName().contains("POINTS"))
								{	//for POKER and RUMMY RING game.					
									if(getBotCountInRoom(r) < allowedMaxRingBots(r))
									{
										//System.out.println("******************Room "+r.getName()+" MID BOT count="+getBotCountInRoom(r)+" allowedMaxRingBots"+allowedMaxRingBots(r) );
										checkAndAddBot(r,"mid");
									}
									else
									{
										//System.out.println("Room "+r.getName()+" already has MAX BOTS. BOT COUNT="+botCount.get(r.getName()));
									}
								}
								else
								{
									//for Rummy games and STT game of Poker.							 
									checkAndAddBot(r,"mid");
								}
							}
							else
							{
								//System.out.println("No non-Bot user found in Room="+r.getName());
							}		
						}
						else
						{
							//System.out.println("No More Bot Account left to add in Room="+r.getName());
						}
//					}
				
				}
			}
			//System.out.println("Front Bot Lists="+frontBotsRoom.size());
			
			try
			{
				Thread.sleep(THREAD_WAIT);  // delay in checking the status of rooms because Smartfox Room variables take time in getting updated.
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
				
		}
		//if the main tgread
		if(! runAllowed)
		{
			clearAllMemory();
		}
			//break;
				
	}
	public void checkAndAddBot(Room r,String botRank)
	{
		if(r!=null)
		{
			//System.out.println("checkAndAddBot for roomname="+r.getName());
			if( !roomsAllowedForBots.contains(r.getName()))
			{
				sendRoomAllowedRequest(r.getName(),botRank);
			}
			else
			{
				if(roomChipType.get(r)!=null && !roomChipType.get(r).equalsIgnoreCase("real"))
					addBotClient(r, botRank,roomChipType.get(r));
				else if(clientSideFrontLogic)
				{
					addBotClient(r, botRank, getChipTypeForClientSideFrontBotLoading());
				}
				else
					sendRoomAllowedRequest(r.getName(),botRank);
			}
		}		
	}
	public boolean roomHasFrontBots(Room r)
	{
		//System.out.println("roomHasFrontBots");
		boolean front=false;
		
		if(isPokerBot())
		{
			for(int i=0; i < roomBotDetails.get(r.getName()).size() ; i++)
			{
				BasicClient pokerBot=(BasicClient)roomBotDetails.get(r.getName()).get(i);
				if(pokerBot!=null && pokerBot.getRank().equalsIgnoreCase("front"))
				{
					return true;
				}
			}
		}
		else
		{
			for(int i=0; i < roomBotDetails.get(r.getName()).size() ; i++)
			{
				RummyBot rummyBot=(RummyBot)roomBotDetails.get(r.getName()).get(i);
				if(rummyBot!=null && rummyBot.getRank().equalsIgnoreCase("front"))
				{
					return true;
				}
			}
						
		}
		return front;		
	}
	public void cleanBotsInconsistency(Room r)
	{	
		//System.out.println("Cleaning Bot Inconsistency.");
		if(nonBotUsersCount(r) > 0)	
		{//removing idle Bots with no seat.
			removeIdleBotsFromRoom(r);
		}	
								   
		if(roomBotDetails.get(r.getName()).size() >= r.getMaxUsers())
		{//check if Bot count is greater than Room count, if so then remove all Bots from room.
			removeBotsFromRoom(r);
		}	
		
		//check if only Bots are playing in room
		if(roomBotDetails.get(r.getName()).size() == r.getUserCount())
		{//check if Bot count becomes equal to Room users count,if so then remove all Bots from room.
			removeBotsFromRoom(r);
		}			
		if(roomBotDetails.get( r.getName()).size() > allowedMaxRingBots(r) )
		{//check if BOT count is more than allowed Bots in a room.
			
			boolean removedIdle=false;
			while( roomBotDetails.get(r.getName()).size() > allowedMaxRingBots(r) )
			{
				removeIdleBotsFromRoom(r);
				removedIdle=true;
				if(removedIdle)
				{// if room has no Idle Bot then remove any of the Bot.
					if(roomBotDetails.get(r.getName()).size() > allowedMaxRingBots(r))
					{
						if(roomBotDetails.get(r.getName()).size()>0)
						{//checking if room has atleast one Bot.
							removeSingleMidBotFromRoom(r.getName());
						}
					}
				}
			}			
		}		
	}
	private void removeSingleMidBotFromRoom(String roomName)
	{			
		if(isPokerBot())
		{
			List<BasicClient> pokerBotList=roomBotDetails.get(roomName);
			Iterator<BasicClient> iterator=pokerBotList.iterator();
			while(iterator.hasNext())
			{
				BasicClient pokerBot=iterator.next();
				if(pokerBot!=null && pokerBot.getRank().equalsIgnoreCase("mid"))
				{
					pokerBot.botLogOut();
					break;
				}
			}
			//avoiding loop traversal to avoid fail safe
//			for(int i=0; i<roomBotDetails.get(roomName).size() ;i++)
//			{
//				BasicClient pokerBot=(BasicClient)roomBotDetails.get(roomName).get(i);
//				if(pokerBot!=null && pokerBot.getRank().equals("mid"))
//				{
//					pokerBot.botLogOut();
//					break;
//				}
//			}
		}
		else
		{
			List<RummyBot> rummyBotList=roomBotDetails.get(roomName);
			Iterator<RummyBot> iterator=rummyBotList.iterator();
			while(iterator.hasNext())
			{
				RummyBot rummyBot=iterator.next();
				if(rummyBot!=null && rummyBot.getRank().equalsIgnoreCase("mid"))
				{
					rummyBot.botLogOut();
					break;
				}
			}
			//avoiding loop traversal to avoid fail safe
//			for(int i=0;i<roomBotDetails.get(roomName).size() ; i++)
//			{
//				RummyBot rummyBot=(RummyBot)roomBotDetails.get(roomName).get(i);
//				if(rummyBot!=null && rummyBot.getRank().equals("mid"))
//				{
//					rummyBot.botLogOut();
//					break;
//				}
//			}
		}			 
	}
	private String getChipTypeForClientSideFrontBotLoading()
	{
		if(isPokerBot())
		{
			return "Freeroll";
		}
		else
		{
			return "dummy";
		}
	}
	private static boolean isPokerBot()
	{
		if(BOT_TYPE.equalsIgnoreCase("Poker"))
			return true;
		else
			return false;
	}
	public void removeIdleBotsFromRoom(Room r)
	{
		if( getBotsWithoutSeats(r).size()> 0 && isPokerBot())
		{	//check if any Bot has not got Seat.	
			//System.out.println("Removing Idle Bots from room.");
			List<BasicClient> pokerBotList=getBotsWithoutSeats(r);
			if(pokerBotList!=null)
			{
				Iterator<BasicClient> iterator=pokerBotList.iterator();
				while(iterator.hasNext())
				{
					BasicClient pokerBot=iterator.next();
					if(pokerBot!=null && pokerBot.hasJoinedRoom() && pokerBot.hasRequestedSeat() && !pokerBot.isSeatGranted() && pokerBot.getRank().equalsIgnoreCase("mid"))
					{
						pokerBot.botLogOut();
					}
				}
			}
			
//			for(int i=0;i< getBotsWithoutSeats(r).size();i++)
//			{
//				BasicClient pokerBot=(BasicClient)getBotsWithoutSeats(r).get(i);
//				if(pokerBot!=null && pokerBot.hasJoinedRoom() && pokerBot.hasRequestedSeat() && !pokerBot.isSeatGranted() && pokerBot.getRank().equals("mid"))
//				{
//					pokerBot.botLogOut();
//				}
//			}
		}
	}
	public void removeBotsIfNotInRoomBotList(Room r)
	{
		
	}
	 
	public ArrayList getBotsWithoutSeats(Room r)
	{
		ArrayList idle=new ArrayList();
		if(isPokerBot())
		{
			if(roomBotDetails.containsKey(r.getName()))
			{
				for(int i=0; i<roomBotDetails.get(r.getName()).size() ; i++)
				{
					BasicClient pokerBot=(BasicClient)roomBotDetails.get(r.getName()).get(i);
					if(pokerBot!=null && pokerBot.hasJoinedRoom() && pokerBot.hasRequestedSeat() && !pokerBot.isSeatGranted())
					{
						idle.add(pokerBot);
					}
				}
			}	
		}
		else
		{
			if(roomBotDetails.containsKey(r.getName()))
			{
				for(int i=0; i<roomBotDetails.get(r.getName()).size() ; i++)
				{
					RummyBot rummyBot=(RummyBot)roomBotDetails.get(r.getName()).get(i);
					if(rummyBot!=null && rummyBot.hasJoinedRoom() && rummyBot.hasRequestedSeat() && !rummyBot.isSeatGranted())
					{
						idle.add(rummyBot);
					}
				}
			}	
		}		
		return idle;
	}
	public void resetBotsInRoom(Room r)
	{
		
		//remove Bots from this room.
		removeBotsFromRoom(r);		
		for(int i=0;i<allowedMaxRingBots(r);i++)
		{
			if( r.getUserCount() < r.getMaxUsers())
			{
				if(roomChipType.get(r)!=null && !roomChipType.get(r).equalsIgnoreCase("real"))
					addBotClient(r, "mid",roomChipType.get(r));
			}
		}
	}
	public void printRunningBotDetails()
	{
		int total=0;	 		
		//System.out.println("BOTS details in Active Rooms:");
		Set<String> roomnames=roomBotDetails.keySet();
		for (String room : roomnames)
		{
		  int count=roomBotDetails.get(room).size();
		  Room r=sfs.getRoomByName(room);		  
		  total=total+roomBotDetails.get(room).size();
		  //System.out.println("Room ="+room+" | BOTS count="+count +" | User count="+nonBotUsersCount(r)); 
		}
		//System.out.println("Total BOTS="+total);
	}
	public  void removeBotsFromRoom(Room r)
	{
		if(isPokerBot())
		{
			List<BasicClient> pokerBotList=roomBotDetails.get(r.getName());
			Iterator<BasicClient> iterator=pokerBotList.iterator();
			while(iterator.hasNext())
			{
				BasicClient pokerBot=iterator.next();
				if(pokerBot!=null && pokerBot.isConnected() && pokerBot.getRank().equalsIgnoreCase("mid"))
				{
					pokerBot.botLogOut();
				}
				try 
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e) 
				{								 
					e.printStackTrace();            		
				}		
			}
		}
		else
		{
			List<RummyBot> rummyBotList=roomBotDetails.get(r.getName());
			Iterator<RummyBot> iterator=rummyBotList.iterator();
			while(iterator.hasNext())
			{
				RummyBot rummyBot=iterator.next();
				if(rummyBot!=null && rummyBot.getRank().equalsIgnoreCase("mid"))
				{
					rummyBot.botLogOut();
					break;
				}
				try 
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e) 
				{								 
					e.printStackTrace();            		
				}		
			}
		}
	}
	public void init()
	{		  
		if(sfs==null)
		{//instantiate only once.
			
		}
		sfs=new SmartFox();		
		//here the true flag assures that SmartFox will automatically connect to server on successful load config.
		try
		{
			sfs.loadConfig(getCfgPath(), true);	
		}
		catch (Exception e) {
			//System.out.println("Unable to connect to host.");
		}
		     		 
		 
 
		sfs.addEventListener(SFSEvent.CONNECTION, new IEventListener() {
	    	 public void dispatch(BaseEvent evt) throws SFSException {
		        System.out.println("Super Bot Connected with Id="+superBotId +" and password="+botAccountMap.get(superBotId).getPassword());
		        //System.out.println("reconnecting before : "+reconnecting);
		        if(reconnecting)
		        {	
		        	//System.out.println("SuperBot Reconnected successfully .Now doing Login.");
		        	reconnectTimer.cancel();	
		        	reconnectionAttempts=0;
		        	//System.out.println("Setting reconnecting to FALSE.Connection");
		            reconnecting=false;
		        }
		        //System.out.println("reconnecting after: "+reconnecting);
		        sfs.send(new LoginRequest(superBotId, getSHAofPass(botAccountMap.get(superBotId).getPassword()) , sfs.getCurrentZone()));
	    	 }
	    	 });
	    sfs.addEventListener(SFSEvent.LOGIN, new IEventListener() {
	    	 public void dispatch(BaseEvent evt) throws SFSException {
	    	 System.out.println("SUPER BOT was Login successful!");	
	    	 timer= new Timer();
			 timer.scheduleAtFixedRate(new KeepAlive(),1,5000);
	    	 // send room join request.
			 JoinRoomRequest request = new JoinRoomRequest("Lobby");	
	    	 sfs.send(request);	    	 
	    	 }
	    	 });	  
	    sfs.addEventListener(SFSEvent.ROOM_JOIN, new IEventListener() { 
	    	public void dispatch(BaseEvent evt) throws SFSException {	    
	    	System.out.println("Super Bot has joined Game Lobby successfully: " + evt.getArguments().get("room"));
			if(! reconnecting)
			{
				try
				{
					runner.start();
				}
				catch(Exception e)
				{
					
				}
			  }
 	    	}
	    }); 
	    sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, new IEventListener() {
	    	 public void dispatch(BaseEvent evt) throws SFSException 
	    	 {
	    		 Map params = evt.getArguments();
	    		 ISFSObject data = (ISFSObject)params.get("params");
	    		 if (params!=null && data!=null && params.get("cmd").equals("game.isRoomAllowed"))
	    		 {
	    			 String roomName=data.getUtfString("roomname");
	    			 boolean joinAllowed=data.getBool("allowed");
	    			 String botRank=data.getUtfString("botrank");
	    			 String chipType=data.getUtfString("chipType");
	    			 //System.out.println("Room has chip type="+chipType);
	    			 roomChipType.put(roomName, chipType);
	    			 Room r=sfs.getRoomByName(roomName);
	    			 
	    			 if(joinAllowed)
	    			 {
	    				 if(!chipType.equalsIgnoreCase("real"))
	    				 {
	    					 addBotClient(r, botRank,chipType);
		    				 roomsAllowedForBots.add(roomName);
		    				 if(botRank.equalsIgnoreCase("front"))
		    				 {
		    					 if(!frontBotsRoom.contains(roomName))
		    						 frontBotsRoom.add(roomName);
		    				 }
		    				 //System.out.println("Bot is allowed to join room="+roomName);
	    				 }	    				 
	    			 }
	    			 else
	    			 {
	    				 //System.out.println(roomName+" is a real cash room so not allowing Bot in this room");
	    			 }
	    		 }
	    		 else if(params!=null && data!=null && params.get("cmd").equals("game.joinRoom"))
	    		 {
	    			 List<String> roomNames=(List<String>) data.getUtfStringArray("rooms");
	    			 //System.out.println("***********Received room names="+roomNames);
	    			 if(roomNames!=null)
	    			 {
	    				 for(int i=0;i<roomNames.size();i++)
	    				 {
	    					 String roomName=roomNames.get(i);
	    					 if(! frontBotsRoom.contains(roomName))
	    					 {
	    						 frontBotsRoom.add(roomName);
	    					 }
	    					 if(! roomsAllowedForBots.contains(roomName))
	    					 {
	    						 roomsAllowedForBots.add(roomName);
	    					 }
	    				 }
	    			 }
	    			 //System.out.println("***********Updated frontBots rooms="+frontBotsRoom);
	    		 }
	    	 //System.out.println("Received Response from server of Keep Alive for SUPER BOT="+superBotId);	  	 
	    	 }
	    	 });
	    sfs.addEventListener(SFSEvent.ROOM_VARIABLES_UPDATE, new IEventListener() {
	    	 public void dispatch(BaseEvent evt) throws SFSException {
	    	 //System.out.println("ROOM_VARIABLES_UPDATE!");		    	  	 
	    	  	 
	    	 }
	    	 });
	    
	    sfs.addEventListener(SFSEvent.LOGIN_ERROR, new IEventListener() {
	    	 public void dispatch(BaseEvent evt) throws SFSException {
	    	 //System.out.println("Super Bot Login failure: " + evt.getArguments().get("errorMessage"));
	    	 if(botAccountMap.containsKey(superBotId))	//bot id in use so asking for new Id n doing login with it.
	    	 {
	    		 //System.out.println("Super Bot Login Id="+superBotId+" is already in use. Doing Login with different Id.");
	    		 botAccountMap.get(superBotId).setStatus(false);	    		  		 
	    	 }
	    	 else
	    	 {
	    		 //System.out.println("Super Bot Login Id="+superBotId+" doesnt exist.Doing Login with different Id.");
	    	 }
//	    	 superBotId=getBotId();
//	    	 System.out.println("Super Bot doing Login with Id="+superBotId);
//    		 sfs.send(new LoginRequest(superBotId,getBotPass(superBotId)));
	    	 
	    	 }
	    	 });
	    
	    
	    
	    sfs.addEventListener(SFSEvent.CONNECTION_LOST, new IEventListener() {
	    	 public void dispatch(BaseEvent evt) throws SFSException {
	    		 System.out.println("Lost Connection of Super Bot="+superBotId+". Doing Login again.");
	    		 //Clear all data
	    		 clearAllOnDisconnection();
	    		 //reset SuperBot account details
	    		 botAccountMap.get(superBotId).setStatus(true);
	    		 superBotId=getBotId();
	    		 if(superBotId != null)
	    		 {	    			 
	    			 System.out.println("Super Bot doing login again with Id="+superBotId+" After 5 mins.");
		    	     reconnecting=true;		    
		    	     //start reconnection timer
		    	     if(reconnectTimer!=null)
		    	     {//stop earlier reconnection timer if running already.
		    	    	 reconnectTimer.cancel();
		    	     }
			    	 reconnectTimer= new Timer();
			    	 reconnectTimer.scheduleAtFixedRate(new Reconnect(),RECONNECTION_INTERVAL,RECONNECTION_INTERVAL);
	    		 }	    		 
	    	 }
	    	 });
	    
	   
	    
	    sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, new IEventListener() { 
	    	public void dispatch(BaseEvent evt) throws SFSException {
	    	//System.out.println("Super Bot was unable to Join Game Lobby : " + evt.getArguments().get("errorMessage")); 
	    	} }); 
	  
	   
	}
	
	public void refreshBotAccounts()
	{	    		
		System.out.println("Super Bot : Refreshing BOT accounts");
		Set<String> keys=botAccountMap.keySet();
		for(String botId:keys)
		{
			botAccountMap.get(botId).setStatus(true);	//making all id's status of Bots to available.
		}
		botAccountMap.get(superBotId).setStatus(false); //superBot id should be kept busy as it is being used by Superbot.
 	}
	
	public static int nonBotUsersCount(Room r)
	{
		 int real=0;		 
		 // System.out.println("Counting NON BOT user in ROOM="+r.getName());
		 if(r!= null && roomBotDetails.containsKey(r.getName()))
		 {
			  if( roomBotDetails.get(r.getName()).size() > 0 )
			  {
				  real= r.getUserCount() - roomBotDetails.get(r.getName()).size();
				  if(real<0)
				  {
					  real=0; // sometimes late update of Room variable may lead into inconsistency of BOT count.
				  }
				  //System.out.println("**************Counting NON BOT user in ROOM="+r.getName()+"nonbot count= "+real);
			  }
			  else
			  {
				  real= r.getUserCount();
			  }
		  }
		  return real;
	}
	public void sendRoomAllowedRequest(String roomName,String botRank)
	{
		//System.out.println("********sendRoomAllowedRequest for roomname="+roomName);
		SFSObject roomChk=new SFSObject();
		roomChk.putUtfString("roomname", roomName);
		roomChk.putUtfString("botrank", botRank);
		sendExtensionRequest("game.isRoomAllowed",roomChk,null);
	 
	}
	public void sendBotCounts()
	{
		int total=0;	 				
		Set<String> roomnames=roomBotDetails.keySet();
		for (String room : roomnames)
		{
		  int count=roomBotDetails.get(room).size();
		  Room r=sfs.getRoomByName(room);
		  total=total+roomBotDetails.get(room).size();		
		}		
		SFSObject botcount=new SFSObject();
		botcount.putInt("botCount", total);		 
		sendExtensionRequest("game.botCount",botcount,null);	 
	}
	
	public static synchronized void updateBotCount(String roomname,BasicClient pokerBot)
	{			
		if(roomBotDetails.containsKey(roomname))
		{				 
			roomBotDetails.get(roomname).add(pokerBot);				 
		}
		else
		{
			ArrayList botList=new ArrayList();
			botList.add(pokerBot);
			roomBotDetails.put(roomname,botList);
		}
	}
	public static synchronized void updateBotCount(String roomname,RummyBot rummyBot)
	{			
		if(roomBotDetails.containsKey(roomname))
		{
			roomBotDetails.get(roomname).add(rummyBot);				 
		}
		else
		{
			ArrayList botList=new ArrayList();
			botList.add(rummyBot);
			roomBotDetails.put(roomname,botList);
		}			
	}
	public int getLeftBotsCount()
	{
		Set<String> idSet=botAccountMap.keySet();
		int count=0;
		for(String id:idSet)
		{
			if(botAccountMap.get(id).getStatus())
			{				 
				count++;				
			}
		}
		return count;
	}
	public int allowedMaxRingBots(Room r)
	{
//		if(r.getMaxUsers()== 9)
//		{
//			return 1; 
//		}
//		if(r.getMaxUsers()== 6)
//		{
//			return 1;
//		}
//		if(r.getMaxUsers()== 4)
//		{
//			return 1;
//		}
//		if(r.getMaxUsers()== 2)
//		{
//			return 1;
//		}		
		return 1;
	}
	public static int getBotCountInRoom(Room r)
	{
		int botCountInRoom=0;
		if(roomBotDetails.containsKey(r.getName()))
		{
			List botList=roomBotDetails.get(r.getName());
			if(isPokerBot())
			{
				for(int i=0;i<botList.size();i++)
				{
					BasicClient pokerBot=(BasicClient)botList.get(i);
					if(pokerBot.getRank().equalsIgnoreCase("mid"))
					{
						botCountInRoom++;
					}
				}
			}
			else
			{
				for(int i=0;i<botList.size();i++)
				{
					RummyBot rummyBot=(RummyBot)botList.get(i);
					if(rummyBot.getRank().equalsIgnoreCase("mid"))
					{
						botCountInRoom++;
					}
				}
			}		
		}
		//System.out.println("mid Bot count in room="+r.getName()+" is "+botCountInRoom);
		return botCountInRoom;
	}
	public static int getFrontBotCountInRoom(Room r)
	{
		int botCountInRoom=0;
		if(roomBotDetails.containsKey(r.getName()))
		{
			List botList=roomBotDetails.get(r.getName());
			if(isPokerBot())
			{
				for(int i=0;i<botList.size();i++)
				{
					BasicClient pokerBot=(BasicClient)botList.get(i);
					if(pokerBot.getRank().equalsIgnoreCase("front"))
					{
						botCountInRoom++;
					}
				}
			}
			else
			{
				for(int i=0;i<botList.size();i++)
				{
					RummyBot rummyBot=(RummyBot)botList.get(i);
					if(rummyBot.getRank().equalsIgnoreCase("front"))
					{
						botCountInRoom++;
					}
				}
			}		
		}
		//System.out.println("mid Bot count in room="+r.getName()+" is "+botCountInRoom);
		return botCountInRoom;
	}
	public static  String getBotId()
	{		
		Set<String> idSet=botAccountMap.keySet();
		String botId=null;
		ArrayList<String> randomId=new ArrayList<String>();
		for(String id:idSet)
		{
			if(botAccountMap.get(id).getStatus())
			{
				if(! pastBotNames.contains(id))
				{
					randomId.add(id);
				}
			}
		}
		
		Random rand=new Random();
		if(randomId.size() > 0)
		{
			botId=randomId.get(rand.nextInt(randomId.size()));
			botAccountMap.get(botId).setStatus(false);
			return botId;
		}
		else
		{
			//no more BOT ID left,all BOT Id have been assigned.
			return null;
		}	
	}	
	public static String getSHAofPass(String botId)
	{
		String password=null;
		MessageDigest m;
		try 
		{
			m = MessageDigest.getInstance("SHA-256");
			byte[] data = botId.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			password = String.format("%1$032X", i);
			password = password.toLowerCase();
			//System.out.println("\n md5 "+ password);
		} 
		catch (NoSuchAlgorithmException e) 
		{ 
			e.printStackTrace();
		}
		return password;
	}	
	public static synchronized void botCountOnLogout(String roomName,BasicClient pokerBot)
	{	
		if(roomBotDetails.containsKey(roomName))
		{
			//System.out.println("List before: "+roomBotDetails.get(roomName));
			roomBotDetails.get(roomName).remove(pokerBot);
			//System.out.println("List after: "+roomBotDetails.get(roomName));
			pokerBot=null;
		}
	}
	public static synchronized void botCountOnLogout(String roomName,RummyBot rummyBot)
	{		
		if(roomBotDetails.containsKey(roomName))
		{
			roomBotDetails.get(roomName).remove(rummyBot);
			rummyBot=null;
		}
	}
	private String getCfgPath() 
	{
		if(isPokerBot())
			return System.getProperty("user.dir") + "/config/sfs-config.xml";
		else
			return System.getProperty("user.dir") + "/config/sfs-config2.xml";
			
	}	 	
	public static void updatePastBotNames(String botId)
	{
		if(pastBotNames.size()== MAX_PAST_BOTNAMES)
		{
			pastBotNames.remove(0);
		}
		pastBotNames.add(botId);
	}
	public void addBotClient(Room r, String botRank,String chipType)
	{
		String botClientId= getBotId();	
		updatePastBotNames(botClientId);
		if(botClientId!=null)
		{
			System.out.println("BOT="+botClientId+" [ Bot-RAnk="+botRank+" ] to Join Room Name="+r.getName());
			if(isPokerBot())
			{
				new BasicClient(botClientId, botAccountMap.get(botClientId).getPassword(), r.getName(),botRank,chipType);
			}
			else
			{
				new RummyBot(botClientId, botAccountMap.get(botClientId).getPassword(), r.getName(),botRank,chipType);				
			}
		}			
	}
	private void clearAllOnDisconnection()
	{
		troubleshootThreads();
		//System.out.println("Super Bot : on Disconnection");
		//remove Bots from rooms on disconnection
		removeBotsFromRoomsOnDisconnection();
		//clear all stored data of Bots and rooms.
		clearDataOnDisconnection();
		//expicitly try to call Garbage Collector.
		System.gc();
	}
	private void clearDataOnDisconnection()
	{
		 //System.out.println("Super Bot : on Disconnection clear data");
		 if(sfs!=null)
		 {
		    sfs.removeAllEventListeners();
		    if(sfs.isConnected())sfs.disconnect();
		 }
		 if(timer!=null)
		 {//no need to send keep alives when Superbot is disconnected.
			 timer.cancel();
		 }
		 if(frontBotsRoom!=null)
		 {
			frontBotsRoom.clear();
		 }
		 if(roomBotDetails!=null)
		 {
			roomBotDetails.clear();
		 }
		 if(roomsAllowedForBots!=null)
		 {
			roomsAllowedForBots.clear();
		 }
		 if(roomChipType!=null)
		 {
			roomChipType.clear();
		 }		
		 if(pastBotNames!=null)
		 {
			pastBotNames.clear();
		 }
		 //no need to clear Bot account map, as it is initialized only once when class is loaded.
	}
	private void removeBotsFromRoomsOnDisconnection()
	{
		Set<String> roomNames=roomBotDetails.keySet();
		for(String roomName :roomNames)
		{
			if(isPokerBot())
			{	 
//				List<BasicClient> pokerBotList=roomBotDetails.get(roomName);
//				Iterator<BasicClient> iterator=pokerBotList.iterator();
//				while(iterator.hasNext())
//				{
//					BasicClient pokerBot=iterator.next();
//					pokerBot.clearAllMemory();
//				}
				List<BasicClient> pokerBotList=roomBotDetails.get(roomName);
				Object[] pokerBotListArray=pokerBotList.toArray();
				for(int i=0; i<pokerBotListArray.length ;i++)
				{
					BasicClient pokerBot = (BasicClient)pokerBotListArray[i];
					pokerBot.clearAllMemory();
				}
				//avoiding loop traversal, this isn't fail safe.
//				for(BasicClient pokerBot:pokerBotList)
//				{
//					if(pokerBot!=null)
//						pokerBot.botLogOut();
//				}
			}
			else
			{
				List<RummyBot> rummyBotList=roomBotDetails.get(roomName);
				Object[] rummyBotListArray=rummyBotList.toArray();
				for(int i=0;i<rummyBotListArray.length; i++)
				{
					RummyBot rummyBot = (RummyBot)rummyBotListArray[i];
					rummyBot.clearAllMemory();
				}
//				Iterator<RummyBot> iterator=rummyBotList.iterator();
//				while(iterator.hasNext())
//				{
//					RummyBot rummyBot=iterator.next();
//					rummyBot.clearAllMemory();
//				}
				//avoiding loop traversal, this isn't fail safe.
//				for(RummyBot rummyBot:rummyBotList)
//				{
//					if(rummyBot!=null)
//						rummyBot.botLogOut();
//				}
			}
		}		
	}
	private void clearAllMemory()
	{
		//System.out.println("Super Bot : clearAllMemory");
		//remove all listeners
		if(sfs!=null)
		{
		   sfs.removeAllEventListeners();
		  //sfs.disconnect();
		}
		//stop threads
		if(runner!=null)
		{
			runner.interrupt();
			runner=null;
		}
		//defreference all references
		 sfs=null;
		 alive=null;
		 //Stop timers and clear all lists and maps.
		 if(timer!=null)
		 {
			timer.cancel();
			timer=null;
		 }
		 if(reconnectTimer!=null)
		 {
			reconnectTimer.cancel();
			reconnectTimer=null;
		 }		 
		 if(botAccountMap!=null)
		 {
			botAccountMap.clear();
			botAccountMap =null;
		 }
		 if(frontBotsRoom!=null)
		 {
			frontBotsRoom.clear();
			frontBotsRoom =null;
		 }
		 if(roomBotDetails!=null)
		 {
			roomBotDetails.clear();
			roomBotDetails =null;
		 }
		 if(roomsAllowedForBots!=null)
		 {
			roomsAllowedForBots.clear();
			roomsAllowedForBots =null;
		 }
		 if(roomChipType!=null)
		 {
			roomChipType.clear();
			roomChipType =null;
		 }		
		 if(pastBotNames!=null)
		 {
			pastBotNames.clear();
			pastBotNames =null;
		 }		 		 
		 if(reader!=null)
		 {
			 try 
			 {
				reader.close();
			 } 
			 catch (IOException e) 
			 {
				// TODO Auto-generated catch block
			   	e.printStackTrace();
			 }
			 reader=null;
		 }
		 botIdFile=null;
		 //set the runAllowed flag to false
		 runAllowed=false;
		//expicitly try to call Garbage Collector.
		 System.gc();
		 //System.out.println("Super Bot : clearAllMemory successful");
	}
	private void sendExtensionRequest(String command, ISFSObject data, Room room)
	{
		if(sfs != null)
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
		}
	}
	private void troubleshootThreads()
	{
		System.out.println("Current Thread count="+Thread.currentThread().getThreadGroup().activeCount());
		System.out.println("Current Thread Group count="+threadPool.activeGroupCount());
		threadPool.list();
		Thread.currentThread().getThreadGroup().list();
	}
	private void requestRoomsForFrontbots()
	{
		ISFSObject sfso= new SFSObject();
		if(!isPokerBot())
		{
			sfso.putInt("Id", RUMMY_FRONT_BOT_CONFIGID);
		}
		else
		{
			sfso.putInt("Id", POKER_FRONT_BOT_CONFIGID);
		}
		sfso.putBool("watch",false);
		if(sfs != null)sfs.send(new ExtensionRequest("game.joinRoom",sfso,null)); 
	}
	public static void main(String[] args)
	{
		ThreadGroup mainPool=new ThreadGroup("threadPool");
		BotSpawner superBot=new BotSpawner(mainPool);
		Thread t=new Thread(superBot,superBotId);
 	}
 
}
