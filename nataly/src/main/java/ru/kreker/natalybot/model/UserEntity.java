package ru.kreker.natalybot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String userId;

    private String chatId;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public UserEntity(String userId, String chatId, UserStatus status){
        this.userId = userId;
        this.chatId = chatId;
        this.status = status;
    }
}