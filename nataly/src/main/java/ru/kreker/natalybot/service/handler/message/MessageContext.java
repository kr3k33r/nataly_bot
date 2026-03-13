package ru.kreker.natalybot.service.handler.message;

import org.telegram.telegrambots.meta.api.objects.Message;

public record MessageContext(Message message, String userId, String chatId) {
}
