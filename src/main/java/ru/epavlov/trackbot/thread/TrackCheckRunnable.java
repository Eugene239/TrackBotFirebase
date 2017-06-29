package ru.epavlov.trackbot.thread;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.parser.MainParser;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.model.ModelTrack;

import java.util.ArrayList;

/**
 * Created by Eugene on 27.06.2017.
 */
public class TrackCheckRunnable implements Runnable {
    private static final Logger Log = Logger.getLogger(TrackCheckRunnable.class);
    private static final String TAG = "[THREAD]: ";
    private static final String THREAD_PATH = "THREAD";
   // public static int cnt=0;

    @Override
    public void run() {
        FirebaseDatabase.getInstance().getReference(Track.PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkTracks(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkTracks(DataSnapshot dataSnapshot) {
        new Thread(() -> {
            Log.info(TAG + "start " + Firebase.getSdf().format(System.currentTimeMillis()));
            FirebaseDatabase.getInstance().getReference(THREAD_PATH).setValue(Firebase.getSdf().format(System.currentTimeMillis()));
            ArrayList<Track> tracks = new ArrayList<>();
            dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
                tracks.add(dataSnapshot1.getValue(Track.class));
            });
            tracks.parallelStream()
                    .filter(track -> track.getUsers().size()>0 && !track.getStatus().equals("SIGNIN"))
                    .forEach(this::check);
            Log.info(TAG + "DONE ");
        }).start();
    }

    public void check(Track track) {
        Track newTrack = MainParser.get().parse(track.getId(), track.getParserCode());
        if (!newTrack.getTime().equals(track.getTime())) {
            Log.warn(TAG + "track updated " + track.getId());
            track.consume(newTrack);
            track.setLastModify(System.currentTimeMillis());
            ModelTrack.get().update(track); //обновляем трек
            track.getUsers().values().forEach(userId -> { //оповещаем всех пользователей
                Output.get().sendTrack(userId, track, "");
                //UserDAO.getInstance().sendTrack(userId, track.getId());
            });
        } else {
            track.setLastModify(System.currentTimeMillis());
            ModelTrack.get().update(track); //обновляем трек
        }
    }
}
