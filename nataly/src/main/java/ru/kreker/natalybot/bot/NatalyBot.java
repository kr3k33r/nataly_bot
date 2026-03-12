package ru.kreker.natalybot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kreker.natalybot.config.TelegramBotProperties;
import ru.kreker.natalybot.router.UpdateRouter;

@Component
@RequiredArgsConstructor
public class NatalyBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final UpdateRouter updateRouter;

    @Override
    public void onUpdateReceived(Update update) {
        updateRouter.route(update);
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }
}