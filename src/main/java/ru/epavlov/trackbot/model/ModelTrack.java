package ru.epavlov.trackbot.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import io.reactivex.subjects.BehaviorSubject;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.dao.TrackDAO;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.parser.MainParser;

/**
 * Created by Eugene on 26.06.2017.
 */
public class ModelTrack {
    private static ModelTrack instance;
    private static final String TAG = "[" + ModelTrack.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(ModelTrack.class);

    public static final String ERROR_TEXT = "$1$";
    public static final String TRACK_NOT_FOUND = "$2$";

    public static ModelTrack get(){
        if(instance == null){
            synchronized (ModelTrack.class) {
                if(instance == null){
                    instance = new ModelTrack();
                }
            }
        }
        return instance;
    }
    private ModelTrack(){

    }
    public BehaviorSubject<Track> getTrack(String key){
        BehaviorSubject<Track> rxTrack = BehaviorSubject.create();
        getTrackFromDatabase(rxTrack, key);
        return rxTrack;
    }

    /**
     * грузим трек из базы
     * @param id
     */
    private void getTrackFromDatabase(BehaviorSubject<Track> rxTrack, String id){
        Firebase.getInstance().getDatabase().getReference(Track.PATH+"/"+id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                rxTrack.onNext(dataSnapshot.getValue(Track.class)); // отправляем новое значение
                else { //получаем код парсера берем с сайта
                    int code = TrackDAO.getInstance().isTrack(id);
                    Track track;
                    if (code==TrackDAO.ERROR_CODE){
                        track= new Track();
                        track.setId(ERROR_TEXT);
                        rxTrack.onNext(track);
                    }else {
                        track =  MainParser.get().parse(id,code);
                        if (track==null) { //не получилось найти
                            track = new Track();
                            track.setId(TRACK_NOT_FOUND);
                            rxTrack.onNext(track);
                        }else {
                            rxTrack.onNext(track);
                        }

                    }
                }
                rxTrack.onComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    };


    /**
     * обновляем трек в базе
     * @param track
     */
    public void update(Track track){
        Firebase.getInstance().getDatabase().getReference(Track.PATH+"/"+track.getId()).setValue(track);
    }
    public void delete(Track track){
        Firebase.getInstance().getDatabase().getReference(Track.PATH+"/"+track.getId()).removeValue();
    }

}
