package ru.epavlov.trackbot.model;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import io.reactivex.subjects.BehaviorSubject;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.dao.TrackDAO;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.parser.MainParser;
import ru.epavlov.trackbot.main.Output;

import java.util.HashMap;

/**
 * Created by Eugene on 26.06.2017.
 */
public class ModelTrack {
    private static ModelTrack instance;
    private static final String TAG = "[" + ModelTrack.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(ModelTrack.class);

    private HashMap<String,BehaviorSubject<Track>> trackMap = new HashMap<>();

    public static ModelTrack get(){
        if(instance == null){
            synchronized (Output.class) {
                if(instance == null){
                    instance = new ModelTrack();
                }
            }
        }
        return instance;
    }
    private ModelTrack(){
        Firebase.getInstance().getDatabase().getReference(Track.PATH).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               //Log.info(TAG+"added "+dataSnapshot.getKey());
             //  trackMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(Track.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (trackMap.containsKey(dataSnapshot.getKey())) {
                    Log.info(TAG+"changed "+dataSnapshot.getKey());
                    trackMap.get(dataSnapshot.getKey()).onNext(dataSnapshot.getValue(Track.class));
                  //  trackMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Track.class));
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (trackMap.containsKey(dataSnapshot.getKey())) {
                    Log.info(TAG+"removed "+dataSnapshot.getKey());
                    trackMap.get(dataSnapshot.getKey()).onNext(null);
                    trackMap.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.info(TAG+"moved "+dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.info(TAG+"canceled "+databaseError.getMessage());
            }
        });

    }
    public BehaviorSubject<Track> getTrack(String key){
        if (trackMap.get(key)==null){
          trackMap.put(key, BehaviorSubject.create());
          getTrackFromDatabase(key);
        }
        return trackMap.get(key);
    }

    /**
     * грузим трек из базы
     * @param id
     */
    private void getTrackFromDatabase(String id){
        Firebase.getInstance().getDatabase().getReference(Track.PATH+"/"+id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                trackMap.get(id).onNext(dataSnapshot.getValue(Track.class)); // отправляем новоен значение
                else { //получаем код парсера берем с сайта
                    int code = TrackDAO.getInstance().isTrack(id);
                    if (code==TrackDAO.ERROR_CODE){
                        trackMap.get(id).onNext(null); //влзвращаем, что трек не найден
                    }else {
                         trackMap.get(id).onNext(MainParser.get().parse(id,code));
                    }
                }
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
        Firebase.getInstance().getDatabase().getReference(Track.PATH+"/"+track).setValue(track);
    }

//    public Set<String> getKeySet(){
//        return trackMap.keySet();
//    }
//    public Collection<Track> getValueSet(){
//        return trackMap.values();
//    }

}
