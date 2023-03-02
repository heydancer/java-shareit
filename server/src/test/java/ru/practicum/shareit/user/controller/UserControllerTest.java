package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.service.UserService;

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
    private static final long FAKE_ID = 99999L;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService service;
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
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.addUser(any(UserDTO.class)))
                .thenReturn(firstUserDTO);

        mvc.perform(post("/users")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDTOJson));
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        when(service.getAllUsers())
                .thenReturn(List.of(firstUserDTO, secondUserDTO));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("First User", "Second User")))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("firstuser@yandex.ru", "seconduser@yandex.ru")));
    }

    @Test
    void shouldReturnEmptyUsers() throws Exception {
        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.getUserById(1L))
                .thenReturn(firstUserDTO);

        mvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDTOJson));
    }

    @Test
    void shouldReturnAndCheckFakeId() throws Exception {
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.getUserById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/users/99999")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAndReturnUser() throws Exception {
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.updateUser(1L, firstUserDTO))
                .thenReturn(firstUserDTO);

        mvc.perform(patch("/users/1")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDTOJson));
    }

    @Test
    void shouldUpdateAndCheckEmptyName() throws Exception {
        firstUserDTO.setName(StringUtils.EMPTY);
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.updateUser(1L, firstUserDTO))
                .thenThrow(new ValidationException("Name cannot be empty"));

        mvc.perform(patch("/users/1")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUpdateAndCheckEmptyEmail() throws Exception {
        firstUserDTO.setEmail(StringUtils.EMPTY);
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.updateUser(1L, firstUserDTO))
                .thenThrow(new ValidationException("Email cannot be empty"));

        mvc.perform(patch("/users/1")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUpdateAndCheckEmptyNameAndEmail() throws Exception {
        firstUserDTO.setName(StringUtils.EMPTY);
        firstUserDTO.setEmail(StringUtils.EMPTY);
        String userDTOJson = objectMapper.writeValueAsString(firstUserDTO);

        when(service.updateUser(1L, firstUserDTO))
                .thenThrow(new ValidationException("Name and Email cannot be empty"));

        mvc.perform(patch("/users/1")
                        .content(userDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteUserById() throws Exception {
        mvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteAndCheckFakeId() throws Exception {
        Mockito.doThrow(new NotFoundException("User not found"))
                .when(service).removeUserById(FAKE_ID);

        mvc.perform(delete("/users/" + FAKE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}