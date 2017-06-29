package ru.epavlov.trackbot.thread;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import ru.epavlov.trackbot.entity.UserBot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene on 29.06.2017.
 */
public class UserActiveRunnable implements Runnable{
    private static final String PATH = "USER_ACTIVE";
    @Override
    public void run() {
        FirebaseDatabase.getInstance().getReference(UserBot.PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<UserBot> users = new ArrayList<>();
                dataSnapshot.getChildren().forEach(userSnapshot->{
                    users.add(userSnapshot.getValue(UserBot.class));
                });
                checkUsers(users);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void checkUsers(List<UserBot> users){
        new Thread(()-> {
            clearActive();
            users.parallelStream()
                    .filter(userBot -> userBot.getTrackList().size() > 0 && userBot.isActive())
                    .forEach(this::addToActive);
        }).start();
    }
    private void clearActive(){
        FirebaseDatabase.getInstance().getReference(PATH).removeValue();
    }
    private void addToActive(UserBot userBot){
        FirebaseDatabase.getInstance().getReference(PATH+"/"+userBot.getId()).setValue(userBot);
    }
}
