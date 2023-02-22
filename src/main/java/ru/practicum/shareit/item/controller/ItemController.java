package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.item.dto.CommentDTO;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
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
    public ItemDTO createItem(@RequestHeader(SHARER_USER_ID) long userId,
                              @Valid @RequestBody ItemDTO itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDTO getItem(@RequestHeader(SHARER_USER_ID) long userId,
                           @PathVariable long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemDTO> getItemsByUserId(@RequestHeader(SHARER_USER_ID) long userId,
                                          @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                          @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        return itemService.getItemsByUserId(userId, new MyPageRequest(from, size, Sort.unsorted()));
    }

    @GetMapping("/search")
    public List<ItemDTO> search(@RequestParam String text,
                                @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {

        return itemService.getItemsByText(text, new MyPageRequest(from, size, Sort.unsorted()));
    }

    @PatchMapping("/{itemId}")
    public ItemDTO updateItem(@RequestHeader(SHARER_USER_ID) long userId,
                              @PathVariable long itemId, @RequestBody ItemDTO itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader(SHARER_USER_ID) long userId,
                           @PathVariable long itemId) {
        itemService.removeItemById(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDTO createComment(@RequestHeader(SHARER_USER_ID) long userId,
                                    @PathVariable long itemId, @Valid @RequestBody CommentDTO commentDTO) {
        return itemService.addComment(userId, itemId, commentDTO);
    }
}
