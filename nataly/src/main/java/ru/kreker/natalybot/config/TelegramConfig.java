package ru.kreker.natalybot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.kreker.natalybot.bot.NatalyBot;

@Configuration
@RequiredArgsConstructor
public class TelegramConfig {

    private final NatalyBot natalyBot;

    @PostConstruct
    public void init() throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(natalyBot);
    }
}