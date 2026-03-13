package ru.kreker.natalybot.service.handler.message;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kreker.natalybot.model.UserStatus;
import ru.kreker.natalybot.service.UserVerificationService;
import ru.kreker.natalybot.telegram.TelegramSender;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FirstVerificationMessageHandler implements MessageHandlerStep {
    private final UserVerificationService verificationService;
    private final TelegramSender sender;

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean handle(MessageContext messageContext) {
        Message message = messageContext.message();
        String userId = messageContext.userId();
        String chatId = messageContext.chatId();
        UserStatus status = verificationService.checkUserStatus(userId, chatId);

        if (status == null) {
            verificationService.registerPendingUser(userId, chatId);

            boolean isGroup = message.getChat().isGroupChat()
                    || message.getChat().isSuperGroupChat();

            if (isGroup) {
                sender.restrictUserInGroup(chatId, userId);

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                InlineKeyboardButton button = new InlineKeyboardButton("Пройти капчу");
                button.setCallbackData("VERIFY " + userId);
                keyboard.setKeyboard(List.of(List.of(button)));

                sender.sendMessageWithKeyboard(chatId,
                        "Для участия в чате пройдите капчу:", keyboard);
            } else if (message.getChat().isUserChat()) {
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                InlineKeyboardButton button = new InlineKeyboardButton("Пройти капчу");
                button.setCallbackData("VERIFY " + userId);
                keyboard.setKeyboard(List.of(List.of(button)));

                sender.sendMessageWithKeyboard(chatId,
                        "Нажмите кнопку, чтобы пройти капчу:", keyboard);
            }

            sender.deleteMessage(chatId, message.getMessageId().toString());
            return false;
        }

        return true;
    }
}
