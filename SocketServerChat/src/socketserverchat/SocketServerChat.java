/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketserverchat;

import socketserverchat.Classes.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gameDB.DbTask;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import socketserverchat.Classes.GameResponse;
import socketserverchat.Classes.MyMessage;
import socketserverchat.Classes.Room;

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
    private GameResponse gameResponse;
    private static int index;
    private static int roomNumber;
    private String[] cellItem = {"x", "o"};
    private static ArrayList<Room> rooms = new ArrayList<>();

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
                            this.ps.println("myName");
                            this.ps.println(this.userName);
                            System.out.println("current user:" + this.userName);
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
                    rooms.add(new Room(this.userName, str));
                    //player one
                    System.out.println("maybe player1" + str);
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
                } else if (optionFlag.equals("map")) {
                    System.out.println("wutt??");
                    System.out.println(str);

                    Gson gson = new Gson();
                    GameResponse g1 = gson.fromJson(str, GameResponse.class);

                    String[][] stringArr = g1.getArr();
                    gameResponse = new GameResponse(stringArr, playerWon(stringArr), draw(stringArr), g1.getPlayer1(), "");

                    //System.out.println("\n---------------------"+gameResponseJson+"\n---------------------------");
                    sendToPlayers(this.userName, gameResponse);
//                    gameinfoToSelf(gameResponseJson);
//                    if(this.userName.equals(g1.getPlayer1())) {
//                        sendMsg(g1.getPlayer2(), gameResponseJson);
//                    }else if(this.userName.equals(g1.getPlayer2())){
//                        sendMsg(g1.getPlayer1(), gameResponseJson);
//                    }
                    optionFlag = "";
                } else if (str.equals("cell got clicked")) {
                    System.out.println("+==++=");
                    optionFlag = "map";
                } else if (str.equals("help")) {
                    this.ps.println("why");
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
            //if (!ch.userName.equals(this.userName)) {
            players = DbTask.getAll(ch.userName);
            ch.ps.println("update player list");
            ch.ps.println(new Gson().toJson(players));
            System.out.println("sendUpdatedMessageToAll: " + new Gson().toJson(players));
            //}
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

    void sendToPlayers(String userName, GameResponse gameResponse) {
        System.out.println("===========\nbefore the loop\n=========");

        for (int i = 0; i < rooms.size(); i++) {
            System.out.println(rooms.get(i) + "user name: " + userName);
            if (rooms.get(i).getPlayer1().equals(userName) || rooms.get(i).getPlayer2().equals(userName)) {
                gameResponse.setTurn(rooms.get(i).getItem());
                Gson gson = new Gson();
                String gameResponseJson = gson.toJson(gameResponse);
                sendMsg(rooms.get(i).getPlayer2(), gameResponseJson);
                System.out.println("------------------------------\ninside the loop\n------------------------");
                sendMsg(rooms.get(i).getPlayer1(), gameResponseJson);
                if (gameResponse.isGameOver()) {
                    System.out.println("*****************\nis game over ???\n*****************");
                    String winner = gameResponse.getTurn();
                    if (winner.equals("x")) {
                        System.out.println("x is the winner: " + rooms.get(i).getPlayer2());
                        DbTask.updateScore(rooms.get(i).getPlayer2());

                    } else {
                        System.out.println("o is the winner" + rooms.get(i).getPlayer1());
                        DbTask.updateScore(rooms.get(i).getPlayer1());

                    }
                    rooms.remove(i);
                }
            }
        }
    }

    void sendMsg(String username, String gameResponseJson) {
        System.out.println("are you getting called");
        for (ChatHandler ch : clientsArrayList) {
            if (ch.userName.equals(username)) {
                ch.ps.println("update game");
                ch.ps.println(gameResponseJson);
            }

        }

    }

    public void gameinfoToSelf(String gameJson) {
        this.ps.println("update game");
        this.ps.println(gameJson);
    }

public boolean playerWon(String[][] stringArr) {
        boolean won = false;


            //row

            for (int i = 0; i < stringArr.length; i++) {
                if (stringArr[i][0].equals(stringArr[i][1]) &&
                        stringArr[i][0].equals(stringArr[i][2]) &&
                        !stringArr[i][0].isEmpty()) {
                    System.out.println("player:" + stringArr[i][0] + "won");
                    won = true;
                }
            }
            //column
            for (int i = 0; i < stringArr.length; i++) {
                if (stringArr[0][i].equals(stringArr[1][i]) &&
                        stringArr[0][i].equals(stringArr[2][i]) &&
                        !stringArr[0][i].isEmpty()) {
                    System.out.println("player:" + stringArr[i][0] + "won");
                    won = true;

                }
            }
            //diagonal
            if (stringArr[0][0].equals(stringArr[1][1]) &&
                    stringArr[0][0].equals(stringArr[2][2]) && !stringArr[0][0].isEmpty()) {
                System.out.println("player:" + stringArr[0][0] + "won");
                won = true;
            }
            if (stringArr[0][2].equals(stringArr[1][1]) &&
                    stringArr[0][2].equals(stringArr[2][0]) && !stringArr[0][2].isEmpty()) {
                System.out.println("player:" + stringArr[0][0] + "won");
                won = true;

            }

        return won;
    }

    public boolean draw(String stringArr[][]) {

            for (int i = 0; i < stringArr.length; i++) {
                for (int j = 0; j < stringArr[i].length; j++) {
                    if (stringArr[i][j].isEmpty()) {
                        return false;
                    }
                }
            }

        System.out.println("test");
        return true;
    }
    public String getItem(){

        return cellItem[(index++) % (cellItem.length)];
    }

}
