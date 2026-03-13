package ru.kreker.natalybot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kreker.natalybot.model.UserEntity;
import ru.kreker.natalybot.model.UserStatus;
import ru.kreker.natalybot.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserVerificationService {
    private final UserRepository userRepository;

    public UserStatus checkUserStatus(String userId, String chatId) {
        Optional<UserEntity> optional = userRepository.findByUserIdAndChatId(userId, chatId);
        return optional.map(UserEntity::getStatus).orElse(null);
    }

    public void registerPendingUser(String userId, String chatId) {
        userRepository.save(new UserEntity(userId, chatId, UserStatus.PENDING));
    }

    public void verifyUser(String userId, String chatId) {
        userRepository.save(new UserEntity(userId, chatId, UserStatus.VERIFIED));
    }
}
