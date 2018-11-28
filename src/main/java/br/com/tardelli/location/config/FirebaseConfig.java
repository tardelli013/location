package br.com.tardelli.location.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


@Configuration
public class FirebaseConfig {

    @Value("${firebase.security.json}")
    private JSONObject firebaseAuth;

    @PostConstruct
    public void initFirebaseConnection() {
        try {

            InputStream serviceAccount = new ByteArrayInputStream(firebaseAuth.toString().getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseApp.initializeApp(new FirebaseOptions.Builder().setCredentials(credentials).build());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}