package ru.kreker.natalybot.service.handler.message;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.kreker.natalybot.model.UserStatus;
import ru.kreker.natalybot.service.UserVerificationService;
import ru.kreker.natalybot.telegram.TelegramSender;

@Component
@RequiredArgsConstructor
public class PendingHandler implements MessageHandlerStep {
    private final UserVerificationService verificationService;
    private final TelegramSender sender;

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public boolean handle(MessageContext messageContext) {
        Message message = messageContext.message();
        String userId = messageContext.userId();
        String chatId = messageContext.chatId();
        UserStatus status = verificationService.checkUserStatus(userId, chatId);

        if (status == UserStatus.PENDING) {
            sender.deleteMessage(chatId, message.getMessageId().toString());
            sender.sendMessage(chatId, "Сначала пройдите капчу.");
            return false;
        }

        return true;
    }
}
