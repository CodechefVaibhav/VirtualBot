package sfs2x.client.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;

import BotLogin.LoggedInBots;
import BotLogin.RequestIniatiator;
import NewClient.CustomizedButton;
import NewClient.MyMainClient;
import NewClient.NewJInternalFrame;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;

import common.Card;
import common.Converter;
import db.CpUser;
import db.dao.CpUserDAO;

import rummy.Points;
import rummy.PointsInfo;
import rummy.WinnerInfo;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.example.RummyBot.ClientKeepAlive;
import sfs2x.client.example.RummyBot.SFSEventHandler;
import sfs2x.client.requests.LeaveRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.LogoutRequest;
import test.iteratorTest;

public class ChildRummyBot extends RummyBot {
	
	ChildEventHandler iEventListenerObj = null;
	RequestIniatiator reqinit = null;
	Timer clientTimer = null;
	
	//String userName = null;
	//String password = null;
	//String roomName = null;
	String botRank = null;
	String chipType = null;
	ChildClientKeepAlive clientKeepAlive = null;
	
	public ChildRummyBot(String userName, String password, String roomName, String botRank, String chipType,RequestIniatiator reqinit)
	{
		super(userName, password,roomName,botRank,chipType);
		
		this.reqinit = reqinit;
		this.roomToJoin = roomName;
		this.botRank = botRank;
		this.chipType = chipType;
		RequestIniatiator.loggedInBotsList.add((new LoggedInBots(userName,password,roomName,botRank,chipType)));
	}
	
