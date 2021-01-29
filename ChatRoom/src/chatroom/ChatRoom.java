/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroom;

import chatroom.Classes.MyMessage;
import chatroom.Classes.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    Button alertInitButton;
    Label alertLabel;
    TextArea textMessageArea;
    TextField loginUserTextField;
    PasswordField loginpwBox;
    private String winner;
    private BorderPane borderPane;
    private Button exitButton;
    private GridPane gridPane;
    private Cell[][] board = new Cell[3][3];
    private String map[][];
    private String turn="o";
    boolean isX;
    private String myUserName;
    private boolean playWithBot;
    private boolean resumeGame;
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("My chat room!");

        //primaryStage.setScene(scene);
        scene = new Scene(login(), 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

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
                    System.out.println("tag6");
                    try {
                        str = dis.readLine();
                        System.out.println("tag5"+str);


                        if (str.equals("registration failed")) {
                            errorLabel.setVisible(true);
                            System.out.println("registraaation faaaaileeed");
                        } else if (str.equals("login failed")) {
                            errorLabel.setVisible(true);
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
                            alertLabel.getScene().setRoot(requestPage(str));
                            System.out.println(str + "sent");
                            flagName = "";
                        } else if (str.equals("accept chat")) {
                            flagName = "accept";
                        } else if (flagName.equals("accept")) {
                            System.out.println("playervs--- " + str);
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

                        }else if(flagName.equals("get map")){
                            Gson gson=new Gson();
                            System.out.println(str);
                            GameResponse gameResponse=gson.fromJson(str,GameResponse.class);

                            if(!gameResponse.getTurn().isEmpty()){
                                turn=gameResponse.getTurn();
                                System.out.println("client"+ gameResponse.getTurn());
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

                            }else  if(gameResponse.isDraw()){

                                drawMessage();
                            }
                            flagName="";
                        }else if(str.equals("myName")){
                            flagName="getName";
                        }else if(flagName.equals("getName")){
                            myUserName=str;
                            flagName="";
                        }else if(str.equals("update game")){

                            flagName="get map";
                        }else if(str.equals("resume game")){
                            flagName="res game";

                        }else if(flagName.equals("res game")){
                            Gson gson=new Gson();

                            map=gson.fromJson(str,String[][].class);
                           resumeGame=true;
                        }
                        System.out.println(str);

                    }catch (IOException ex) {
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
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        errorLabel = new Label("Invalid credentials");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        grid.add(errorLabel, 1, 5);
        Text scenetitle = new Text("Welcome to tic tac toe ");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);
        loginUserTextField = new TextField();
        grid.add(loginUserTextField, 1, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);
        loginpwBox = new PasswordField();
        grid.add(loginpwBox, 1, 2);
        Button loginButton = new Button("Sign in");
        Button registerButton = new Button("Register");
        HBox hbBtn = new HBox(10);

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

        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(loginButton, registerButton);
        grid.add(hbBtn, 1, 4);
        return grid;
    }

    public void playerLogin() {
        Player player = new Player(loginUserTextField.getText(), loginpwBox.getText());
        ps.println("login");
        ps.println(new Gson().toJson(player));
    }

    public GridPane register() {
        GridPane gridPane = new GridPane();
        errorLabel = new Label("Invalid credentials");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(40, 40, 40, 40));

        gridPane.setHgap(10);

        gridPane.setVgap(10);

        ColumnConstraints columnOneConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        columnOneConstraints.setHalignment(HPos.RIGHT);

        ColumnConstraints columnTwoConstrains = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnTwoConstrains.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstrains);

        Label headerLabel = new Label("Registeration");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gridPane.add(headerLabel, 0, 0, 2, 1);
        GridPane.setHalignment(headerLabel, HPos.LEFT);
        GridPane.setMargin(headerLabel, new Insets(0, 0, 10, 0));

        //add username
        Label username = new Label("USER Name ");
        gridPane.add(username, 0, 1);

        // Add username field
        TextField usernameField = new TextField();
        usernameField.setPrefHeight(40);
        gridPane.add(usernameField, 1, 1);

        // Add nickname
        Label nickname = new Label("Nick Name ");
        gridPane.add(nickname, 0, 2);

        // Add nickname field 
        TextField nicknameField = new TextField();
        nicknameField.setPrefHeight(40);
        gridPane.add(nicknameField, 1, 2);

        // Add password
        Label password = new Label("password");
        gridPane.add(password, 0, 3);

        // Add password field
        PasswordField passwordfield = new PasswordField();
        passwordfield.setPrefHeight(40);
        gridPane.add(passwordfield, 1, 3);

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
        grid.setVgap(4);
        grid.setHgap(10);
        grid.setPadding(new Insets(2, 2, 2, 2));

        // option list 
        final ComboBox playerComboBox = new ComboBox();
        System.out.println(playerList.size() + " number");
        for (Player pp : playerList) {
            playerComboBox.getItems().add(
                    pp.getUsername() + "\t" + pp.getPoints()
            );
        }

        playerComboBox.setValue("please choose one player");

        // handle of option list 
        playerComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(playerComboBox.getSelectionModel().getSelectedIndex());
                int index = playerComboBox.getSelectionModel().getSelectedIndex();
                username = playerList.get(index).getUsername();
            }

        });

        // text 
        Label headerLabel = new Label("let's play "+myUserName);
        headerLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
        GridPane.setHalignment(headerLabel, HPos.CENTER);

        // radio button
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton radioButton1 = new RadioButton("computer");
        RadioButton radioButton2 = new RadioButton("player");
        radioButton1.setToggleGroup(radioGroup);
        radioButton2.setToggleGroup(radioGroup);
        radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {

                RadioButton chk = (RadioButton)t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
                System.out.println("Selected Radio Button - "+chk.getText());
                if(chk.getText().equals("computer")){
                    playWithBot=true;
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

        playButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("heyaaaaaaaaaaaa");
                if(playWithBot==false) {
                    ps.println("chat");
                    ps.println(username);
                    isX = true;
                }else {


                    playButton.getScene().setRoot(playPage(""));
                    isX=true;
                }

            }
        });

        alertLabel = new Label("alert");
        alertLabel.setVisible(false);
        System.out.println("Alert Initialized" + alertLabel.getText());

        /*alertInitButton = new Button("alert");
        
         alertInitButton.setOnAction(new EventHandler<ActionEvent>() {

         @Override
         public void handle(ActionEvent event) {
         Alert alert = new Alert(AlertType.INFORMATION);
         alert.setTitle("About");
         alert.setHeaderText(null);
         alert.setContentText("Created by: Ahmed Atef\nCreated on: 10-01-2021");
         alert.showAndWait();
         }
         });*/
        //add text/optionlist/radiobutton /playbuttton to grid
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
                if(playWithBot==false) {
                    ps.println("exit");
                    System.exit(0);
                }else{
                    exitButton.getScene().setRoot(mainPage());
                    ps.println("save map");
                    Gson gson=new Gson();
                   String map= gson.toJson(cellValues(),String[][].class);
                    ps.println(map);
                    playWithBot=false;
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
        if(resumeGame){
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (map!=null) {
                        board[i][j].getPlayerMove().setText(map[i][j]);
                    }
                }

            }
        }
        borderPane.setCenter(gridPane);
        borderPane.setRight(root);


        return borderPane;

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
        //username=user;
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
                System.out.println(user);
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
                turn="o";
                isX=false;
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

    public void drawMessage(){
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
    public boolean playerWon(){
        //row
        boolean won=false;
        for(int i=0;i<board.length;i++ ){
            if(board[i][0].getPlayerMove().getText().equals(board[i][1].getPlayerMove().getText()) &&
                    board[i][0].getPlayerMove().getText().equals(board[i][2].getPlayerMove().getText())&&
                    !board[i][0].getPlayerMove().getText().isEmpty()){
                System.out.println("player:"+board[i][0].getPlayerMove().getText()+"won");
                won=true;
                winner=board[i][0].getPlayerMove().getText();
            }
        }
        //column
        for(int i=0;i<board.length;i++ ){
            if(board[0][i].getPlayerMove().getText().equals(board[1][i].getPlayerMove().getText()) &&
                    board[0][i].getPlayerMove().getText().equals(board[2][i].getPlayerMove().getText())&&
                    !board[0][i].getPlayerMove().getText().isEmpty()){
                System.out.println("player:"+board[i][0].getPlayerMove().getText()+"won");
                won=true;
                winner=board[0][i].getPlayerMove().getText();
            }
        }
        //diagonal
        if(board[0][0].getPlayerMove().getText().equals(board[1][1].getPlayerMove().getText()) &&
                board[0][0].getPlayerMove().getText().equals(board[2][2].getPlayerMove().getText())&&!board[0][0].getPlayerMove().getText().isEmpty()){
            System.out.println("player:"+board[0][0].getPlayerMove().getText()+"won");
            won=true;
            winner=board[0][0].getPlayerMove().getText();
        }
        if(board[0][2].getPlayerMove().getText().equals(board[1][1].getPlayerMove().getText())&&
                board[0][2].getPlayerMove().getText().equals(board[2][0].getPlayerMove().getText())&&!board[0][2].getPlayerMove().getText().isEmpty()){
            System.out.println("player:"+board[0][0].getPlayerMove().getText()+"won");
            won=true;
            winner=board[0][2].getPlayerMove().getText();
        }

        return won;
    }
    public void winMessage(){
        String msg;
        if(winner.equals("o")){
            msg="O won!";
        }else {
            msg="X won!";
        }



        Alert gameEndAlert=new Alert(Alert.AlertType.INFORMATION);
        gameEndAlert.setTitle("game ended");
        gameEndAlert.setHeaderText(msg);
        gameEndAlert.showAndWait();
       // ChatRoom
    }
    public void botMove(){

        while (true){
            int x= new Random().nextInt(2-0+1)+0;
            int y=new Random().nextInt(2-0+1)+0;
            if(board[x][y].getPlayerMove().getText().isEmpty()){
                board[x][y].getPlayerMove().setText("o");
                break;
            }
        }

    }

    public boolean gameOver(){
        for(int i=0;i<board.length;i++){
            for(int j=0;j<board[i].length;j++){
                if(board[i][j].getPlayerMove().getText().isEmpty()){
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
