package ru.epavlov.trackbot.entity;

import ru.epavlov.trackbot.firebase.Firebase;

/**
 * Created by Eugene on 23.06.2017.
 */
public class CallBackEntity {
    public static final String RAW = "CallBack/RAW";
    public static final String ERROR = "CallBack/ERROR";
    public static final String CORRECT = "CallBack/CORRECT";

    private Integer id;
    private boolean checked;
    private String text;
    private String createdTime;

    public CallBackEntity() {

    }

    public CallBackEntity(Integer id, String data) {
        this.id=id;
        this.text = data;
        createdTime = Firebase.getSdf().format(System.currentTimeMillis());
        checked =false;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}
