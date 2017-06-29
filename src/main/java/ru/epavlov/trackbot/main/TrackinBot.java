package ru.epavlov.trackbot.main;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.epavlov.trackbot.logic.CallBackListener;
import ru.epavlov.trackbot.logic.MessageListener;
import ru.epavlov.trackbot.model.ModelUser;
import ru.epavlov.trackbot.thread.TrackCheckRunnable;
import ru.epavlov.trackbot.thread.UserActiveRunnable;
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
    private MessageListener messageListener;
    private CallBackListener callBackListener;

    public TrackinBot() {
        Log.warn(TAG + "Start");
        Output.get().setBot(this);
        Output.get().notifyAdmins("_____Start_____");
        messageListener = new MessageListener(this); //создаем обработчика сообщений
        callBackListener = new CallBackListener(this);

        //запускаем сервис
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(2);
         exec.scheduleAtFixedRate(new TrackCheckRunnable(), 0, 1, TimeUnit.HOURS);
         exec.scheduleAtFixedRate(new UserActiveRunnable(),0,1,TimeUnit.DAYS);
    }

    /**
     * получаем сообщение с сервера
     *
     * @param update
     */
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) { //обработчик сообщений
            ModelUser.get().getUser(update.getMessage().getChat().getId()).subscribe(user -> {
                user= ModelUser.get().checkUser(user,update.getMessage()); //проверяем пользователя по базе
                messageListener.parseMessage(user, update.getMessage()); //чекаем сообщение
            });
            return;
        }
        if (update.hasCallbackQuery()) { //обработчик колбэков
            ModelUser.get().getUser(update.getCallbackQuery().getFrom().getId()).subscribe(user->{
                user = ModelUser.get().checkUser(user, update.getCallbackQuery().getFrom());
                callBackListener.parseCallback(user, update.getCallbackQuery());

            });
            return;
        }

    }


    public String getBotUsername() {
        return "TrackBot";
    }

    public String getBotToken() {
        return Security.tocken;
    }

}

