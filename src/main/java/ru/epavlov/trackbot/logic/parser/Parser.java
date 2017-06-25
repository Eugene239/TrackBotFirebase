package ru.epavlov.trackbot.logic.parser;

import ru.epavlov.trackbot.entity.Track;

/**
 * Created by Eugene on 13.05.2017.
 */
public interface Parser {
    public Track getData(String track_id);
    String getText(Track track, String desc);
    String getName();
    boolean checkTrack(String track_id);
}
