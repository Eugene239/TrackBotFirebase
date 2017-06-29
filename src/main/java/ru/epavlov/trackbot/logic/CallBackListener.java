package ru.epavlov.trackbot.logic;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.epavlov.trackbot.entity.CallBackEntity;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.model.MessageModel;
import ru.epavlov.trackbot.model.ModelTrack;
import ru.epavlov.trackbot.model.ModelUser;
import ru.epavlov.trackbot.util.Strings;

/**
 * Created by Eugene on 25.06.2017.
 */
public class CallBackListener {
    private static final String TAG = "[" + CallBackListener.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(CallBackListener.class);
    private static final String DIVIDER = "::";
    private TelegramLongPollingBot bot;

    public CallBackListener(TelegramLongPollingBot bot) {
        this.bot = bot;
    }


    public void parseCallback(UserBot user, CallbackQuery callbackQuery) {
        Log.info(TAG+user.getFIO()+" "+callbackQuery.getData());
        String command = callbackQuery.getData().split(DIVIDER)[0];
        String param = callbackQuery.getData().split(DIVIDER)[1];
        switch (command) {
            case "get_track":
                get_track(callbackQuery, user, param);
                break;
            case "delete_track": deleteTrack(callbackQuery,user,param); break;
            case "change_track_name": changeTrackName(callbackQuery,user,param); break;
        }
    }

    private void remove(String key) {
        Firebase.getInstance().getDatabase().getReference(CallBackEntity.RAW).child(key).removeValue();
    }

    private void changeTrackName(CallbackQuery callbackQuery,UserBot user,String trackId){
        Output.get().sendOnlyText(user.getId(),Strings.ENTER_DESC);
        MessageModel.get().addCommand(user.getId(),trackId);
        answerCallBack(callbackQuery);
    }
    private void deleteTrack(CallbackQuery callbackQuery,UserBot user,String trackId){
        if (user.getTrackList().containsKey(trackId)){
            user.getTrackList().remove(trackId);
            ModelUser.get().update(user);
            Output.get().sendOnlyText(user.getId(), Strings.TRACK_HAS_BEEN_DELETED.replace(Strings.MASK,trackId));
        }
        ModelTrack.get().getTrack(trackId).take(1).subscribe(track -> {
            if (track.getUsers().containsKey(user.getId()+"")){
                track.getUsers().remove(user.getId()+"");
                ModelTrack.get().update(track);
            }
        });

        answerCallBack(callbackQuery);

    }
    private void get_track(CallbackQuery query, UserBot user, String track_id) {
        ModelTrack.get().getTrack(track_id).take(1).subscribe(track -> {
            String desc="";
            if (user.getTrackList().containsKey(track_id)) {
                desc= user.getTrackList().get(track_id).getName();
            }
            Output.get().sendTrack(user.getId(),track,desc);
            answerCallBack(query);
        });
    }

    private void answerCallBack(CallbackQuery query) {
        try {
            bot.answerCallbackQuery(new AnswerCallbackQuery().setCallbackQueryId(query.getId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
