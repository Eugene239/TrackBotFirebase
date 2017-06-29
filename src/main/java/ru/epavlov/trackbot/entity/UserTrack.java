package ru.epavlov.trackbot.entity;

import ru.epavlov.trackbot.firebase.Firebase;

/**
 * Created by Eugene on 24.06.2017.
 */
public class UserTrack {
    private String createdTime;
    private String name;
    private String trackId;

    public UserTrack() {
    }

    public UserTrack(Track track){
        this.trackId = track.getId();
        this.createdTime = Firebase.getSdf().format(System.currentTimeMillis());
    }
    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }
}
