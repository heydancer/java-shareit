package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private long nextId = 0L;
    private final Map<Long, User> userMap = new HashMap<>();

    @Override
    public Optional<User> findUserById(long userId) {
        if (!userMap.containsKey(userId)) {
            return Optional.empty();
        } else {
            return Optional.of(userMap.get(userId));
        }
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User addUser(User user) {
        getNextId(user);
        userMap.put(nextId, user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        userMap.replace(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUserById(long userId) {
        userMap.remove(userId);
    }

    private void getNextId(User user) {
        user.setId(++nextId);
    }
}
