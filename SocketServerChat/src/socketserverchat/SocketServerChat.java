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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import socketserverchat.Classes.GameResponse;
import socketserverchat.Classes.GameResponse;
import socketserverchat.Classes.MyMessage;
import socketserverchat.Classes.MyMessage;
import socketserverchat.Classes.Player;
import socketserverchat.Classes.Room;
import socketserverchat.Classes.Room;

/**
 *
 * @author atef
 */
public class SocketServerChat {

    /**
     * @param args the command line arguments
     */
    private static ServerSocket serverSocket;
    private ChatHandler chatHandler;
    // static ServerGUI myGui ;

    static ArrayList<Player> allPlayers = new ArrayList<>();
    static boolean isUpdatedUser = false;

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public static void stopServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                ChatHandler.pauseAll();
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketServerChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void resumeServerSocket() {
        ChatHandler.resumeAll();
    }

    public SocketServerChat() {
        try {
            serverSocket = new ServerSocket(5005);
        } catch (IOException ex) {
            Logger.getLogger(SocketServerChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (true) {
            if (!serverSocket.isClosed()) {
                Socket s;
                try {
                    s = serverSocket.accept();
                    chatHandler = new ChatHandler(s);
                } catch (IOException ex) {
                    String message = ex.getMessage();
                    System.out.println(message);

                }

            }
        }
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
    private Socket socket;

    public ChatHandler(Socket s) throws IOException {
        dis = new DataInputStream(s.getInputStream());
        ps = new PrintStream(s.getOutputStream());
        socket = s;
        clientsArrayList.add(this);
        start();
    }

    public static boolean checkExistence(String username) {
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
            sendSelfMessageWithFlag("myPoints", String.valueOf(p.getPoints()));
            sendAllPlayers(p.getUsername(), "login successfully");
            SocketServerChat.allPlayers = DbTask.getAll("");
            SocketServerChat.isUpdatedUser = true;
        }
    }

    public void playerLogin(String str, String optionFlag) {
        Player p = convertJsonToPlayer(str);
        if (p != null) {
            Player dataBasePlayer = DbTask.getPerson(p);
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
                if (str != null) {
                    if (optionFlag.equals("chat")) {
                        sendMessageToPlayer(str, "request chat");
                        optionFlag = "";
                    } else if (str.equals("login")) {
                        optionFlag = "login";
                    } else if (optionFlag.equals("login")) {
                        playerLogin(str, optionFlag);
                        this.ps.println("resume game");
                        this.ps.println(DbTask.getMap(this.userName));
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
                                sendAllPlayers(p.getUsername(), "registered successfully");
                                this.ps.println("myName");
                                this.ps.println(this.userName);
                                SocketServerChat.allPlayers = DbTask.getAll("");
                                SocketServerChat.isUpdatedUser = true;
                            } else {
                                this.ps.println("registration failed");
                            }
                        }
                        optionFlag = "";

                    } else if (str.equals("chat")) {
                        optionFlag = "chat";
                    } else if (str.equals("accepted")) {
                        optionFlag = "accepted";
                    } else if (optionFlag.equals("accepted")) {
                        sendMessageToPlayer(str, "accept chat");
                        rooms.add(new Room(this.userName, str));
                        //player one
                        optionFlag = "";
                    } else if (str.equals("send message")) {
                        optionFlag = "sent";
                    } else if (optionFlag.equals("sent")) {
                        Gson gson = new Gson();
                        MyMessage message = gson.fromJson(str, MyMessage.class);
                        sendTextMessageToPlayer(message);
                        optionFlag = "";
                    } else if (str.equals("exit")) {
                        DbTask.updateOffLine(this.userName);
                        clientsArrayList.remove(this);
                        this.stop();
                    } else if (optionFlag.equals("map")) {

                        Gson gson = new Gson();
                        GameResponse g1 = gson.fromJson(str, GameResponse.class);

                        String[][] stringArr = g1.getArr();
                        gameResponse = new GameResponse(stringArr, playerWon(stringArr), draw(stringArr), g1.getPlayer1(), "");

                        sendToPlayers(this.userName, gameResponse);

                        optionFlag = "";
                    } else if (str.equals("cell got clicked")) {
                        optionFlag = "map";
                    } else if (str.equals("help")) {
                        this.ps.println("why");
                    } else if (str.equals("save map")) {
                        optionFlag = "save to db";
                    } else if (optionFlag.equals("save to db")) {

                        DbTask.saveMap(str, this.userName);
                        optionFlag = "";
                    } else if (str.equals("reset game")) {
                        DbTask.saveMap(null, this.userName);
                    } else if (str.equals("resume play")) {
                        this.ps.println("resume-game-play");
                        this.ps.println(DbTask.getMap(this.userName));
                    }else if(str.equals("back")){
                        Room roomToRemove=null;
                        for(Room currentRoom:rooms){
                            if(currentRoom.getPlayer1().equals(this.userName)||currentRoom.getPlayer2().equals(this.userName)){
                                if(currentRoom.getPlayer1().equals(this.userName)){
                                    DbTask.updateScore(currentRoom.getPlayer2());
                                }else {
                                    DbTask.updateScore(currentRoom.getPlayer1());
                                }
                                sendMessageToPlayer(currentRoom.getPlayer1(),"back-pressed");
                                sendMessageToPlayer(currentRoom.getPlayer2(),"back-pressed");
                                roomToRemove=currentRoom;
                            }
                        }
                        if(roomToRemove!=null){
                            rooms.remove(roomToRemove);
                        }
                    }
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

    public static void pauseAll() {
        for (ChatHandler ch : clientsArrayList) {
            ch.ps.println("pause");
            ch.suspend();
        }
    }

    public static void resumeAll() {
        for (ChatHandler ch : clientsArrayList) {
            ch.resume();
        }
    }

    public void sendMessageToAll() {
        ArrayList<Player> players;
        for (ChatHandler ch : clientsArrayList) {
            players = DbTask.getAll(ch.userName);
            ch.ps.println("update player list");
            ch.ps.println(new Gson().toJson(players));
        }
    }

    void sendMessageToPlayer(String username, String message) {
        for (ChatHandler ch : clientsArrayList) {
            if (ch.userName.equals(username)) {
                ch.ps.println(message);
                ch.ps.println(this.userName);
            }
        }
    }

    void sendTextMessageToPlayer(MyMessage sentMessage) {
        for (ChatHandler ch : clientsArrayList) {
            if (ch.userName.equals(sentMessage.getUsername())) {
                ch.ps.println("text message");
                ch.ps.println(this.userName + " >>> " + sentMessage.getMessage());
                this.ps.println("text message");
                this.ps.println("me >>> " + sentMessage.getMessage());
            }
        }

    }

    void sendToPlayers(String userName, GameResponse gameResponse) {

        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getPlayer1().equals(userName) || rooms.get(i).getPlayer2().equals(userName)) {
                gameResponse.setTurn(rooms.get(i).getItem());
                Gson gson = new Gson();
                String gameResponseJson = gson.toJson(gameResponse);
                sendMsg(rooms.get(i).getPlayer2(), gameResponseJson);
                sendMsg(rooms.get(i).getPlayer1(), gameResponseJson);
                if (gameResponse.isGameOver()) {
                    String winner = gameResponse.getTurn();
                    if (winner.equals("x")) {
                        DbTask.updateScore(rooms.get(i).getPlayer2());

                        for (ChatHandler ch : clientsArrayList) {
                            if (ch.userName.equals(rooms.get(i).getPlayer2())) {
                                ch.ps.println("myPoints");
                               int score= DbTask.getScore(rooms.get(i).getPlayer2());
                                System.out.println("player 2 new score"+score);
                                ch.ps.println(score);
                            }
                        }


                    } else {
                        DbTask.updateScore(rooms.get(i).getPlayer1());
                        for (ChatHandler ch : clientsArrayList) {
                            if (ch.userName.equals(rooms.get(i).getPlayer1())) {
                                ch.ps.println("myPoints");
                                int score= DbTask.getScore(rooms.get(i).getPlayer1());
                                System.out.println("player 1new score"+score);
                                ch.ps.println(score);
                            }
                        }
                    }
                    sendMessageToAll();
                    rooms.remove(i);
                } else if (gameResponse.isDraw()) {
                    sendMessageToAll();
                    rooms.remove(i);
                }
            }
        }
    }

    void sendMsg(String username, String gameResponseJson) {
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
            if (stringArr[i][0].equals(stringArr[i][1])
                    && stringArr[i][0].equals(stringArr[i][2])
                    && !stringArr[i][0].isEmpty()) {
                won = true;
            }
        }
        //column
        for (int i = 0; i < stringArr.length; i++) {
            if (stringArr[0][i].equals(stringArr[1][i])
                    && stringArr[0][i].equals(stringArr[2][i])
                    && !stringArr[0][i].isEmpty()) {
                won = true;

            }
        }
        //diagonal
        if (stringArr[0][0].equals(stringArr[1][1])
                && stringArr[0][0].equals(stringArr[2][2]) && !stringArr[0][0].isEmpty()) {
            won = true;
        }
        if (stringArr[0][2].equals(stringArr[1][1])
                && stringArr[0][2].equals(stringArr[2][0]) && !stringArr[0][2].isEmpty()) {
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

        return true;
    }

    public String getItem() {

        return cellItem[(index++) % (cellItem.length)];
    }

}
