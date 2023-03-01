package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {
    private final UserService userService;

    @Test
    void shouldReturnAllUsers() {
        UserDTO firstUserDTO = UserDTO.builder()
                .name("First User")
                .email("firstuser@yandex.ru")
                .build();

        UserDTO secondUserDTO = UserDTO.builder()
                .name("Second User")
                .email("seconduser@yandex.ru")
                .build();

        UserDTO first = userService.addUser(firstUserDTO);
        UserDTO second = userService.addUser(secondUserDTO);
        List<UserDTO> userDTOs = userService.getAllUsers();

        Assertions.assertEquals(first.getId(), 1L);
        Assertions.assertEquals(second.getId(), 2L);
        assertEquals(userDTOs.size(), 2);
    }
}
