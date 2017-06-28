package ru.epavlov.trackbot.dao;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.entity.MessageBot;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.entity.UserTrack;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.MessageListener;
import ru.epavlov.trackbot.logic.parser.MainParser;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.util.Strings;


/**
 * Created by Eugene on 23.06.2017.
 */
public class TrackDAO {
    public static final int ERROR_CODE = -1;
    private static TrackDAO instance;
    private static final String TAG = "[" + TrackDAO.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(TrackDAO.class);

    public static TrackDAO getInstance() {
        if (instance == null) {
            synchronized (TrackDAO.class) {
                if (instance == null) {
                    instance = new TrackDAO();
                }
            }
        }
        return instance;
    }

    /**
     * возвращаем код парсера по треку
     *
     * @param track
     * @return
     */
    public int isTrack(String track) {
        for (Integer parserCode : MainParser.get().getParsers()) {
            if (MainParser.get().checkTrack(track, parserCode)) {
                return parserCode;
            }
        }
        return ERROR_CODE;
    }

    /**
     * Проверяем трек из базы и парсером, добавляем к нему юзеров, присылаем реузультат
     *
     * @param key
     * @param messageBot
     */
    public void getTrack(String key, MessageBot messageBot) {
        String trackId = messageBot.getText();
        int code = isTrack(trackId);
        if (code == ERROR_CODE) { //это не трек
            Output.get().sendOnlyText(messageBot.getId(), Strings.ERROR_MESSAGE);
            MessageListener.replaceToError(key, messageBot);
            return;
        }
//      ModelTrack.get()
//                .getTrack(trackId)
//                .take(1)
//                .subscribe(track -> {
//                    Log.info(TAG+trackId+" "+track);
//                    if (track != null) { //если он был в базе
//                        addUser(track, messageBot.getId());
//                        Output.get().sendTrack(messageBot.getId(), track, ""); //присылаем результат
//                        MessageListener.replaceToCorrect(key, messageBot);
//                    } else {
//                        track = MainParser.get().parse(trackId, code);
//                        if (track == null) notifyErrorTrack(key, messageBot); //не удалось его получить
//                        else {
//                            addUser(track, messageBot.getId());
//                            Output.get().sendTrack(messageBot.getId(), track, ""); //присылаем результат
//                            MessageListener.replaceToCorrect(key, messageBot);
//                        }
//                    }
//
//                });
        //смотрим есть ли он в базе
        Firebase.getInstance()
                .getDatabase()
                .getReference(Track.PATH + "/" + trackId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //есть в базе
                    Track track = dataSnapshot.getValue(Track.class);
                    addUser(track, messageBot.getId()); //проверяем пользователей его
                    Output.get().sendTrack(messageBot.getId(), track, ""); //присылаем результат
                    MessageListener.replaceToCorrect(key, messageBot);
                } else { //его не было в базе
                    Track track = MainParser.get().parse(trackId, code);
                    if (track == null) notifyErrorTrack(key, messageBot); //не удалось его получить
                    else {
                        addUser(track, messageBot.getId());
                        Output.get().sendTrack(messageBot.getId(), track, ""); //присылаем результат
                        MessageListener.replaceToCorrect(key, messageBot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * оповещаем,что трек не был найден и отправляем его в ошибки
     *
     * @param key
     * @param messageBot
     */
    private void notifyErrorTrack(String key, MessageBot messageBot) {
        MessageListener.replaceToError(key, messageBot);
        //высылаем, что трек не найден
        Output.get().sendErrorTrack(messageBot.getId(), messageBot.getText());
    }

    /**
     * Проверка пользователей трека
     *
     * @param track
     * @param userId
     */
    public void addUser(Track track, Long userId) {
        Firebase.getInstance().getDatabase().getReference(UserBot.PATH + "/" + userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserBot userBot = dataSnapshot.getValue(UserBot.class);
                    if (!userBot.getTrackList().keySet().contains(track.getId())) { // не было такого трека
                        userBot.getTrackList().put(track.getId(), new UserTrack(track)); //добавляем новый
                        UserDAO.getInstance().updateUser(userBot); //обновляем пользователя
                    }
                    if (!track.getUsers().keySet().contains(userId + "")) { //у трека не было такого пользователя
                        track.getUsers().put(userId + "", userId);
                        updateTrack(track);
                    }
                } else {
                    Log.error(TAG + "addUser::no such user" + userId); //такого не может быть
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * отправляем трек пользовтелю
     *
     * @param userBot
     * @param trackId
     */
    public void sendTrackToUser(UserBot userBot, String trackId, String desc) {
        Firebase.getInstance().getDatabase().getReference(Track.PATH + "/" + trackId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Track track = dataSnapshot.getValue(Track.class);
                    Output.get().sendTrack(userBot.getId(), track, userBot.getTrackList().get(trackId) == null ? "" : userBot.getTrackList().get(trackId).getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateTrack(Track track) {
        new Thread(() ->
                Firebase.getInstance()
                        .getDatabase()
                        .getReference(Track.PATH + "/" + track.getId())
                        .setValue(track)
        ).start();
    }


}


