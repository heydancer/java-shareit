package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDTO addUser(UserDTO userDto) {
        User user = userMapper.toModel(userDto);

        log.info("Adding user");

        return userMapper.toDTO(userRepository.save(user));
    }

    public UserDTO getUserById(long userId) {
        log.info("Getting user with ID: {}", userId);

        return userMapper.toDTO(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found")));
    }

    public List<UserDTO> getAllUsers() {
        log.info("Getting all users");

        return userMapper.toDTOList(userRepository.findAll());
    }

    public UserDTO updateUser(long userId, UserDTO userDto) {
        User updatedUser = userMapper.toModel(userDto);
        checkForUpdate(userId, updatedUser);

        log.info("Updating user with ID: {}", userId);

        return userMapper.toDTO(userRepository.save(updatedUser));
    }

    public void removeUserById(long userId) {
        if (userRepository.findById(userId).isPresent()) {
            log.info("Removing user with ID: {}", userId);

            userRepository.deleteById(userId);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    private void checkForUpdate(long userId, User user) {
        User oldUser = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        user.setId(userId);

        if (user.getName() == null) {
            user.setName(oldUser.getName());
        } else if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
    }
}
