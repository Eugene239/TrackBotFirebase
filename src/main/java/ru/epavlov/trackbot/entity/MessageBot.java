package ru.epavlov.trackbot.entity;

import ru.epavlov.trackbot.firebase.Firebase;

/**
 * Created by Eugene on 20.06.2017.
 */
public class MessageBot {
    public static final String RAW = "Message/RAW";
    public static final String ERROR = "Message/ERROR";
    public static final String CORRECT = "Message/CORRECT";

    private Long id;
    private String text;
    private String createdTime;

    public MessageBot() {
    }

    public MessageBot(Long id,String text) {
        this.id=id;
        this.text = text;
        createdTime = Firebase.getSdf().format(System.currentTimeMillis());
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
