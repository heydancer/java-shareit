package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.SimplifiedBookingDTO;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
@Builder
public class ItemDTO {
    long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;

    private List<CommentDTO> comments;

    private SimplifiedBookingDTO lastBooking;

    private SimplifiedBookingDTO nextBooking;

    private User owner;
}
