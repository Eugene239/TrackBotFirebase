package ru.epavlov.trackbot.main;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.epavlov.trackbot.dao.TrackDAO;
import ru.epavlov.trackbot.dao.UserDAO;
import ru.epavlov.trackbot.entity.CallBackEntity;
import ru.epavlov.trackbot.entity.MessageBot;
import ru.epavlov.trackbot.entity.Track;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.CallBackListener;
import ru.epavlov.trackbot.logic.MessageListener;
import ru.epavlov.trackbot.util.Security;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * получает сообщение, сохраняет его в базу
 * Created by Eugene on 20.06.2017.
 */
public class TrackinBot extends TelegramLongPollingBot {
    private static final String TAG = "[" + TrackinBot.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(TrackinBot.class);
    private static final String tocken = Security.tocken;
    private static final String TTAG = "[THREAD]: ";
    private static final String THREAD_PATH="THREAD";

    public TrackinBot() {
        Log.warn(TAG + "Start");
        //прослушиваем сообщения
        new MessageListener(this);
        //прослушиваем колбеки
        new CallBackListener(this);
        Output.get().setBot(this);

        //запускаем сервис
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            //Log.info(wait);
            Firebase.getInstance().getDatabase().getReference(THREAD_PATH).setValue(Firebase.getSdf().format(System.currentTimeMillis()));
            Log.info(TTAG+"start "+Firebase.getSdf().format(System.currentTimeMillis()));
            Firebase.getInstance().getDatabase().getReference(Track.PATH).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
                        Track track= dataSnapshot1.getValue(Track.class);
                        Log.info(TTAG+"check "+ track.getId());
                        TrackDAO.getInstance().check(track);
                    });
                    Log.info(TTAG+"DONE");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }, 0, 1, TimeUnit.HOURS);
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) { //обработчик сообщений
            UserDAO.getInstance().checkUser(update.getMessage());
            addMessageToParse(update.getMessage());
            return;
        }
        if (update.hasCallbackQuery()) { //обработчик колбэков
            addCallBackToParse(update.getCallbackQuery());
            return;
        }
    }

    private void addCallBackToParse(CallbackQuery callbackQuery) {
        CallBackEntity callBackEntity = new CallBackEntity(callbackQuery.getFrom().getId(), callbackQuery.getData());
        Firebase.getInstance().getDatabase().getReference(CallBackEntity.RAW+"/"+callbackQuery.getFrom().getId()).setValue(callBackEntity);
        try {
            answerCallbackQuery(new AnswerCallbackQuery().setCallbackQueryId(callbackQuery.getId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addMessageToParse(Message message) {
        //добавляем сырое сообщение на обработку
        Firebase.getInstance()
                .getDatabase()
                .getReference(MessageBot.RAW)
                .push()
                .setValue(new MessageBot(message.getChatId(), message.getText()));
    }


    public String getBotUsername() {
        return "TrackBot";
    }

    public String getBotToken() {
        return tocken;
    }


}

