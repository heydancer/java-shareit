package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDTO;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private BookingService service;

    private BookingDTO firstBookingDTO;
    private BookingDTO secondBookingDTO;

    @BeforeEach
    void beforeEach() {
        firstBookingDTO = BookingDTO.builder()
                .id(1L)
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build();

        secondBookingDTO = BookingDTO.builder()
                .id(2L)
                .itemId(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void shouldCreateAndReturnBooking() throws Exception {
        String bookingDTOJson = objectMapper.writeValueAsString(firstBookingDTO);

        when(service.addBooking(1L, firstBookingDTO))
                .thenReturn(firstBookingDTO);

        mvc.perform(post("/bookings")
                        .header(SHARER_USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingDTOJson))
                .andExpect(status().isOk())
                .andExpect(content().json(bookingDTOJson));
    }

    @Test
    void shouldChangeBookingStatus() throws Exception {
        String bookingDTOJson = objectMapper.writeValueAsString(secondBookingDTO);

        when(service.changeStatus(1L, 2L, true))
                .thenReturn(secondBookingDTO);

        mvc.perform(patch("/bookings/" + 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1)
                        .queryParam("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json(bookingDTOJson));
    }

    @Test
    void shouldReturnEmptyBookingsByBooker() throws Exception {
        when(service.getAllByBookerId(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        mvc.perform(get("/bookings").header(SHARER_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnAllBookingsByBooker() throws Exception {
        when(service.getAllByBookerId(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of(firstBookingDTO, secondBookingDTO));

        mvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].itemId", containsInAnyOrder(1, 2)));
    }

    @Test
    void shouldReturnEmptyBookingsByOwner() throws Exception {
        when(service.getAllByOwnerId(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 2))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnAllBookingsByOwner() throws Exception {
        when(service.getAllByOwnerId(anyLong(), any(), any(Pageable.class)))
                .thenReturn(List.of(firstBookingDTO, secondBookingDTO));

        mvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].itemId", containsInAnyOrder(1, 2)));
    }
}