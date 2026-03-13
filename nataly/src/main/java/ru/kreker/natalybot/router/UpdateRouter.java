package ru.kreker.natalybot.router;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kreker.natalybot.service.handler.CallbackHandler;
import ru.kreker.natalybot.service.handler.message.MessageHandler;

@Component
@RequiredArgsConstructor
public class UpdateRouter {

    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    public void route(Update update) {
        if (update.hasMessage()) {
            messageHandler.handle(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallback(update);
        }
    }

}