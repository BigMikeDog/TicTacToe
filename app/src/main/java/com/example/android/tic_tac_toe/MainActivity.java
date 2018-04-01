package com.example.android.tic_tac_toe;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button topLeftSquare;
    Button topMiddleSquare;
    Button topRightSquare;
    Button middleLeftSquare;
    Button middleMiddleSquare;
    Button middleRightSquare;
    Button bottomLeftSquare;
    Button bottomMiddleSquare;
    Button bottomRightSquare;

    Button twoPlayerBegin;
    Button onePlayerBegin;

    Boolean playerOneTurn=true;
    Boolean AIGame;

    String boardState[][] = new String[3][3];
    Button buttonLookup[][] = new Button[3][3];

    String playerOneSymbol;
    String playerTwoSymbol;
    String emptySymbol;

    NeuralNetwork network = new NeuralNetwork();

    Context context;

    int moves=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=getApplicationContext();

        topLeftSquare=findViewById(R.id.button_top_left);
        topMiddleSquare=findViewById(R.id.button_top_middle);
        topRightSquare=findViewById(R.id.button_top_right);
        middleLeftSquare=findViewById(R.id.button_middle_left);
        middleMiddleSquare=findViewById(R.id.button_middle_middle);
        middleRightSquare=findViewById(R.id.button_middle_right);
        bottomLeftSquare=findViewById(R.id.button_bottom_left);
        bottomMiddleSquare=findViewById(R.id.button_bottom_middle);
        bottomRightSquare=findViewById(R.id.button_bottom_right);

        twoPlayerBegin=findViewById(R.id.button_player_select_2);
        onePlayerBegin=findViewById(R.id.button_player_select_1);

        buttonLookup[0][0]=topLeftSquare;
        buttonLookup[0][1]=topMiddleSquare;
        buttonLookup[0][2]=topRightSquare;
        buttonLookup[1][0]=middleLeftSquare;
        buttonLookup[1][1]=middleMiddleSquare;
        buttonLookup[1][2]=middleRightSquare;
        buttonLookup[2][0]=bottomLeftSquare;
        buttonLookup[2][1]=bottomMiddleSquare;
        buttonLookup[2][2]=bottomRightSquare;

        playerOneSymbol= getResources().getString(R.string.player_one_symbol);
        playerTwoSymbol= getResources().getString(R.string.player_two_symbol);
        emptySymbol= getResources().getString(R.string.empty_symbol);

        clearBoard();

        File weightFile = new File(context.getFilesDir(),"weights");
        File biasFile = new File(context.getFilesDir(),"biases");
        Log.d("process", "Internal Storage Location: "+getFilesDir().getAbsolutePath());
        if (weightFile.exists()){
            try{
                network.readWeights(context);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            network.generateRandomWeights();
            network.saveWeights(context);
            Log.d("process", "onCreate: Weights file created and initiated with random weights.");
        }
        if (biasFile.exists()){
            try{
                network.readBiases(context);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            network.saveBiases(context);
            Log.d("process", "onCreate: Biases file created.");
        }

        Log.d("process", "onCreate: Finished");
    }

    //Button Click Events Being Handles
    public void topLeftClicked(View view){
        updateBoard(0,0,false);
    }
    public void topMiddleClicked(View view){
        updateBoard(0,1,false);
    }
    public void topRightClicked(View view){
        updateBoard(0,2,false);
    }
    public void middleLeftClicked(View view){
        updateBoard(1,0,false);
    }
    public void middleMiddleClicked(View view){
        updateBoard(1,1,false);
    }
    public void middleRightClicked(View view){
        updateBoard(1,2,false);
    }
    public void bottomLeftClicked(View view){
        updateBoard(2,0,false);
    }
    public void bottomMiddleClicked(View view){
        updateBoard(2,1,false);
    }
    public void bottomRightClicked(View view){
        updateBoard(2,2,false);
    }

    public void resetBoard(View view){
        clearBoard();
    }

    public void twoPlayerBegin(View view){
        //reset board
        clearBoard();
        //make board interactive
        enableBoard();
        //Make button not clickable
        twoPlayerBegin.setClickable(false);
        onePlayerBegin.setClickable(false);
        AIGame=false;
    }

    public void onePlayerBegin(View view){
        //reset board
        clearBoard();
        //make board interactive
        enableBoard();
        //make button not clickable
        onePlayerBegin.setClickable(false);
        twoPlayerBegin.setClickable(false);
        AIGame=true;
    }

    public void clearBoard(){
        for (int c=0;c<=2;c++){
            for (int r=0;r<=2;r++){
                updateBoard(r,c,true);
            }
        }
        twoPlayerBegin.setClickable(true);
        onePlayerBegin.setClickable(true);
        moves=0;
    }

    public void disableBoard(){
        for (int c=0;c<=2;c++){
            for (int r=0;r<=2;r++){
                buttonLookup[r][c].setClickable(false);
            }
        }
    }

    public void enableBoard(){
        for (int c=0;c<=2;c++){
            for (int r=0;r<=2;r++){
                buttonLookup[r][c].setClickable(true);
            }
        }
    }

    public void updateBoard(int row,int col,boolean clear){
        if(clear){
            buttonLookup[row][col].setText(R.string.empty_symbol);
            buttonLookup[row][col].setClickable(false);
            buttonLookup[row][col].setTextColor(getResources().getColor(R.color.colorBlack));
            boardState[row][col]=emptySymbol;
            playerOneTurn=true;
        }else{
            if (boardState[row][col].equals(emptySymbol)){
                if(playerOneTurn){
                    buttonLookup[row][col].setText(playerOneSymbol);
                    buttonLookup[row][col].setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    boardState[row][col]=playerOneSymbol;
                    network.setInput(row,col,playerOneSymbol);
                    playerOneTurn=false;
                }else{
                    buttonLookup[row][col].setText(playerTwoSymbol);
                    buttonLookup[row][col].setTextColor(getResources().getColor(R.color.colorAccentDark));
                    boardState[row][col]=playerTwoSymbol;
                    network.setInput(row,col,playerTwoSymbol);
                    playerOneTurn=true;
                    if (AIGame){
                        enableBoard();
                    }
                }
                buttonLookup[row][col].setClickable(false);
                Log.d("boardState", "updateBoard: "+Arrays.deepToString(boardState));
                if (checkForWin()){return;}
                if (AIGame&&(!playerOneTurn)){
                    disableBoard();
                    tAI.run();
                }
            }
        }
    }

    public boolean checkForWin(){
        String player[]={playerOneSymbol,playerTwoSymbol};
        moves++;
        for (int p=0;p<=1;p++){
            //check player win in rows
            for (int r=0;r<=2;r++){
                for (int c=0;c<=2;c++){
                    if(!boardState[r][c].equals(player[p])){break;}
                    if(c==2){
                        Log.d("winner", "checkForWin: "+player[p]);
                        toastWinner(player[p]);
                        disableBoard();
                        return true;
                    }
                }
            }
            //check player win in columns
            for (int c=0;c<=2;c++){
                for (int r=0;r<=2;r++){
                    if(!boardState[r][c].equals(player[p])){break;}
                    if(r==2){
                        Log.d("winner", "checkForWin: "+player[p]);
                        toastWinner(player[p]);
                        disableBoard();
                        return true;
                    }
                }
            }
            //check player win in negative diagonal
            for (int d=0;d<=2;d++){
                if(!boardState[d][d].equals(player[p])){break;}
                if(d==2){
                    Log.d("winner", "checkForWin: "+player[p]);
                    toastWinner(player[p]);
                    disableBoard();
                    return true;
                }
            }
            //check player win in positive diagonal
            for (int d=0;d<=2;d++){
                if(!boardState[d][2-d].equals(player[p])){break;}
                if(d==2){
                    Log.d("winner", "checkForWin: "+player[p]);
                    toastWinner(player[p]);
                    disableBoard();
                    return true;
                }
            }
        }
        if (moves==9){
            Log.d("catGame", "checkForWin: Cat's Game");
            toastTie();
            disableBoard();
            return true;
        }
        return false;
    }

    private void toastWinner(final String player){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast winnerToast=Toast.makeText(context,getString(R.string.winning_toast,player),Toast.LENGTH_LONG);
                winnerToast.show();
            }
        });
    }

    private void toastTie(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast draw=Toast.makeText(context,getString(R.string.cat_game),Toast.LENGTH_LONG);
                draw.show();
            }
        });
    }

    Thread tAI = new Thread(new Runnable() {
        @Override
        public void run() {
            AITurn();
        }
    });

    public void AITurn(){
        network.forwardPropagate();

        int row=0;
        int col=0;
        int sel=-0;
        int cellOrder[]=network.getPickedCellOrder();
        for (int i=0;i<=8;i++){
            switch (cellOrder[i]){
                case 0:
                    row=0;
                    col=0;
                    sel=0;
                    break;
                case 1:
                    row=0;
                    col=1;
                    sel=1;
                    break;
                case 2:
                    row=0;
                    col=2;
                    sel=2;
                    break;
                case 3:
                    row=1;
                    col=0;
                    sel=3;
                    break;
                case 4:
                    row=1;
                    col=1;
                    sel=4;
                    break;
                case 5:
                    row=1;
                    col=2;
                    sel=5;
                    break;
                case 6:
                    row=2;
                    col=0;
                    sel=6;
                    break;
                case 7:
                    row=2;
                    col=1;
                    sel=7;
                    break;
                case 8:
                    row=2;
                    col=2;
                    sel=8;
                    break;
                default:
                    Log.d("UNEXPECTED", "AITurn: AI returns non 0-8 choice.");
            }

            if (boardState[row][col].equals(emptySymbol)){
                updateBoard(row,col,false);
                Log.d("process", "AITurn: Position Chosen: "+sel);
                break;
            }
        }
        network.backPropagate(boardState);
        network.saveWeights(context);
        network.saveBiases(context);
    }
}