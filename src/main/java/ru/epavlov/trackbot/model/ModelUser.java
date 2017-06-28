package ru.epavlov.trackbot.model;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.main.Output;

import java.util.HashMap;

/**
 * Created by Eugene on 26.06.2017.
 */
public class ModelUser {
    private static ModelUser instance;
    private static final String TAG = "[" + ModelUser.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(ModelUser.class);

    private HashMap<String,UserBot> userkMap = new HashMap<>();

    public static ModelUser get(){
        if(instance == null){
            synchronized (Output.class) {
                if(instance == null){
                    instance = new ModelUser();
                }
            }
        }
        return instance;
    }
    private ModelUser(){
        Firebase.getInstance().getDatabase().getReference(UserBot.PATH).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
             //   Log.info(TAG+"added "+dataSnapshot.getKey());
                userkMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(UserBot.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
              //  Log.info(TAG+"changed "+dataSnapshot.getKey());
                userkMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(UserBot.class));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               // Log.info(TAG+"removed "+ dataSnapshot.getKey());
                userkMap.remove(dataSnapshot.getKey());
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
    public UserBot getUser(String key){
        return userkMap.get(key);
    }
}
