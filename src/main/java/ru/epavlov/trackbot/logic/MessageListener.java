package ru.epavlov.trackbot.logic;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.entity.UserTrack;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.model.MessageModel;
import ru.epavlov.trackbot.model.ModelTrack;
import ru.epavlov.trackbot.model.ModelUser;
import ru.epavlov.trackbot.util.Strings;


/**
 * Created by Eugene on 20.06.2017.
 */
public class MessageListener {
    private static final String TAG = "[" + MessageListener.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(MessageListener.class);

    private TelegramLongPollingBot bot;

    public MessageListener(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void parseMessage(UserBot user, Message message) {
        Log.info(TAG+ user.getFIO()+": "+message.getText());
        if (isDefaultCommand(user, message.getText())) return; //стандартная комада
        if (MessageModel.get().isCommand(user,message.getText())) return; //комнда для трека
        ModelTrack.get().getTrack(message.getText()).subscribe(track -> {
            switch (track.getId()) {
                case ModelTrack.ERROR_TEXT:
                    Output.get().sendOnlyText(user.getId(), Strings.ERROR_MESSAGE);
                    MessageModel.get().addError(message);
                    return;
                case ModelTrack.TRACK_NOT_FOUND:
                    Output.get().sendErrorTrack(user.getId(),message.getText());
                    MessageModel.get().addError(message);
                    return;
                default:
                    track.getUsers().computeIfAbsent(user.getId() + "", s -> user.getId());
                    user.getTrackList().computeIfAbsent(track.getId(), s -> new UserTrack(track));
                    ModelTrack.get().update(track);
                    ModelUser.get().update(user);
                    Output.get().sendTrack(user.getId(), track, user.getTrackList().get(track.getId()).getName()); //отправляем ему трек
            }
        });
    }

    private boolean isDefaultCommand(UserBot user, String text) {
        switch (text.toLowerCase().trim()) {
            case "/mylist":
                ModelUser.get().getMyList(user.getId());
                return true;
            case "/help":
                Output.get().sendOnlyText(user.getId(), Strings.HELP);
                return true;
            case "/stat":
                return true;
            case "/start":
                return true;
            default:
                return false;
        }
    }

}
