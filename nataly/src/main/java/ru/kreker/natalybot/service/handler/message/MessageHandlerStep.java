package ru.kreker.natalybot.service.handler.message;

import org.springframework.core.Ordered;

public interface MessageHandlerStep extends Ordered {
    boolean handle(MessageContext messageContext);
}
