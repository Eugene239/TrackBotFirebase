package ru.epavlov.trackbot.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

/**
 * Created by Eugene on 20.06.2017.
 */
public class Firebase {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm");
    private static Firebase instance = new Firebase();

    private Firebase(){
        InputStream serviceAccount =
                this.getClass().getClassLoader().getResourceAsStream("google-services.json");
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl("https://trackbot-eba9a.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
        }catch (IOException e){
            e.printStackTrace();
            Runtime.getRuntime().exit(-1);
        }
    }
    public FirebaseDatabase getDatabase(){
        return FirebaseDatabase.getInstance();
    }
    public static Firebase getInstance(){
        return instance;
    }

    public static SimpleDateFormat getSdf() {
        return sdf;
    }
}
