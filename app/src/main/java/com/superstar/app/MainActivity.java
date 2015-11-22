package com.superstar.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.superstar.app.business.SongBO;
import com.superstar.app.vo.SongVO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Kent on 2015/11/9.
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Button btnAddSong;
    private Button btnFavorite;
    private Button btnSearch;
    private EditText edtText;
    private Spinner spnMusicRole;
    private ListView musicView;
    private SongBO songBO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        db = openOrCreateDatabase("songDB", MODE_PRIVATE, null);
        songBO = new SongBO(db, super.getAssets());
        this.checkIsTableCreated();

        btnSearch = (Button)findViewById(R.id.btnSaerch);
        btnSearch.setOnClickListener(searchSongListener);

        musicView = (ListView)findViewById(R.id.musicView);
        edtText = (EditText)findViewById(R.id.queryText);

        spnMusicRole = (Spinner)findViewById(R.id.spnMusicRole);
        ArrayAdapter adapterRoles = ArrayAdapter.createFromResource(this, R.array.roles, R.layout.spinner_item);
        adapterRoles.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spnMusicRole.setAdapter(adapterRoles);

        btnAddSong = (Button)findViewById(R.id.btnAddSong);
        btnAddSong.setOnClickListener(addSongListener);

        btnFavorite = (Button)findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(favoriteListener);

    }

    private Button.OnClickListener searchSongListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Cursor cursor;
            String queryString = edtText.getText().toString();
            if("歌曲名".equals(spnMusicRole.getSelectedItem().toString())){
                //以歌曲名查詢
                cursor = songBO.getSongDAO().querySongByName(queryString);
            } else {
                //以演唱者查詢
                cursor = songBO.getSongDAO().querySongBySinger(queryString);
            }

            displayMusicView(cursor);
        }
    };

    private Button.OnClickListener addSongListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            showAddSongDialog();
        }
    };


    protected void showAddSongDialog() {
        // get add_song_dialog.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.add_song_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);
        final EditText editAddSongName = (EditText) promptView.findViewById(R.id.editAddSongName);
        final EditText editAddSinger= (EditText) promptView.findViewById(R.id.editAddSinger);
        final EditText editAddSongNum = (EditText) promptView.findViewById(R.id.editAddSongNum);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
            .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (isEmpty(editAddSongNum.getText().toString()) || isEmpty(editAddSongName.getText().toString()) ||
                        isEmpty(editAddSinger.getText().toString())) {
                        Toast.makeText(MainActivity.this, "新增失敗,每個項目都要填寫", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SongVO songVO = new SongVO(
                            editAddSongNum.getText().toString(),
                            editAddSongName.getText().toString(),
                            editAddSinger.getText().toString());

                    songBO.getSongDAO().addSong(songVO);
                    Toast.makeText(MainActivity.this, "新增成功,曲名:" + editAddSongName.getText().toString(),
                            Toast.LENGTH_SHORT).show();

                }
            })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                }
            });

        // create an alert dialog
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private boolean isEmpty(String str){
        if(str == null || str.equals(""))
            return true;
        return false;
    }

    private void displayMusicView(Cursor cursor) {
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, R.layout.songlist, cursor, new String[] { "num", "name", "singer" },
                new int[] { R.id.textNum, R.id.textName, R.id.textSinger }, 0);
        musicView.setAdapter(dataAdapter);
        musicView.setOnItemLongClickListener(itemLongClickListener);

        //hidden 虛擬鍵盤
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if(!cursor.moveToFirst())
            Toast.makeText(MainActivity.this, "查無資料!", Toast.LENGTH_SHORT).show();
    }

    private ListView.OnItemLongClickListener itemLongClickListener = new ListView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            SQLiteCursor cursor = (SQLiteCursor) parent.getItemAtPosition(position);
            final Integer songId = cursor.getInt(0);
            String songName = cursor.getString(2);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("刪除曲目")
                    .setMessage("是否刪除歌曲:"+ songName)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            songBO.getSongDAO().deleteSongById(songId);
                            displayMusicView(songBO.getSongDAO().queryAllSong());
                            Toast.makeText(MainActivity.this,  "刪除成功", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();

            return false;
        }
    };

    private Button.OnClickListener favoriteListener = new Button.OnClickListener() {
        @Override
        public void onClick(View paramView) {
            Toast.makeText(MainActivity.this, "開發中，暫無功用!", Toast.LENGTH_SHORT).show();
        }
    };


    private void checkIsTableCreated(){
            Log.i("flag", "isTableExist():" + songBO.getSongDAO().isTableExist());
            if(!songBO.getSongDAO().isTableExist()) {
                songBO.getSongDAO().createTable();
                new MainActivity.asyncTaskUpdateProgress().execute();
            }
    }

    //將song.txt同步歌曲至SQLite
    public class asyncTaskUpdateProgress extends AsyncTask<Void, Integer, Void> {
        ProgressDialog progressDialog;
        int musicSum = 18141;//歌曲數量

        public asyncTaskUpdateProgress() {}

        protected Void doInBackground(Void[] paramArrayOfVoid) {

            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader( songBO.getAm().open("song.txt")));
                String line;
                while(progressDialog.getProgress() < progressDialog.getMax()) {

                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                        SongVO songVO = songBO.generateSongVO(line);
                        songBO.getSongDAO().addSong(songVO);
                        progressDialog.incrementProgressBy(1);

                    }
                }
                System.out.println("Done!");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void paramVoid) {
            displayMusicView(songBO.getSongDAO().queryAllSong());
            this.progressDialog.dismiss();
        }

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("歌曲初始化");
            progressDialog.setMessage("請稍待數分鐘...");
            progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(musicSum);
            progressDialog.show();
        }
    }

    //當螢幕旋轉時觸發,防止螢幕旋轉Activity killed
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
            Log.i("flag","onConfigurationChanged = ORIENTATION_LANDSCAPE");
        }
        else {
            // 什麼都不用寫
            Log.i("flag","onConfigurationChanged");
        }
    }


    @Override
    protected void onDestroy() {
        Log.i("flag", "onDestroy");
        super.onDestroy();
        db.close();
    }
}
