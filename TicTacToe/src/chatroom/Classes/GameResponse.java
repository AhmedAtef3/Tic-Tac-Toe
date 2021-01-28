package chatroom.Classes;

public class GameResponse {
    private String turn;
    private String[][] arr;
    private boolean gameOver;
    private boolean draw;
    private String player1;
    private String player2;


    public GameResponse(String turn, String[][] arr, boolean gameOver, boolean draw, String player1, String player2) {
        this.turn = turn;
        this.arr = arr;
        this.gameOver = gameOver;
        this.draw = draw;
        this.player1 = player1;
        this.player2 = player2;
    }

    public String getPlayer2() {
        return player2;
    }

    public String getTurn() {
        return turn;
    }

    public String[][] getArr() {
        return arr;
    }

    public boolean isGameOver() {
        return gameOver;
    }



    public GameResponse(String turn, String[][] arr, boolean gameOver, boolean draw, String player2) {
        this.turn = turn;
        this.arr = arr;
        this.gameOver = gameOver;
        this.draw = draw;
        this.player2 = player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public GameResponse() {
    }



    public void setTurn(String turn) {
        this.turn = turn;
    }

    public void setArr(String[][] arr) {
        this.arr = arr;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }
}
