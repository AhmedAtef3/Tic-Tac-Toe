/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketserverchat;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import socketserverchat.Classes.Player;

/**
 *
 * @author atef
 */
public class ServerGUI extends Application {

    private boolean firstStartServerFlag = false;
    private Thread serverThread;
    private SocketServerChat startServer;
    Thread updatePlayerThread;
    @Override
    public void init() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Timer timer = new Timer();
        int begin = 0;
        int timeInterval = 1000;
        timer.schedule(new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                counter++;
                if (! SocketServerChat.isUpdatedUser) {
                     Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        // do some code 
                                        System.out.println(SocketServerChat.allPlayers.size());
                                        SocketServerChat.isUpdatedUser = false;
                                    }
                                });
                }
                else {
  
                    System.out.println(SocketServerChat.allPlayers.size());
                    for (Player pp : SocketServerChat.allPlayers)
                    {
                        System.out.println(pp.getUsername() + " , "+ pp.getFlag());
                    }
                }
                
            }
        } , begin, timeInterval);

        BorderPane root = new BorderPane();
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        HBox buttons = new HBox(10, startButton, stopButton);
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (firstStartServerFlag == false) {
                    firstStartServerFlag = true;
                    ServerSocketThread.setPressedButton("start");
                    ServerSocketThread serverStart = new ServerSocketThread();
                    SocketServerChat.resumeServerSocket();
                    System.out.println("Thread: " + serverThread);
                }
            }
        });

        stopButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                firstStartServerFlag = false;
                System.out.println("Thread suspended");
                ServerSocketThread.setPressedButton("stop");
                SocketServerChat.stopServerSocket();
            }
        });

        root.setCenter(buttons);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}



class ServerSocketThread extends Thread {

    private static SocketServerChat startServer;
    private static String pressedButton;

    public static String getPressedButton() {
        return pressedButton;
    }

    public static SocketServerChat getStartServer() {
        return startServer;
    }

    public static void setPressedButton(String buttonStatus) {
        pressedButton = buttonStatus;
    }

    public ServerSocketThread() {
        this.start();
    }

    @Override
    public void run() {
        try {

            startServer = new SocketServerChat();
            System.out.println("port: " + startServer.getServerSocket());
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
