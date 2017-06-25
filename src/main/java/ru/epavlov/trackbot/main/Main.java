package ru.epavlov.trackbot.main;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;

/**
 * Created by Eugene on 20.06.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TrackinBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

}
