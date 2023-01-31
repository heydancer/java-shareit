package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item addItem(Item item, User user);

    Optional<Item> findItem(long itemId);

    List<Item> findItemsByUserId(long userId);

    List<Item> findItems(String text);

    Item updateItemByUserId(Item item);

    void deleteItemById(long itemId);

    void deleteAllItemsByUserId(long userId);
}
