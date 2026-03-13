package ru.kreker.natalybot.service.handler.message;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.springframework.stereotype.Component;
import ru.kreker.natalybot.service.MessageService;
import ru.kreker.natalybot.telegram.TelegramSender;

@Component
@RequiredArgsConstructor
public class BusinessLogicMessageHandler implements MessageHandlerStep {
    private final MessageService messageService;
    private final TelegramSender sender;

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public boolean handle(MessageContext messageContext) {
        String response = messageService.process(messageContext.message().getText(),
                messageContext.userId(),
                messageContext.chatId());
        sender.sendMessage(messageContext.chatId(), response);
        return true;
    }
}
