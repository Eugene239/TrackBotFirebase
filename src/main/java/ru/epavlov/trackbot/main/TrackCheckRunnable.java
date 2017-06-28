package ru.epavlov.trackbot.main;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.dao.TrackDAO;
import ru.epavlov.trackbot.dao.UserDAO;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.parser.MainParser;

/**
 * Created by Eugene on 27.06.2017.
 */
public class TrackCheckRunnable implements   Runnable {
    private static final Logger Log = Logger.getLogger(TrackCheckRunnable.class);
    private static final String TAG = "[THREAD]: ";
    private static final String THREAD_PATH="THREAD";

    @Override
    public void run() {
        FirebaseDatabase.getInstance().getReference(Track.PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.info(TAG +"start "+Firebase.getSdf().format(System.currentTimeMillis()));
                FirebaseDatabase.getInstance().getReference(THREAD_PATH).setValue(Firebase.getSdf().format(System.currentTimeMillis()));
              //  Firebase.getInstance().getDatabase().getReference(THREAD_PATH).setValue(Firebase.getSdf().format(System.currentTimeMillis()));
              //  System.out.println(dataSnapshot.getValue().toString()+" "+dataSnapshot.getValue().getClass().getSimpleName());
//                HashMap<String,Object> trackHashMap = (HashMap<String, Object>) dataSnapshot.getValue();
//                trackHashMap.values().forEach(track -> {
//                    System.out.println(((Track)track).getId());
//                });
//                dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
//                   check(dataSnapshot1.getValue(Track.class));
//                });
                Log.info(TAG +"DONE ");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void check(Track track) {

        Track newTrack = MainParser.get().parse(track.getId(), track.getParserCode());
        if (!newTrack.getTime().equals(track.getTime())) {
            Log.warn(TAG + "track updated " + track.getId());
            track.consume(newTrack);
            track.setLastModify(System.currentTimeMillis());
            TrackDAO.getInstance().updateTrack(track); //обновляем трек
            track.getUsers().values().forEach(userId -> { //оповещаем всех пользователей
                UserDAO.getInstance().sendTrack(userId, track.getId());
            });
        } else {
            track.setLastModify(System.currentTimeMillis());
            TrackDAO.getInstance().updateTrack(track); //обновляем трек
        }
    }
}
