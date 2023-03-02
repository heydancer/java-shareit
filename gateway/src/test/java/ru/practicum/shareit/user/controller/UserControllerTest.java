package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDTO;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserClient client;
    private UserDTO firstUserDTO;
    private UserDTO secondUserDTO;

    @BeforeEach
    void beforeEach() {
        firstUserDTO = UserDTO.builder()
                .id(1L)
                .name("First User")
                .email("firstuser@yandex.ru")
                .build();

        secondUserDTO = UserDTO.builder()
                .id(2L)
                .name("Second User")
                .email("seconduser@yandex.ru")
                .build();
    }

    @Test
    void shouldCreateAndReturnUser() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstUserDTO);

        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(client.create(any(UserDTO.class)))
                .thenReturn(response);

        mvc.perform(post("/users")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDTOJson));
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(firstUserDTO, secondUserDTO));

        when(client.getUsers())
                .thenReturn(response);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("First User", "Second User")))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("firstuser@yandex.ru", "seconduser@yandex.ru")));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(secondUserDTO);

        String userDTOJson = objectMapper.writeValueAsString(secondUserDTO);

        when(client.getById(2L))
                .thenReturn(response);

        mvc.perform(get("/users/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDTOJson));
    }

    @Test
    void shouldUpdateAndReturnUser() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstUserDTO);

        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(client.update(1L, firstUserDTO))
                .thenReturn(response);

        mvc.perform(patch("/users/1")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDTOJson));
    }

    @Test
    void shouldCreateAndCheckEmptyName() throws Exception {
        firstUserDTO.setName("");

        String body = objectMapper.writeValueAsString(firstUserDTO);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateAndCheckNullName() throws Exception {
        firstUserDTO.setName(null);

        String body = objectMapper.writeValueAsString(firstUserDTO);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateAndCheckEmptyEmail() throws Exception {
        firstUserDTO.setEmail("");

        String body = objectMapper.writeValueAsString(firstUserDTO);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateAndCheckNullEmail() throws Exception {
        firstUserDTO.setEmail(null);

        String body = objectMapper.writeValueAsString(firstUserDTO);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateAndCheckInvalidEmail() throws Exception {
        firstUserDTO.setEmail("Invalid Email");

        String body = objectMapper.writeValueAsString(firstUserDTO);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteUserById() throws Exception {
        mvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}