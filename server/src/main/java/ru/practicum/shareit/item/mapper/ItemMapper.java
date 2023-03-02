package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.BaseMapper;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper implements BaseMapper<ItemDTO, Item> {
    @Override
    public Item toModel(ItemDTO itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(itemDto.getOwner())
                .build();
    }

    public Item toModel(ItemDTO itemDto, Request request) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(request)
                .owner(itemDto.getOwner())
                .build();
    }

    @Override
    public ItemDTO toDTO(Item item) {
        Long requestId = null;

        if (item.getRequest() != null) requestId = item.getRequest().getId();
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(requestId)
                .build();
    }

    @Override
    public List<ItemDTO> toDTOList(List<Item> items) {
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
