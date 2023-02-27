package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.SimplifiedBookingDTO;
import ru.practicum.shareit.user.model.User;

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

    private Long requestId;

    private List<CommentDTO> comments;

    private SimplifiedBookingDTO lastBooking;

    private SimplifiedBookingDTO nextBooking;

    @JsonIgnore
    private User owner;
}
