package BotLogin;

/**
*
* @author  vaibhav
*/
import java.util.Map;

import javax.swing.JOptionPane;

import com.smartfoxserver.v2.exceptions.SFSException;

import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;

public class SFSEventHandler implements IEventListener {

	public RequestIniatiator requestIniatiatorObj = null;
	public ServerResponseHandler svrResponseHandlerObj = null;
	public static volatile boolean hasDisconnected = false;
	
	public SFSEventHandler(RequestIniatiator requestIniatiatorObj) {
		this.requestIniatiatorObj = requestIniatiatorObj;
		svrResponseHandlerObj = new ServerResponseHandler(requestIniatiatorObj);
	}
	/**({@link #dispatch(BaseEvent event)}
	 * : Handles response sent from server
	 * @author vaibhav
	 * */
	@Override
	public void dispatch(BaseEvent event) throws SFSException
	{
		if(event.getType().equals(SFSEvent.CONFIG_LOAD_SUCCESS))
		{
			System.out.println("Config load success");
		}
		else if(event.getType().equals(SFSEvent.CONFIG_LOAD_FAILURE))
		{
			System.out.println("Config load failed");
		}
		else if(event.getType().equals(SFSEvent.CONNECTION))
		{
			System.out.println("CONNECTION*****************************************");
			requestIniatiatorObj.stopReconnectionTimer();
			requestIniatiatorObj.sendLoginRequest();
		}
		else if(event.getType().equals(SFSEvent.CONNECTION_LOST))
		{
			hasDisconnected = true;
			requestIniatiatorObj.clearGameRoomsOnDisconnection();
			requestIniatiatorObj.startReconnectionTimer();
			
			
//			if(command == JOptionPane.YES_OPTION || command == JOptionPane.OK_OPTION)
//			{
//				
//			}
			System.out.println("CONNECTION_LOST");
		}
		else if(event.getType().equals(SFSEvent.CONNECTION_RESUME))
		{
			JOptionPane.showMessageDialog(null, "Reconnected Successfully to server");
		}
		else if(event.getType().equals(SFSEvent.LOGOUT))
		{
			System.out.println("LOGOUT***************");
			requestIniatiatorObj.setLoggedinSucessfully(false);
			requestIniatiatorObj.setHasJoinedRoom(false);
			System.out.println("LOGOUT");
		}
		else if(event.getType().equals(SFSEvent.LOGIN))
		{
			System.out.println("LOGIN*********************************");
			requestIniatiatorObj.startSendingKeepAlive();
			requestIniatiatorObj.setLoggedinSucessfully(true);
			if(requestIniatiatorObj.getLoginnum() == 1)
			{
				requestIniatiatorObj.openLobbyClient();
				if(hasDisconnected)
				{
					requestIniatiatorObj.joinRoomRequestAfterReconnection();
					hasDisconnected = false;
				}
			}
			else
			{
				//requestIniatiatorObj.joinRoom(requestIniatiatorObj.getRoomName());
			}
			//requestIniatiatorObj.joinRoom("Lobby");
		}
		else if (event.getType().equals(SFSEvent.LOGIN_ERROR))
		{
			requestIniatiatorObj.setLoggedinSucessfully(false);
			System.out.println("LOGIN_ERROR");
			JOptionPane.showMessageDialog(null,"Incorrect Username or password");
			System.exit(0);
		}
		else if (event.getType().equals(SFSEvent.ROOM_JOIN))
		{
			//requestIniatiatorObj.setHasJoinedRoom(true);
			//requestIniatiatorObj.prepareChatClient();
			System.out.println("ROOM_JOIN******************");
		}
		else if (event.getType().equals(SFSEvent.ROOM_JOIN_ERROR))
		{
			System.out.println("ROOM_JOIN_ERROR");
		}
		else if (event.getType().equals(SFSEvent.EXTENSION_RESPONSE))
		{
			System.out.println("EXTENSION_RESPONSE");
			svrResponseHandlerObj.onExtension((SFSEvent)event);
		}
	}
	
	

}
