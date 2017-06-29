package ru.epavlov.trackbot.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.reactivex.subjects.BehaviorSubject;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.util.Strings;

/**
 * Created by Eugene on 26.06.2017.
 */
public class ModelUser {
    private static ModelUser instance;
    private static final String TAG = "[" + ModelUser.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(ModelUser.class);
    private static final String PATH = "User/";


    public static ModelUser get() {
        if (instance == null) {
            synchronized (ModelUser.class) {
                if (instance == null) {
                    instance = new ModelUser();
                }
            }
        }
        return instance;
    }

    private ModelUser() {

    }

    public BehaviorSubject<UserBot> getUser(Long id) {
        BehaviorSubject<UserBot> rxUser = BehaviorSubject.create();
        getFromDataBase(rxUser, id);
        return rxUser;
    }

    public BehaviorSubject<UserBot> getUser(Integer id) {
        return getUser(Long.valueOf(id));
    }

    private void getFromDataBase(BehaviorSubject<UserBot> rxUser, Long id) {
        FirebaseDatabase.getInstance().getReference(PATH + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    rxUser.onNext(dataSnapshot.getValue(UserBot.class));
                    rxUser.onComplete();
                } else {
                    rxUser.onNext(new UserBot());
                    rxUser.onComplete();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                rxUser.onNext(new UserBot());
                rxUser.onComplete();
            }
        });
    }

    public void createUser(UserBot userBot) {
        createUser(userBot, false);
    }

    public void createUser(UserBot userBot, boolean notify) {
        if (notify) Output.get().sendOnlyText(userBot.getId(), Strings.GREETING);
        FirebaseDatabase.getInstance().getReference(PATH + userBot.getId()).setValue(userBot);
    }

    public void update(UserBot userBot) {
        createUser(userBot, false);
    }

    public void getMyList(Long id) {
        Firebase.getInstance().getDatabase().getReference(UserBot.PATH + "/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserBot userBot = dataSnapshot.getValue(UserBot.class);
                if (userBot.getTrackList() == null || userBot.getTrackList().size() == 0) {
                    Output.get().sendOnlyText(id, Strings.NO_TRACKS);
                } else {
                    Output.get().sendMysList(id, userBot.getTrackList());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public UserBot checkUser(UserBot user, Message message) {
        if (!message.getChat().getId().equals(user.getId())) { //не было такого пользователя
            user = new UserBot(message);
            Log.info(TAG + "new user " + user.getId() + " " + user.getFIO());
            if (message.getText().equals("/start")) {
                ModelUser.get().createUser(user, true); //только залогинился
            } else {
                ModelUser.get().createUser(user); //старый пользователь, но его нет в базе
            }
        }
        if (!user.isActive()) user.setActive(true); //обновляем статус
        user.setLastMessageTime(Firebase.getSdf().format(System.currentTimeMillis()));
        ModelUser.get().update(user);
        return user;
    }

    public UserBot checkUser(UserBot userbot, User user) {
        if (user.getId().longValue() != userbot.getId()) { //не было такого пользователя
            userbot = new UserBot(user);
            Log.info(TAG + "new user " + user.getId() + " " + userbot.getFIO());
            ModelUser.get().createUser(userbot); //старый пользователь, но его нет в базе
        }
        if (!userbot.isActive()) userbot.setActive(true); //обновляем статус
        userbot.setLastMessageTime(Firebase.getSdf().format(System.currentTimeMillis()));
        ModelUser.get().update(userbot);
        return userbot;
    }

}
