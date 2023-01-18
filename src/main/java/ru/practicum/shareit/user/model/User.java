package ru.practicum.shareit.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class User {
    private long id;
    private String name;
    private String email;

    @JsonIgnore
    private List<Long> itemIds;
}
