package ru.epavlov.trackbot.logic;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.epavlov.trackbot.dao.UserDAO;
import ru.epavlov.trackbot.entity.CallBackEntity;
import ru.epavlov.trackbot.firebase.Firebase;

/**
 * Created by Eugene on 25.06.2017.
 */
public class CallBackListener {
    private static final String TAG = "[" + CallBackListener.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(CallBackListener.class);

    private TelegramLongPollingBot bot;

    public CallBackListener(TelegramLongPollingBot bot) {
        this.bot =bot;
        Firebase.getInstance()
                .getDatabase()
                .getReference(CallBackEntity.RAW)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //System.out.println(s+" "+dataSnapshot.getValue(MessageBot.class).getText());
                        parseCallback(dataSnapshot.getKey(), dataSnapshot.getValue(CallBackEntity.class));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void parseCallback(String key,CallBackEntity callBackEntity){
        Log.info(TAG+callBackEntity.getId()+": "+callBackEntity.getText());
        String command = callBackEntity.getText().split("::")[0];
        String param = callBackEntity.getText().split("::")[1];

        switch (command) {
            case "get_track":UserDAO.getInstance().sendTrack(Long.valueOf(callBackEntity.getId()),param);  remove(key); break;
            default: break;
//                delete_track(query, trackUser, param);
//                break;
//            case "get_track":
//                get_track(query, trackUser, param);
//                break;
//            case "change_track_name": change_track_name(query,trackUser, param); break; //
//            case "stats": showStats(query,trackUser,param); break;
//            default:
//                MessageController.getInstance().sendCustomMessage(trackUser.getUserId(), "Неизвестная команда");
//                answerCallBack(query);

        }
    }

    private void remove(String key){
        Firebase.getInstance().getDatabase().getReference(CallBackEntity.RAW).child(key).removeValue();
    }
}
