package socketserverchat.Classes;

public class Room {
    private String player1;
    private String player2;
    private int index;

    private String[] cellItem={"x","o"};
    public Room(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Room() {
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }



    public void setPlayer2(String player2) {
        this.player2 = player2;
    }
    public String getItem(){

        return cellItem[(index++) % (cellItem.length)];
    }

    @Override
    public String toString() {
        return "Player1="+this.player1+",player2="+this.player2;
    }
}
