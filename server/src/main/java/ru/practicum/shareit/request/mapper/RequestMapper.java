package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.BaseMapper;
import ru.practicum.shareit.request.dto.RequestDTO;
import ru.practicum.shareit.request.model.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper implements BaseMapper<RequestDTO, Request> {
    @Override
    public Request toModel(RequestDTO itemRequestDTO) {
        return Request.builder()
                .description(itemRequestDTO.getDescription())
                .build();
    }

    @Override
    public RequestDTO toDTO(Request itemRequest) {
        return RequestDTO.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(new ArrayList<>())
                .build();
    }

    @Override
    public List<RequestDTO> toDTOList(List<Request> itemRequests) {
        return itemRequests.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
