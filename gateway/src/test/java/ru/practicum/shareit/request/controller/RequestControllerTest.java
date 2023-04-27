package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.RequestDTO;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc
class RequestControllerTest {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RequestClient client;
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
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstRequestDTO);

        String requestDTOJson = objectMapper.writeValueAsString(firstRequestDTO);

        when(client.create(1L, firstRequestDTO))
                .thenReturn(response);

        mvc.perform(post("/requests")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTOJson))
                .andExpect(status().isOk())
                .andExpect(content().json(requestDTOJson));
    }

    @Test
    void shouldReturnRequestListByOwnerId() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(firstRequestDTO, secondRequestDTO));

        when(client.getRequestsByUserId(1L))
                .thenReturn(response);

        mvc.perform(get("/requests")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("First Request Description", "Second Request Description")));
    }

    @Test
    void shouldReturnAllRequests() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(firstRequestDTO, secondRequestDTO));

        when(client.getRequestsByUserIdWithPagination(1L, 0, 10))
                .thenReturn(response);

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
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(secondRequestDTO);

        String requestDTOJson = objectMapper.writeValueAsString(secondRequestDTO);

        when(client.getRequestById(1L, 2L))
                .thenReturn(response);

        mvc.perform(get("/requests/" + 2)
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(requestDTOJson));
    }
}
