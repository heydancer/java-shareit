package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findUserById(long userId);

    List<User> findAllUsers();

    User addUser(User user);

    User updateUser(User user);

    void deleteUserById(long userId);
}
