package ru.epavlov.trackbot.logic;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.epavlov.trackbot.dao.TrackDAO;
import ru.epavlov.trackbot.dao.UserDAO;
import ru.epavlov.trackbot.entity.MessageBot;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.model.ModelTrack;
import ru.epavlov.trackbot.util.Strings;

/**
 * Created by Eugene on 20.06.2017.
 */
public class MessageListener {
    private static final String TAG = "[" + MessageListener.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(MessageListener.class);

    private TelegramLongPollingBot bot;

    public MessageListener(TelegramLongPollingBot bot) {
        this.bot =bot;
        Firebase.getInstance().getDatabase()
                .getReference(MessageBot.RAW)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //System.out.println(s+" "+dataSnapshot.getValue(MessageBot.class).getText());
                parseMessage( dataSnapshot.getKey(), dataSnapshot.getValue(MessageBot.class));
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

    private void parseMessage(String key,MessageBot messageBot){
        Log.info(TAG+messageBot.getId()+": "+ messageBot.getText());
        if (!messageBot.isChecked()) { //если эта комнда не была проверена
            if (isDefaultCommand(messageBot, key))  return;//если станадртная команда
            //неверное сообщение
            if (TrackDAO.getInstance().isTrack(messageBot.getText())==TrackDAO.ERROR_CODE){//не трек
                Output.get().sendOnlyText(messageBot.getId(),Strings.ERROR_MESSAGE);
                replaceToError(key,messageBot);
                return;
            }
            ModelTrack.get().getTrack(messageBot.getText()).take(1).subscribe(track -> {
               if (track==null) Output.get().sendOnlyText(messageBot.getId(),Strings.NO_TRACK.replace(Strings.MASK,messageBot.getText()));
               else Output.get().sendTrack(messageBot.getId(),track,"");
               replaceToCorrect(key,messageBot);
            });
            //старый способ TrackDAO.getInstance().getTrack(key,messageBot); //поверяем трек старое
        }else { //отправляем сообщения в ошибочные
            replaceToError(key,messageBot);
        }
    }

    public static void remove(String key){ //удаление из базы команнды
        FirebaseDatabase.getInstance().getReference(MessageBot.RAW).child(key).removeValue();
    }
    //перемещение в обработанные сообщения
    public static void replaceToCorrect(String key, MessageBot messageBot){
        remove(key);
        messageBot.setChecked(true);
        FirebaseDatabase.getInstance().getReference(MessageBot.CORRECT+"/"+messageBot.getText()).setValue(messageBot);
    }

    //переменщение в ошибочные
    public static void replaceToError(String key,MessageBot messageBot){
        remove(key);
        messageBot.setChecked(true);
        FirebaseDatabase.getInstance().getReference(MessageBot.ERROR+"/"+messageBot.getText()).setValue(messageBot);
    }

    private boolean isDefaultCommand(MessageBot messageBot, String key){
        switch (messageBot.getText().toLowerCase().trim()){
            case "/mylist": UserDAO.getInstance().getMyList(messageBot.getId());  remove(key); return true;
            case "/help":  Output.get().sendOnlyText(messageBot.getId(), Strings.HELP);  remove(key); return true;
            case "/stat":  remove(key); return true;
            case "/start":
                //Output.get().sendOnlyText(messageBot.getId(), Strings.GREETING); проверяется каждое сообщение
                remove(key);
                return true;
            default: return false;
        }
    }

}
