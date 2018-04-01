package com.example.android.tic_tac_toe;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class NeuralNetwork {

    private double nodeActivation[][]= new double[4][9];//[0=i,1=h1,2=h2,3=o][node's row]
    private double nodeBias[][]= new double[3][9];//[0=h1,1=h2,2=o][node's row]
    private double weight[][][]= new double[3][9][9];//[0=i to h1,1=h1 to h2,2=h2 to o][initial node's row][final node's row]
    private int inputLookup[][]= {{0,1,2},{3,4,5},{6,7,8}};

    private double expectedActivation[] = new double[9];

    void saveWeights(Context context){
        StringBuilder stringBuilder =new StringBuilder();
        FileOutputStream outputStream;

        for (int c=0;c<=2;c++){
            for (int ni=0;ni<=8;ni++){
                for (int nf=0;nf<=8;nf++){
                    stringBuilder.append(weight[c][ni][nf]);
                    stringBuilder.append("\n");
                }
            }
        }

        try{
            outputStream = context.openFileOutput("weights", Context.MODE_PRIVATE);
            outputStream.write(stringBuilder.toString().getBytes());
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void saveBiases(Context context){
        StringBuilder stringBuilder =new StringBuilder();
        FileOutputStream outputStream;

        for (int c=0;c<=2;c++){
            for (int n=0;n<=8;n++){
                stringBuilder.append(nodeBias[c][n]);
                stringBuilder.append("\n");
            }
        }

        try{
            outputStream = context.openFileOutput("biases", Context.MODE_PRIVATE);
            outputStream.write(stringBuilder.toString().getBytes());
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void readWeights(Context context) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new FileReader(context.getFilesDir() + "/" + "weights"));
        String line;

        for (int c=0;c<=2;c++) {
            for (int ni = 0; ni <= 8; ni++) {
                for (int nf = 0; nf <= 8; nf++) {
                    if ((line = bufferedReader.readLine()) != null) {
                        double num = Double.parseDouble(line.trim());
                        weight[c][ni][nf] = num;
                    } else {
                        break;
                    }
                }
            }
        }
        bufferedReader.close();
        Log.d("process", "readWeights: Finished");
    }

    void readBiases(Context context) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new FileReader(context.getFilesDir() + "/" + "biases"));
        String line;

        for (int c=0;c<=2;c++){
            for (int n=0;n<=8;n++){
                if ((line = bufferedReader.readLine()) != null) {
                    double num = Double.parseDouble(line.trim());
                    nodeBias[c][n]=num;
                } else {
                    break;
                }
            }
        }
        bufferedReader.close();
        Log.d("process","readBiases: Finished");
    }

    void setInput(int r,int c,String value){
        switch (value){
            case "X":nodeActivation[0][inputLookup[r][c]]=1; break;
            case "O":nodeActivation[0][inputLookup[r][c]]=-1; break;
            default:Log.d("Error", "setInput: INVALID INPUT TRIED");
        }
    }

    void generateRandomWeights(){
        for (int c=0;c<=2;c++){
            for (int nInitial=0;nInitial<=8;nInitial++){
                for (int nFinal=0;nFinal<=8;nFinal++){
                    weight[c][nInitial][nFinal]=Math.random();
                }
            }
        }
    }

    public void clearBiases(){
        for (int c=0;c<=2;c++){
            for (int n=0;n<=8;n++){
                nodeBias[c][n]=0;
            }
        }
    }

    private static double sigmoid(double x){
        return 1/(1+Math.exp(-x));
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private double inputWeightActivationSum(int c, int n){
        double z=0;
        for (int i=0;i<=8;i++){
            //sum all of the products of the activations of the previous column multiplied by the weight connecting the current node to it.
            z+=(weight[c-1][i][n]*nodeActivation[c-1][i]);
        }
        return z;
    }

    void forwardPropagate(){
        for (int c=1;c<=3;c++){
            for (int n=0; n<=8;n++){
                //Log.d("Activations", "forwardPropagate: current calculation col: "+c+" node: "+n);
                //activation for node at (c,n) is the node's bias plus the weight times activation passed to it.
                nodeActivation[c][n]=sigmoid(inputWeightActivationSum(c,n)+nodeBias[c-1][n]);
                //Log.d("Activations", "forwardPropagate: "+Arrays.deepToString(nodeActivation));
            }
        }
    }

    int[] getPickedCellOrder(){
        int rankedCells[]= new int[9];
        int orderedCells[]=new int[9];
        double outputNodeValues[]=new double[9];

        System.arraycopy(nodeActivation[3],0, outputNodeValues,0,9);
        Log.d("CopyCheck", "getPickedCell: values copied over: "+ Arrays.toString(outputNodeValues));

        for (int i=0;i<=8;i++){
            int count=0;
            for (int j=0;j<=8;j++){
                if (outputNodeValues[j]>outputNodeValues[i]){
                    count++;
                }
            }
            rankedCells[i]=count;
        }

        for (int i=0;i<=8;i++){
            orderedCells[rankedCells[i]]=i;
        }

        Log.d("OrderCheck", "getPickedCellOrder: Picked order"+Arrays.toString(rankedCells));
        Log.d("OrderCheck", "getPickedCellOrder: Selected cell"+Arrays.toString(orderedCells));
        return orderedCells;
    }

    void backPropagate(String boardState[][]){
        //set all expected values to 0 except for i which goes to 1
        for (int i=0;i<=8;i++){
            expectedActivation[i]=0;
        }
        //find expected move
        String board[]= new String[9];
        for (int r=0;r<=2;r++){
            for (int c=0;c<=2;c++){
                board[inputLookup[r][c]]=boardState[r][c];
            }
        }
        int position=miniMax(board,1);
        Log.d("process", "backPropagate: Position Expected: "+position);
        if (position==10|position==-10){return;}
        expectedActivation[position]=1;
        double weightGradient[][][]=getWeightGradient();
        for (int c=0;c<=2;c++){
            for (int i=0;i<=8;i++){
                for (int f=0;f<=8;f++){
                    weight[c][i][f]=weight[c][i][f]-weightGradient[c][i][f];
                }
            }
        }
        double biasGradient[][]=getBiasGradient();
        for (int c=0;c<=2;c++){
            for (int r=0;r<=8;r++){
                nodeBias[c][r]=nodeBias[c][r]-biasGradient[c][r];
            }
        }
        Log.d("process", "backPropagate: Complete");
    }

    private double[][][] getWeightGradient(){
        double weightGradient[][][]=new double[3][9][9];
        for (int c=0;c<=2;c++){
            for (int i=0;i<=8;i++){
                for (int f=0;f<=8;f++){
                    weightGradient[c][i][f]=0;
                }
            }
        }
        //col 2 weights
        for (int i=0;i<=8;i++){
            for (int r=0;r<=8;r++){
                double a=2*(nodeActivation[3][r]-expectedActivation[r]);
                double b=(sigmoid(inputWeightActivationSum(3,r)+nodeBias[2][r]))*(1-(sigmoid(inputWeightActivationSum(3,r)+nodeBias[2][r])));
                double c=nodeActivation[2][i];
                weightGradient[2][i][r]=a*b*c;
            }
        }
        //col 1 weights
        for (int f=0;f<=8;f++){
            for (int i=0;i<=8;i++){
                for (int r=0;r<=8;r++){
                    double a=2*(nodeActivation[3][r]-expectedActivation[r]);
                    double b=(sigmoid(inputWeightActivationSum(3,r)+nodeBias[2][r]))*(1-(sigmoid(inputWeightActivationSum(3,r)+nodeBias[2][r])));
                    double c=weight[2][f][r];
                    double d=(sigmoid(inputWeightActivationSum(2,f)+nodeBias[1][f]))*(1-(sigmoid(inputWeightActivationSum(2,f)+nodeBias[1][f])));
                    double e=nodeActivation[1][i];
                    weightGradient[1][i][f]+=(a*b*c*d*e)/9;
                }
            }
        }
        //col 0 weights
        for (int r0=0;r0<=8;r0++){
            for (int k=0;k<=8;k++){
                for (int j=0;j<=8;j++){
                    double a=2*(nodeActivation[3][r0]-expectedActivation[r0]);
                    double b=(sigmoid(inputWeightActivationSum(3,r0)+nodeBias[2][r0]))*(1-(sigmoid(inputWeightActivationSum(3,r0)+nodeBias[2][r0])));
                    double c=0;
                    for (int i=0; i<=8;i++){
                        c=+(sigmoid(inputWeightActivationSum(2,i)+nodeBias[1][i]))*(1-(sigmoid(inputWeightActivationSum(2,i)+nodeBias[1][i])));
                    }
                    double d=(sigmoid(inputWeightActivationSum(1,j)+nodeBias[0][j]))*(1-(sigmoid(inputWeightActivationSum(1,j)+nodeBias[0][j])));
                    double e=nodeActivation[0][k];
                    weightGradient[0][k][j]+=(a*b*c*d*e)/9;
                }
            }
        }
        return weightGradient;
    }

    private double[][] getBiasGradient(){
        double biasGradient[][] = new double[3][9];
        for (int c=0;c<=2;c++){
            for (int r=0;r<=8;r++){
                biasGradient[c][r]=0;
            }
        }
        //col 2 biases
        for (int r=0;r<=8;r++){
            double a=2*(nodeActivation[3][r]-expectedActivation[r]);
            double b=(sigmoid(inputWeightActivationSum(3,r)+nodeBias[2][r]))*(1-(sigmoid(inputWeightActivationSum(3,r)+nodeBias[2][r])));
            biasGradient[2][r]=a*b;
        }
        //col 1 biases
        for (int r0=0;r0<=8;r0++){
            for (int rB=0;rB<=8;rB++){
                double a=2*(nodeActivation[3][r0]-expectedActivation[r0]);
                double b=(sigmoid(inputWeightActivationSum(3,r0)+nodeBias[2][r0]))*(1-(sigmoid(inputWeightActivationSum(3,r0)+nodeBias[2][r0])));
                double c=weight[2][rB][r0];
                double d=(sigmoid(inputWeightActivationSum(2,rB)+nodeBias[1][rB]))*(1-(sigmoid(inputWeightActivationSum(2,rB)+nodeBias[1][rB])));
                biasGradient[1][rB]+=(a*b*c*d)/9;
            }
        }
        //col 0 biases
        for (int r0=0;r0<=8;r0++){
            for (int j=0;j<=8;j++){
                double a=2*(nodeActivation[3][r0]-expectedActivation[r0]);
                double b=(sigmoid(inputWeightActivationSum(3,r0)+nodeBias[2][r0]))*(1-(sigmoid(inputWeightActivationSum(3,r0)+nodeBias[2][r0])));
                double c=0;
                for (int i=0; i<=8;i++){
                    c=+(sigmoid(inputWeightActivationSum(2,i)+nodeBias[1][i]))*(1-(sigmoid(inputWeightActivationSum(2,i)+nodeBias[1][i])));
                }
                double d=(sigmoid(inputWeightActivationSum(1,j)+nodeBias[0][j]))*(1-(sigmoid(inputWeightActivationSum(1,j)+nodeBias[0][j])));
                biasGradient[0][j]+=(a*b*c*d)/9;
            }
        }
        return biasGradient;
    }



    private int miniMax(String board[], int p){
        String player[]={"X","O"};
        int victor = checkWin(board);
        if (victor!=0){return score(victor);}
        if (p==1){
            int bestValue = Integer.MIN_VALUE;
            int bestSpot =0;

            for (int i=0;i<board.length;i++){
                if (!board[i].equals("")){continue;}
                board[i]=player[p];
                int value=miniMax(board,0);
                if (value>bestValue){
                    bestValue=value;
                    bestSpot=i;
                }
                board[i]="";
            }
            return bestSpot;
        }else{
            int bestValue = Integer.MAX_VALUE;
            int bestSpot = 0;
            for(int i=0;i<board.length;i++){
                if (!board[i].equals("")){continue;}
                board[i]=player[p];
                int value=miniMax(board,1);
                if (value<bestValue){
                    bestValue=value;
                    bestSpot=i;
                }
                board[i]="";
            }
            return bestSpot;
        }
    }

    @Contract(pure = true)
    private int score(int gameState)
    {
        if(gameState==2){ //O wins.
            return 10;
        }else if(gameState==1){ //X wins
            return -10;
        }
        return 0;
    }

    private int checkWin(@NotNull String board[]){
        String player[]={"X","O"};
        String boardState[][]={{board[0],board[1],board[2]},{board[3],board[4],board[5]},{board[6],board[7],board[8]}};
        for (int p=0;p<=1;p++){
            //check player win in rows
            for (int r=0;r<=2;r++){
                for (int c=0;c<=2;c++){
                    if(!boardState[r][c].equals(player[p])){break;}
                    if(c==2){
                        return p+1;
                    }
                }
            }
            //check player win in columns
            for (int c=0;c<=2;c++){
                for (int r=0;r<=2;r++){
                    if(!boardState[r][c].equals(player[p])){break;}
                    if(r==2){
                        return p+1;
                    }
                }
            }
            //check player win in negative diagonal
            for (int d=0;d<=2;d++){
                if(!boardState[d][d].equals(player[p])){break;}
                if(d==2){
                    return p+1;
                }
            }
            //check player win in positive diagonal
            for (int d=0;d<=2;d++){
                if(!boardState[d][2-d].equals(player[p])){break;}
                if(d==2){
                    return p+1;
                }
            }
        }
        for (int c=0;c<=2;c++){
            for (int r=0;r<=2;r++){
                if (boardState[r][c].equals("")){return 0;}
                if ((r==2)&&(c==2)){
                    return 3;
                }
            }
        }
        return 0;
    }
}
