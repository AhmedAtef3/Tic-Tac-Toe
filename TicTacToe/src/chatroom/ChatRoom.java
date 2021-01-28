/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroom;

import chatroom.Classes.Cell;
import chatroom.Classes.GameResponse;
import chatroom.Classes.MyMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.DataInputStream;
import chatroom.Classes.Player;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import java.lang.reflect.Type;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import chatroom.Classes.Player;

/**
 *
 * @author atef
 */
import chatroom.Classes.Player;
import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author atef
 */
public class ChatRoom extends Application {

    Socket mysocket;
    DataInputStream dis;
    PrintStream ps;
    Thread chatThread;
    TextArea textArea;
    Label errorLabel;
    Scene scene;
    List<Player> playerList;
    String username;
    int userActiveFlag;
    Button alertInitButton;
    Label alertLabel;
    TextArea textMessageArea;
    TextField loginUserTextField;
    PasswordField loginpwBox;
    Label labelTrial;
    ComboBox playerComboBox;
    Circle circle;
    Label labelTrial2;
    GridPane gridPanes;
    private BorderPane borderPane;
    private Button exitButton;
    private GridPane gridPane;
    private Cell[][] board = new Cell[3][3];
    private String turn = "o";
    boolean isX;
    private String myUserName;
    private boolean playWithBot;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("My chat room!");

        //primaryStage.setScene(scene);
        scene = new Scene(login(), 800, 500);
        scene.getStylesheets().addAll(this.getClass().getResource("ChatRoomStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                ps.println("exit");
                System.exit(0);
            }
        });

    }

    /**
     * @param args the command line arguments
     */
    @Override
    public void init() throws IOException {
        mysocket = new Socket("127.0.0.1", 5005);
        dis = new DataInputStream(mysocket.getInputStream());
        ps = new PrintStream(mysocket.getOutputStream());

        chatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean successfull = false;
                String flagName = "";
                while (true) {
                    String str = null;
                    try {
                        str = dis.readLine();
                        System.out.println(str);

                        if (str.equals("registration failed")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    errorLabel.setVisible(true);
                                }
                            });

                            System.out.println("registraaation faaaaileeed");
                        } else if (str.equals("login failed")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    errorLabel.setVisible(true);
                                }
                            });
                            System.out.println("logiiin faaaaileeed");
                        } else if (str.equals("login successfully")) {
                            successfull = true;

                        } else if (str.equals("registered successfully")) {
                            System.out.println("hoss");
                            successfull = true;
                        } else if (str.equals("request chat")) {
                            System.out.println("ana dkhlt hna");
                            flagName = "request";

                        } else if (flagName.equals("request")) {
                            final String temp = str;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    alertLabel.getScene().setRoot(requestPage(temp));
                                }
                            });
                            
                            System.out.println(str + "sent");
                            flagName = "";
                        } else if (str.equals("accept chat")) {
                            flagName = "accept";
                        } else if (flagName.equals("accept")) {
                            System.out.println("player " + str);
                            final String playerName = str;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    alertLabel.getScene().setRoot(playPage(playerName));
                                }
                            });
                            flagName = "";
                        } else if (str.equals("text message")) {
                            System.out.println("here " + str);
                            flagName = "message";
                        } else if (flagName.equals("message")) {
                            System.out.println("mar7aba " + str);
                            textMessageArea.appendText(str + "\n");
                            flagName = "";
                        } else if (successfull == true) {

                            String json_string = str;
                            System.out.println(json_string);
                            Gson gson = new Gson();
                            ArrayList<Player> players = new ArrayList<>();
                            Type playerListType = new TypeToken<ArrayList<Player>>() {
                            }.getType();
                            playerList = gson.fromJson(json_string, playerListType);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    errorLabel.getScene().setRoot(mainPage());
                                }

                            });

                            int i = 0;
                            for (Player pp : playerList) {
                                System.out.println(pp.getUsername());
                            }
                            successfull = false;

                        } else if (str.equals("update player list")) {
                            flagName = "update player list";
                            System.out.println("how many times?");

                        } else if (flagName.equals("update player list")) {
                            Gson gson = new Gson();

                            ArrayList<Player> players = new ArrayList<>();
                            Type playerListType = new TypeToken<ArrayList<Player>>() {
                            }.getType();
                            System.out.println("before playerlist: " + str);
                            playerList = gson.fromJson(str, playerListType);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("combobox update");
                                    if (playerComboBox != null) {
                                        playerComboBox.getItems().clear();
                                        for (Player pp : playerList) {
                                            if (pp.getFlag() == 1) {
                                                circle = new Circle(3, Color.GREEN);
                                                labelTrial = new Label(pp.getUsername(), circle);
                                                labelTrial2 = new Label("Score: " + String.valueOf(pp.getPoints()));
                                                gridPanes = new GridPane();
                                                int i = 0;
                                                gridPanes.setHgap(20);
                                                gridPanes.add(labelTrial, i, 0);
                                                gridPanes.add(labelTrial2, i, 1);
                                                i++;

                                            } else {
                                                circle = new Circle(3, Color.RED);
                                                labelTrial = new Label(pp.getUsername(), circle);
                                                labelTrial2 = new Label("Score: " + String.valueOf(pp.getPoints()));
                                                gridPanes = new GridPane();
                                                int i = 0;
                                                gridPanes.setHgap(20);
                                                gridPanes.add(labelTrial, i, 0);
                                                gridPanes.add(labelTrial2, i, 1);
                                                i++;

                                            }

                                            playerComboBox.getItems().add(
                                                    gridPanes
                                            );
                                        }
                                    }
                                }

                            });
