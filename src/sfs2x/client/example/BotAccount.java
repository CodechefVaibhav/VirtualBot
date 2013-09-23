package sfs2x.client.example;
//NOTE : This class is basically used to carry the instances of the BOT Accounts containing information 
//about their User names , password and Status. and will be used in BotSpawner class to keep track of the 
// availability of BOT accounts, count of BOTS in different rooms.

public class BotAccount 
{
	private String username;
	private String password;
	private Boolean status;
	
	BotAccount()
	{		
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Boolean getStatus() {
		return status;
	}
	 
}
