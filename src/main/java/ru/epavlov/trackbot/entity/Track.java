package ru.epavlov.trackbot.entity;

import com.google.gson.annotations.SerializedName;
import ru.epavlov.trackbot.firebase.Firebase;

import java.util.HashMap;

/**
 * Created by Eugene on 20.06.2017.
 */
public class Track {
    public static final String PATH = Track.class.getSimpleName();

    private String id;
    private String status;

    @SerializedName(value = "desc")
    private String text;
    private String last_modify;
    private String last_check;
    private String time;
    private HashMap<String,Long> users = new HashMap<>();

    public HashMap<String, Long> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, Long> users) {
        this.users = users;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private int parserCode;

    public int getParserCode() {
        return parserCode;
    }

    public void setParserCode(int parserCode) {
        this.parserCode = parserCode;
    }

    public Track() {
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLast_modify() {
        return last_modify;
    }

    public void setLast_modify(String last_modify) {
        this.last_modify = last_modify;
    }

    public String getLast_check() {
        return last_check;
    }

    public void setLast_check(String last_check) {
        this.last_check = last_check;
    }

    public void setLastModify(long time) {
        last_modify = Firebase.getSdf().format(time);
    }

    public  void consume(Track track){
        setText(track.getText());
        setTime(track.getTime());
        setParserCode(track.getParserCode());
        setStatus(track.getStatus());
    }
}