//                            System.out.println("update list: " + str);
                            flagName = "";
                        } else if (flagName.equals("get map")) {
                            Gson gson = new Gson();
                            System.out.println(str);
                            GameResponse gameResponse = gson.fromJson(str, GameResponse.class);

                            if (!gameResponse.getTurn().isEmpty()) {
                                turn = gameResponse.getTurn();
                                System.out.println("client" + gameResponse.getTurn());
                            }
                            String[][] stringArr = gameResponse.getArr();
                            //test loop
                            System.out.println("----------------------------------------------");
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    System.out.print(stringArr[i][j] + ",");
                                }
                                System.out.println();
                            }
                            for (int i = 0; i < board.length; i++) {
                                for (int j = 0; j < board[i].length; j++) {
                                    if (!stringArr[i][j].isEmpty()) {
                                        board[i][j].getPlayerMove().setText(stringArr[i][j]);
                                    }
                                }

                            }
                            if (gameResponse.isGameOver()) {
                                winMessage(gameResponse.getTurn());

                            } else if (gameResponse.isDraw()) {

                                drawMessage();
                            }
                            flagName = "";
                        } else if (str.equals("myName")) {
                            flagName = "getName";
                        } else if (flagName.equals("getName")) {
                            myUserName = str;
                            flagName = "";
                        } else if (str.equals("update game")) {

                            flagName = "get map";
                        }
                        System.out.println(str);

                    } catch (IOException ex) {
                        System.out.println("lina");
                    }
                }
            }
        });
        chatThread.start();

    }

    public static void main(String[] args) {
        launch(args);
    }

    public GridPane login() {
        GridPane grid = new GridPane();
        grid.setId("pane");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        errorLabel = new Label("Invalid credentials");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        grid.add(errorLabel, 1, 18);
        //Text scenetitle = new Text("Welcome to tic tac toe");
        //scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        //grid.add(scenetitle, 0, 0, 2, 1);
        //Label userName = new Label("User Name:");
//        grid.setAlignment(Pos.CENTER);

        //grid.add(userName, 0, 1);
        loginUserTextField = new TextField();
        GridPane.setHalignment(loginUserTextField, HPos.CENTER);

//        loginUserTextField.set;
        grid.add(loginUserTextField, 0, 16);
        GridPane.setColumnSpan(loginUserTextField, 2);
        //Label pw = new Label("Password:");
        //grid.add(pw, 0, 2);
        loginUserTextField.setPromptText("Username");
        loginUserTextField.setFocusTraversable(false);
//      loginUserTextField.setStyle("-fx-background-color:white; -fx-font-family: Consolas; -fx-text-fill:#a72b1b;");
        loginUserTextField.setId("textField");

        loginpwBox = new PasswordField();
        GridPane.setHalignment(loginpwBox, HPos.CENTER);
        grid.add(loginpwBox, 0, 17);
        GridPane.setColumnSpan(loginpwBox, 2);
        loginpwBox.setPromptText("Password");
        loginpwBox.setFocusTraversable(false);
//      loginpwBox.setStyle("-fx-background-color:white; -fx-font-family: Consolas; -fx-text-fill:#a72b1b;");
        loginpwBox.setId("textField");

        Button loginButton = new Button("Sign in");
        grid.add(loginButton, 0, 19);
        loginButton.setId("record-sales");
        GridPane.setHalignment(loginButton, HPos.LEFT);
//        loginButton.setMaxWidth(100);
        loginButton.setPrefSize(120, 30);
//        loginButton.setMaxHeight(70);

        Button registerButton = new Button("Register");
        grid.add(registerButton, 1, 19);
        registerButton.setId("record-sales");
        GridPane.setHalignment(registerButton, HPos.RIGHT);
        registerButton.setPrefSize(120, 30);
//        registerButton.setMaxWidth(100);
//        registerButton.setMaxHeight(70);
        //HBox hbBtn = new HBox(10);

        loginButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                playerLogin();
            }
        });

        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                registerButton.getScene().setRoot(register());
            }

        });

