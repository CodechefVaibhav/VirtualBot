package sfs2x.client.example;

public class MyTestClass extends RummyBot {

	public MyTestClass(String name, String pwd, String roomname,
			String botRank, String chipType) {
		super(name, pwd, roomname, botRank, chipType);
		
	}

	public static void main(String...w)
	{
		new MyTestClass("pada","sitanshu","POINTS4#3485","mid","dummy").myStart();
	}
	
	public void myStart()
	{
		init();
	}
}
