package ru.kreker.natalybot.repository;

import org.springframework.stereotype.Repository;
import ru.kreker.natalybot.model.UserEntity;
import ru.kreker.natalybot.model.UserStatus;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, UserStatus> users = new ConcurrentHashMap<>();

    @Override
    public UserStatus getStatus(String userId, String chatId) {
        return users.get(userId + "_" + chatId);
    }

    @Override
    public void save(String userId, String chatId, UserStatus status) {
        users.put(userId + "_" + chatId, status);
    }
}
