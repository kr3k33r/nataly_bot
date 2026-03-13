package ru.kreker.natalybot.service.handler.message;

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
    private final List<MessageHandlerStep> handlerSteps;

    public void handle(Message message) {
        MessageContext context = new MessageContext(message, message.getFrom().getId().toString(), message.getChatId().toString());

        for(MessageHandlerStep step: handlerSteps){
            if(!step.handle(context)){
                return;
            }
        }
    }
}
