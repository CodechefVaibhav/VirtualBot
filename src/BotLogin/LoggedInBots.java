package BotLogin;

public class LoggedInBots {

	private String userName = null;
	private String password = null;
	private String roomName = null;
	private String chipType = null;
	private String botRank = null;
	
	public LoggedInBots(String userName, String password, String roomName, String botRank, String chipType)
	{
		this.userName = userName;
		this.password = password;
		this.roomName = roomName;
		this.botRank = botRank;
		this.chipType = chipType;
	}
	
	public String getChipType() {
		return chipType;
	}

	public void setChipType(String chipType) {
		this.chipType = chipType;
	}

	public String getBotRank() {
		return botRank;
	}

	public void setBotRank(String botRank) {
		this.botRank = botRank;
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
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
}
