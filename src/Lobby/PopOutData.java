package Lobby;

/**
*
* @author  vaibhav
*/
import java.util.Comparator;
import java.util.List;

public class PopOutData implements Comparator<PopOutData> {
	
	private String roomName = null;
	private boolean playOption = false;
	private List<String> playerName;
	
	
	public List<String> getPlayerName() {
		return playerName;
	}
	public void setPlayerName(List<String> playerName) {
		this.playerName = playerName;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public boolean isPlayOption() {
		return playOption;
	}
	public void setPlayOption(boolean playOption) {
		this.playOption = playOption;
	}
	
	@Override
	public int compare(PopOutData o1, PopOutData o2) {
		return o1.playerName.size() > o2.playerName.size()? -1 : 
			o1.playerName.size() == o2.playerName.size()?0:1;
		
	}
}