//        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
//        hbBtn.getChildren().addAll(loginButton, registerButton);
//        grid.add(hbBtn, 1, 12);
        return grid;
    }

    public void playerLogin() {
        Player player = new Player(loginUserTextField.getText(), loginpwBox.getText());
        ps.println("login");
        ps.println(new Gson().toJson(player));
    }

    public GridPane register() {
        GridPane gridPane = new GridPane();
        gridPane.setId("pane");
        errorLabel = new Label("Invalid credentials");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        gridPane.setAlignment(Pos.CENTER);
        //gridPane.setPadding(new Insets(40, 40, 40, 40));

        gridPane.setHgap(10);
        gridPane.setVgap(10);

        ColumnConstraints columnOneConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        columnOneConstraints.setHalignment(HPos.RIGHT);

        ColumnConstraints columnTwoConstrains = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnTwoConstrains.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstrains);

        //Header Location
//        Label headerLabel = new Label("Registeration");
//        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
//        gridPane.add(headerLabel, 0, 0, 2, 1);
//        GridPane.setHalignment(headerLabel, HPos.LEFT);
//        GridPane.setMargin(headerLabel, new Insets(0, 0, 10, 0));
        //add username
//        Label username = new Label("USER Name ");
//        gridPane.add(username, 0, 1);
        // Add username field
        TextField usernameField = new TextField();
        gridPane.add(usernameField, 0, 15);
//        usernameField.setPrefHeight(40);
//        gridPane.add(usernameField, 1, 1);

        // Add nickname
//        Label nickname = new Label("Nick Name ");
//        gridPane.add(nickname, 0, 2);
        // Add nickname field 
        TextField nicknameField = new TextField();
        gridPane.add(nicknameField, 0, 16);
//        nicknameField.setPrefHeight(40);
//        gridPane.add(nicknameField, 1, 2);

        // Add password
//        Label password = new Label("password");
//        gridPane.add(password, 0, 3);
        // Add password field
        PasswordField passwordfield = new PasswordField();
        gridPane.add(passwordfield, 0, 17);
