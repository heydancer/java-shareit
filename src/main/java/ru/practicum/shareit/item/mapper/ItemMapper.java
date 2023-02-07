package ru.practicum.shareit.item.mapper;

import java.util.List;
import java.util.stream.Collectors;

import ru.practicum.shareit.common.BaseMapper;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.model.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper implements BaseMapper<ItemDTO, Item> {
    @Override
    public Item toModel(ItemDTO itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    @Override
    public ItemDTO toDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    @Override
    public List<ItemDTO> toDTOList(List<Item> items) {
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
