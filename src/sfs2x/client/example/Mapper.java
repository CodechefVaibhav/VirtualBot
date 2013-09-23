package sfs2x.client.example;
import java.util.*;
public class Mapper {
	
	public static HashMap<String,Integer> suitMap=new HashMap<String, Integer>();
	public static HashMap<String,Integer> faceMap=new HashMap<String, Integer>();
	
	static
	{
		initMaps();
	}
	public static void initMaps()
	{
		//initialising Face text and values.
		faceMap.put("2", 2);
		faceMap.put("3", 3);
		faceMap.put("4", 4);
		faceMap.put("5", 5);
		faceMap.put("6", 6);
		faceMap.put("7", 7);
		faceMap.put("8", 8);
		faceMap.put("9", 9);
		faceMap.put("10",10);
		faceMap.put("j", 11);
		faceMap.put("q", 12);
		faceMap.put("k", 13);
		faceMap.put("1", 14);
		faceMap.put("0", 21);//paperJoker
		
		//initialising Suit text and values.
		suitMap.put("c", 0);
		suitMap.put("d", 1);
		suitMap.put("h", 2);
		suitMap.put("s", 3);
	}
}
