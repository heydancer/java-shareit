package ru.practicum.shareit.item.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
public class ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
    }

    public ItemDTO addItem(long userId, ItemDTO itemDto) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validate(itemDto);

        Item item = itemMapper.toModel(itemDto);
        item.setUserId(userId);

        log.info("Adding item");

        return itemMapper.toDTO(itemRepository.addItem(item, user));
    }

    public ItemDTO getById(long itemId) {
        log.info("Getting item with ID: {}", itemId);

        return itemMapper.toDTO(itemRepository.findItem(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found")));
    }

    public List<ItemDTO> getItemsByUserId(long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        log.info("Getting all items by user ID: {}", userId);

        return itemMapper.toDTOList(itemRepository.findItemsByUserId(userId));
    }

    public List<ItemDTO> getItemsByText(String text) {
        log.info("Getting all items by text: {}", text);

        if (StringUtils.isEmpty(text)) {
            return List.of();
        }

        return itemMapper.toDTOList(itemRepository.findItems(text));
    }

    public ItemDTO updateItem(long userId, long itemId, ItemDTO itemDto) {
        Item updatedItem = itemMapper.toModel(itemDto);
        checkForUpdate(userId, itemId, updatedItem);

        log.info("Updating item with ID: {}", itemId);

        return itemMapper.toDTO(itemRepository.updateItemByUserId(updatedItem));
    }

    public void removeItemById(long userId, long itemId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getItemIds().remove(itemId)) {
            log.info("Removing item with ID: {}", itemId);

            itemRepository.deleteItemById(itemId);
        } else {
            throw new NotFoundException("Item not found");
        }
    }

    private void validate(ItemDTO itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Available cannot be null");
        }
        if (itemDto.getDescription() == null) {
            throw new BadRequestException("Description cannot be null");
        }
    }

    private void checkForUpdate(long userId, long itemId, Item item) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item oldItem = itemRepository.findItem(itemId)
                .orElseThrow(() -> new NullPointerException("Item not found"));

        if (user.getItemIds().contains(itemId)) {
            item.setId(oldItem.getId());
            item.setUserId(oldItem.getUserId());

            if (item.getName() == null && item.getDescription() == null) {
                item.setName(oldItem.getName());
                item.setDescription(oldItem.getDescription());

            } else if (item.getName() == null && item.getAvailable() == null) {
                item.setName(oldItem.getName());
                item.setAvailable(oldItem.getAvailable());

            } else if (item.getDescription() == null && item.getAvailable() == null) {
                item.setDescription(oldItem.getDescription());
                item.setAvailable(oldItem.getAvailable());
            }

        } else {
            throw new NotFoundException("User does not store item with ID: " + itemId);
        }
    }
}
