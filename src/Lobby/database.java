/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lobby;

import BotLogin.RequestIniatiator;
import BotLogin.TableFrame;
import Lobby.MyTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author vaibhav
 */
public class database {
    
	private RequestIniatiator reqInitObj = null;
	private static volatile database dbObj = null;
    public Connection con = null;
    public Statement st = null;
    public ArrayList ar = new ArrayList();
    public Data dtObj;
    //public Vector<String> vect;
    public Vector vect;
    public Vector<Data> parentVector = new Vector<Data>();
    
    private database(RequestIniatiator reqInitObj) 
    {
        try{
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cardplay","root","");
        st = con.createStatement();
        setReqInitObj(reqInitObj);
        getData();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static database getDatabaseInstance(RequestIniatiator reqInitObj)
    {
    	if(dbObj==null)
    	{
    		dbObj = new database(reqInitObj);
    	}
    	
    	return dbObj;
    }
    
    public void getData()
    {
        try{
        String qry = "SELECT DISTINCT "+ 
                      "cp_game_config.id,"+
                      "cp_game_config.game_variant,"+
                      "cp_game_config.chip_type,"+
                      "cp_game_config.num_game_tables,"+
                      "cp_game_config.max_players,"+
                      "cp_game_config.buyin_high,"+
                      "cp_game_config.buyin_low,"+
                      "cp_game_config.comission_rate,"+				  
                      "cp_game_config.is_dynamic,"+
                      "cp_game_config.small_blind,"+
                      "cp_game_config.big_blind,"+
                      "cp_game_config.user_count,"+
                      "cp_game_config.start_date,"+
                      "cp_game_config.remarks,"+
                      "cp_game_config.rank,"+
                      "cp_game_config.type,"+
                      "cp_game_config.buyin_text "+
                      "FROM cp_game_config where cp_game_config.chip_type = \"dummy\" ";
        ResultSet rs = st.executeQuery(qry);
        
       
//        Pattern pattern = Pattern.compile("21");
        
        while(rs.next())
        {
            //Vector myVect = getMyVector();
            Data myDataObj = getDataObj();
//            myVect.addElement(new String(rs.getString("remarks")));
//            myVect.addElement(new Integer(rs.getInt("buyin_high")));
//            myVect.addElement(new Integer(rs.getInt("buyin_low")));
//            myVect.addElement(new Integer(rs.getInt("small_blind"))); 
//            myVect.addElement(new Integer(rs.getInt("big_blind"))); 

//            String remarksString = rs.getString("remarks");
//            Matcher matcher = pattern.matcher(remarksString);
//            if (matcher.find()) {
//                System.out.println("21 card game found"); //prints /{item}/
//            } 
//            else { 
	            if(rs.getString("remarks")!=null && rs.getInt("num_game_tables")>1)
	        	{
	            	myDataObj.setGameName(new String(rs.getString("remarks")));
	            	myDataObj.setBuyin_high(new Integer(rs.getInt("buyin_high")));
	            	myDataObj.setBuyin_low(new Integer(rs.getInt("buyin_low")));
	            	myDataObj.setSmall_blind(new Integer(rs.getInt("small_blind"))); 
	            	myDataObj.setBig_blind(new Integer(rs.getInt("big_blind"))); 
	            	myDataObj.setConfigId(new Integer(rs.getInt("id")));
	            	parentVector.add(myDataObj);
	        	}
//            }
//            System.out.println(gameName);
//            String gameName = rs.getString("remarks");
//            int buyin_high = rs.getInt("buyin_high");
//            int buyin_low = rs.getInt("buyin_low");
//            int small_blind = rs.getInt("small_blind");
//            int big_blind = rs.getInt("big_blind");
//            String buyinHigh = Integer.toString(buyin_high);
//            String buyInLow = Integer.toString(buyin_low);
//            String smallBlind = Integer.toString(small_blind);
//            String bigBlind = Integer.toString(big_blind);
//            myVect.addElement(gameName);
//            myVect.addElement(buyinHigh);
//            myVect.addElement(buyInLow);
//            myVect.addElement(smallBlind);
//            myVect.addElement(bigBlind);
        }
        
        prepareTableAndFrame();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void prepareTableAndFrame()
    {
        TableFrame frame = TableFrame.getTabelFrameInstance("Adda52 FreeRoll Games");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
//        Vector<String> columnNames = new Vector<String>();
//        columnNames.addElement("Game Name");
//        columnNames.addElement("Buyin-High");
//        columnNames.addElement("Buyin-Low");
//        columnNames.addElement("Small-Blind");
//        columnNames.addElement("Big-Blind");
        
        MyTableModel mtmObj = MyTableModel.getMyTableModelInstance(parentVector);
        JTable table = MyTableModel.getTableObj();
      
//        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
//        public void valueChanged(ListSelectionEvent event) {
//            System.out.println(table.getValueAt(table.getSelectedRow(), 0).toString());
//        }
//        });
        table.addMouseListener(new DataGridEventHandler(table, reqInitObj));
//        table.addMouseListener(new MouseAdapter() {
//  public void mouseClicked(MouseEvent e) {
//    if (e.getClickCount() == 1) {
//      JTable target = (JTable)e.getSource();
//      target.getSelectedRow();
//      int row = target.getSelectedRow();
//      int column = target.getSelectedColumn();
//      MyTableModel = (MyTableModel)target.getModel().g
//      System.out.println(row+" && "+column);
//    }
//  }
//});
       // table.setModel(new MyTableModel(columnNames));
        //table.setVisible(true);
//        JTable table = new JTable(new MyTableModel(columnNames));
//        MyTableModel mtObj = (MyTableModel)table.getModel();
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
    
	public RequestIniatiator getReqInitObj() {
		return reqInitObj;
	}

	public void setReqInitObj(RequestIniatiator reqInitObj) {
		this.reqInitObj = reqInitObj;
	}
//    public Vector getMyVector()
//    {
//        vect = new Vector();
//        return vect;
//    }
    public Data getDataObj()
    {
        dtObj = new Data();
        return dtObj;
    }
    public static void main(String... w)
    {
        //new database().getData();
    }
    
    
}
