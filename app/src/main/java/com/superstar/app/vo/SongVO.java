package com.superstar.app.vo;

/**
 * Created by Kent on 2015/11/9.
 */
public class SongVO
{
    private String num;
    private String name;
    private String singer;

    public SongVO(String num, String name, String singer) {
        this.num = num;
        this.name = name;
        this.singer = singer;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public String getSinger() {
        return singer;
    }
}