//        passwordfield.setPrefHeight(40);
//        gridPane.add(passwordfield, 1, 3);

        // Add register button
        Button RegisterButton = new Button("Register");
        RegisterButton.setPrefHeight(40);
        RegisterButton.setDefaultButton(true);
        RegisterButton.setPrefWidth(100);
        gridPane.add(RegisterButton, 0, 7, 1, 1);
        GridPane.setMargin(RegisterButton, new Insets(20, 0, 20, 0));

        //add back button
        final Button backButton = new Button("Back");
        backButton.setPrefHeight(40);
        backButton.setDefaultButton(true);
        backButton.setPrefWidth(100);
        gridPane.add(backButton, 1, 7, 1, 1);
        GridPane.setMargin(backButton, new Insets(20, 0, 20, 0));

        //Add label
        gridPane.add(errorLabel, 1, 8);

        backButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                backButton.getScene().setRoot(login());
            }
        });

        RegisterButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                Player player = new Player(usernameField.getText(), nicknameField.getText(), passwordfield.getText());
                ps.println("register");
                ps.println(new Gson().toJson(player));
            }
        });

        return gridPane;
    }

    public String getUsername() {
        return username;
    }

    public GridPane mainPage() {
        // properties of text 
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(2, 2, 2, 2));
        grid.setId("second-pane");

        // option list 
        playerComboBox = new ComboBox();
        System.out.println(playerList.size() + " number");
        //Circle circle;
        //label labelTrial;
        //Label labelTrial2;
        //GridPane gridPanes;

        for (Player pp : playerList) {
            if (pp.getFlag() == 1) {
                circle = new Circle(3, Color.GREEN);
                labelTrial = new Label(pp.getUsername(), circle);
                labelTrial2 = new Label("Score: " + String.valueOf(pp.getPoints()));
                gridPanes = new GridPane();
                int i = 0;
                gridPanes.setHgap(20);
                gridPanes.add(labelTrial, i, 0);
                gridPanes.add(labelTrial2, i, 1);
                i++;

            } else {
                circle = new Circle(3, Color.RED);
                labelTrial = new Label(pp.getUsername(), circle);
                labelTrial2 = new Label("Score: " + String.valueOf(pp.getPoints()));
                gridPanes = new GridPane();
                int i = 0;
                gridPanes.setHgap(20);
                gridPanes.add(labelTrial, i, 0);
                gridPanes.add(labelTrial2, i, 1);
                i++;

            }

            playerComboBox.getItems().add(
                    gridPanes
            );
        }

        playerComboBox.setValue("please choose one player");
        playerComboBox.setId(("combo-box"));

        // handle of option list 
        playerComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(playerComboBox.getSelectionModel().getSelectedIndex());
                int index = playerComboBox.getSelectionModel().getSelectedIndex();
                username = playerList.get(index).getUsername();
                userActiveFlag = playerList.get(index).getFlag();
            }

        });

        // text 
        Label headerLabel = new Label("let's play " + myUserName);
        headerLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
        GridPane.setHalignment(headerLabel, HPos.CENTER);
        headerLabel.setId("main-title");

        // radio button
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton radioButton1 = new RadioButton("computer");
        radioButton1.setId("radio-button");
        RadioButton radioButton2 = new RadioButton("player");
        radioButton1.setToggleGroup(radioGroup);
        radioButton2.setToggleGroup(radioGroup);
        radioButton2.setId("radio-button");

        radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {

                RadioButton chk = (RadioButton) t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
                System.out.println("Selected Radio Button - " + chk.getText());
                if (chk.getText().equals("computer")) {
                    playWithBot = true;
                }

            }
        });

        VBox hbox = new VBox(radioButton1, radioButton2);
        hbox.setSpacing(10);
        //play button
        Button playButton = new Button("play");
        playButton.setPrefHeight(40);
        playButton.setDefaultButton(true);
        playButton.setPrefWidth(100);
        playButton.setId("record-sales");

        playButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                isX = true;
                if (playWithBot) {
                    playButton.getScene().setRoot(playPage(""));
                } else if (userActiveFlag == 1) {
                    ps.println("chat");
                    ps.println(username);
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alertActive = new Alert(AlertType.ERROR);
                            alertActive.setTitle("Error Message");
                            alertActive.setHeaderText("You can't play with offline player");
                            alertActive.showAndWait();
                        }
                    });
                }

                //playButton.getScene().setRoot(playPage());
            }
        });

        alertLabel = new Label("alert");
        alertLabel.setVisible(false);
        System.out.println("Alert Initialized" + alertLabel.getText());

        grid.add(headerLabel, 2, 2);
        grid.add(playerComboBox, 2, 10);
        grid.add(hbox, 2, 6);
        grid.add(playButton, 9, 30);
        grid.add(alertLabel, 10, 50);

        return grid;

    }

    public boolean isPlayWithBot() {
        return playWithBot;
    }

    public BorderPane playPage(String user) {
        textMessageArea = new TextArea();
        textMessageArea.setEditable(false);

        TextField textField = new TextField();
        textField.setMinWidth(200.0);
        textField.setPrefWidth(300.0);
        textField.setMaxWidth(350.0);

        Button sendButton = new Button("send");
        exitButton = new Button("exit");

        sendButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                MyMessage myMessage = new MyMessage(user, textField.getText());
                ps.println("send message");
                ps.println(new Gson().toJson(myMessage));
                textField.clear();
            }
        });

        exitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (playWithBot == false) {
                    ps.println("exit");
                    System.exit(0);
                } else {
                    exitButton.getScene().setRoot(mainPage());
                    playWithBot = false;
                }
            }
        });

        Insets insets = new Insets(20);

        //textArea.appendText(client.receivedMessage);
        HBox hBox = new HBox(30, textField, sendButton, exitButton);
        BorderPane root = new BorderPane();
        root.setCenter(textMessageArea);
        BorderPane.setMargin(textMessageArea, insets);
        root.setBottom(hBox);
        BorderPane.setMargin(hBox, insets);
        borderPane = new BorderPane();
        gridPane = new GridPane();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Cell cell = new Cell(this);
                gridPane.add(cell, j, i);
                board[i][j] = cell;
            }
        }
        borderPane.setCenter(gridPane);
        borderPane.setRight(root);

        return borderPane;

        // return root;
        //Scene scene = new Scene(root, 500, 500);
        /*primaryStage.setTitle("Chat Room");
         primaryStage.setScene(scene);
         primaryStage.show();*/
    }

    public GridPane requestPage(String user) {
        System.out.println("I'm in request page!!!");

        GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(25, 25, 25, 25));

        Label notation = new Label(user + " wants to play with you, do you accept?");

        //GridPane.setHalignment(notation, HPos.CENTER);
        Button acceptButton = new Button("accept");
        Button cancelButton = new Button("cancel");

        acceptButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                acceptButton.getScene().setRoot(playPage(user));
                ps.println("accepted");
                ps.println(user);
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                cancelButton.getScene().setRoot(mainPage());
            }
        });

        gp.add(notation, 2, 1, 3, 1);
        gp.add(acceptButton, 2, 2);
        gp.add(cancelButton, 4, 2);

        return gp;
    }

    public void winMessage(String winner) {
        String msg;
        if (!winner.equals("x")) {
            msg = "O won!";
        } else {
            msg = "X won!";
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert gameEndAlert = new Alert(Alert.AlertType.INFORMATION);
                gameEndAlert.setTitle("game ended");
                gameEndAlert.setHeaderText(msg);
                gameEndAlert.showAndWait();
                turn = "o";
                isX = false;
                exitButton.getScene().setRoot(mainPage());

            }
        });

    }

    public void cleanMap() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j].getPlayerMove().setText("");
            }
        }

    }

    public void drawMessage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert gameEndAlert = new Alert(Alert.AlertType.INFORMATION);
                gameEndAlert.setTitle("game ended");
                gameEndAlert.setHeaderText("draw!");
                gameEndAlert.showAndWait();

            }

        });
    }

    public String[][] cellValues() {
        String[][] values = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                values[i][j] = board[i][j].getPlayerMove().getText().toString();
            }
        }
        return values;
    }

    public DataInputStream getDis() {
        return dis;
    }

    public PrintStream getPs() {
        return ps;
    }

    public boolean isX() {
        return isX;
    }

    public String getTurn() {
        return turn;
    }

    public String getMyUserName() {
        return myUserName;
    }

    public boolean playerWon() {
        //row
        boolean won = false;
        for (int i = 0; i < board.length; i++) {
            if (board[i][0].getPlayerMove().getText().equals(board[i][1].getPlayerMove().getText())
                    && board[i][0].getPlayerMove().getText().equals(board[i][2].getPlayerMove().getText())
                    && !board[i][0].getPlayerMove().getText().isEmpty()) {
                System.out.println("player:" + board[i][0].getPlayerMove().getText() + "won");
                won = true;
            }
        }
        //column
        for (int i = 0; i < board.length; i++) {
            if (board[0][i].getPlayerMove().getText().equals(board[1][i].getPlayerMove().getText())
                    && board[0][i].getPlayerMove().getText().equals(board[2][i].getPlayerMove().getText())
                    && !board[0][i].getPlayerMove().getText().isEmpty()) {
                System.out.println("player:" + board[i][0].getPlayerMove().getText() + "won");
                won = true;

            }
        }
        //diagonal
        if (board[0][0].getPlayerMove().getText().equals(board[1][1].getPlayerMove().getText())
                && board[0][0].getPlayerMove().getText().equals(board[2][2].getPlayerMove().getText()) && !board[0][0].getPlayerMove().getText().isEmpty()) {
            System.out.println("player:" + board[0][0].getPlayerMove().getText() + "won");
            won = true;
        }
        if (board[0][2].getPlayerMove().getText().equals(board[1][1].getPlayerMove().getText())
                && board[0][2].getPlayerMove().getText().equals(board[2][0].getPlayerMove().getText()) && !board[0][2].getPlayerMove().getText().isEmpty()) {
            System.out.println("player:" + board[0][0].getPlayerMove().getText() + "won");
            won = true;

        }

        return won;
    }

    public void winMassage() {
        String msg;
        if (board[0][0].getItem().equals("x")) {
            msg = "O won!";
        } else {
            msg = "X won!";
        }

        Alert gameEndAlert = new Alert(Alert.AlertType.INFORMATION);
        gameEndAlert.setTitle("game ended");
        gameEndAlert.setHeaderText(msg);
        gameEndAlert.showAndWait();

    }

    public void botMove() {

        while (true) {
            int x = new Random().nextInt(2 - 0 + 1) + 0;
            int y = new Random().nextInt(2 - 0 + 1) + 0;
            if (board[x][y].getPlayerMove().getText().isEmpty()) {
                board[x][y].getPlayerMove().setText("o");
                break;
            }
        }

    }

    public boolean gameOver() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].getPlayerMove().getText().isEmpty()) {
                    return false;
                }
            }
        }
        System.out.println("test");
        return true;
    }

}
/*


 Platform.runLater(new Runnable() {
 @Override
 public void run() {
 Alert gameEndAlert = new Alert(Alert.AlertType.INFORMATION);
 gameEndAlert.setTitle("game ended");
 gameEndAlert.setHeaderText("draw!");
 gameEndAlert.showAndWait();
 }

 });


 */
