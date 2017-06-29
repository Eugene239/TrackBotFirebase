package ru.epavlov.trackbot.dao;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import ru.epavlov.trackbot.entity.UserBot;
import ru.epavlov.trackbot.firebase.Firebase;
import ru.epavlov.trackbot.main.Output;
import ru.epavlov.trackbot.util.Strings;

/**
 * Created by Eugene on 20.06.2017.
 */
public class UserDAO {
    private static UserDAO instance;
    private static final String TAG = "[" + UserDAO.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(UserDAO.class);

    public static UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    private void createUser(Message message){
        UserBot user = new UserBot(message.getChatId(),message.getChat().getUserName(),message.getChat().getFirstName(),message.getChat().getLastName());
        if (message.getText().equals("/start")) Output.get().sendOnlyText(user.getId(), Strings.GREETING);
        updateUser(user);
    }
    public void updateUser(UserBot user){
        new Thread(()->
                Firebase.getInstance()
                        .getDatabase()
                        .getReference(UserBot.PATH+"/"+user.getId())
                        .setValue(user)
        ).start();
    }

    /**
     * Провреяем, что этот пользователь есть в базе
     * если нет, то добавляем, если был неактивныйм, то активируем
     * @param message
     */
    public void checkUser(Message message){
        //проверяем есть ли такой пользователь
        Firebase.getInstance().getDatabase().getReference(UserBot.PATH + "/" + message.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserBot userBot = dataSnapshot.getValue(UserBot.class);
                    //если он был неактивен, то возвращаем активность
                    if (!userBot.isActive()) {
                        userBot.setActive(true);
                        UserDAO.getInstance().updateUser(userBot);
                    }
                } else {
                    //создаем пользователя
                    UserDAO.getInstance().createUser(message);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * пользователь удалил бота
     * теперь он неактивен
     * @param id
     */
    public void botDisabledByUser(String id){
        //удаляем его нахрен


        Firebase.getInstance().getDatabase().getReference(UserBot.PATH + "/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    UserBot user= dataSnapshot.getValue(UserBot.class);
                    user.setActive(false);
                    updateUser(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

