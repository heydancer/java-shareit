package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId);

    @Query("SELECT item FROM Item item " +
            "WHERE item.available = TRUE " +
            "AND (lower(item.name) LIKE %?1% " +
            "OR lower(item.description) LIKE %?1%)")
    List<Item> search(String text);
}
