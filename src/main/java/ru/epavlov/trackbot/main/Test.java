package ru.epavlov.trackbot.main;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Eugene on 20.06.2017.
 */
public class Test {
    public void init() throws IOException {
        InputStream serviceAccount =
                this.getClass().getClassLoader().getResourceAsStream("google-services.json");

        //      FirebaseCredentials.fromCertificate()
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://trackbot-eba9a.firebaseio.com")
                .build();

        FirebaseApp firebaseApp= FirebaseApp.initializeApp(options);
        System.out.println(FirebaseDatabase.getSdkVersion());
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().push().setValue("aloha");
//        FirebaseDatabase.getInstance(firebaseApp).getReference().push().setValue("ref").addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(Task<Void> task) {
//                System.out.println("ferfe");
//            }
//        });

    }
    public static void main(String[] args) throws IOException {
       // Firebase firebase = new Firebase(Security.firebaseKey);
      //  FileInputStream serviceAccount = (FileInputStream) Test.class.getClassLoader().getResourceAsStream("google-services.json");
    Test test = new Test();
    test.init();
   // while (true);
  //      FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReference().push().setValue("oooo");
       // ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        //classLoader.getResourceAsStream();

        //response = firebase.put( dataMap );

    }

}
