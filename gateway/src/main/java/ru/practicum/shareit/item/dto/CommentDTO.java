package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private long id;

    @NotEmpty(message = "Comment cannot be empty")
    private String text;

    private LocalDateTime created;

    private String authorName;
}
