package com.example.cocaro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.util.Date;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    InitialActivity initialActivity = new InitialActivity();
    private TextView tvTurn;
    private GridLayout gridLayout;
    private Button[][] buttons;
    private int rows = 17;
    private int columns = 11;
    private boolean isPlayerX = true;
    private boolean gameEnded = false;
    private Stack<Move> moveHistory = new Stack<>();
    SQLiteDatabase sqlitedb;
    Dialog dialog,dialog1,dialog2;
    private static class Move {
        int row;
        int col;
        char player;

        Move(int row, int col, char player) {
            this.row = row;
            this.col = col;
            this.player = player;
        }
    }
    Button btnUndo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showRulesDialog();

        tvTurn = findViewById(R.id.tvTurn);
        gridLayout = findViewById(R.id.gridLayout);
        buttons = new Button[rows][columns];
        Button btnBack = findViewById(R.id.btnBack);
        btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setEnabled(false);
        Button btnNewGame = findViewById(R.id.btnNewGame);
        sqlitedb = openOrCreateDatabase("ResultHistory.db", MODE_PRIVATE, null);
        try{
            String createQuery = "CREATE TABLE tblhistory(game INTEGER primary key AUTOINCREMENT,winner TEXT , time Text)";
            sqlitedb.execSQL(createQuery);
        }catch(Exception e){
            Log.e("Error", "This table exists");
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.show();
            }
        });

        //exit
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_question);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialogbox));
        dialog.setCancelable(false);

        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnEXIT = dialog.findViewById(R.id.btnDialogExit);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnEXIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        //replay
        dialog1 = new Dialog(MainActivity.this);
        dialog1.setContentView(R.layout.question_replay);
        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialogbox));
        dialog1.setCancelable(false);

        Button btnCancel1 = dialog1.findViewById(R.id.btnDialogCancel);
        Button btnReplay = dialog1.findViewById(R.id.btnDialogExit);

        btnCancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
            }
        });

        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
                resetGame();
            }
        });

        //undo
        dialog2 = new Dialog(MainActivity.this);
        dialog2.setContentView(R.layout.question_undo);
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialogbox));
        dialog2.setCancelable(false);

        Button btnCancel2 = dialog2.findViewById(R.id.btnDialogCancel);
        Button btnUNDO = dialog2.findViewById(R.id.btnDialogExit);

        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
            }
        });

        btnUNDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
                undoMove();
            }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.show();
            }
        });

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.show();
            }
        });

        initializeBoard();

    }

    private void showRulesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Luật chơi")
                .setMessage(
                        "- Hai người chơi đóng vai trò bên X (màu đỏ, sẽ đi trước) và bên O (màu xanh, sẽ đi sau) , sau đó sẽ luân phiên chơi.\n" +
                        "- Người chơi sẽ chiến thắng khi hoàn thành 5 dấu X hoặc O thẳng hàng theo chiều ngang, chiều dọc và hai chiều chéo.\n")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void initializeBoard() {
        int cellSize = calculateCellSize();
        gridLayout.setColumnCount(columns);
        gridLayout.setRowCount(rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int row = i;
                int col = j;
                buttons[i][j] = new Button(this);
                buttons[i][j].setTextSize(16);
                buttons[i][j].setPadding(2, 2, 2, 2);
                buttons[i][j].setOnClickListener(v -> makeMove(row, col));
                buttons[i][j].setBackground(getResources().getDrawable(R.drawable.button_background));
                buttons[i][j].setTextColor(getResources().getColor(R.color.default_text_color));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(7, 9, 6, 6);
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                gridLayout.addView(buttons[i][j], params);
            }
        }
    }

    private int calculateCellSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int minDimension = Math.min(width, height);
        int cellSize = minDimension / Math.max(columns, rows);
        return cellSize;
    }

    private void makeMove(int row, int col) {
        if (buttons[row][col].getText().toString().isEmpty()) {
            btnUndo.setEnabled(true);
            char player = isPlayerX ? 'X' : 'O';
            buttons[row][col].setText(String.valueOf(player));
            buttons[row][col].setTextColor(getResources().getColor(player == 'X' ? R.color.x_text_color : R.color.o_text_color));
            if (checkWinner(row, col)) {
                gameEnded = true;
                char winner = isPlayerX ? 'X' : 'O';
                tvTurn.setBackgroundColor(Color.YELLOW);
                tvTurn.setTextColor(Color.RED);
                tvTurn.setText("THE WINNER IS "+ winner);
                showWinnerDialog(winner);
                LocalDate winnerAtTime = LocalDate.now();
                ContentValues values = new ContentValues();
                values.put("winner", String.valueOf(winner));
                values.put("time", String.valueOf(winnerAtTime));
                String mess = "";
                if(sqlitedb.insert("tblhistory", null, values) == -1){
                    mess="Fail to Insert Record!";
                }
                else{
                    mess="The winner is " + winner;
                }
                Toast.makeText(MainActivity.this, mess, Toast.LENGTH_LONG).show();
            }
            else {
                tvTurn.setText("TURN : " + (player == 'X' ? "O" : "X"));
            }
            isPlayerX = !isPlayerX;
            moveHistory.push(new Move(row, col, player));
        }
    }

    private boolean checkWinner(int row, int col) {
        char player = isPlayerX ? 'X' : 'O';
        String playerText = String.valueOf(player);

        int count = 1;
        int c = col - 1;
        while (c >= 0 && buttons[row][c].getText().toString().equals(playerText)) {
            count++;
            c--;
        }
        c = col + 1;
        while (c < columns && buttons[row][c].getText().toString().equals(playerText)) {
            count++;
            c++;
        }
        if (count >= 5) return true;

        count = 1;
        int r = row - 1;
        while (r >= 0 && buttons[r][col].getText().toString().equals(playerText)) {
            count++;
            r--;
        }
        r = row + 1;
        while (r < rows && buttons[r][col].getText().toString().equals(playerText)) {
            count++;
            r++;
        }
        if (count >= 5) return true;

        count = 1;
        r = row + 1;
        c = col + 1;
        while (r < rows && c < columns && buttons[r][c].getText().toString().equals(playerText)) {
            count++;
            r++;
            c++;
        }
        r = row - 1;
        c = col - 1;
        while (r >= 0 && c >= 0 && buttons[r][c].getText().toString().equals(playerText)) {
            count++;
            r--;
            c--;
        }
        if (count >= 5) return true;

        count = 1;
        r = row + 1;
        c = col - 1;
        while (r < rows && c >= 0 && buttons[r][c].getText().toString().equals(playerText)) {
            count++;
            r++;
            c--;
        }
        r = row - 1;
        c = col + 1;
        while (r >= 0 && c < columns && buttons[r][c].getText().toString().equals(playerText)) {
            count++;
            r--;
            c++;
        }
        return count >= 5;
    }
    private void showWinnerDialog(char winner){
        btnUndo.setEnabled(false);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.winner_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_main));
        MediaPlayer mp_winning = MediaPlayer.create(this, R.raw.winning);
        mp_winning.setVolume(0.3f,0.3f);
        mp_winning.start();
        TextView tv_content  = dialog.findViewById(R.id.tvcontent);
        Button btnreplay = dialog.findViewById(R.id.btn_replay),
                btncancel = dialog.findViewById(R.id.btn_cancel),
                btnhistory =dialog.findViewById(R.id.btn_history);

        tv_content.setText("Player " + winner + " wins!");
        btnreplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnUndo.setEnabled(true);
                mp_winning.stop();
                mp_winning.release();
                resetGame();
                dialog.dismiss();
            }
        });
        btnhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentHist = new Intent(MainActivity.this, HistoryActivity.class);
                setButtonsFalse();
                startActivity(intentHist);
                dialog.dismiss();
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                setButtonsFalse();
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    private void undoMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            buttons[lastMove.row][lastMove.col].setText("");
            buttons[lastMove.row][lastMove.col].setTextColor(getResources().getColor(R.color.default_text_color));
            tvTurn.setText("TURN : " + (lastMove.player == 'X' ? "X" : "O"));
            isPlayerX = (lastMove.player == 'X');
        }
    }

    private void resetGame() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                buttons[i][j].setText("");
                setButtonsTrue();
                tvTurn.setBackground(ContextCompat.getDrawable(this, R.drawable.turn_indicator_background));
                tvTurn.setTextColor(Color.BLACK);
            }
        }

        isPlayerX = true;
        tvTurn.setText("TURN : X");
        moveHistory.clear();
    }

    private void setButtonsFalse() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void setButtonsTrue() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                buttons[i][j].setEnabled(true);
            }
        }
    }
}
