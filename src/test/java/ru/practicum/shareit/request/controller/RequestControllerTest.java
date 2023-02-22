package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.RequestDTO;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc
class RequestControllerTest {
    public static final long FAKE_ID = 99999L;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private RequestService service;

    private RequestDTO firstRequestDTO;
    private RequestDTO secondRequestDTO;

    @BeforeEach
    void beforeEach() {
        firstRequestDTO = RequestDTO.builder()
                .id(1L)
                .description("First Request Description")
                .build();

        secondRequestDTO = RequestDTO.builder()
                .id(2L)
                .description("Second Request Description")
                .build();
    }

    @Test
    void shouldCreateAndReturnRequest() throws Exception {
        String requestDTOJson = objectMapper.writeValueAsString(firstRequestDTO);

        when(service.addRequest(1L, firstRequestDTO))
                .thenReturn(firstRequestDTO);

        mvc.perform(post("/requests")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTOJson))
                .andExpect(status().isOk())
                .andExpect(content().json(requestDTOJson));
    }

    @Test
    void shouldCreateAndCheckFakeId() throws Exception {
        String requestDTOJson = objectMapper.writeValueAsString(firstRequestDTO);

        when(service.addRequest(FAKE_ID, firstRequestDTO))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(post("/requests")
                        .header(SHARER_USER_ID, FAKE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTOJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyRequestListByOwnerId() throws Exception {
        when(service.getRequestListByOwnerId(1L))
                .thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnRequestListByOwnerId() throws Exception {
        when(service.getRequestListByOwnerId(1L))
                .thenReturn(List.of(firstRequestDTO, secondRequestDTO));

        mvc.perform(get("/requests")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("First Request Description", "Second Request Description")));
    }

    @Test
    void shouldReturnEmptyAllRequests() throws Exception {
        when(service.getAllRequestList(anyLong(), any(Pageable.class)))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnAllRequests() throws Exception {
        when(service.getAllRequestList(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(firstRequestDTO, secondRequestDTO));

        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("First Request Description", "Second Request Description")));
    }

    @Test
    void shouldReturnRequestById() throws Exception {
        String requestDTOJson = objectMapper.writeValueAsString(secondRequestDTO);

        when(service.getRequestById(1L, 2L))
                .thenReturn(secondRequestDTO);

        mvc.perform(get("/requests/" + 2)
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(requestDTOJson));
    }
}