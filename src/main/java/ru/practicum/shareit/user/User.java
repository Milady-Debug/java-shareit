package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    @Email(message = "Электронная почта должна содержать символ @")
    private String email;
}
