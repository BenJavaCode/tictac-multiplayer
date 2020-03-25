import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TicTacMain {

    State[][] gameArray;

    public TicTacMain(State[][] gameArray) {

        this.gameArray = gameArray;
    }

    public enum State{
        EMPTY,CROSS,CIRCLE;
    }

    public void initBoard(State[][] SA){

        for(int  i = 0; i < SA.length; i++){
            for (int j = 0; j<SA[i].length; j++){
                SA[i][j] = State.EMPTY;
            }
        }
    }

    public JSONObject makeMove(int player, State[][] SA) throws InputMismatchException, SocketException {

        System.out.println("it is player " + player + "'s turn" + "press 1-6, you can only press empty tiles!");

        //for sending to socket
        JSONObject jsonObject =  new JSONObject();

        //Logic
        State currentPlayer;
        if(player == 1){
            currentPlayer = State.CIRCLE;
        }else {
            currentPlayer = State.CROSS;
        }

        boolean x = false;

        while (!x){
            Scanner scanner = new Scanner(System.in);
            Scanner scanner2 = new Scanner(System.in);

            String in = scanner.next();
            String in2 = scanner2.next();

            try {
                int inp = Integer.parseInt(in);
                inp --;
                int inp2 = Integer.parseInt(in2);
                inp2--;

                //json for socket
                jsonObject.put("FM", inp);
                jsonObject.put("SM", inp2);

                if(inp >= 0 && inp<3 && inp2 >= 0 && inp2<3 && SA[inp][inp2] == State.EMPTY){
                    SA[inp][inp2] = currentPlayer;
                    x = true;
                }else {
                    System.out.println("Dette træk er ikke lovligt, prøv igen :(");
                }
            }catch (Exception idiot){
                System.out.println("Indtast et gyldigt nmr.. prøv igen");
            }





        }

     return jsonObject;
    }


    public void receiveMove(int player, State[][] SA, int first, int second) throws InputMismatchException {

        //Logic
        State currentPlayer;
        if(player == 1){
            currentPlayer = State.CIRCLE;
        }else {
            currentPlayer = State.CROSS;
        }

        boolean x = false;

        while (!x){

            try {

               int inp = first;
               int inp2 = second;

                if(inp >= 0 && inp<3 && inp2 >= 0 && inp2<3 && SA[inp][inp2] == State.EMPTY){
                    SA[inp][inp2] = currentPlayer;
                    x = true;
                }else {
                    System.out.println("Dette træk er ikke lovligt, prøv igen (modspiller):(");
                }
            }catch (Exception idiot){
                System.out.println("Indtast et gyldigt nmr.. prøv igen(modspiller)");
            }
        }

    }

    public int check(State[][] SA){

        //Horizontal
        for(int i = 0; i<SA.length; i++ ){
            if(SA[i][0]==SA[i][1] && SA[i][0] == SA[i][2] && SA[i][0] != State.EMPTY){
                if (SA[i][0] == State.CIRCLE){
                    return 2;
                }
                if (SA[i][0] == State.CROSS){
                    return 3;
                }
            }
        }

        //Vertical
        for(int i = 0; i<SA.length; i++){
            if(SA[0][i]==SA[1][i] && SA[2][i] == SA[0][i] && SA[i][0] != State.EMPTY){

                if (SA[0][i] == State.CIRCLE){
                    return 4;
                }
                if (SA[0][i] == State.CROSS){
                    return 5;
                }
            }
        }

        //Cross
        if(SA[0][0]==SA[1][1] && SA[0][0] == SA[2][2] && SA[0][0] != State.EMPTY){

            if (SA[0][0] == State.CIRCLE){
                return 6;
            }
            if (SA[0][0] == State.CROSS){
                return 7;
            }

        }else if(SA[0][2]==SA[1][1] && SA[0][2] == SA[2][0] && SA[0][2] != State.EMPTY){

            if (SA[0][2] == State.CIRCLE){
                return 6;
            }
            if (SA[0][2] == State.CROSS){
                return 7;
            }
        }

        //draw
        for(int i = 0; i < SA.length; i++){
            for(int j = 0; j<SA[i].length; j++){
                if (SA[i][j] == State.EMPTY){
                    return 1;
                }
            }
            return 8;
        }

        return 1;
    }


    public void printBoard(State[][] SA){

        for(int  i = 0; i < SA.length; i++){
            System.out.print("| ");
            for (int j = 0; j<SA[i].length; j++){


                System.out.print(SA[i][j]);
                System.out.print(" |");
            }
            System.out.println("");

        }
    }

    public void moveToGraphic(int x, int y){
        System.out.println("\u001b[" + y + ";" + x + "H"); //move to row y, and column x
    }

    public void clearScreen(){
        System.out.println("\u001b[2J"); // clear entire screen
    } // \u001b er unicode for escape, når [ kommer efter escape er vi klar til at modtage besked til consol cursoren

    public void clearLine(int line){
        System.out.println("\u001b[" + line + ";0H");
        System.out.println("\u001b[0J"); //clear from cursor to end pf screen
        System.out.println("\u001b[1J");//clear from cursor to beginning of scrreen
    }




    public static void main(String [] args){
        //init game logic
        State[][] arr = new State[3][3];
        TicTacMain board = new TicTacMain(arr);

        Scanner scan = new Scanner(System.in);

        int player = 1;
        int play = 1;
        //game init
        String playGame = "ja";

        //init socket connection
        try{
            System.out.println("listening..");
            ServerSocket serverSocket = new ServerSocket(7777);
            Socket socket = serverSocket.accept(); // listen
            System.out.println("connection established");

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!playGame.equals("nej") ){

                board.initBoard(arr);

                if(playGame.equals("nej") ){
                    play=1;
                }

                while(play==1){
                    //play game

                    //receive move
                    player = 1;
                    String incoming = bufferedReader.readLine();
                    JSONParser jsonParser = new JSONParser();
                    Object object = jsonParser.parse(incoming);
                    JSONObject jsonObject = (JSONObject)object;
                    Long Smsg = (Long) jsonObject.get("FM");
                    Long Fmsg = (Long) jsonObject.get("SM");
                    int zz = Math.toIntExact(Smsg);
                    int zy = Math.toIntExact(Fmsg);
                    board.receiveMove(player,arr,zz,zy);

                    play = board.check(arr);
                    if (play == 2){
                        System.out.println("Circle wins horizontally!");
                        break;
                    }
                    if (play == 3){
                        System.out.println("CRoSS wins horizontally!");
                        break;
                    }
                    if (play == 4){
                        System.out.println("Circle wins Vertically!");
                        break;
                    }
                    if (play == 5){
                        System.out.println("CROSS wins Vertically!");
                        break;
                    }
                    if (play == 6){
                        System.out.println("Circle wins Diagonally!");
                        break;
                    }
                    if (play == 7){
                        System.out.println("CRoss wins Diagonally!");
                        break;
                    }
                    if (play == 8){
                        System.out.println("Its a draaaaw :()..");
                        break;
                    }
                    board.printBoard(arr);
                    System.out.println("");



                    //make move
                    player = 2;
                    printWriter.println(board.makeMove(player,arr).toJSONString());

                    play = board.check(arr);
                    if (play == 2){
                        System.out.println("Circle wins horizontally!");
                        break;
                    }
                    if (play == 3){
                        System.out.println("CRoSS wins horizontally!");
                        break;
                    }
                    if (play == 4){
                        System.out.println("Circle wins Vertically!");
                        break;
                    }
                    if (play == 5){
                        System.out.println("CROSS wins Vertically!");
                        break;
                    }
                    if (play == 6){
                        System.out.println("Circle wins Diagonally!");
                        break;
                    }
                    if (play == 7){
                        System.out.println("CRoss wins Diagonally!");
                        break;
                    }
                    if (play == 8){
                        System.out.println("Its a draaaaw :()..");
                        break;
                    }
                    board.printBoard(arr);
                    System.out.println("");

                }

                System.out.println("Spil igen? tast 'nej' for at stoppe spillet.");
                playGame = scan.next();

            }


        }catch (Exception e){
            e.printStackTrace();
        }




    }

}
