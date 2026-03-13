package ru.kreker.natalybot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kreker.natalybot.model.UserEntity;
import ru.kreker.natalybot.model.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUserIdAndChatId(String userId, String chatId);
}
