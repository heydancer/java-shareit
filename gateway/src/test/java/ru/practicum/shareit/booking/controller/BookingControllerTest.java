package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingClient client;
    private BookItemRequestDTO firstBookingDTO;
    private BookItemRequestDTO secondBookingDTO;

    @BeforeEach
    void beforeEach() {
        firstBookingDTO = BookItemRequestDTO.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        secondBookingDTO = BookItemRequestDTO.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
    }

    @Test
    void shouldCreateAndReturnBooking() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(firstBookingDTO);

        String bookingDTOJson = objectMapper.writeValueAsString(firstBookingDTO);

        when(client.create(anyLong(), any(BookItemRequestDTO.class)))
                .thenReturn(response);

        mvc.perform(post("/bookings")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingDTOJson))
                .andExpect(status().isOk())
                .andExpect(content().json(bookingDTOJson));
    }

    @Test
    void shouldChangeBookingStatus() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(secondBookingDTO);

        String bookingDTOJson = objectMapper.writeValueAsString(secondBookingDTO);

        when(client.changeStatus(1L, 2L, true))
                .thenReturn(response);

        mvc.perform(patch("/bookings/" + 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1)
                        .queryParam("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json(bookingDTOJson));
    }

    @Test
    void shouldReturnAllBookingsByBooker() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(firstBookingDTO, secondBookingDTO));

        when(client.getBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        mvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].itemId", containsInAnyOrder(1, 2)));
    }

    @Test
    void shouldReturnAllBookingsByOwner() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK)
                .body(List.of(firstBookingDTO, secondBookingDTO));

        when(client.getBookingsByOwnerId(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        mvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].itemId", containsInAnyOrder(1, 2)));
    }
}