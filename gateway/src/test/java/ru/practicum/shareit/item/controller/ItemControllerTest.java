package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDTO;
import ru.practicum.shareit.item.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemClient client;
    private ItemDTO firstItemDTO;
    private ItemDTO secondItemDTO;
    private CommentDTO commentDTO;

    @BeforeEach
    void beforeEach() {
        firstItemDTO = ItemDTO.builder()
                .id(1L)
                .name("First Item")
                .description("First Item Description")
                .available(true)
                .build();

        secondItemDTO = ItemDTO.builder()
                .id(2L)
                .name("Second Item")
                .description("Second Item Description")
                .available(true)
                .build();

        commentDTO = CommentDTO.builder()
                .id(1L)
                .text("Test Comment")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateAndReturnItem() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstItemDTO);

        String itemDTOJson = objectMapper.writeValueAsString(firstItemDTO);

        when(client.create(anyLong(), any(ItemDTO.class)))
                .thenReturn(response);

        mvc.perform(post("/items")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemDTOJson))
                .andExpect(status().isOk())
                .andExpect(content().json(itemDTOJson));
    }

    @Test
    void shouldReturnItemById() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstItemDTO);

        String itemDTOJson = objectMapper.writeValueAsString(firstItemDTO);

        when(client.getById(anyLong(), anyLong()))
                .thenReturn(response);

        mvc.perform(get("/items/1")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(itemDTOJson));
    }

    @Test
    void shouldReturnItemsByUserId() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(firstItemDTO, secondItemDTO));

        when(client.getItemsByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(response);

        mvc.perform(get("/items")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("First Item", "Second Item")))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("First Item Description", "Second Item Description")))
                .andExpect(jsonPath("$[*].available", containsInAnyOrder(true, true)));
    }

    @Test
    void shouldSearchItemsByText() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(secondItemDTO));

        when(client.getItemsByText(anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        mvc.perform(get("/items/search").queryParam("text", "Second Item")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(secondItemDTO))));
    }

    @Test
    void shouldUpdateAndReturnItem() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstItemDTO);

        String itemDTOJson = objectMapper.writeValueAsString(firstItemDTO);

        when(client.update(anyLong(), anyLong(), any(ItemDTO.class)))
                .thenReturn(response);

        mvc.perform(patch("/items/1")
                        .header(SHARER_USER_ID, 1)
                        .content(itemDTOJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(itemDTOJson));
    }

    @Test
    void shouldDeleteItemById() throws Exception {
        mvc.perform(delete("/items/1")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateAndReturnComment() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(commentDTO);

        String commentDTOJson = objectMapper.writeValueAsString(commentDTO);

        when(client.createComment(anyLong(), anyLong(), any(CommentDTO.class)))
                .thenReturn(response);

        mvc.perform(post("/items/1/comment")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDTOJson))
                .andExpect(status().isOk())
                .andExpect(content().json(commentDTOJson));
    }
}
