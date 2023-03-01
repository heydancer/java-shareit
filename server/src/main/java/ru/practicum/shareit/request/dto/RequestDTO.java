package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RequestDTO {
    private long id;

    private LocalDateTime created;

    private String description;

    private List<ItemDTO> items;
}
