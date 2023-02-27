package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void shouldCreateItemById() {
        UserDTO userDTO = UserDTO.builder()
                .name("Test User")
                .email("testemail@yandex.ru")
                .build();

        ItemDTO itemDTO = ItemDTO.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        UserDTO createdUserDTO = userService.addUser(userDTO);
        ItemDTO createdItemDTO = itemService.addItem(createdUserDTO.getId(), itemDTO);

        assertEquals(createdItemDTO.getOwner().getId(), createdUserDTO.getId());
    }
}
