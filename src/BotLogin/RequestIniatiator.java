package BotLogin;

/**
*
* @author  vaibhav
*/
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
	import java.util.Iterator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;

import Lobby.PopOutData;
import Lobby.RoomListPopUp;
import Lobby.database;
import NewClient.CustomizedButton;
import NewClient.MyMainClient;
import NewClient.NewJInternalFrame;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import common.Converter;

import db.CpUser;
import db.dao.CpUserDAO;

import poker.Seat;
import poker.SeatInfo;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.example.ChildRummyBot;
import sfs2x.client.example.RummyBot;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LeaveRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.LogoutRequest;
import sun.misc.Cleaner;

public class RequestIniatiator {

	private RoomListPopUp roomListPopUpObj =null;
	private database dbObj = null;

	/** loginnum is used while logging-in
	 * 1 is used when you login as admin else
	 * 2 is used for bot login
	 * */
	private int loginnum =0;
	private final int KEEP_ALIVE_INTERVAL=5*1000;
	private int myPlayerId = 0;
	private int requestedSeatId;
	private int buyInLow =0;
	private int buyInHigh =0;
	private int maxPlayers =0;
	private int minPlayers =0;
	
	private double dummyChips = 0;	
	private double chips;
	
	private float pointMultiplier = 0;
	
	private String chipType = null;
	private String userName = null;
	private String password = null;
	private String roomName = null;
	private static String LOBBY_ROOM_NAME = "Lobby";
	private String gameType = null;
	private String pointsVariant =null;
	
	private boolean reloadChips=false;
	private boolean isLoggedinSucessfully = false;
	private boolean hasJoinedRoom = false;
	private boolean takenSeat = false;
	private boolean isValueGame = false;
	private boolean accountInfoReceived = false;
	
	private Room roomObj = null;
	
	private Timer clientTimer;
	
	private ClientKeepAlive clientKeepAlive;
	
	public SmartFox sfs = null;
	
	private IEventListener iEventListenerObj = null;
	
	public MyMainClient mObj;
	
	private ReconnectionTimer recon = null;

	public ConcurrentHashMap<Integer, NewJInternalFrame> botByFrameMap = new ConcurrentHashMap<Integer, NewJInternalFrame>();
	public ConcurrentHashMap<Integer, RummyBot> botByRummyBot = new ConcurrentHashMap<Integer, RummyBot>();
	public static volatile ArrayList<LoggedInBots> loggedInBotsList = new ArrayList<LoggedInBots>();
	private SeatInfo seatinfo;
	
	public RequestIniatiator()
	{
		
	}
	