	@Override
	public void init()
	{
		sfs = new SmartFox();
		iEventListenerObj = new ChildEventHandler();
		sfs.addEventListener(SFSEvent.CONNECTION, iEventListenerObj);
		sfs.addEventListener(SFSEvent.CONNECTION_LOST, iEventListenerObj);
		sfs.addEventListener(SFSEvent.LOGIN, iEventListenerObj);
		sfs.addEventListener(SFSEvent.ROOM_JOIN, iEventListenerObj);
		sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, iEventListenerObj);	
		sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, iEventListenerObj);
		sfs.addEventListener(SFSEvent.LOGIN_ERROR, iEventListenerObj);
		sfs.addEventListener(SFSEvent.LOGOUT, iEventListenerObj);
		
		sfs.loadConfig(getCfgPath(), true);
	}
	
	@Override
	public String getCfgPath()
	{
		return "./config/sfs-config2.xml";
	}
	
	public void onExtension(SFSEvent evt)
	{
		Map params = evt.getArguments();
		ISFSObject data = (ISFSObject)params.get("params");	
		
		if (params.get("cmd").equals("game.chat"))
		{
			String msg = data.getUtfString("msg");
			NewJInternalFrame tempInternalFrame = null;
			if(!reqinit.botByFrameMap.isEmpty())
				tempInternalFrame = (NewJInternalFrame)reqinit.botByFrameMap.get(new Integer(this.myPlayerId));
			if(tempInternalFrame!=null)
				tempInternalFrame.updateString(msg);		
		}
		if (params.get("cmd").equals("game.seatinfo"))
		{
			if(data==null)
			{
				System.out.println("SFSObject was found Null on SEATINFO response. Bot="+userName+" doing LOGOUT.");
				sendBotLogOutRequest();
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
			//isValueGame=data.getBool("valueGame");
			gameType=data.getUtfString("gameType");
			pointsVariant=data.getUtfString("pointsVariant");
			pointMultiplier=data.getFloat("multiplier");
			maxPlayers=data.getInt("players");
			minPlayers=data.getInt("minPlayers");
			//resp.data.amount=sfso.getUtfString("amount");
			//chipType =sfso.getUtfString("chipType");
			//reqinit.sendClientReady();
			
			if(!reqinit.botByFrameMap.containsKey(myPlayerId))
			{
				Room roomObj = sfs.getRoomByName(roomToJoin);
				CpUser cpUserObj = new CpUserDAO().findById(myPlayerId);
				String botName = cpUserObj.getUserName();
				mObj = MyMainClient.getInstance();
				NewJInternalFrame internalTempObj  = mObj.createInternalFrame(this.gameRoom.getName(),botName, myPlayerId, roomObj, this);
				reqinit.botByFrameMap.put(new Integer(this.myPlayerId), internalTempObj);
			}
			
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
				sendBotLogOutRequest();  // bot should do Logout on game end.
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
				sendBotLogOutRequest();
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
				sendBotLogOutRequest();
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
				sendBotLogOutRequest();
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
		else if (params.get("cmd").equals("game.Message"))
		{
			Collection<String> dealerChat = data.getUtfStringArray("type");
			String[] dataArray = ((List<String>)dealerChat).toArray(new String[dealerChat.size()]);
			if(dataArray[0].equalsIgnoreCase("DC"))
			{
				NewJInternalFrame tempInternalFrame = null;
				if(!reqinit.botByFrameMap.isEmpty())
					tempInternalFrame = (NewJInternalFrame)reqinit.botByFrameMap.get(new Integer(this.myPlayerId));
				if(tempInternalFrame!=null)
					tempInternalFrame.updateDealerChat(dataArray[1]);
					
				
				//mObj.getInternalFrame().updateDealerChat(dataArray[1]);
			}
		}
		else if(params.get("cmd").equals("game.rowclickdata"))
		{
			String roomnamebyplayoptionsstr = data.getUtfString("roomnamebyplayoptionsstr");
			String playersnames = data.getUtfString("playersnames");
			if((roomnamebyplayoptionsstr!=null && roomnamebyplayoptionsstr!="") && (playersnames!=null && playersnames!=""))
				reqinit.rowClickResponseSentFromServer(roomnamebyplayoptionsstr, playersnames);
		}
		else if(params.get("cmd").equals("game.keepAlive"))
		{
			System.out.println("keppALIVE@@@@@@@@@@@@@@@@@@@@@@@ : bot");
		}
	}
	
	 class ChildEventHandler extends SFSEventHandler
	 {

		@Override
		public void dispatch(BaseEvent event) throws SFSException
		{
			if(event.getType().equals(SFSEvent.LOGIN))
			{
				System.out.println("LOGIN FOUND*****************************");
				clientTimer= new Timer();
		    	clientKeepAlive=new ChildClientKeepAlive();
			    clientTimer.scheduleAtFixedRate(clientKeepAlive,1,KEEP_ALIVE_INTERVAL);
			    
			    if(roomToJoin !=null && !roomToJoin.isEmpty())
			    	reqinit.setRoomObj(sfs.getRoomByName(roomToJoin));
			    	joinRoom(roomToJoin);
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
		    		//clearGameRoomsOnDisconnection();
		    		//closeClient();
		    		clearAllMemory();
		    		
		    		//updateBotByFrameMap();
		    		    		 
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
			else if (event.getType().equals(SFSEvent.CONNECTION))
			{
				System.out.println("#####################Username "+userName);
				System.out.println("#####################Password "+password);
				System.out.println("CONNECTION*********************CHILD");
				System.out.println("Bot="+userName+" connected successfully. Now doing Login.");
		    	// Login in current zone
				String zone = sfs.getCurrentZone();
				sfs.send(new LoginRequest(userName, password, sfs.getCurrentZone()));	
			}
			else if(event.getType().equals(SFSEvent.LOGOUT))
			{
				System.out.println("Sending command to kill all related timers");
				botLogOut();
			}
			else if(event.getType().equals(SFSEvent.CONNECTION_RESUME))
			{
				System.out.println("CONNECTION RESUME : "+userName);
			}
		    else if (event.getType().equals(SFSEvent.LOGIN))
		    {	
		    	System.out.println("Bot="+userName + " Logged in successfully.");
		    	//start keep alive timer
		    	clientTimer= new Timer();
		    	clientKeepAlive=new ChildClientKeepAlive();
			    clientTimer.scheduleAtFixedRate(clientKeepAlive,1,KEEP_ALIVE_INTERVAL);
			  
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
		   else if (event.getType().equals(SFSEvent.EXTENSION_RESPONSE))
		   {	
		    	try
		    	{
		    		onExtension((SFSEvent) event);
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
		    }	
			
		}
		
	 }
	 
	 @Override
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
			
			clearAllMemory();
		
		}
	 }
	 
	 /**({@link #sendBotLogOut()}
		 *	sends log out request to server
		 * */
		public boolean sendBotLogOut()
		{
			boolean flag = false;
			try{
				if(sfs.isConnected())
				{
					sfs.send(new LogoutRequest());
					flag = true;
				}
			}
			catch(Exception e)
			{
				flag = false;
				e.printStackTrace();
			}
			
			return flag;
		}
		
		/**({@link #sendLeaveRoomRequest()}
		 *	sends leave room request to server
		 * */
		public boolean sendLeaveRoomRequest()
		{
			boolean flag = false;
			try{
			LeaveRoomRequest request = new LeaveRoomRequest();
			sfs.send(request);
			flag = true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				flag = false;
			}
			
			return flag;
		}
	 
	 public void closeClient()
		{
			if(reqinit.botByFrameMap.containsKey(this.myPlayerId))
			{
				try{
				NewJInternalFrame frameTempObj = reqinit.botByFrameMap.get(this.myPlayerId);
				if(frameTempObj!=null)
				{
					frameTempObj.dispose();
					System.out.println("Client closed successfully");
				}
				
				//removing object(id and frameObject pair) from map
				reqinit.botByFrameMap.remove(this.myPlayerId);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				//botLogOut();
			}
			
		}
	 
	 class ChildClientKeepAlive extends ClientKeepAlive
	 {
		 @Override
		 public void run()
		 {
				SFSObject data=new SFSObject();	 
				System.out.println("KEEPALIVE SENDING******************CHILD");
				sendExtensionRequest("game.keepAlive",data,null);
		 }
	 }
	 
	 public void clearMinimizingPanel()
	 {
		 CopyOnWriteArrayList<CustomizedButton> buttonTemp = mObj.getButtonObj();
			if(buttonTemp!=null)
			{
				Iterator itr = buttonTemp.iterator();
				while(itr.hasNext())
				{
					CustomizedButton btn = (CustomizedButton)itr.next();
					if(btn.getText().trim().equalsIgnoreCase(roomToJoin.trim()))
					{
						mObj.getJPanelObj().removeButtonByName(btn);
						mObj.getJPanelObj().revalidate();
						buttonTemp.remove(btn);
					}
				}
			}
			
	 }
	 
	
	 /**({@link #sendBotOutRequest(int configId)}
		 * method is called when chat client window is closed. Performs five actions.
		 * a) sends leave room request
		 * b) sends log out request
		 * c) disposes chat client window
		 * d) accordingly manages botbyframe map
		 * e) call to kill lively threads related with requested playerID
		 * f) clear all memory will be called when logout event will be fired. 
		 * */
		public void sendBotLogOutRequest()
		{
			clearMinimizingPanel();
			
			if(sendLeaveRoomRequest())
			{
				System.out.println("Leave Room Request sent successfully");
			}
			else
			{
				System.out.println("Leave Room Request sending failed");
			}
			
			if(sendBotLogOut())
			{
				System.out.println("Bot Log out Request Sent Successfully");
			}
			else
			{
				System.out.println("Bot Log out Request Sending Failed");
			}
			
			//Sending close client command
			closeClient();
			
			//botLogOut();
			//updateBotByFrameMap();
			
			
		}
		public void updateBotByFrameMap()
		{
			if(reqinit.botByFrameMap.containsKey(this.myPlayerId))
			{
				try{
				NewJInternalFrame frameTempObj = reqinit.botByFrameMap.get(this.myPlayerId);
				if(frameTempObj!=null)
				{
					frameTempObj.dispose();
					System.out.println("Client closed successfully");
				}
				
				//removing object(id and frameObject pair) from map
				reqinit.botByFrameMap.remove(this.myPlayerId);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
	
	
}
