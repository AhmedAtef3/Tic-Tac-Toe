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

import java.io.DataInputStream;
import chatroom.Classes.Player;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import java.util.Random;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

/**
 *
 * @author atef
 */
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Toggle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

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
    private String winner;
    Label labelTrial;
    ComboBox playerComboBox;
    Circle circle;
    Label labelTrial2;
    GridPane gridPanes;
    private BorderPane borderPane;
    private Button backToMenuButton;
    private GridPane gridPane;
    private Cell[][] board = new Cell[3][3];
    private String map[][];
    private Button playButton;
    private String turn = "o";
    boolean isX;
    private String myUserName;
    private boolean playWithBot;
    private boolean resumeGame;
    private Label scoreLabel;
    private int myScore = 0 ;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Tic Tac Toe");

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
                        if (str != null) {

                            if (str.equals("registration failed")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        errorLabel.setVisible(true);
                                    }
                                });

                            } else if (str.equals("login failed")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        errorLabel.setVisible(true);
                                    }
                                });
                            } else if (str.equals("login successfully")) {
                                successfull = true;

                            } else if (str.equals("registered successfully")) {
                                successfull = true;
                            } else if (str.equals("request chat")) {
                                flagName = "request";

                            } else if (flagName.equals("request")) {
                                final String temp = str;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertLabel.getScene().setRoot(requestPage(temp));
                                    }
                                });

                                flagName = "";
                            } else if (str.equals("accept chat")) {
                                flagName = "accept";
                            } else if (flagName.equals("accept")) {
                                final String playerName = str;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertLabel.getScene().setRoot(playPage(playerName));
                                    }
                                });
                                flagName = "";
                            } else if (str.equals("text message")) {
                                flagName = "message";
                            } else if (flagName.equals("message")) {
                                textMessageArea.appendText(str + "\n");
                                flagName = "";
                            } else if (successfull == true) {

                                String json_string = str;
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

                                successfull = false;

                            } else if (str.equals("update player list")) {
                                flagName = "update player list";

                            } else if (flagName.equals("update player list")) {
                                Gson gson = new Gson();

                                ArrayList<Player> players = new ArrayList<>();
                                Type playerListType = new TypeToken<ArrayList<Player>>() {
                                }.getType();
                                playerList = gson.fromJson(str, playerListType);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (playerComboBox != null) {
                                            playerComboBox.getItems().clear();

                                            for (Player pp : playerList) {
                                                playerComboBox.getItems().add(
                                                        pp.getUsername() + "," + pp.getPoints() + "," + pp.getFlag()
                                                );
                                            }

                                            playerComboBox.setValue("please choose one player");
                                            // Set the CellFactory property
                                            playerComboBox.setCellFactory(new ShapeCellFactory());
                                            // Set the ButtonCell property
                                            playerComboBox.setButtonCell(new ShapeCell());
                                            ////////////////////////////////////////////////
                                            playerComboBox.setValue("please choose one player");
                                            playerComboBox.setId(("combo-box"));
                                        }
                                    }

                                });
                                flagName = "";
                            } else if (flagName.equals("get map")) {
                                Gson gson = new Gson();
                                GameResponse gameResponse = gson.fromJson(str, GameResponse.class);

                                if (!gameResponse.getTurn().isEmpty()) {
                                    turn = gameResponse.getTurn();
                                }
                                String[][] stringArr = gameResponse.getArr();
                                //test loop
                                for (int i = 0; i < 3; i++) {
                                    for (int j = 0; j < 3; j++) {
                                    }
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
                            } else if (str.equals("pause")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert alertActive = new Alert(AlertType.WARNING);
                                        alertActive.setTitle("Warning Message");
                                        alertActive.setHeaderText("Server is down now but your message will arrive as soon as server is back");
                                        alertActive.showAndWait();
                                    }
                                });
                            } else if (str.equals("resume-game-play")) {
                                flagName = "resume-game";
                            } else if (flagName.equals("resume-game")) {
                                Gson gson = new Gson();

                                map = gson.fromJson(str, String[][].class);
                                resumeGame = true;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        playButton.getScene().setRoot(playPage(""));

                                    }
                                });

                                flagName = "";
                            }
                            else if(str.equals("myPoints"))
                            {
                                flagName = "myPoints";
                            }
                            else if(flagName.equals("myPoints"))
                            {
                                myScore= Integer.valueOf(str);
                               Platform.runLater(new Runnable() {
                                   @Override
                                   public void run() {
                                       if(scoreLabel!=null) {
                                           scoreLabel.setText("points:" + myScore);
                                       }
                                   }
                               });
                                
                            flagName="";
                            }else if(str.equals("back-pressed")){
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert alertActive = new Alert(AlertType.ERROR);
                                        alertActive.setTitle("player left");
                                        alertActive.setHeaderText("a player has left the game!");
                                        alertActive.showAndWait();
                                        turn = "o";
                                        isX = false;
                                        backToMenuButton.getScene().setRoot(mainPage());

                                    }
                                });
                            }

                        }
                    } catch (IOException ex) {
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

        loginUserTextField = new TextField();
        GridPane.setHalignment(loginUserTextField, HPos.CENTER);

        grid.add(loginUserTextField, 0, 16);
        GridPane.setColumnSpan(loginUserTextField, 2);

        loginUserTextField.setPromptText("Username");
        loginUserTextField.setFocusTraversable(false);
        loginUserTextField.setId("textField");

        loginpwBox = new PasswordField();
        GridPane.setHalignment(loginpwBox, HPos.CENTER);
        grid.add(loginpwBox, 0, 17);
        GridPane.setColumnSpan(loginpwBox, 2);
        loginpwBox.setPromptText("Password");
        loginpwBox.setFocusTraversable(false);
        loginpwBox.setId("textField");

        Button loginButton = new Button("Sign in");
        grid.add(loginButton, 0, 19);
        loginButton.setId("buttons");
        GridPane.setHalignment(loginButton, HPos.LEFT);

        loginButton.setPrefSize(120, 30);

        Button registerButton = new Button("Register");
        grid.add(registerButton, 1, 19);
        registerButton.setId("buttons");

        GridPane.setHalignment(registerButton, HPos.RIGHT);
        registerButton.setPrefSize(120, 30);

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
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        errorLabel = new Label("Invalid credentials");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        GridPane.setHalignment(errorLabel, HPos.RIGHT);
        gridPane.add(errorLabel, 1, 22);

        TextField usernameField = new TextField();
        GridPane.setHalignment(usernameField, HPos.CENTER);

        usernameField.setPromptText("Username");
        GridPane.setColumnSpan(usernameField, 2);

        gridPane.add(usernameField, 0, 19);
        usernameField.setId("textField");

        TextField nicknameField = new TextField();

        GridPane.setHalignment(nicknameField, HPos.CENTER);

        nicknameField.setPromptText("nickname");

        gridPane.add(nicknameField, 0, 20);
        nicknameField.setId("textField");
        GridPane.setColumnSpan(nicknameField, 2);

        PasswordField passwordfield = new PasswordField();
        GridPane.setHalignment(passwordfield, HPos.CENTER);
        GridPane.setColumnSpan(passwordfield, 2);
        passwordfield.setPromptText("password");
        gridPane.add(passwordfield, 0, 21);
        passwordfield.setId("textField");

        Button RegisterButton = new Button("Register");

        RegisterButton.setPrefSize(120, 30);
        RegisterButton.setId("buttons");
        GridPane.setHalignment(RegisterButton, HPos.LEFT);

        gridPane.add(RegisterButton, 0, 23);

        final Button backButton = new Button("Back");

        backButton.setPrefSize(120, 30);
        backButton.setId("buttons");
        GridPane.setHalignment(backButton, HPos.RIGHT);
        gridPane.add(backButton, 1, 23);

        backButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                backButton.getScene().setRoot(login());
            }
        });

        RegisterButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                if (!usernameField.getText().equals("") && !nicknameField.getText().equals("") && !passwordfield.getText().equals("")) {
                    Player player = new Player(usernameField.getText(), nicknameField.getText(), passwordfield.getText());
                    ps.println("register");
                    ps.println(new Gson().toJson(player));
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alertActive = new Alert(AlertType.ERROR);
                            alertActive.setTitle("Error Message");
                            alertActive.setHeaderText("All fields are required !!!");
                            alertActive.showAndWait();
                        }
                    });
                }
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

        playerComboBox = new ComboBox();
        for (Player pp : playerList) {
            playerComboBox.getItems().add(
                    pp.getUsername() + "," + pp.getPoints() + "," + pp.getFlag()
            );
        }

        playerComboBox.setValue("please choose one player");

        // Set the CellFactory property
        playerComboBox.setCellFactory(new ShapeCellFactory());
        // Set the ButtonCell property
        playerComboBox.setButtonCell(new ShapeCell());
        ////////////////////////////////////////////////
        playerComboBox.setValue("please choose one player");
        playerComboBox.setId(("combo-box"));

        // handle of option list 
        playerComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (playerComboBox.getSelectionModel().getSelectedIndex() > -1) {

                    int index = playerComboBox.getSelectionModel().getSelectedIndex();
                    username = playerList.get(index).getUsername();
                    userActiveFlag = playerList.get(index).getFlag();

                }
            }

        });

        Label headerLabel = new Label("Welcome To The Game");
         scoreLabel = new Label("Score " + myScore);
        headerLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
        scoreLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 1));
        GridPane.setHalignment(headerLabel, HPos.CENTER);
        headerLabel.setId("main-title");
        scoreLabel.setId("submain-title");

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
                if (chk.getText().equals("computer")) {
                    playWithBot = true;
                }

            }
        });

        VBox hbox = new VBox(radioButton1, radioButton2);
        hbox.setSpacing(10);
        //play button
        playButton = new Button("play");
        playButton.setPrefHeight(70);
        playButton.setDefaultButton(true);
        playButton.setPrefWidth(130);
        playButton.setId("buttons");

        playButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (playWithBot) {
                    resumeGame = true;
                    ps.println("resume play");

                    isX = true;
                } else if (userActiveFlag == 1) {
                    ps.println("chat");
                    ps.println(username);
                    isX = true;
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

            }
        });

        alertLabel = new Label("alert");
        alertLabel.setVisible(false);

        grid.add(headerLabel, 2, 2);
        grid.add(scoreLabel, 2, 4);
        grid.add(playerComboBox, 2, 10);
        grid.add(hbox, 2, 8);
        grid.add(playButton, 10, 22);
        grid.add(alertLabel, 10, 52);

        return grid;

    }

    public boolean isPlayWithBot() {
        return playWithBot;
    }

    public BorderPane playPage(String user) {
        textMessageArea = new TextArea();
        textMessageArea.setEditable(false);
        textMessageArea.setPrefSize(300, 345);
        AnchorPane chatPane = new AnchorPane();
        BorderPane.setAlignment(chatPane, Pos.CENTER);
        chatPane.setPrefSize(277, 400);

        textMessageArea.setLayoutX(5);
        textMessageArea.setLayoutY(5);
        if (!playWithBot) {

            chatPane.getChildren().add(textMessageArea);
        }

        TextField textField = new TextField();

        textField.setPrefWidth(180.0);

        Button sendButton = new Button("send");
        sendButton.setPrefWidth(50);
        sendButton.setId("buttons2");
        backToMenuButton = new Button("back");
        backToMenuButton.setPrefWidth(50);
        backToMenuButton.setId("buttons2");

        sendButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                MyMessage myMessage = new MyMessage(user, textField.getText());
                ps.println("send message");
                ps.println(new Gson().toJson(myMessage));
                textField.clear();
            }
        });

        backToMenuButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (playWithBot == false) {
                    ps.println("back");;
                } else {
                    resumeGame = false;
                    playWithBot = false;
                    turn = "o";
                    isX = false;
                    ps.println("save map");
                    Gson gson = new Gson();
                    String myMap = gson.toJson(cellValues(), String[][].class);
                    ps.println(myMap);
                    map = null;
                    backToMenuButton.getScene().setRoot(mainPage());

                }

            }
        });

        Insets insets = new Insets(20);
        HBox hBox;
        if (!playWithBot) {
            hBox = new HBox(10, textField, sendButton, backToMenuButton);
        } else {
            hBox = new HBox(10, backToMenuButton);
        }
        BorderPane root = new BorderPane();
        if (!playWithBot) {

            root.setCenter(textMessageArea);
        }
        root.setId("left-borderPane");
        BorderPane.setMargin(textMessageArea, insets);
        root.setBottom(hBox);
        BorderPane.setMargin(hBox, insets);
        borderPane = new BorderPane();
        borderPane.setId("second-pane");
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Cell cell = new Cell(this);
                gridPane.add(cell, j, i);
                board[i][j] = cell;
            }
        }
        if (resumeGame) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (map != null) {
                        board[i][j].getPlayerMove().setText(map[i][j]);
                    }
                }

            }
        }
        borderPane.setCenter(gridPane);
        borderPane.setRight(root);

        return borderPane;

    }

    public GridPane requestPage(String user) {

        GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(25, 25, 25, 25));
        gp.setId("second-pane");

        Label notation = new Label(user + " wants to play with you, do you accept?");
        notation.setId("main-title");

        Button acceptButton = new Button("accept");
        Button cancelButton = new Button("cancel");

        acceptButton.setId("acceptbtn");
        cancelButton.setId("acceptbtn");

        acceptButton.setPrefHeight(40);
        acceptButton.setPrefWidth(90);

        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(90);
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

        GridPane.setHalignment(notation, HPos.CENTER);
        GridPane.setHalignment(acceptButton, HPos.CENTER);
        GridPane.setHalignment(cancelButton, HPos.CENTER);
        gp.add(notation, 0, 0);
        HBox requestHBox = new HBox(10.0, acceptButton, cancelButton);
        requestHBox.setAlignment(Pos.CENTER);
        GridPane.setHalignment(requestHBox, HPos.CENTER);
        gp.add(requestHBox, 0, 1);

        return gp;
    }

    public void winMessage(String whoWon) {
        String msg;
        if (!whoWon.equals("x")) {
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
                backToMenuButton.getScene().setRoot(mainPage());

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
                turn = "o";
                isX = false;
                backToMenuButton.getScene().setRoot(mainPage());

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
                won = true;
                winner = board[i][0].getPlayerMove().getText();
            }
        }
        //column
        for (int i = 0; i < board.length; i++) {
            if (board[0][i].getPlayerMove().getText().equals(board[1][i].getPlayerMove().getText())
                    && board[0][i].getPlayerMove().getText().equals(board[2][i].getPlayerMove().getText())
                    && !board[0][i].getPlayerMove().getText().isEmpty()) {
                won = true;
                winner = board[0][i].getPlayerMove().getText();
            }
        }
        //diagonal
        if (board[0][0].getPlayerMove().getText().equals(board[1][1].getPlayerMove().getText())
                && board[0][0].getPlayerMove().getText().equals(board[2][2].getPlayerMove().getText()) && !board[0][0].getPlayerMove().getText().isEmpty()) {
            won = true;
            winner = board[0][0].getPlayerMove().getText();
        }
        if (board[0][2].getPlayerMove().getText().equals(board[1][1].getPlayerMove().getText())
                && board[0][2].getPlayerMove().getText().equals(board[2][0].getPlayerMove().getText()) && !board[0][2].getPlayerMove().getText().isEmpty()) {
            won = true;
            winner = board[0][2].getPlayerMove().getText();
        }

        return won;
    }

    public void winMessage() {
        String msg;
        if (winner.equals("o")) {
            msg = "O won!";
        } else {
            msg = "X won!";
        }

        Alert gameEndAlert = new Alert(Alert.AlertType.INFORMATION);
        gameEndAlert.setTitle("game ended");
        gameEndAlert.setHeaderText(msg);
        gameEndAlert.showAndWait();
        // ChatRoom
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
        return true;
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
