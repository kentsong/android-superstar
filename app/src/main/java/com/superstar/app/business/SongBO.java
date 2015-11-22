package com.superstar.app.business;

import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.superstar.app.dao.SongDAO;
import com.superstar.app.vo.SongVO;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Kent on 2015/11/9.
 */
public class SongBO {

    private SQLiteDatabase db;
    private AssetManager am;
    public SongDAO songDAO;


    public SongBO(SQLiteDatabase db, AssetManager am) {
        this.db = db;
        this.am = am;
        songDAO = new SongDAO(db);

    }



    public void syncAssetsSongDataToDB() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader( am.open("song.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                SongVO songVO = this.generateSongVO(line);
                songDAO.addSong(songVO);
            }

            System.out.println("Done!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SongVO generateSongVO (String line) {
        String[] args = line.split(",");
        //有些歌曲沒有演唱者,所以檢驗長度是2
        if(args.length == 2) {
            return new SongVO(args[0], args[1], "");
        }
        else if(args.length == 3) {
            return new SongVO(args[0], args[1], args[2]);
        } else {
            Log.i("flag", "請檢察song.txt資料格式, readString:" +line);
            throw new IllegalArgumentException("讀取song.txt資料格式有誤");
        }

    }

    public SongDAO getSongDAO() {
        return songDAO;
    }

    public AssetManager getAm() {
        return am;
    }
}
