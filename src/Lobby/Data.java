/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lobby;

/**
 *
 * @author vaibhav
 */
public class Data {
    private String gameName;
    private int buyin_high;
    private int buyin_low;
    private int small_blind;
    private int big_blind;
    private int configId;
    
    public Data()
    {
        
    }
    
    public Data(String gameName, int buyin_high ,int buyin_low ,int small_blind, int big_blind)
    {
        this.gameName = gameName;
        this.buyin_high = buyin_high;
        this.buyin_low = buyin_low;
        this.small_blind = small_blind;
        this.big_blind = big_blind;
    }
    
    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }
    public int getBig_blind() {
        return big_blind;
    }

    public void setBig_blind(int big_blind) {
        this.big_blind = big_blind;
    }

    public int getBuyin_high() {
        return buyin_high;
    }

    public void setBuyin_high(int buyin_high) {
        this.buyin_high = buyin_high;
    }

    public int getBuyin_low() {
        return buyin_low;
    }

    public void setBuyin_low(int buyin_low) {
        this.buyin_low = buyin_low;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getSmall_blind() {
        return small_blind;
    }

    public void setSmall_blind(int small_blind) {
        this.small_blind = small_blind;
    }
}
