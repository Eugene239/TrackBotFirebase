package ru.epavlov.trackbot.logic.command;

import ru.epavlov.trackbot.firebase.Firebase;

/**
 * проверяет стандартные команды пользователя
 * Created by Eugene on 23.06.2017.
 */
public class Command {
    public static final String NEW = "COMMAND/NEW";
    //public static final String DONE = "COMMAND/NEW";
    private Long id;
    private boolean checked;
    private String text;
    private String createdTime;

    public Command() {
    }

    public Command(Long id,String text) {
        this.id=id;
        this.text = text;
        createdTime = Firebase.getSdf().format(System.currentTimeMillis());
        checked =false;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
