package chatroom;

import com.google.gson.Gson;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class Cell extends StackPane {
    private Text playerMove;
    private Rectangle cellRectangle;
    private String[] cellItem={"x","o"};
    private static int index=1;
    private ChatRoom logic;


    public Text getPlayerMove() {
        return playerMove;
    }

    public Cell(ChatRoom logic){
        this.logic=logic;
        cellRectangle=new Rectangle(100,100);

        playerMove=new Text();
        cellRectangle.setFill(null);
        cellRectangle.setStroke(Color.BLACK);
        playerMove.setFont(Font.font(50));

        this.getChildren().addAll(cellRectangle,playerMove);
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(playerMove.getText().isEmpty()) {
                    //System.out.println("im playing vs"+logic.getOpponent());
                    if(logic.isX&&logic.getTurn().equals("o")) {
                        playerMove.setText("x");
                        logic.getPs().println("cell got clicked");
                        stringToJason();
                        System.out.println(logic.getTurn()+"*******");
                    }else if(!logic.isX&&logic.getTurn().equals("x")){
                            playerMove.setText("o");
                        logic.getPs().println("cell got clicked");
                            stringToJason();
                            System.out.println(logic.getTurn()+"-----");


                    }

            if(logic.isPlayWithBot()) {
                System.out.println("last index"+index);
                if (logic.playerWon()) {
                    logic.winMessage();
                    logic.cleanMap();
                    index = 1;
                    logic.ps.println("reset game");
                    return;

                }
                if (!logic.gameOver()) {
                    logic.botMove();
                    index=1;

                }


                if (logic.playerWon()) {
                    logic.winMessage();
                    System.out.println(index);
                    logic.cleanMap();
                    index = 1;
                    logic.ps.println("reset game");
                    return;
                }
                if(logic.gameOver()){
                    logic.drawMessage();
                    System.out.println(index);
                    logic.cleanMap();
                    index=1;
                    logic.ps.println("reset game");
                    return;
                }



            }

                }




            }
        });
    }
    public String getItem(){

        return cellItem[(index++) % (cellItem.length)];
    }
    public void stringToJason(){
        GameResponse gameResponse=new GameResponse();
        String [][]arr=logic.cellValues();
        gameResponse.setArr(arr);
        System.out.println("player1="+logic.getMyUserName());
        System.out.println("player2="+logic.getUsername());
       // gameResponse.setPlayer2(logic.getUsername());
        gameResponse.setPlayer1(logic.getMyUserName());
        Gson gson = new Gson();
        gson.toJson(gameResponse);
        logic.getPs().println(gson.toJson(gameResponse));
        System.out.println("test??");

    }


}
