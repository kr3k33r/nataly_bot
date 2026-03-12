package ru.kreker.natalybot.telegram;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.kreker.natalybot.bot.NatalyBot;

@Component
public class TelegramSender {

    private final NatalyBot bot;

    public TelegramSender(@Lazy NatalyBot bot) {
        this.bot = bot;
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(String chatId, String messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(Integer.valueOf(messageId));

        try {
            bot.execute(deleteMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);

        try {
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unrestrictUserInGroup(String chatId, String userId) {
        ChatPermissions permissions = new ChatPermissions();

        permissions.setCanSendMessages(true);
        permissions.setCanSendMediaMessages(true);
        permissions.setCanSendPolls(true);
        permissions.setCanSendOtherMessages(true);
        permissions.setCanInviteUsers(true);

        setRestrict(chatId, userId, permissions);
    }

    public void restrictUserInGroup(String chatId, String userId) {
        ChatPermissions permissions = new ChatPermissions();
        permissions.setCanSendMessages(false);
        permissions.setCanSendMediaMessages(false);
        permissions.setCanSendPolls(false);
        permissions.setCanSendOtherMessages(false);
        permissions.setCanAddWebPagePreviews(false);
        permissions.setCanInviteUsers(false);

        setRestrict(chatId, userId, permissions);
    }

    private void setRestrict(String chatId, String userId, ChatPermissions permissions) {
        RestrictChatMember restrict = new RestrictChatMember();
        restrict.setChatId(chatId);
        restrict.setUserId(Long.parseLong(userId));
        restrict.setPermissions(permissions);

        try {
            bot.execute(restrict);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}