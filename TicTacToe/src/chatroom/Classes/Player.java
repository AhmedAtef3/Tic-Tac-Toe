/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroom.Classes;

/**
 *
 * @author atef
 */
public class Player {

    private int id;
    private String username;
    private String password;
    private int points;
    private int flag;
    private String nickName = "";

    public Player(String username, String password ) {
        this.username = username;
        this.password = password;
        this.nickName = nickName;

    }
    
    public Player(String username, String nickname, String password) {
        this.username = username;
        this.nickName = nickname;
        this.password = password;
        this.flag = 1;
    }
    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }
    
}