package ru.kreker.natalybot.service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kreker.natalybot.model.UserStatus;
import ru.kreker.natalybot.service.MessageService;
import ru.kreker.natalybot.service.UserVerificationService;
import ru.kreker.natalybot.telegram.TelegramSender;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final MessageService messageService;
    private final UserVerificationService verificationService;
    private final TelegramSender sender;

    public void handle(Message message) {
        String userId = message.getFrom().getId().toString();
        String chatId = message.getChatId().toString();

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
                sender.deleteMessage(chatId, message.getMessageId().toString());

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                InlineKeyboardButton button = new InlineKeyboardButton("Пройти капчу");
                button.setCallbackData("VERIFY " + userId);
                keyboard.setKeyboard(List.of(List.of(button)));

                sender.sendMessageWithKeyboard(chatId,
                        "Нажмите кнопку, чтобы пройти капчу:", keyboard);
            }

            return;
        }

        if (status == UserStatus.PENDING) {
            sender.deleteMessage(chatId, message.getMessageId().toString());
            sender.sendMessage(chatId, "Сначала пройдите капчу.");
            return;
        }

        String response = messageService.process(message.getText(), userId, chatId);
        sender.sendMessage(chatId, response);
    }
}
