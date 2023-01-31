package ru.practicum.shareit.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, ItemRepository itemRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.userMapper = userMapper;
    }

    public UserDTO addUser(UserDTO userDto) {
        User user = userMapper.toModel(userDto);
        checkDuplicateEmail(user);

        log.info("Adding user");

        return userMapper.toDTO(userRepository.addUser(user));
    }

    public UserDTO getUserById(long userId) {
        log.info("Getting user with ID: {}", userId);

        return userMapper.toDTO(userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    public List<UserDTO> getAllUsers() {
        log.info("Getting all users");

        return userMapper.toDTOList(userRepository.findAllUsers());
    }

    public UserDTO updateUser(long userId, UserDTO userDto) {
        User updatedUser = userMapper.toModel(userDto);

        checkDuplicateEmail(updatedUser);
        checkForUpdate(userId, updatedUser);

        log.info("Updating user with ID: {}", userId);

        return userMapper.toDTO(userRepository.updateUser(updatedUser));
    }

    public void removeUserById(long userId) {
        if (userRepository.findUserById(userId).isPresent()) {
            log.info("Removing user with ID: {}", userId);

            itemRepository.deleteAllItemsByUserId(userId);
            userRepository.deleteUserById(userId);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    private void checkDuplicateEmail(User user) {
        for (User allUser : userRepository.findAllUsers()) {
            if (user.getEmail() != null && user.getEmail().equals(allUser.getEmail())) {
                throw new ValidationException("Duplicate email");
            }
        }
    }

    private void checkForUpdate(long userId, User user) {
        User oldUser = userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setId(userId);
        user.setItemIds(oldUser.getItemIds());

        if (user.getName() == null) {
            user.setName(oldUser.getName());
        } else if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
    }
}
