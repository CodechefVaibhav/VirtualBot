package BotLogin;

/**
*
* @author  vaibhav
*/
import java.util.Collection;
import java.util.List;
import java.util.Map;

import NewClient.NewJInternalFrame;

import com.smartfoxserver.v2.entities.data.ISFSObject;

import sfs2x.client.core.SFSEvent;

public class ServerResponseHandler {

	public RequestIniatiator requestIniatiatorObj = null;
	public ServerResponseHandler(RequestIniatiator requestIniatiatorObj)
	{
		this.requestIniatiatorObj = requestIniatiatorObj;
	}
	public void onExtension(SFSEvent evt)
	{
		Map params = evt.getArguments();
		ISFSObject data = (ISFSObject)params.get("params");	
		System.out.println(params.get("cmd")+" #################### : Command");
		if(data==null)
		{
			System.out.println("data response found null");
			return;
		}
		if (params.get("cmd").equals("game.keepAlive"))
		{
			System.out.println("KeepAlive@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		else if (params.get("cmd").equals("game.seatinfo"))
		{
			if(data == null)
			{
				System.out.println("SeatInfo found null");
				return;
			}
			else
			{
				//String seatInfoStr = data.getUtfString("seatinfo");
				requestIniatiatorObj.prepareSeatInfoSentFromServer(data);
			}
			
			System.out.println("game.seatinfo received@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		else if (params.get("cmd").equals("game.roomdata"))
		{
			//client ready is sent when game.clientready command is sent from client
			requestIniatiatorObj.sendClientReady();
			
			requestIniatiatorObj.handleRoomData(data);
			
			//room data is sent from server when client joins room
			System.out.println("game.roomdata@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		else if (params.get("cmd").equals("game.takeseat"))
		{
			/**game.takeseat command is sent from server when user
			 * sends game.clientready command to server. 
			 * */
			requestIniatiatorObj.initiateTakingSeat();
			System.out.println("game.takeseat@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		else if (params.get("cmd").equals("game.started")) // not needed really
		{
			System.out.println("game.started@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		else if (params.get("cmd").equals("game.ended")) // not needed really
		{
			System.out.println("game.ended");
		}
		else if (params.get("cmd").equals("game.handcard")) // not needed really
		{
			
		}
		else if (params.get("cmd").equals("game.cutjoker")) // not needed really
		{
			
		}
		else if (params.get("cmd").equals("game.discard")) // not needed really			
		{
			
		}
		else if (params.get("cmd").equals("game.rummyuserturn")) 
		{
			
		}
		else if (params.get("cmd").equals("game.pickedcard")) // not needed really
		{
			
		}
		else if (params.get("cmd").equals("game.useraction"))  // not needed, just logging
		{
			
		}
		else if (params.get("cmd").equals("game.points")) // not needed really
		{
			
		}
		else if (params.get("cmd").equals("game.meld")) // not needed really
		{
			
		}
		else if (params.get("cmd").equals("game.PlayerAway")) // not needed really
		{
			System.out.println("game.PlayerAway");
		}
		else if (params.get("cmd").equals("game.winner")) 
		{
			
		}
		else if (params.get("cmd").equals("game.chat"))
		{
			System.out.println("Chat@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			String msg = data.getUtfString("msg");
			requestIniatiatorObj.showUserChatSentFromServer(msg);
		}
		else if (params.get("cmd").equals("game.Message"))
		{
			System.out.println("Dealer Chat@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			Collection<String> dealerChat = data.getUtfStringArray("type");
			if(dealerChat.size()>0)
			{
				String[] dataArray = ((List<String>)dealerChat).toArray(new String[dealerChat.size()]);
				if(dataArray.length>0)
				{
					requestIniatiatorObj.showDealerChatSentFromServer(dataArray);
				}
			}
			
		}
		else if(params.get("cmd").equals("game.account"))
		{
			requestIniatiatorObj.setAccountInfoReceived(true);
			System.out.println("game.account@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			requestIniatiatorObj.handleAccountInfoSentFromServer(data);	
		}
		else if(params.get("cmd").equals("game.rowclickdata"))
		{
			String roomnamebyplayoptionsstr = data.getUtfString("roomnamebyplayoptionsstr");
			String playersnames = data.getUtfString("playersnames");
			if((roomnamebyplayoptionsstr!=null && roomnamebyplayoptionsstr!="") && (playersnames!=null && playersnames!=""))
				requestIniatiatorObj.rowClickResponseSentFromServer(roomnamebyplayoptionsstr, playersnames);
		}

	}
}
