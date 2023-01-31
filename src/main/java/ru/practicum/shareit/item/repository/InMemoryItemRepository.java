package ru.practicum.shareit.item.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private long nextId = 0L;
    private final Map<Long, Item> itemMap = new HashMap<>();

    @Override
    public Item addItem(Item item, User user) {
        getNextId(item);
        user.getItemIds().add(item.getId());

        itemMap.put(item.getId(), item);

        return item;
    }

    @Override
    public Optional<Item> findItem(long itemId) {
        if (!itemMap.containsKey(itemId)) {
            return Optional.empty();
        } else {
            return Optional.of(itemMap.get(itemId));
        }
    }

    @Override
    public List<Item> findItemsByUserId(long userId) {
        return itemMap.values()
                .stream()
                .filter(item -> item.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItems(String text) {
        List<Item> items = new ArrayList<>();

        for (Item value : itemMap.values()) {
            if (StringUtils.containsAnyIgnoreCase(value.getName(), text))
                items.add(value);

            else if (StringUtils.containsAnyIgnoreCase(value.getDescription(), text)) {
                items.add(value);
            }
        }

        return items.stream()
                .distinct()
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public Item updateItemByUserId(Item item) {
        itemMap.replace(item.getId(), item);

        return item;
    }

    @Override
    public void deleteItemById(long itemId) {
        itemMap.remove(itemId);
    }

    @Override
    public void deleteAllItemsByUserId(long userId) {
        itemMap.values().removeIf(item -> item.getUserId() == userId);
    }

    private void getNextId(Item item) {
        item.setId(++nextId);
    }
}
