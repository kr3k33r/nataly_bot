package ru.kreker.natalybot.repository;

import ru.kreker.natalybot.model.UserStatus;

public interface UserRepository {
    UserStatus getStatus(String userId, String chatId);

    void save(String userId, String chatId, UserStatus status);
}
