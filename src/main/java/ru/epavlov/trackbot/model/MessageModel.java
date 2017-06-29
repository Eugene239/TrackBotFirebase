package ru.epavlov.trackbot.model;

import com.google.firebase.database.FirebaseDatabase;
import org.telegram.telegrambots.api.objects.Message;
import ru.epavlov.trackbot.entity.MessageBot;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.util.Strings;

import java.util.HashMap;


/**
 * Created by Eugene on 29.06.2017.
 */
public class MessageModel {
    public static final String ERROR = "Message/ERROR";
    public static final String DIVIDER = "~";
    private static MessageModel instance;
    private HashMap<Long,String> trackCaommandMap= new HashMap<>();
    public static MessageModel get(){
        if(instance == null){
            synchronized (MessageModel.class) {
                if(instance == null){
                    instance = new MessageModel();
                }
            }
        }
        return instance;
    }

    public void addError(Message message){
        addError(message.getChat().getId(),message.getText());
    }
    public void addError(Long id, String text){
        FirebaseDatabase.getInstance().getReference(ERROR+"/"+id+DIVIDER+text).setValue(new MessageBot(id,text));
    }
    public boolean isCommand(UserBot user,String text){
        if (trackCaommandMap.containsKey(user.getId())){
            user.getTrackList().get(trackCaommandMap.get(user.getId())).setName(text);
            ModelUser.get().update(user);
            Output.get().sendOnlyText(user.getId(), Strings.DESC_ADDED);
            trackCaommandMap.remove(user.getId());
            return true;
        }else return false;
    }
    public void addCommand(Long id,String track){
        trackCaommandMap.put(id,track);
    }
}

