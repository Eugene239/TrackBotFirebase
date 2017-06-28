package ru.epavlov.trackbot.main;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.epavlov.trackbot.dao.UserDAO;
import ru.epavlov.trackbot.entity.CallBackEntity;
import ru.epavlov.trackbot.entity.MessageBot;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.logic.CallBackListener;
import ru.epavlov.trackbot.logic.MessageListener;
import ru.epavlov.trackbot.util.Security;

/**
 * получает сообщение, сохраняет его в базу
 * Created by Eugene on 20.06.2017.
 */
public class TrackinBot extends TelegramLongPollingBot {
    private static final String TAG = "[" + TrackinBot.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(TrackinBot.class);

    public TrackinBot() {
        Log.warn(TAG + "Start");
        Output.get().notifyAdmins("_____Start_____");
        //прослушиваем сообщения
        new MessageListener(this);
        //прослушиваем колбеки
        new CallBackListener(this);
        Output.get().setBot(this);
        //запускаем сервис
//        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//        exec.scheduleAtFixedRate(new TrackCheckRunnable(), 0, 3, TimeUnit.HOURS);
    }

    /**
     * получаем сообщение с сервера
     * @param update
     */
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

    /**
     * добавляем колбек в базу для обработки
     * @param callbackQuery
     */
    private void addCallBackToParse(CallbackQuery callbackQuery) {
        CallBackEntity callBackEntity = new CallBackEntity(callbackQuery.getFrom().getId(), callbackQuery.getData());
        Firebase.getInstance().getDatabase().getReference(CallBackEntity.RAW+"/"+callbackQuery.getFrom().getId()).setValue(callBackEntity);
        try {
            answerCallbackQuery(new AnswerCallbackQuery().setCallbackQueryId(callbackQuery.getId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * добавляем сообщение для обработки
     * @param message
     */
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
        return Security.TRACK_TEST;
    }

}

