package ru.epavlov.trackbot.main;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.epavlov.trackbot.dao.UserDAO;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.entity.UserTrack;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.parser.MainParser;
import ru.epavlov.trackbot.util.Security;
import ru.epavlov.trackbot.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Eugene on 23.06.2017.
 */
public class Output {
    private static final String TAG = "[" + Output.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(Output.class);

    private static  Output instance;
    private TelegramLongPollingBot bot;
    public static Output get(){
        if(instance == null){
            synchronized (Output.class) {
                if(instance == null){
                    instance = new Output();
                }
            }
        }
        return instance;
    }


    public void sendOnlyText(Long userId, String text){
        send(new SendMessage().setText(text).setChatId(userId));
    }

    private void send(SendMessage sendMessage){
            Firebase.getInstance().getDatabase().getReference(UserBot.PATH+"/"+sendMessage.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        UserBot userBot = dataSnapshot.getValue(UserBot.class);
                        if(userBot!=null && userBot.isActive()){
                           try {
                               bot.sendMessage(sendMessage);
                           }catch (Exception e){
                               UserDAO.getInstance().botDisabledByUser(sendMessage.getChatId());
                               e.printStackTrace();
                           }
                        }else {
                         if (userBot!=null)  Log.error(TAG+"blocked user:: "+userBot.getId()+" "+userBot.getFIO());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }
    public void sendTrack(Long userId,Track track, String desc){
     //   System.out.println("send "+userId);
        send(new SendMessage().setText(MainParser.get().getText(track,desc)).setChatId(userId).setReplyMarkup(getTrackMessageKeyboard(track.getId())));

    }
    public void notifyAdmins(String text){
        Arrays.stream(Security.admins).forEach(admin->{
            sendOnlyText(admin,text);
        });
    }
    public void setBot(TelegramLongPollingBot bot){
        this.bot = bot;
    }


    private InlineKeyboardMarkup getTrackMessageKeyboard(String trackID) {
        //todo add
        return null;
//        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
//        List<InlineKeyboardButton> list = new ArrayList<>();
//        list.add(new InlineKeyboardButton().setText("Добавить/Изменить описание").setCallbackData("change_track_name::" + trackID));
//        keyboardMarkup.getKeyboard().add(list);
//        list = new ArrayList<>();
//        //list.add(new InlineKeyboardButton().setText("Изменить трекер").setCallbackData("tracker::" + trackID));
//        list.add(new InlineKeyboardButton().setText("Удалить").setCallbackData("delete_track::" + trackID));
//        keyboardMarkup.getKeyboard().add(list);
//
//        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getTrackListKeyboard(HashMap<String,UserTrack> tracks) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        tracks.forEach((id,userTrack)->{
            List<InlineKeyboardButton> list = new ArrayList<>();
            list.add(new InlineKeyboardButton()
                    .setText(id + (userTrack.getName() == null ? "" : ("\n" + userTrack.getName())))
                    .setCallbackData("get_track::" + id));
            keyboardMarkup.getKeyboard().add(list);
        });
        return keyboardMarkup;
    }

    /**
     * отправляем пользователю, что его трек не найден
     * @param id пользователя
     * @param track строковое представление трека
     */
    public void sendErrorTrack(Long id,String track){
        send(new SendMessage().setChatId(id).setText(Strings.NO_TRACK.replace(Strings.MASK,track)));
    }
    public void sendMysList(Long id, HashMap<String,UserTrack> tracks){
        send(new SendMessage().setChatId(id).setText(Strings.YOUR_TRACKS).setReplyMarkup(getTrackListKeyboard(tracks)));
    }
}
