package com.superstar.app.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.superstar.app.vo.SongVO;

/**
 * Created by Kent on 2015/11/9.
 */
public class SongDAO {

    private SQLiteDatabase db;

    public SongDAO(SQLiteDatabase db) {
        this.db = db;
    }

    public void createTable() {
        Log.i("flag", "create table");
        String sql = " CREATE TABLE song (_id INTEGER PRIMARY KEY,num TEXT,name TEXT,singer TEXT)";
        db.execSQL(sql);
    }

    public void addSong(SongVO songVO) {
        Log.i("flag", "add song");
        String sql = "insert into song (num, name, singer) values (?,?,?)";
        db.execSQL(sql,new String[]{songVO.getNum(), songVO.getName(), songVO.getSinger()});
    }

    public Cursor querySongByName(String name) {
        Log.i("flag", "query song by name");
        String sql = "select * from song where name like ?";
        String[] args = new String[1];
        args[0] = "%"+name+"%";
        return db.rawQuery(sql, args);
    }

    public Cursor querySongBySinger(String name) {
        Log.i("flag", "query song by singer");
        String sql = "select * from song where singer like ?";
        String[] args = new String[1];
        args[0] = "%"+name+"%";
        return db.rawQuery(sql, args);
    }

    public Cursor queryAllSong() {
        Log.i("flag", "query all song");
        String sql = "select * from song ";
        return db.rawQuery(sql, null);
    }

    public void deleteSongById(Integer id) {
        String sql = "delete from song where _id = ?";
        db.execSQL(sql, new Integer[]{id});
    }

    public boolean isTableExist() {
        String sql = "SELECT count(*) FROM sqlite_master where type = ? and name = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"table", "song"});
        if(!cursor.moveToFirst()){
             return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

}