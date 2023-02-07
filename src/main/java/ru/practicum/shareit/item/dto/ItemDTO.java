package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.SimplifiedBookingDTO;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
public class ItemDTO {
    long id;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Description cannot be empty")
    private String description;

    private Boolean available;

    private List<CommentDTO> comments;

    private SimplifiedBookingDTO lastBooking;

    private SimplifiedBookingDTO nextBooking;
}
