package com.nishchhal.tic_tac_toe.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Controller
public class GameSocketController extends TextWebSocketHandler  {
 
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private char[] board = {'-', '-', '-', '-', '-', '-', '-', '-', '-'};
    private final List<Player> players = new ArrayList<>();
    private Player currentPlayerTurn;
    private int currentTurnPlayerIndex;

    @MessageMapping("/join")       // Listens to /app/join
    public void PlayerJoined(String playerName) throws Exception {
        if(players.size() >= 2){
            System.err.println("CANNOT ADD MORE PLAYERS");
            return;
        }
        if(players.isEmpty()){
            players.add(new Player(playerName, 'X'));
        }
        else{
            players.add(new Player(playerName, 'O'));
        }

        currentPlayerTurn = players.get(0);
        currentTurnPlayerIndex = 0;
        System.err.println("################################ Set Player and broadcasting now");

        BroadcastPlayers();
        BroadcastTurnUpdate();
    }

    @MessageMapping("/endTurn")
    public void EndTurn(Move move){
        System.err.println("################################ EndTurn " + move.playerName + " AND " + move.index);
        System.err.println("################################ END TURN CurrentPlayer " + currentPlayerTurn.name);
        if(!move.playerName.equals(currentPlayerTurn.name)){
            System.err.println("################################ END TURN Return " + move.playerName.equals(currentPlayerTurn.name));
            return;
        }

        System.err.println("################################ SET BOARD " + currentPlayerTurn.name);
        // Set Board from current player
        board[move.index] = currentPlayerTurn.symbol;

        if(CheckGameOver()){
            System.err.println("################################ GAME OVER " + currentPlayerTurn.name);
            BroadcastBoardUpdate();
            return;
        }

        // End the Turn
        currentTurnPlayerIndex +=1;
        currentTurnPlayerIndex = currentTurnPlayerIndex % 2;
        currentPlayerTurn = players.get(currentTurnPlayerIndex);

        System.err.println("################################ NOT GAME OVER " + currentPlayerTurn.name);
        BroadcastBoardUpdate();
        BroadcastTurnUpdate();
    }

    public void BroadcastPlayers() {
        messagingTemplate.convertAndSend("/topic/players", players);
    }

    public void BroadcastTurnUpdate() {
        messagingTemplate.convertAndSend("/topic/turn", currentPlayerTurn);
    }

    public void BroadcastBoardUpdate() {
        messagingTemplate.convertAndSend("/topic/boardState", board);
    }

    public void BroadcastGameOver(GameOverState gameOverState) {
         messagingTemplate.convertAndSend("/topic/gameOver", gameOverState);
    }

    private boolean CheckGameOver(){
        if(CurrentPlayerWin()){
            System.err.println("################################ Player " + currentPlayerTurn.name + " Won the game");
            BroadcastGameOver(new GameOverState("Win", currentPlayerTurn));
            return true;
        }

        if(IsBoardFilled()){
            System.err.println("################################ Board is filled and is a Draw");
            BroadcastGameOver(new GameOverState("Draw", null));
            return true;
        }

        return false;
    }

    private boolean IsBoardFilled(){
        for (char c : board) {
            if(c == '-'){
                return false;
            }
        }

        return true;
    }

    private boolean CurrentPlayerWin(){
        // All possible winning line index sets
        int[][] WINNING_LINES = {
            {0, 1, 2},  // rows
            {3, 4, 5},
            {6, 7, 8},

            {0, 3, 6},  // columns
            {1, 4, 7},
            {2, 5, 8},

            {0, 4, 8},  // diagonals
            {2, 4, 6}
        };

        for (int[] line : WINNING_LINES) {
            if (board[line[0]] == currentPlayerTurn.symbol &&
                board[line[1]] == currentPlayerTurn.symbol &&
                board[line[2]] == currentPlayerTurn.symbol) {
                return true;
            }
        }
        return false;
    }

    record Player(String name, char symbol){}
    record GameOverState(String state, Player winner){}
    record Move(String playerName, int index) {}
}

