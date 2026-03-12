package ru.kreker.natalybot.service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kreker.natalybot.service.UserVerificationService;
import ru.kreker.natalybot.telegram.TelegramSender;

@Component
@RequiredArgsConstructor
public class CallbackHandler {
    private final UserVerificationService verificationService;
    private final TelegramSender sender;

    public void handleCallback(Update update) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String userId = update.getCallbackQuery().getFrom().getId().toString();
        String callbackData = update.getCallbackQuery().getData();

        if (("VERIFY " + userId).equals(callbackData)) {
            verificationService.verifyUser(userId, chatId);
            sender.sendMessage(chatId, "Вы успешно прошли капчу!");
            sender.deleteMessage(chatId, String.valueOf(update.getCallbackQuery().getMessage().getMessageId()));
        }
    }
}
