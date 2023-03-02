package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private long id;

    private String text;

    private LocalDateTime created;

    private String authorName;
}
