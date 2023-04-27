package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDTO;
import ru.practicum.shareit.item.dto.ItemDTO;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(SHARER_USER_ID) long userId,
                                             @Valid @RequestBody ItemDTO itemDTO) {
        log.info("Creating item {}, userId={}", itemDTO, userId);
        return itemClient.create(userId, itemDTO);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(SHARER_USER_ID) long userId,
                                          @PathVariable long itemId) {
        log.info("Get item with id={}, userId={}", itemId, userId);
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestHeader(SHARER_USER_ID) long userId,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                                   @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Get items by userId={}", userId);
        return itemClient.getItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                         @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Search items by text={}", text);
        return itemClient.getItemsByText(text, from, size);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(SHARER_USER_ID) long userId,
                                             @PathVariable long itemId, @RequestBody ItemDTO itemDTO) {
        log.info("Update item {}, itemId={}", itemDTO, itemId);
        return itemClient.update(userId, itemId, itemDTO);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader(SHARER_USER_ID) long userId,
                                             @PathVariable long itemId) {
        log.info("Delete itemId={}", itemId);
        return itemClient.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(SHARER_USER_ID) long userId,
                                                @PathVariable long itemId, @Valid @RequestBody CommentDTO commentDTO) {
        log.info("Create comment {}, itemId={}", commentDTO, itemId);
        return itemClient.createComment(userId, itemId, commentDTO);
    }
}
