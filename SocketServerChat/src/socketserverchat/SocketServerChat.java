/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketserverchat;

import com.google.gson.Gson;
import gameDB.DbTask;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import socketserverchat.Classes.MyMessage;
import socketserverchat.Classes.Player;

/**
 *
 * @author atef
 */
public class SocketServerChat {

    /**
     * @param args the command line arguments
     */
    ServerSocket serverSocket;

    public SocketServerChat() throws IOException {
        serverSocket = new ServerSocket(5005);
        while (true) {
            Socket s = serverSocket.accept();
            new ChatHandler(s);
        }
    }

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        new SocketServerChat();

    }
}

class ChatHandler extends Thread {

    String userName = "";
    DataInputStream dis;
    PrintStream ps;
    static ArrayList<ChatHandler> clientsArrayList = new ArrayList<>();

    public ChatHandler(Socket s) throws IOException {
        dis = new DataInputStream(s.getInputStream());
        ps = new PrintStream(s.getOutputStream());
        clientsArrayList.add(this);
        start();
    }

    public static boolean checkExistence(String username) {
        System.out.println("size: " + clientsArrayList.size());
        if (clientsArrayList.size() > 0) {
            for (ChatHandler user : clientsArrayList) {
                if (user.userName.equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendSelfMessageWithFlag(String flag, String body) {
        this.ps.println(flag);
        this.ps.println(body);
    }

    public void sendSelfMessage(String body) {
        this.ps.println(body);
    }

    public Player convertJsonToPlayer(String json_string) {
        Gson gson = new Gson();
        Player p = gson.fromJson(json_string, Player.class);
        return p;
    }

    //To update the list of players 

    public void sendAllPlayers(String username, String flag) {
        this.userName = username;
        ArrayList<Player> players = DbTask.getAll(username);
        sendSelfMessageWithFlag(flag, new Gson().toJson(players));
        sendMessageToAll();
    }

    public void sendLoginDataAfterLoginSuccessfully(Player p) {
        if (ChatHandler.checkExistence(p.getUsername())) {
            sendSelfMessage("login failed");
        } else {
            sendAllPlayers(p.getUsername(), "login successfully");
        }
    }

    public void playerLogin(String str, String optionFlag) {
        Player p = convertJsonToPlayer(str);
        if (p != null) {
            Player dataBasePlayer = DbTask.getPerson(p);
//            System.out.println(dataBasePlayer.getUsername() + " Null pointer Exception ");
            if (dataBasePlayer != null) {
                sendLoginDataAfterLoginSuccessfully(dataBasePlayer);
            } else {
                sendSelfMessage("login failed");
            }
        }
    }

    public void run() {
        String optionFlag = "";
        while (true) {
            DbTask.defineConnection();
            String str = null;
            try {
                str = dis.readLine();
                System.out.println("str: " + str);
                if (optionFlag.equals("chat")) {
                    sendMessageToPlayer(str, "request chat");
                    System.out.println(optionFlag);
                    optionFlag = "";
                } else if (str.equals("login")) {
                    optionFlag = "login";
                } else if (optionFlag.equals("login")) {
                    playerLogin(str, optionFlag);
                    optionFlag = "";
                } else if (str.equals("register")) {
                    optionFlag = "register";
                } else if (optionFlag.equals("register")) {
                    String json_string = str;
                    Gson gson = new Gson();
                    Player p = gson.fromJson(json_string, Player.class);
                    if (p != null) {
                        int idNumber = DbTask.register(p);
                        if (idNumber != -1) {
//                            this.ps.println("registered successfully");
//                            this.userName = p.getUsername();
//                            ArrayList<Player> players = DbTask.getAll(idNumber);
//                            ps.println(new Gson().toJson(players));
                            sendAllPlayers(p.getUsername(), "registered successfully");
                        } else {
                            this.ps.println("registration failed");
                        }
                    }
                    System.out.println(optionFlag);
                    optionFlag = "";

                } else if (str.equals("chat")) {
                    System.out.println("ana dkhlt hna el awl");
                    optionFlag = "chat";
                } else if (str.equals("accepted")) {
                    System.out.println("ana dkhlt hna tany");
                    optionFlag = "accepted";
                } else if (optionFlag.equals("accepted")) {
                    sendMessageToPlayer(str, "accept chat");
                    System.out.println(optionFlag);
                    optionFlag = "";
                } else if (str.equals("send message")) {
                    System.out.println("sent sent sent");
                    optionFlag = "sent";
                } else if (optionFlag.equals("sent")) {
                    System.out.println("ana hna");
                    Gson gson = new Gson();
                    MyMessage message = gson.fromJson(str, MyMessage.class);
                    sendTextMessageToPlayer(message);
                    System.out.println(optionFlag);
                    optionFlag = "";
                } else if (str.equals("exit")) {
                    DbTask.updateOffLine(this.userName);
                    clientsArrayList.remove(this);
                    this.stop();
                }

            } catch (IOException ex) {
                Logger.getLogger(ChatHandler.class.getName()).log(Level.SEVERE, null, ex);
                ChatHandler.this.ps.close();
                try {
                    ChatHandler.this.dis.close();
                } catch (IOException ex1) {
                    Logger.getLogger(ChatHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
                this.stop();
                clientsArrayList.remove(this);

            }
        }
    }

    void sendMessageToAll() {
        ArrayList<Player> players;
        for (ChatHandler ch : clientsArrayList) {
            if (!ch.userName.equals(this.userName)) {
                players = DbTask.getAll(ch.userName);
                ch.ps.println("update player list");
                ch.ps.println(new Gson().toJson(players));
                System.out.println("sendUpdatedMessageToAll: " + new Gson().toJson(players));
            }
        }
    }

    void sendMessageToPlayer(String username, String message) {
        for (ChatHandler ch : clientsArrayList) {
            System.out.println(username + ", " + ch.userName);
            if (ch.userName.equals(username)) {
                ch.ps.println(message);
                ch.ps.println(this.userName);
            }
        }
    }

    void sendTextMessageToPlayer(MyMessage sentMessage) {
        for (ChatHandler ch : clientsArrayList) {
            System.out.println(sentMessage.getUsername() + ", " + sentMessage.getMessage() + ", " + ch.userName);
            if (ch.userName.equals(sentMessage.getUsername())) {
                ch.ps.println("text message");
                ch.ps.println(this.userName + " >>> " + sentMessage.getMessage());
                this.ps.println("text message");
                this.ps.println("me >>> " + sentMessage.getMessage());
            }
        }

    }
}
