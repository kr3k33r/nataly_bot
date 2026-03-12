package ru.kreker.natalybot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kreker.natalybot.model.UserStatus;
import ru.kreker.natalybot.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final UserRepository userRepository;

    public String process(String text, String userId, String chatId) {
        return "Ты написал: " + text;
    }

}
