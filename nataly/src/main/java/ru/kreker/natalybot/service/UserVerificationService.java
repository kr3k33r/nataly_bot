package ru.kreker.natalybot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kreker.natalybot.model.UserStatus;
import ru.kreker.natalybot.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserVerificationService {
    private final UserRepository userRepository;

    public UserStatus checkUserStatus(String userId, String chatId) {
        return userRepository.getStatus(userId, chatId);
    }

    public void registerPendingUser(String userId, String chatId) {
        userRepository.save(userId, chatId, UserStatus.PENDING);
    }

    public void verifyUser(String userId, String chatId) {
        userRepository.save(userId, chatId, UserStatus.VERIFIED);
    }
}
