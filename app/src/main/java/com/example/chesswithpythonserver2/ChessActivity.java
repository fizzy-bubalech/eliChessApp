package com.example.chesswithpythonserver2;

import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChessActivity extends AppCompatActivity {
    GridView boardGridView, piecesGridView;
    String[][] board;
   // TextView logText;
    static boolean selected;
    private int selectedPosition;
    ImageAdapterBoard imageAdapterBoard;
    ImageAdapterPieces imageAdapterPieces;
    // public static ChessPiece chessPiece;

    TextView textViewClkWhite, textViewClkBlack;
    private int whiteCounter10MilliSeconds = 0;
    private int whiteCounterSeconds = 0;
    private int whiteCounterMinutes = 0;
    private int whiteCounterHours = 0;
    private int blackCounter10MilliSeconds = 0;
    private int blackCounterSeconds = 0;
    private int blackCounterMinutes = 0;
    private int  blackCounterHours = 0;

    // clock threads loop flags
    private boolean runningClkWhite = false;
    private boolean runningClkBlack = false;

    // clock threads pause and resume
    private boolean whiteClkResume;
    private boolean blackClkResume;

    // sounds
    static SoundPool moveSoundWhite;
    static SoundPool moveSoundBlack;
    static SoundPool captureSound;
    static SoundPool chessPiecesFallSound;
    static SoundPool checkSound;
    static SoundPool drawSound;

    static int moveSoundWhiteID;
    static int moveSoundBlackID;
    static int captureSoundID;
    static int chessPiecesFallSoundID;
    static int checkSoundID;
    static int drawSoundID;

    static State state;
    private VoiceState voiceStateHuman = VoiceState.NONE;
    private VoiceState voiceStateComputer = VoiceState.NONE;

    private int level;

    private String SERVER_IP;
    private int SERVER_PORT;

    Thread ConnectThread = null;

    private final ArrayList<String> availableMovesSquares = new ArrayList<>();
    private String tempFromSquarePiece;
    private int tempFromLocation;

    private String fiveCharUCI;
    private final String[] pawnPromotionOptions = {
            "queen", "rook", "knight", "bishop"};

    private static String str_move_from_UCI;
    private static int move_to;


    // if the board view is black or white facing the player
    private ColorFacingBoard colorFacingBoard = ColorFacingBoard.WHITE;

    private ImageButton btnChoosePlayWhite;
    private ImageButton btnChoosePlayBlack;
    private Button btnConnectServer;
    private Button btnStartPlaying;
    private RadioButton r1, r2, r3;


    private TextView diffChooseText;
    private TextView chooseColorTextView;
    private RadioGroup group_view;

    // flag if is connected to server
    private boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);

        diffChooseText = findViewById(R.id.diffChooseText);
        chooseColorTextView = findViewById(R.id.chooseColorTextView);
        group_view = findViewById(R.id.group_view);

        r1 = findViewById(R.id.easy);
        r2 = findViewById(R.id.medium);
        r3 = findViewById(R.id.hard);

        /** create sounds for play later ...
         *
         */
        chessPiecesFallSound = new SoundPool(99, AudioManager.STREAM_MUSIC, 0);
        chessPiecesFallSoundID = chessPiecesFallSound.load(this, R.raw.chesspiecesfall, 1);

        moveSoundWhite = new SoundPool(99, AudioManager.STREAM_MUSIC, 0);
        moveSoundWhiteID = moveSoundWhite.load(this, R.raw.move_white, 1);

        moveSoundBlack = new SoundPool(99, AudioManager.STREAM_MUSIC, 0);
        moveSoundBlackID = moveSoundBlack.load(this, R.raw.move, 1);

        captureSound = new SoundPool(99, AudioManager.STREAM_MUSIC, 0);
        captureSoundID = captureSound.load(this, R.raw.capture, 1);

        checkSound = new SoundPool(99, AudioManager.STREAM_MUSIC, 0);
        checkSoundID = checkSound.load(this, R.raw.check, 1);

        drawSound = new SoundPool(99, AudioManager.STREAM_MUSIC, 0);
        drawSoundID = drawSound.load(this, R.raw.draw_sound, 1);

        btnChoosePlayBlack = findViewById(R.id.btnChoosePlayBlack);
        btnChoosePlayWhite = findViewById(R.id.btnChoosePlayWhite);
        btnChoosePlayWhite.setBackgroundResource(R.drawable.white_pawn_selected);
        btnConnectServer = findViewById(R.id.btnConnectServer);
        btnStartPlaying = findViewById(R.id.btnStartPlaying);

        colorFacingBoard = ColorFacingBoard.WHITE;
        // init board white facing
        board = new String[][]{{"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"},
                {"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"},
                {"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"}
        };

        // set textview of clocks
        textViewClkWhite = findViewById(R.id.textViewClockWhite);
        textViewClkWhite.setVisibility(View.INVISIBLE);
        textViewClkBlack = findViewById(R.id.textViewClockBlack);
        textViewClkBlack.setVisibility(View.INVISIBLE);

        piecesGridView = findViewById(R.id.piecesGreedView);
        boardGridView = findViewById(R.id.boardGreedView);


        // start playing
        state = State.GAME_START;

        // connect to server
        SERVER_IP = "192.168.1.14";
        SERVER_PORT = 7800;
        // logText.setText("Welcome to chess\n new game session started\n");
        ConnectThread = new Thread(new ThreadConnect());
        ConnectThread.start();
        Toast.makeText(this,"Connecting to server ...", Toast.LENGTH_SHORT).show();
        if (!isConnected)
            Toast.makeText(this,"Try again to connect", Toast.LENGTH_SHORT).show();

        btnChoosePlayWhite.setOnClickListener(v -> {
                btnChoosePlayWhite.setBackgroundResource(R.drawable.white_pawn_selected);
                btnChoosePlayBlack.setBackgroundResource(R.drawable.black_pawn);
                colorFacingBoard = ColorFacingBoard.WHITE;

                // init board white facing
                board = new String[][]{{"BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"},
                        {"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"},
                        {"", "", "", "", "", "", "", ""},
                        {"", "", "", "", "", "", "", ""},
                        {"", "", "", "", "", "", "", ""},
                        {"", "", "", "", "", "", "", ""},
                        {"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"},
                        {"WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"}
                };

        });

        btnChoosePlayBlack.setOnClickListener(v -> {
                btnChoosePlayBlack.setBackgroundResource(R.drawable.black_pawn_selected);
                btnChoosePlayWhite.setBackgroundResource(R.drawable.white_pawn);
                colorFacingBoard = ColorFacingBoard.BLACK;
                // init board black facing
                board = new String[][]{{"WR", "WN", "WB", "WK", "WQ", "WB", "WN", "WR"},
                        {"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"},
                        {"", "", "", "", "", "", "", ""},
                        {"", "", "", "", "", "", "", ""},
                        {"", "", "", "", "", "", "", ""},
                        {"", "", "", "", "", "", "", ""},
                        {"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"},
                        {"BR", "BN", "BB", "BK", "BQ", "BB", "BN", "BR"}
                };

        });

        btnStartPlaying.setOnClickListener(v -> {
                // if there is a connection to server then start playing
                if (isConnected) {
                    if (colorFacingBoard == ColorFacingBoard.WHITE)
                        state = State.WAIT_PLAYER_CHOOSE_MOVE;
                    else state = State.WAIT_SERVER_MOVE_FINISH;

                    // disable view of all buttons
                    btnConnectServer.setVisibility(View.INVISIBLE);
                    btnStartPlaying.setVisibility(View.INVISIBLE);
                    btnChoosePlayWhite.setVisibility(View.INVISIBLE);
                    btnChoosePlayBlack.setVisibility(View.INVISIBLE);
                    // disable view of all others
                    diffChooseText.setVisibility(View.INVISIBLE);
                    chooseColorTextView.setVisibility(View.INVISIBLE);
                    group_view.setVisibility(View.INVISIBLE);

                    imageAdapterBoard = new ImageAdapterBoard(ChessActivity.this);
                    boardGridView.setAdapter(imageAdapterBoard);
                    imageAdapterPieces = new ImageAdapterPieces(ChessActivity.this, board);
                    piecesGridView.setAdapter(imageAdapterPieces);

                    // send level and who starts - human or computer
                    String message;
                    level = 1;
                    if (r2.isChecked()) level = 2;
                    else if (r3.isChecked()) level = 3;

                    if (colorFacingBoard == ColorFacingBoard.WHITE) message = "human";
                    else message = "computer";
                    message = level + message;

                    // show clocks
                    textViewClkWhite.setVisibility(View.VISIBLE);
                    textViewClkBlack.setVisibility(View.VISIBLE);

                    // set clocks on running START - but paused at first
                    runningClkWhite = true;
                    runningClkBlack = true;

                    // set white clock on resume
                    whiteClkResume = true;

                    // start clocks threads
                    startWhiteClockThread();
                    startBlackClockThread();

                    // send message
                    new Thread(new ThreadSendToServer(message)).start();
                }
                else
                    Toast.makeText(ChessActivity.this,"No connection to Server, can't start", Toast.LENGTH_SHORT).show();

        });

        btnConnectServer.setOnClickListener(v -> {
                // connect to server
                SERVER_IP = "192.168.1.14";
                SERVER_PORT = 7800;
                // logText.setText("Welcome to chess\n new game session started\n");
                ConnectThread = new Thread(new ThreadConnect());
                ConnectThread.start();

                Toast.makeText(ChessActivity.this,"Connecting to server ...", Toast.LENGTH_SHORT).show();
                if (!isConnected)
                  Toast.makeText(ChessActivity.this,"Trying to connect", Toast.LENGTH_SHORT).show();


        });

        piecesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /** if the player can play, then check is move
                    check locations, cause the boards location are different
                 */
                position = change_position_value_client_to_server(position);
                if (state == State.WAIT_PLAYER_CHOOSE_MOVE) {
                    // if the player didn't selected a piece to move
                    if (!selected) {
                        // here we have to set all the available Moves Squares Pieces
                       for (int i = 0; i < availableMovesSquares.size(); i++) {
                            String str = availableMovesSquares.get(i).substring(0, 2);
                            int location = Integer.parseInt(str);
                            if (location == position) {

                                // save new selected position
                                selectedPosition = position;

                                // set available moves of the selected piece
                                // create array for board, 0 usual color , 1 - move color
                                int[] arr = new int[64];

                                for (int x = 0; x < availableMovesSquares.size(); x++) {
                                    if (Integer.parseInt(availableMovesSquares.get(x).substring(0, 2)) ==
                                            position) {
                                        str = availableMovesSquares.get(x).substring(3, 5);
                                        location = Integer.parseInt(str);
                                        // set this board location for available move of the selected piece
                                        int avl_move = Integer.parseInt(availableMovesSquares.get(x).substring(3,5));
                                        avl_move = change_position_value_client_to_server(avl_move);
                                        arr[avl_move] = 1;
                                    }
                                }
                                // refresh "chess-board" for the new view
                                imageAdapterBoard = new ImageAdapterBoard(ChessActivity.this, arr);
                                boardGridView.setAdapter(imageAdapterBoard);
                                selected = true;
                                break;
                            }
                            else {
                                // do nothing;
                            }
                        }

                    }

                    // the player has already selected a piece to move
                    else {
                        boolean foundALegalMove = false;
                        /** check if we are moving the right/selected piece
                            take the saved selected piece and compare it if it can move to this position
                         */
                        for (int i = 0; i < availableMovesSquares.size(); i++) {
                            String str_move_from = availableMovesSquares.get(i).substring(0, 2);
                            int move_from = Integer.parseInt(str_move_from);
                            String str_move_to = availableMovesSquares.get(i).substring(3, 5);
                            move_to = Integer.parseInt(str_move_to);



                            // check if a mach between position and move_from
                            if (position == move_to && selectedPosition == move_from) {
                                /** found match
                                // send the move, but before ...
                                // translate it to UCI format
                                 */
                                str_move_from_UCI =
                                        String.valueOf((char) (move_from % 8 + 97))
                                        + String.valueOf((char) (move_from / 8 + 49)) +
                                        String.valueOf((char) (move_to % 8 + 97))
                                        + String.valueOf((char) (move_to / 8 + 49));


                                // translate move
                                move_from = change_position_value_client_to_server(move_from);


                                /**
                                // used for "en passant" move, - see below "en passant" move
                                // we want to save to move to position before it is changed
                                // by the move action
                                 */
                                int move_to_Saved_For_En_passant = change_position_value_client_to_server(move_to);
                                String saveCurrentBoardSquareValue =
                                        board[(move_to_Saved_For_En_passant)/8][(move_to_Saved_For_En_passant)%8];

                                /**
                                // check if promotion of pawn to a queen/rook/night/knight
                                // use many-choice alertdialog
                                // check if BLACK pawn is going to be promoted
                                // a2a1, b2b1 ... h2h1
                                 */
                                if ((colorFacingBoard == ColorFacingBoard.BLACK && (str_move_from_UCI.equals("a2a1") ||
                                        str_move_from_UCI.equals("b2b1") ||
                                        str_move_from_UCI.equals("c2c1") || str_move_from_UCI.equals("d2d1") ||
                                        str_move_from_UCI.equals("e2e1") || str_move_from_UCI.equals("f2f1") ||
                                        str_move_from_UCI.equals("g2g1") || str_move_from_UCI.equals("h2h1"))
                                        && board[(move_from) / 8][move_from % 8].equals("BP")) ||

                                        (colorFacingBoard == ColorFacingBoard.WHITE && (str_move_from_UCI.equals("a7a8") ||
                                                str_move_from_UCI.equals("b7b8") ||
                                                str_move_from_UCI.equals("c7c8") || str_move_from_UCI.equals("d7d8") ||
                                                str_move_from_UCI.equals("e7e8") || str_move_from_UCI.equals("f7f8") ||
                                                str_move_from_UCI.equals("g7g8") || str_move_from_UCI.equals("h7h8"))
                                                && board[(move_from) / 8][move_from % 8].equals("WP")))
                                        {
                                    // start dialog for choosing the pawn promotion
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ChessActivity.this);
                                    builder.setTitle("Select pawn promotion");
                                    builder.setItems(pawnPromotionOptions, new DialogInterface.OnClickListener() {
                                        @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                            move_to = change_position_value_client_to_server(move_to);

                                            String s;

                                            if (colorFacingBoard == ColorFacingBoard.WHITE) s="W";
                                            else s = "B";

                                            if ("queen".equals(pawnPromotionOptions[which])) {
                                            str_move_from_UCI = str_move_from_UCI + "q";
                                                board[move_to / 8][move_to % 8] = s +"Q";
                                        } else if ("rook".equals(pawnPromotionOptions[which])) {
                                            str_move_from_UCI = str_move_from_UCI + "r";
                                                board[move_to / 8][move_to % 8] = s+"R";
                                        } else if ("bishop".equals(pawnPromotionOptions[which])) {
                                            str_move_from_UCI = str_move_from_UCI + "b";
                                                board[move_to / 8][move_to % 8] = s+"B";
                                        } else if ("knight".equals(pawnPromotionOptions[which])) {
                                            str_move_from_UCI = str_move_from_UCI + "n";
                                                board[move_to / 8][move_to % 8] = s+"N";
                                        }
                                        // send the move to server
                                        new Thread(new ThreadSendToServer(str_move_from_UCI)).start();
                                        }
                                    });
                                    builder.show();
                                }

                                // send the move to server
                                else {
                                    new Thread(new ThreadSendToServer(str_move_from_UCI)).start();
                                    // update board for the piece move
                                    // but before change location to client locations
                                    move_to = change_position_value_client_to_server(move_to);
                                   // move_from - we did change_position_value_client_to_server already before;

                                    board[move_to / 8][move_to % 8] =
                                            board[move_from / 8][move_from % 8];
                                }

                                board[(move_from) / 8][move_from % 8] = "";

                                /** check for castle
                                 *
                                 */
                                // for black
                                // small castle
                                // if facing color is white
                                if (colorFacingBoard == ColorFacingBoard.WHITE) {
                                    if (board[move_to / 8][move_to % 8].equals("WK") && move_to == 62 && move_from == 60) {
                                        // move the right white rook
                                        board[(move_from + 1)/ 8][(move_from + 1) % 8] = "WR";
                                        board[(move_to + 1) / 8][(move_to + 1) % 8] = "";
                                    }
                                }
                                // check facing color is black
                                if (colorFacingBoard == ColorFacingBoard.BLACK) {
                                    if (board[move_to / 8][move_to % 8].equals("BK") && move_to == 57 && move_from == 59) {
                                        // move the right black rook
                                        board[7][2] = "BR";
                                        board[7][0] = "";
                                    }
                                }
                                // big castle
                                if (colorFacingBoard == ColorFacingBoard.WHITE) {
                                    if (board[move_to / 8][move_to % 8].equals("WK") && move_to == 58 && move_from == 60) {
                                        // move the right white rook
                                        board[(move_from - 1)/ 8][(move_from - 1) % 8] = "WR";
                                        board[(move_to - 2) / 8][(move_to - 2) % 8] = "";
                                    }
                                }
                                if (colorFacingBoard == ColorFacingBoard.BLACK) {
                                    if (board[move_to / 8][move_to % 8].equals("BK") && move_to == 61 && move_from == 59) {
                                        // move the right black rook
                                        board[7][4] = "BR";
                                        board[7][7] = "";
                                    }
                                }

                                /** check for Human "en passant" move
                                 * see : picture en_passant.jpg in res --> drawable
                                 * The last rule about pawns is called “en passant,”
                                 * which is French for “in passing”.
                                 * If a pawn moves out two squares on its first move,
                                 * and by doing so lands to the side of
                                 * an opponent's pawn (effectively jumping past the other pawn's ability to capture it),
                                 * that other pawn has the option of capturing the first pawn as it passes by.
                                 * This special move must be done immediately after the first pawn has moved past,
                                 * otherwise the option to capture it is no longer available.
                                 *
                                 */

                                // if a computer pawn is moving to a location on board, where this new location is empty
                                // and it is moving to other column - then this is "en passant" move.

                                tempFromSquarePiece = board[move_from / 8][move_from % 8];
                                if (colorFacingBoard == ColorFacingBoard.WHITE) {
                                    if (tempFromSquarePiece.equals("WP") && saveCurrentBoardSquareValue.equals("") &&
                                            tempFromLocation - 8 != move_to_Saved_For_En_passant &&
                                            board[(move_to_Saved_For_En_passant + 8) / 8][(move_to_Saved_For_En_passant + 8) % 8].equals("BP")) {
                                        // take out the black pawn above this new location
                                        board[(move_to_Saved_For_En_passant + 8) / 8][(move_to_Saved_For_En_passant + 8) % 8] = "";
                                    }
                                }

                                if (colorFacingBoard == ColorFacingBoard.BLACK) {
                                    if (tempFromSquarePiece.equals("WP") && saveCurrentBoardSquareValue.equals("") &&
                                            tempFromLocation + 8 != move_to_Saved_For_En_passant &&
                                            board[(move_to_Saved_For_En_passant - 8) / 8][(move_to_Saved_For_En_passant - 8) % 8].equals("BP")) {
                                        // take out the black pawn above this new location
                                        board[(move_to_Saved_For_En_passant - 8) / 8][(move_to_Saved_For_En_passant - 8) % 8] = "";
                                    }
                                }

                                /** update pieces and board
                                 *
                                 */
                                int[] arr = new int[64];
                                for (i = 0; i < 64; i++) arr[i] = 0;
                                // refresh "chess-board" for the new view
                                imageAdapterBoard = new ImageAdapterBoard(ChessActivity.this, arr);
                                boardGridView.setAdapter(imageAdapterBoard);
                                imageAdapterPieces = new ImageAdapterPieces(ChessActivity.this, board);
                                piecesGridView.setAdapter(imageAdapterPieces);
                                voiceStateHuman = VoiceState.MOVE;
                                selected = false;
                                // update state to wait an answer from server
                                // WAIT_ANSWER_FROM_SERVER_LEGAL_MOVE, the move OK or ERROR
                                state = State.WAIT_ANSWER_FROM_SERVER_LEGAL_MOVE;

                                foundALegalMove = true;
                                break;

                            }



                        }
                        // if not a legal move (selected) then clean previous available moves
                        // and refresh the screen for clean view
                        if (!foundALegalMove) {
                            selected = false;
                            // update/clear from previous selection board
                            int[] arr = new int[64];
                            for (int i = 0; i < 64; i++) arr[i] = 0;
                            // refresh "chess-board" for the new view
                            imageAdapterBoard = new ImageAdapterBoard(ChessActivity.this, arr);
                            boardGridView.setAdapter(imageAdapterBoard);
                        }

                    }

                }
            }
        });
    }


    private PrintWriter output;
    private BufferedReader input;

    // this is a tread for connecting to server
    class ThreadConnect implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChessActivity.this,"Connected to Server", Toast.LENGTH_SHORT).show();
                        isConnected = true;
                        btnConnectServer.setVisibility(View.INVISIBLE);
                        state = State.GAME_START;

                    }
                });
                new Thread(new ThreadReceiveFromServer()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // this is a thread for sending a message to server
    class ThreadReceiveFromServer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               // logText.append("server: " + message + "\n");
                                // check the received message
                                check_message(message);
                            }
                        });
                    } else {
                        ConnectThread = new Thread(new ThreadConnect());
                        ConnectThread.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class ThreadSendToServer implements Runnable {
        private String message;
        ThreadSendToServer(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            Log.e("eli", "run: " + message);
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  //  logText.append("client: " + message + "\n");
                }
            });
        }
    }


    /** here we check the received messages from server
     * ***************************************************************************************
     * """
     * Errors:
     * ERROR 00 - Not a Legal move
     *
     * Commands:
     * COMMAND 00 - captured by Computer
     * COMMAND 01 - move from square
     * COMMAND 02 - move to square
     * COMMAND 03 - check by Computer
     * COMMAND 04 - Available moves Squares
     * COMMAND 05 - Promotion
     * COMMAND 06 - Drop
     * COMMAND 07 - CheckMate - Computer WON
     * COMMAND 08 - captured by Human
     * COMMAND 09 - check by Human
     * COMMAND 10 - CheckMate - Human WON
     * COMMAND 11 - Human turn
     * COMMAND 12 - OK
     * COMMAND 13 - Computer move was
     * COMMAND 14 - Human turn finished
     * COMMAND 15 - Stalemate
     * COMMAND 16 - insufficient material
     * COMMAND 17 -
     * COMMAND 18 - can claim threefold repetition
     * COMMAND 19 - can claim fifty moves
     * COMMAND 20 - can claim draw
     *
     * info:
     * INFO 01 - MoveHistory
     * INFO 02 - MoveHistory UCI
     * INFO 03 - Available moves
     * INFO 04 - Available moves UCI
     *
     * """
     * @param message message
     */
    public void check_message(String message) {
        // check for COMMAND
        if (message.startsWith("COMMAND")) {
            String contain = message.substring(0, 10);
            switch (contain) {
                // COMMAND 00 - captured by Computer
                case "COMMAND 00" : {
                    // set voice state to capture
                    voiceStateComputer = VoiceState.CAPTURE;
                    break;
                }
                // COMMAND 08 - captured by Human
                case "COMMAND 08" : {
                    // set voice state to capture
                    voiceStateHuman = VoiceState.CAPTURE;
                    break;
                }
                case "COMMAND 01" : {
                    // get the piece location from this square
                    String locationStr = message.substring("COMMAND 01 - move from square ".length());
                    int location = Integer.parseInt(locationStr);
                    location = change_position_value_client_to_server(location);
                    tempFromLocation = location;
                    tempFromSquarePiece = board[(location)/8][(location)%8];
                    board[(location)/8][(location)%8] = "";
                    break;
                }
                case "COMMAND 02" : {
                    // get the piece location from this square
                    String locationStr = message.substring("COMMAND 02 - move to square ".length());
                    int location = Integer.parseInt(locationStr);
                    location = change_position_value_client_to_server(location);

                    // used for "en passant" move, - see below "en passant" move
                    String saveCurrentBoardSquareValue = board[(location)/8][(location)%8];

                    board[(location)/8][(location)%8] = tempFromSquarePiece;

                    /**
                    // take care of pawn promotion:
                    // check if pawn 5 chars UCI move received, which is a promotion of a pawn
                     ---  like for example e7e8q - promote pawn to queen
                     */
                    if (!fiveCharUCI.equals("")) {
                        switch (fiveCharUCI) {
                            // case received UCI command to promote to queen
                            case "q" : {
                                // if human plays black
                                if (colorFacingBoard == ColorFacingBoard.BLACK)
                                    // then promote the received move, of the computer to white queen
                                    board[(location) / 8][(location) % 8] = "WQ";
                                // else human plays white
                                else
                                    // then promote the received move, of the computer to black queen
                                    board[(location) / 8][(location) % 8] = "BQ";
                                break;
                            }
                            // case received UCI command to promote to rook
                            case "r" : {
                                // if human plays black
                                if (colorFacingBoard == ColorFacingBoard.BLACK)
                                    // then promote the received move, of the computer to white rook
                                    board[(location) / 8][(location) % 8] = "WR";
                                    // else human plays white
                                else
                                    // then promote the received move, of the computer to black rook
                                    board[(location) / 8][(location) % 8] = "BR";
                                break;
                            }
                            // case received UCI command to promote to knight
                            case "n" : {
                                // if human plays black
                                if (colorFacingBoard == ColorFacingBoard.BLACK)
                                    // then promote the received move, of the computer to white knight
                                    board[(location) / 8][(location) % 8] = "WN";
                                    // else human plays white
                                else
                                    // then promote the received move, of the computer to black knight
                                    board[(location) / 8][(location) % 8] = "BN";
                                break;
                            }
                            // case received UCI command to promote to bishop
                            case "b" : {
                                // if human plays black
                                if (colorFacingBoard == ColorFacingBoard.BLACK)
                                    // then promote the received move, of the computer to white bishop
                                    board[(location) / 8][(location) % 8] = "WB";
                                    // else human plays white
                                else
                                    // then promote the received move, of the computer to black bishop
                                    board[(location) / 8][(location) % 8] = "BB";
                                break;
                            }
                            default:
                        }
                    }

                    /**
                    // take care for castling
                     */
                    // if facing black - Human plays Black
                    if (colorFacingBoard == ColorFacingBoard.BLACK) {
                        // small castle for white - computer
                        if (board[location / 8][location % 8].equals("WK") && location == 1 && tempFromLocation == 3) {
                            // move the right white rook
                            // 0  1  2  3  4  5  6  7
                            board[(location + 1 ) / 8][(location + 1) % 8] = "WR";
                            board[0][0] = "";
                        }
                        // big castle for white - computer
                        if (board[location / 8][location % 8].equals("WK") && location == 5 && tempFromLocation == 3)  {
                            // move the right white rook
                            board[(location - 1 ) / 8][(location - 1) % 8] = "WR";
                            board[(location + 2 ) / 8][(location + 2) % 8] = "";
                        }
                    }
                    else
                    // if facing white - Human plays white
                    {
                        // small castle for black - computer
                        if (board[location / 8][location % 8].equals("BK") && location == 6 && tempFromLocation == 4) {
                            // move the right black rook
                            // small castle
                            board[(location - 1)/ 8][(location - 1) % 8] = "BR";
                            board[(location + 1)/ 8][(location + 1) % 8] = "";
                        }
                        // big castle for black - computer
                        if (board[location / 8][location % 8].equals("BK") && location == 2 && tempFromLocation == 4) {
                            // move the right black rook
                            board[(location + 1)/ 8][(location + 1) % 8] = "BR";
                            board[0][0] = "";
                        }

                    }


                    /** check for Computer "en passant" move
                     * see : picture en_passant.jpg in res --> drawable
                     * The last rule about pawns is called “en passant,”
                     * which is French for “in passing”.
                     * If a pawn moves out two squares on its first move,
                     * and by doing so lands to the side of
                     * an opponent's pawn (effectively jumping past the other pawn's ability to capture it),
                     * that other pawn has the option of capturing the first pawn as it passes by.
                     * This special move must be done immediately after the first pawn has moved past,
                     * otherwise the option to capture it is no longer available.
                     *
                     */

                    // if a computer pawn is moving to a location on board, where this new location is empty
                    // and it is moving to other column - then this is "en passant" move.

                    if (colorFacingBoard == ColorFacingBoard.BLACK) {
                        if (tempFromSquarePiece.equals("WP") && saveCurrentBoardSquareValue.equals("") &&
                                tempFromLocation - 8 != location && board[(location + 8) / 8][(location + 8) % 8].equals("BP")) {
                            // take out the black pawn above this new location
                            board[(location + 8) / 8][(location + 8) % 8] = "";
                        }
                    }

                    if (colorFacingBoard == ColorFacingBoard.WHITE) {
                        if (tempFromSquarePiece.equals("WP") && saveCurrentBoardSquareValue.equals("") &&
                                tempFromLocation + 8 != location && board[(location - 8) / 8][(location - 8) % 8].equals("BP")) {
                            // take out the black pawn above this new location
                            board[(location - 8) / 8][(location - 8) % 8] = "";
                        }
                    }

                    // show Computer last move on board
                    int[] arr = new int[64];
                    arr[tempFromLocation] = 2;
                    arr[location] = 2;
                    // refresh "chess-board" for the new view
                    imageAdapterBoard = new ImageAdapterBoard(ChessActivity.this, arr);
                    boardGridView.setAdapter(imageAdapterBoard);

                    // refresh "chess-pieces"
                    imageAdapterPieces = new ImageAdapterPieces(ChessActivity.this, board);
                    piecesGridView.setAdapter(imageAdapterPieces);

                    // set voice state to move
                    voiceStateComputer = VoiceState.MOVE;
                    break;
                }
                // COMMAND 09 - check by Human
                case "COMMAND 09" : {
                    voiceStateHuman = VoiceState.CHECK;
                    break;
                }
                // COMMAND 09 - check by Computer
                case "COMMAND 03" : {
                    voiceStateComputer = VoiceState.CHECK;
                    break;
                }
                case "COMMAND 04" : {
                    // reset availableMovesSquares
                    availableMovesSquares.clear();
                    String squaresStr = message.substring("COMMAND 04 - Available moves Squares ".length());
                    // add extra space for 7 sizes
                    String tempSquareStr = squaresStr + " ";
                    for (int i=0; i < squaresStr.length(); i+=7) {
                        String oneSquareStr = tempSquareStr.substring(0, 5);
                        tempSquareStr = tempSquareStr.substring(7);
                        availableMovesSquares.add(oneSquareStr);
                        // 7 cause we have a comma and a space between the squares
                        // Available moves Squares  62:47, 62:45, 57:42, 57:40, 55:47, 54:46 .....
                    }
                    break;
                }

                // COMMAND 10 - CheckMate - Human WON
                case "COMMAND 10" :
                    voiceStateHuman = VoiceState.CHECKMATE;
                    //pause clocks
                    whiteClkResume = false;
                    blackClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play sound of checkmate
                    playSoundChessPiecesFall();
                    show_alert("You WON");
                    break;
                // COMMAND 07 - CheckMate - Computer WON
                case "COMMAND 07" : {
                    voiceStateComputer = VoiceState.CHECKMATE;
                    //pause clocks
                    whiteClkResume = false;
                    blackClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play sound of checkmate
                    playSoundChessPiecesFall();
                    show_alert("Computer WON");
                    break;
                }
                // COMMAND 11 - Human turn
                case "COMMAND 11" : {
                    // set state to wait for Human to make move
                    state = State.WAIT_PLAYER_CHOOSE_MOVE;
                    // here we play the sound state of Computer
                    switch (voiceStateComputer) {
                        case MOVE: playSoundBlackMove();
                            break;
                        case CAPTURE: playSoundCapture();
                            break;
                        case CHECK: playSoundCheck();
                            break;
                            // checkmate is played when the game ends at COMMAND 10 - CheckMate - Human WON
                        default: voiceStateComputer = VoiceState.NONE;
                    }

                    // set human clock on, computer clock off
                    // if human is white (computer is black)
                    if (colorFacingBoard == ColorFacingBoard.WHITE) {
                        blackClkResume = false;
                        whiteClkResume = true;
                        textViewClkWhite.setBackgroundColor(Color.YELLOW);
                        textViewClkBlack.setBackgroundColor(Color.WHITE);
                    }
                    else  {
                        blackClkResume = true;
                        whiteClkResume = false;
                        textViewClkWhite.setBackgroundColor(Color.WHITE);
                        textViewClkBlack.setBackgroundColor(Color.YELLOW);
                   }

                    break;
                }

                // COMMAND 14 - Human turn finished
                case "COMMAND 14" : {
                    // here we play the sound state of Human
                    switch (voiceStateHuman) {
                        case MOVE:
                            playSoundWhiteMove();
                            break;
                        case CAPTURE:
                            playSoundCapture();
                            break;
                        case CHECK:
                            playSoundCheck();
                            break;
                        // checkmate is played when the game ends at COMMAND 07 - CheckMate - Computer WON
                        default:
                            voiceStateHuman = VoiceState.NONE;
                    }

                }
                // COMMAND 12 - OK
                case "COMMAND 12" : {
                    // set state to wait for player to make move
                    state = State.WAIT_SERVER_MOVE_FINISH;

                    // set computer clock on, human clock off
                    // if computer is white (human is black)
                    if (colorFacingBoard == ColorFacingBoard.BLACK) {
                        blackClkResume = false;
                        whiteClkResume = true;
                        textViewClkWhite.setBackgroundColor(Color.YELLOW);
                        textViewClkBlack.setBackgroundColor(Color.WHITE);
                    }
                    else  {
                        blackClkResume = true;
                        whiteClkResume = false;
                        textViewClkWhite.setBackgroundColor(Color.WHITE);
                        textViewClkBlack.setBackgroundColor(Color.YELLOW);
                    }
                    break;
                }
                // COMMAND 13 - Computer move was
                case "COMMAND 13" : {
                    // check if special move like for example e7e8q - promote pawn to queen
                    // if so, save this five's char
                    String str = message.substring("COMMAND 13 - Computer move was ".length());
                    if (str.length() == 5) fiveCharUCI = str.substring(4);
                    else fiveCharUCI = "";
                    break;
                }

                /**
                 *  no WIN
                 */
                case "COMMAND 15":
                    // stop clocks, game ended
                    blackClkResume = false;
                    whiteClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play draw sound
                    playSoundDraw();
                    show_alert("Stalemate");
                    break;

                case "COMMAND 16":
                    // stop clocks, game ended
                    blackClkResume = false;
                    whiteClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play draw sound
                    playSoundDraw();
                    show_alert("Insufficient material");
                    break;

                case "COMMAND 18":
                    // stop clocks, game ended
                    blackClkResume = false;
                    whiteClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play draw sound
                    playSoundDraw();
                    show_alert("Claimed threefold repetition");
                    break;
                case "COMMAND 19":
                    // stop clocks, game ended
                    blackClkResume = false;
                    whiteClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play draw sound
                    playSoundDraw();
                    show_alert("Claimed fifty moves");
                    break;

                case "COMMAND 20":
                    // stop clocks, game ended
                    blackClkResume = false;
                    whiteClkResume = false;
                    // end game
                    state = State.GAME_START;
                    // play draw sound
                    playSoundDraw();
                    show_alert("claimed draw");
                    break;

                default:
            }
        }
        // check for ERROR
        if (message.startsWith("ERROR")) {

        }
        // check for INFO
        if (message.startsWith("INFO")) {


        }
    }


    private void startWhiteClockThread() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (runningClkWhite) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (whiteClkResume) {
                        handler.post(new Runnable() {
                            public void run() {
                                whiteCounter10MilliSeconds++;
                                if (whiteCounter10MilliSeconds > 99) {
                                    whiteCounterSeconds++;
                                    whiteCounter10MilliSeconds = 0;
                                }
                                if (whiteCounterSeconds > 59) {
                                    whiteCounterMinutes++;
                                    whiteCounterSeconds = 0;
                                    if (whiteCounterMinutes > 59) {
                                        whiteCounterHours++;
                                        whiteCounterMinutes = 0;
                                    }
                                }
                                String _10Millis;
                                String seconds;
                                String minutes;
                                if (whiteCounter10MilliSeconds < 10) _10Millis = "0" + whiteCounter10MilliSeconds;
                                else _10Millis = "" + whiteCounter10MilliSeconds;
                                if (whiteCounterSeconds < 10) seconds = "0" + whiteCounterSeconds;
                                else seconds = "" + whiteCounterSeconds;
                                if (whiteCounterMinutes < 10) minutes = "0" + whiteCounterMinutes;
                                else minutes = "" + whiteCounterMinutes;
                                String s = whiteCounterHours + ":" + minutes + ":" + seconds + ":" + _10Millis;
                                textViewClkWhite.setText(s);
                            }
                        });
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private void startBlackClockThread() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                while (runningClkBlack) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (blackClkResume) {
                        handler.post(new Runnable() {
                            public void run() {
                                blackCounter10MilliSeconds++;
                                if (blackCounter10MilliSeconds > 99) {
                                    blackCounterSeconds++;
                                    blackCounter10MilliSeconds = 0;
                                }
                                if (blackCounterSeconds > 59) {
                                    blackCounterMinutes++;
                                    blackCounterSeconds = 0;
                                    if (blackCounterMinutes > 59) {
                                        blackCounterHours++;
                                        blackCounterMinutes = 0;
                                    }
                                }
                                String _10Millis;
                                String seconds;
                                String minutes;
                                if (blackCounter10MilliSeconds < 10) _10Millis = "0" + blackCounter10MilliSeconds;
                                else _10Millis = "" + blackCounter10MilliSeconds;
                                if (blackCounterSeconds < 10) seconds = "0" + blackCounterSeconds;
                                else seconds = "" + blackCounterSeconds;
                                if (blackCounterMinutes < 10) minutes = "0" + blackCounterMinutes;
                                else minutes = "" + blackCounterMinutes;
                                String s = blackCounterHours + ":" + minutes + ":" + seconds + ":" + _10Millis;
                                textViewClkBlack.setText(s);
                            }
                        });
                    }
                }
            }

        };
        new Thread(runnable).start();
    }



    public int change_position_value_client_to_server(int position) {
        /** if facing color is white then do this
         // check locations, cause the boards location are different
         // here on client it is:
         *  0  1  2  3  4  5  6  7
         *  8  9 10 11 12 13 14 15
         * 16 17 18 19 20 21 22 23
         * 24 25 26 27 28 29 30 31
         * 32 33 34 35 36 37 38 39
         * 40 41 42 43 44 45 46 47
         * 48 49 50 51 52 53 54 55
         * 56 57 58 59 60 61 62 63
         *
         *
         * and on server it is:
         * 56 57 58 59 60 61 62 63
         * 48 49 50 51 52 53 54 55
         * 40 41 42 43 44 45 46 47
         * 32 33 34 35 36 37 38 39
         * 24 25 26 27 28 29 30 31
         * 16 17 18 19 20 21 22 23
         *  8  9 10 11 12 13 14 15
         *  0  1  2  3  4  5  6  7
         */
        // if facing color is white then do this
        if (colorFacingBoard == ColorFacingBoard.WHITE) {
            if (position < 8) position = position + 56;
            else if (position < 16) position = position + 40;
            else if (position < 24) position = position + 24;
            else if (position < 32) position = position + 8;
            else if (position < 40) position = position - 8;
            else if (position < 48) position = position - 24;
            else if (position < 56) position = position - 40;
            else if (position < 64) position = position - 56;
        }

        // if if facing color is black then do this
        /** if facing is color is black then do this
         // check locations, cause the boards location are different
         // here on client it is:
         * 63 62 61 60 59 58 57 56
         * 55 54 53 52 51 50 49 48
         * -- -- -- -- -- -- -- --
         *
         *
         *
         * 15 14 13 12 11 10  9  8
         *  7  6  5  4  3  2  1  0
         *
         *
         * and on server it is:
         * 56 57 58 59 60 61 62 63
         * 48 49 50 51 52 53 54 55
         * 40 41 42 43 44 45 46 47
         * 32 33 34 35 36 37 38 39
         * 24 25 26 27 28 29 30 31
         * 16 17 18 19 20 21 22 23
         *  8  9 10 11 12 13 14 15
         *  0  1  2  3  4  5  6  7
         */
        else {
            // if facing color is black then do this
            if (colorFacingBoard == ColorFacingBoard.BLACK) {
                if (position % 8 == 0) position = position + 7;
                else if (position % 8 == 1) position = position + 5;
                else if (position % 8 == 2) position = position + 3;
                else if (position % 8 == 3) position = position + 1;
                else if (position % 8 == 4) position = position - 1;
                else if (position % 8 == 5) position = position - 3;
                else if (position % 8 == 6) position = position - 5;
                else if (position % 8 == 7) position = position - 7;
            }

        }

        return position;
    }


    public static void playSoundWhiteMove() {
        moveSoundWhite.play(moveSoundWhiteID, 0.5f, 0.5f, 1, 0, 0.99f);
    }

    public static void playSoundBlackMove() {
        moveSoundBlack.play(moveSoundBlackID, 0.5f, 0.5f, 1, 0, 0.99f);
    }

    public static void playSoundChessPiecesFall() {
        chessPiecesFallSound.play(chessPiecesFallSoundID, 0.5f, 0.5f, 1, 0, 0.99f);
    }

    public static void playSoundCapture() {
        captureSound.play(chessPiecesFallSoundID, 0.5f, 0.5f, 1, 0, 0.99f);
    }

    public static void playSoundCheck() {
        checkSound.play(checkSoundID, 0.5f, 0.5f, 1, 0, 0.99f);
    }

    public static void playSoundDraw() {
        drawSound.play(drawSoundID, 0.5f, 0.5f, 1, 0, 0.99f);
    }

    // show alert dialog for game end
    public void show_alert(String s) {
        new AlertDialog.Builder(ChessActivity.this)
                .setTitle("Game Ended " + s)
                .setMessage("Do you want another game")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}