	public void init()
	{
		sfs = new SmartFox();
		iEventListenerObj = new SFSEventHandler(this);
		sfs.addEventListener(SFSEvent.CONNECTION, iEventListenerObj);
		sfs.addEventListener(SFSEvent.CONNECTION_RESUME, iEventListenerObj);
		sfs.addEventListener(SFSEvent.CONNECTION_LOST, iEventListenerObj);
		sfs.addEventListener(SFSEvent.LOGIN, iEventListenerObj);
		sfs.addEventListener(SFSEvent.ROOM_JOIN, iEventListenerObj);
		sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, iEventListenerObj);	
		sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, iEventListenerObj);
		sfs.addEventListener(SFSEvent.LOGIN_ERROR, iEventListenerObj);
		sfs.addEventListener(SFSEvent.LOGOUT, iEventListenerObj);
		
		sfs.loadConfig(getCfgPath(), true);
	}
	
	public void initiateLogin(String userName, String password)
	{
		if(sfs!=null)
		{
			if(sfs.isConnected())sfs.disconnect();
			sfs.removeAllEventListeners();
			sfs = null;
		}
		this.userName = userName;
		//this.password = password;
		
		MessageDigest m;
			
		try 
		{
			m = MessageDigest.getInstance("SHA-256");
			byte[] data = password.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			this.password = String.format("%1$032X", i);
			this.password = this.password.toLowerCase();
			System.out.println(this.password);

			init();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}

	}
	
	private String getCfgPath()
	{
		return "./config/sfs-config2.xml";
	}
	
	public void prepareSeatInfoSentFromServer(ISFSObject data)
	{
		System.out.println("prepare SeatInfo Sent From Server");
		setSeatinfo((SeatInfo) data.getClass("seatinfo"));
		
		boolean grant=false;
		for(int i=0 ; i< seatinfo.getSeats().size() ; i++ )
		{					
			if( seatinfo.getSeats().get(i).seatId == getRequestedSeatId() )
			{							
				if( ( seatinfo.getSeats().get(i).isOccupied()) && ( seatinfo.getSeats().get(i).getPlayerId() ==getMyPlayerId()) )
				{
					grant=true;
					
					break;
				}				 
			}
		}
		if(grant)
		{
			setTakenSeat(true);	
		}
		else
		{
			System.out.println("Seat was not granted to BOT="+userName+" . so Bot doing LOgout.");
			sendBotLogOutRequest();
		}

	}
	/**({@link #sendClientReady()}
	 * sends client ready signal to server when room data is received from
	 * server
	 * */
	public void sendClientReady()
	{
		System.out.println("Sending client ready request***********");
		ISFSObject sfso  = new SFSObject();	
		sfso.putBool("reCon", false);
		sfso.putBool("watch", false);
		sendExtensionRequest("game.clientready", sfso, getRoomObj());		
	}
	
	public void sendLoginRequest()
	{
		String zone = "";
		zone = sfs.getCurrentZone();
		System.out.println("My Zone : "+sfs.getCurrentZone()+"****************");
		System.out.println("My Password : "+password);
		try{
		sfs.send(new LoginRequest(userName,password,sfs.getCurrentZone()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void startReconnectionTimer()
	{
		recon = new  ReconnectionTimer(sfs,this);
	}
	
	public void stopReconnectionTimer()
	{
		if(getRecon() != null)
		{
			getRecon().cancel();
			getRecon().purge();
			recon = null;
		}
	}
	public void openLobbyClient()
	{
		dbObj = database.getDatabaseInstance(this);
//		dbObj.setReqInitObj(this);
//		dbObj.getData();
	}
	public void prepareChatClient(RummyBot rbObj)
	{
		Room room = sfs.getRoomByName(roomName);
		//myPlayerId=sfs.getMySelf().getVariable("PlayerId").getIntValue();
		if(myPlayerId>0)
		{
			if(!botByFrameMap.containsKey(getMyPlayerId()))
			{
				Room roomObj = sfs.getRoomByName(roomName);
				setRoomObj(roomObj);
				CpUser cpUserObj = new CpUserDAO().findById(getMyPlayerId());
				String botName = cpUserObj.getUserName();
				mObj = MyMainClient.getInstance();
				NewJInternalFrame internalTempObj  = mObj.createInternalFrame(roomName,botName, getMyPlayerId(), roomObj, rbObj);
				botByFrameMap.put(new Integer(getMyPlayerId()), internalTempObj);
			}
		}
	}
	
	public void showUserChatSentFromServer(String msg)
	{
		NewJInternalFrame tempInternalFrame = null;
		if(!botByFrameMap.isEmpty())
			tempInternalFrame = (NewJInternalFrame)botByFrameMap.get(new Integer(this.myPlayerId));
		if(tempInternalFrame!=null)tempInternalFrame.updateString(msg);
	}
	
	public void showDealerChatSentFromServer(String[] dealerChat)
	{
		if(dealerChat[0].equalsIgnoreCase("DC"))
		{
			NewJInternalFrame tempInternalFrame = null;
			if(!botByFrameMap.isEmpty())
				tempInternalFrame = (NewJInternalFrame)botByFrameMap.get(new Integer(this.myPlayerId));
			if(tempInternalFrame!=null)tempInternalFrame.updateDealerChat(dealerChat[1]);
			//mObj.getInternalFrame().updateDealerChat(dataArray[1]);
		}
	}
	
	
	/**{@link #sendUserChat(String msg,Room roomObj)}
	 * sends user chat from client to server
	 * */
	public void sendUserChat(String msg,Room roomObj)
	{
		ISFSObject sfso = new SFSObject();
		sfso.putUtfString("msg", msg);
		sendExtensionRequest("game.chat", sfso, roomObj);
	}
	
	
	/**{@link #sendConfigIdForRoomListPopOut(int configId)}
	 * sends keep alive signal. Method is called when user successfully logs in
	 * and is called from {@link #SFSEventHandler} class's Login event is fired}
	 * */
	public void startSendingKeepAlive()
	{
		clientTimer= new Timer();
    	clientKeepAlive=new ClientKeepAlive();
	    clientTimer.scheduleAtFixedRate(clientKeepAlive,1,KEEP_ALIVE_INTERVAL);
	}
	
	public void joinRoom(String roomname)
	{	
		if(isLoggedinSucessfully)
		{
			Room room =sfs.getRoomByName(roomname);
			if(room == null)
			{
				System.out.println("Room Found Null");
				return;
			}
			else
			{
				System.out.println("Joining Room");
			}
			//System.out.println("Bot="+userName+" is joining Room "+roomname);
			
			JoinRoomRequest request = new JoinRoomRequest(roomname);		
			sfs.send(request);	
		}
		

	}
	
	
	/**({@link #sendConfigIdForRoomListPopOut(int configId)}
	 * sends configID to get data for room list popout from
	 * server
	 * @return boolean flag : true if request sent successfully
	 * */
	public boolean sendConfigIdForRoomListPopOut(int configId)
	{
		boolean flag = false;
		try{
			  ISFSObject sfso = new SFSObject();	
			  sfso.putInt("gameConfigId", configId);	
			  sendExtensionRequest("game.rowclickdata",sfso,null);
			  flag = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			flag = false;
		}
	  return flag;
	}
	
	
	/**{@link #rowClickResponseSentFromServer(String roomnamebyplayoptionsstr ,String playersnames)}
	 * handles response sent from server for "game.rowclick" command
	 * 
	 * @param roomnamebyplayoptionsstr: is a string having roomname and a boolean value,
	 * structured as "roomname$boolean,roomname$boolean,roomname$boolean.."
	 * 
	 * @param playersnames : is also a string having names of playing players(seated) in rooms,
	 * structured as "a,b,c#d,a,e..." where abc are players in one room and so on
	 * 
	 * */
	public void rowClickResponseSentFromServer(String roomnamebyplayoptionsstr ,String playersnames)
    {
		ArrayList<PopOutData>finalUserDataArray = new ArrayList<PopOutData>();
		
		String[] roomNameByPlayOptionsPairs = roomnamebyplayoptionsstr.split(",");
    	String[] playerNamesByRoomPairs = playersnames.split("#");
    	
    	
    	
    	if(roomNameByPlayOptionsPairs != null&& playerNamesByRoomPairs != null )
		{
			for(int k=0 ; k<roomNameByPlayOptionsPairs.length ; k++)
			{
				PopOutData roomNameObject = new PopOutData();
				if(roomNameByPlayOptionsPairs[k]!=null && roomNameByPlayOptionsPairs[k]!="" && roomNameByPlayOptionsPairs[k]!=" ")
				{
					String tempString = roomNameByPlayOptionsPairs[k].trim();
					String[] roomNamePairs=tempString.split("\\$");
					if(roomNamePairs.length == 2)
					{
						roomNameObject.setRoomName(roomNamePairs[0]);
						roomNameObject.setPlayOption(Boolean.parseBoolean(roomNamePairs[1]));
						roomNameObject.setPlayerName(null);
						finalUserDataArray.add(roomNameObject);
					}
				}
			}
			
			
			for(int i=0 ; i<finalUserDataArray.size() ; i++)
			{
				PopOutData playerInfoObj= finalUserDataArray.get(i);
				String[] players = playerNamesByRoomPairs[i].split(",");
				List<String>playerName = Arrays.asList(players);
				playerInfoObj.setPlayerName(playerName);
			}
		}
		
		//starts populating
    	renderComponentOfRoomListPopOut(finalUserDataArray);
		
	}
	
	
	/**{@link #renderComponentOfRoomListPopOut(ArrayList<PopOutData> finalUserDataArray)}
	 * responsible for creating roomlist pop and populating roomlist popup.
	 * 
	 * @param finalUserDataArray: which is a generic arraylist accepts PopOutData class
	 * object. Each object corresponds to one room and has three values roomname, boolean
	 * values and names of players in that room(names are in the List Form)
	 * */
	public void renderComponentOfRoomListPopOut(ArrayList<PopOutData> finalUserDataArray)
	{
		Collections.sort((List)finalUserDataArray , new PopOutData());
		
		if(roomListPopUpObj!=null)
        {
            if(roomListPopUpObj.isEnabled())
            {
           	 roomListPopUpObj.dispose();
           	 roomListPopUpObj = new RoomListPopUp(finalUserDataArray.size());
            }
        }
        else
        {
       	 roomListPopUpObj = new RoomListPopUp(finalUserDataArray.size());
        }
		
		for(int i =0; i<finalUserDataArray.size() ; i++)
		{
		
			PopOutData popupObj = finalUserDataArray.get(i);
			roomListPopUpObj.addRecursivePanels(popupObj, i);
			 
		}
		
    	
    }
	
	
	/**({@link #sendBotOutRequest(int configId)}
	 * method is called when chat client window is closed. Performs five actions.
	 * a) sends leave room request
	 * b) sends log out request
	 * c) disposes chat client window
	 * d) accordingly manages botbyframe map
	 * e) call to kill lively threads related with requested playerID 
	 * */
	public void sendBotLogOutRequest()
	{
		if(sendLeaveRoomRequest())
		{
			System.out.println("Leave Room Request sent successfully");
			setHasJoinedRoom(false);
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
		
		if(botByFrameMap.containsKey(getMyPlayerId()))
		{
			try{
			NewJInternalFrame frameTempObj = botByFrameMap.get(getMyPlayerId());
			if(frameTempObj!=null)
			{
				frameTempObj.dispose();
				System.out.println("Client closed successfully");
			}
			
			//removing object(id and frameObject pair) from map
			botByFrameMap.remove(getMyPlayerId());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		killAllTimersAndThreads();
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
	
	
	/**{@linkplain #takeSeat()}
	 * Take seat sent from server*/
	public void initiateTakingSeat()
	{
		//setRequestedSeatId(takeRandomSeat());
		sendAccountInfoRequest();
	}
	
	public void sendTakeSeat(int amt)
	{
		try{
		ISFSObject sfso  = new SFSObject();
		sfso.putInt("seatid",getRequestedSeatId());	
		sfso.putInt("amt",amt);			
		sfso.putInt("playerid",getMyPlayerId());
		System.out.println("Take Seat Sent Successfully");
		sendExtensionRequest("game.takeseat",sfso,getRoomObj());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void prepareTakeSeat()
	{
			
		int amtNeededHigh = buyInHigh - (int)chips;
		if (amtNeededHigh < 0) amtNeededHigh = 0;
		
		int amtNeededLow = buyInLow - (int)chips;
		if (amtNeededLow < 0) amtNeededLow = 0;
		
		int amt = -1;

		if (chips < amtNeededLow) amt = -1;
		else if (chips >= amtNeededLow && chips <= amtNeededHigh) amt = (int)chips;
		else if (chips > amtNeededHigh) amt = amtNeededHigh;
		
		if (amt != -1) {
			System.out.println("Sufficient amount to take seat");
			sendTakeSeat(amt);
		}
		else
		{
			System.out.println("Not enough funds to request for take seat");
			sendBotLogOutRequest();
		}
	}
	public void handleAccountInfoSentFromServer(ISFSObject sfsObj)
	{
		if(accountInfoReceived)
		{
		if(getChipType().equals("real"))
		{
			setChips(sfsObj.getDouble("realChips"));
			dummyChips=chips;
		}
		if(getChipType().equals("Freeroll"))
		{//free roll chips
			setChips(sfsObj.getDouble("Freeroll"));
		}		
		if(getChipType().equals("VIP"))
		{//free roll chips
			setChips(sfsObj.getDouble("vipChips"));
		}	
		if(getChipType().equals("dummy"))
		{//free roll chips
			setChips(sfsObj.getDouble("dummyChips"));
		}	
		
		if(chips < 1000)
		{
			reloadChips=true;
			requestedSeatId= takeRandomSeat();
			sendChipsRelaodRequest();
		}
		else
		{
			reloadChips=false;
			if(requestedSeatId == -1)
			{
				System.out.println("BOT="+userName+" is doing Logout as no more Seats are vacant in the Room="+getRoomName());
				sendBotLogOutRequest();
			}
			else
			{
				prepareTakeSeat();
			}						
		}
		}
		else
		{
			JOptionPane.showMessageDialog(null,"Account Info not received");
		}
		
	}
	
	public void sendChipsRelaodRequest()
	{
		SFSObject sfsob = new SFSObject();			
		System.out.println("Account is insufficient so sending Chip reload request. BOT="+userName);
		sendExtensionRequest("game.reloadchips",sfsob,null); 
	}
	/**
	 * @author vaibhav
	 * @param concurrentSeatLeftObj
	 * Due to ConcurrentThreadModification Exception normal ArrayList
	 * is replaced with CopyOnWriteArrayList which actually copies
	 * data into it before performing operations over data.
	 * */
	private int takeRandomSeat()
	{
		/*
		CopyOnWriteArrayList<Seat> concurrentSeatLeftObj = new CopyOnWriteArrayList<Seat>();
		System.out.println("Bot="+userName+" taking Random Seat.");
		List<Seat> seatLeft=new ArrayList<Seat>();
		if(getSeatinfo()!=null)seatLeft = getSeatinfo().getSeats();
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
		*/
		Random randSeat=new Random();
		//if(seatLeft.size()>0)
		{
			int seatIndex=randSeat.nextInt(getSeatinfo().getSeats().size()); // return some random Seat index from the list of vacant Seats.
			return getSeatinfo().getSeats().get(seatIndex).getSeatId();
			//return seatLeft.get(seatIndex).getSeatId();	
		}
		/*else
		{
			System.out.println("No more vacant Seats Left !!!!");
			return  -1;
		}	*/	 
	}
	
	public void sendAccountInfoRequest()
	{
		System.out.println("Sending account info request to server");
		SFSObject sfsob = new SFSObject();
		sfsob.putUtfString("chipType",getChipType());
		sfsob.putUtfString("roomName",getRoomName());
		sendExtensionRequest("game.account",sfsob,null);
	}
	
	public void killAllTimersAndThreads()
	{
		ClientKeepAlive keepAliveObj = getClientKeepAlive();
		if(keepAliveObj!=null)
		{
			keepAliveObj.cancel();
			keepAliveObj = null;
		}
		
		Timer timerObj = getClientTimer();
		if(timerObj!=null)
		{
			timerObj.cancel();
			timerObj = null;
		}
		
		if(sfs.isConnected())
		{
			sfs.removeAllEventListeners();
			sfs.disconnect();
			sfs = null;
		}
		
		iEventListenerObj=null;
		roomName = null;
	}
	
	public void handleRoomData(ISFSObject sfsObj) {
		System.out.println("Handling Room Data Sent*********");
		buyInLow=sfsObj.getInt("buyInLow");
		buyInHigh=sfsObj.getInt("buyInHigh");
		//isValueGame=sfsObj.getBool("valueGame");
		gameType=sfsObj.getUtfString("gameType");
		//pointsVariant=sfsObj.getUtfString("pointsVariant");
		//pointMultiplier=sfsObj.getFloat("multiplier");
		maxPlayers=sfsObj.getInt("players");
		minPlayers=sfsObj.getInt("minPlayers");
		setChipType(sfsObj.getUtfString("chipType"));
	}
	
	private void sendExtensionRequest(String command, ISFSObject data, Room room)
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
						//botLogOut();
						//clearAllMemory();
					}
				}
			}
	}
	
	public void startBot(String userName, String password)
	{
//		Room roomObj = sfs.getRoomByName(getRoomName());
//		setRoomObj(roomObj);
		String botRank = "mid";
		System.out.println("BOT="+userName+" [ Bot-RAnk="+botRank+" ] to Join Room Name="+getRoomName());
		botByRummyBot.put(getMyPlayerId(),new ChildRummyBot(userName, password,getRoomName(),botRank,getChipType(),this));
		//botByRummyBot.put(getMyPlayerId(),new RummyBot(userName, password,getRoomName(),botRank,getChipType()));
		
	}
	
	public void closeClient()
	{
		if(botByFrameMap.containsKey(getMyPlayerId()))
		{
			try{
			NewJInternalFrame frameTempObj = botByFrameMap.get(getMyPlayerId());
			if(frameTempObj!=null)
			{
				frameTempObj.dispose();
				System.out.println("Client closed successfully");
			}
			
			//removing object(id and frameObject pair) from map
			botByFrameMap.remove(getMyPlayerId());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	 public void clearGameRoomsOnDisconnection()
	 {
		 mObj = MyMainClient.getInstance();
		 
		 CopyOnWriteArrayList<NewJInternalFrame> frameTempObj = mObj.getInternalFramesArrayList();
		 Iterator itr = frameTempObj.iterator();
		 while(itr.hasNext())
		 {
			 NewJInternalFrame tempObj = (NewJInternalFrame)itr.next();
			 tempObj.dispose();
		 }
		 
		 CopyOnWriteArrayList<CustomizedButton> buttonTempObj = mObj.getButtonObj();
		 Iterator btnItr = buttonTempObj.iterator();
		 while(btnItr.hasNext())
		 {
			 CustomizedButton btnTempObj = (CustomizedButton)btnItr.next();
			 mObj.getJPanelObj().removeButtonByName(btnTempObj);
		 }
		 mObj.getJPanelObj().revalidate();
		 botByFrameMap.clear();
	 }
	 
	 
		public void joinRoomRequestAfterReconnection()
		{
			Iterator loggedInBotsItr = loggedInBotsList.iterator();
			while(loggedInBotsItr.hasNext())
			{
				LoggedInBots botObj = (LoggedInBots)loggedInBotsItr.next();
				new ChildRummyBot(botObj.getUserName(), botObj.getPassword(), botObj.getRoomName(), botObj.getBotRank(), botObj.getChipType(),this);
			}
			
			loggedInBotsList.clear();
		}

	
	//Setter Getter methods
	
	public ClientKeepAlive getClientKeepAlive() {
		return clientKeepAlive;
	}

	public void setClientKeepAlive(ClientKeepAlive clientKeepAlive) {
		this.clientKeepAlive = clientKeepAlive;
	}
	
	public Timer getClientTimer() {
		return clientTimer;
	}

	public void setClientTimer(Timer clientTimer) {
		this.clientTimer = clientTimer;
	}
	
	public int getLoginnum() {
		return loginnum;
	}

    /**({@link #setLoginnum(int loginnum)}
     * Sets loginnum, is used while logging-in
	 * 1 is used when you login as admin else
	 * 2 is used for bot login*/
	public void setLoginnum(int loginnum) {
		this.loginnum = loginnum;
	}
	
	public boolean isLoggedinSucessfully() {
		return isLoggedinSucessfully;
	}

	public void setLoggedinSucessfully(boolean isLoggedinSucessfully) {
		this.isLoggedinSucessfully = isLoggedinSucessfully;
	}
	
	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	public int getMyPlayerId() {
		return myPlayerId;
	}

	public void setMyPlayerId(int myPlayerId) {
		this.myPlayerId = myPlayerId;
	}

	public Room getRoomObj() {
		return roomObj;
	}

	public void setRoomObj(Room roomObj) {
		this.roomObj = roomObj;
	}

	public boolean isHasJoinedRoom() {
		return hasJoinedRoom;
	}

	public void setHasJoinedRoom(boolean hasJoinedRoom) {
		this.hasJoinedRoom = hasJoinedRoom;
	}
	
	public boolean isTakenSeat() {
		return takenSeat;
	}

	public void setTakenSeat(boolean takenSeat) {
		this.takenSeat = takenSeat;
	}
	
	public String getChipType() {
		return chipType;
	}

	public void setChipType(String chipType) {
		this.chipType = chipType;
	}
	
	public SeatInfo getSeatinfo() {
		return seatinfo;
	}

	public void setSeatinfo(SeatInfo seatinfo) {
		this.seatinfo = seatinfo;
	}

	public int getRequestedSeatId() {
		return requestedSeatId;
	}

	public void setRequestedSeatId(int requestedSeatId) {
		this.requestedSeatId = 5/*requestedSeatId*/;
	}

	public double getChips() {
		return chips;
	}

	public void setChips(double chips) {
		this.chips = chips;
	}
	
	public int getBuyInLow() {
		return buyInLow;
	}

	public void setBuyInLow(int buyInLow) {
		this.buyInLow = buyInLow;
	}

	public int getBuyInHigh() {
		return buyInHigh;
	}

	public void setBuyInHigh(int buyInHigh) {
		this.buyInHigh = buyInHigh;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public float getPointMultiplier() {
		return pointMultiplier;
	}

	public void setPointMultiplier(float pointMultiplier) {
		this.pointMultiplier = pointMultiplier;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getPointsVariant() {
		return pointsVariant;
	}

	public void setPointsVariant(String pointsVariant) {
		this.pointsVariant = pointsVariant;
	}

	public boolean isValueGame() {
		return isValueGame;
	}

	public void setValueGame(boolean isValueGame) {
		this.isValueGame = isValueGame;
	}
	
	public boolean isAccountInfoReceived() {
		return accountInfoReceived;
	}

	public void setAccountInfoReceived(boolean accountInfoReceived) {
		this.accountInfoReceived = accountInfoReceived;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public ReconnectionTimer getRecon() {
		return recon;
	}

	public void setRecon(ReconnectionTimer recon) {
		this.recon = recon;
	}

	private class ClientKeepAlive extends TimerTask
	{

		@Override
		public void run() {
			SFSObject data=new SFSObject();	 
			sendExtensionRequest("game.keepAlive",data,null);
			
		}
		
	}
	
	

	
	
}
