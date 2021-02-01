/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketserverchat;

import gameDB.DbTask;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
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
    ListView<String> listView;
     boolean isStarted = false;

    @Override
    public void init() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        primaryStage.setTitle("Server");

        listView = new ListView<String>();

        listView.setPrefSize(200, 520);
        //Creating the layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(5, 5, 5, 50));
        layout.getChildren().addAll(listView);

        //Setting the stage
        Timer timer = new Timer();
        int begin = 0;
        int timeInterval = 1000;
        timer.schedule(new TimerTask() {
            int counter = 0;

            @Override
            public void run() {
                counter++;

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        if (listView != null) {
                            listView.getItems().clear();

                            for (Player pp : DbTask.getAll("")) {
                                listView.getItems().add(
                                        pp.getUsername() + "," + pp.getPoints() + "," + pp.getFlag()
                                );
                            }

                            listView.setCellFactory(new ShapeCellFactory());

                        }

                        SocketServerChat.isUpdatedUser = false;
                    }
                });

                for (Player pp : SocketServerChat.allPlayers) {
                }
//                }

            }
        }, begin, timeInterval);

        BorderPane root = new BorderPane();
        root.setId("second-pane");
        Button startButton = new Button("Start");

        startButton.setId("startbutton");

        Button stopButton = new Button("Stop");

        stopButton.setId("stopbutton");
        HBox buttons = new HBox(30, startButton, stopButton);
        buttons.setAlignment(Pos.CENTER);

        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        if (listView != null) {
                            listView.getItems().clear();

                            for (Player pp : DbTask.getAll("")) {
                                listView.getItems().add(
                                        pp.getUsername() + "," + pp.getPoints() + "," + pp.getFlag()
                                );
                            }

                            listView.setCellFactory(new ShapeCellFactory());

                        }

                        SocketServerChat.isUpdatedUser = false;
                    }
                });
                if (firstStartServerFlag == false) {
                    firstStartServerFlag = true;
                    ServerSocketThread.setPressedButton("start");
                    ServerSocketThread serverStart = new ServerSocketThread();
                    SocketServerChat.resumeServerSocket();
                }
            }
        });

        stopButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                firstStartServerFlag = false;
                ServerSocketThread.setPressedButton("stop");
                SocketServerChat.stopServerSocket();
            }
        });
        
        Label playersListLabel = new Label("List of players");
        playersListLabel.setId("main-title");
        BorderPane.setAlignment(playersListLabel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(playersListLabel, new Insets(5, 17, 5, 17));

        root.setTop(playersListLabel);
        root.setCenter(buttons);
        root.setRight(layout);
        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().addAll(this.getClass().getResource("serverstyle.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });
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
        startServer = new SocketServerChat();
    }
}

//To edit combobox shape
class ShapeCell extends ListCell<String> {

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText("");
            GridPane cellShape = this.getShape(item);
            setGraphic(cellShape);
        }
    }

    public GridPane getShape(String shapeType) {
        GridPane shape = null;
        Circle status;

        if (shapeType != null) {
            String[] inputItemsSplit = shapeType.split(",");
            if (Integer.valueOf(inputItemsSplit[2]) == 1) {
                status = new Circle(3, Color.GREEN);
            } else {
                status = new Circle(3, Color.RED);
            }
            Label playerNameStatus = new Label(inputItemsSplit[0], status);
            Label playerScore = new Label("Score: " + inputItemsSplit[1]);
            shape = new GridPane();
            shape.add(playerNameStatus, 0, 0);
            shape.add(playerScore, 0, 1);
        }

        return shape;
    }
}

class ShapeCellFactory implements Callback<ListView<String>, ListCell<String>> {

    @Override
    public ListCell<String> call(ListView<String> listview) {
        return new ShapeCell();
    }
}
