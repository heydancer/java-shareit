package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDTO createItem(@RequestHeader(SHARER_USER_ID) long userId, @Valid @RequestBody ItemDTO itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @GetMapping
    public List<ItemDTO> getItemsByUserId(@RequestHeader(SHARER_USER_ID) long userId) {
        return itemService.getItemsByUserId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDTO getItem(@PathVariable long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping("/search")
    public List<ItemDTO> search(@RequestParam String text) {
        return itemService.getItemsByText(text);
    }

    @PatchMapping("/{itemId}")
    public ItemDTO updateItem(@RequestHeader(SHARER_USER_ID) long userId, @PathVariable long itemId,
                           @RequestBody ItemDTO itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader(SHARER_USER_ID) long userId, @PathVariable long itemId) {
        itemService.removeItemById(userId, itemId);
    }
}
