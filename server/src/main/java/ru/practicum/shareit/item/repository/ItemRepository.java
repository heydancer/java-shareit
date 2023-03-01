package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId, Pageable pageable);

    @Query("SELECT item FROM Item item " +
            "WHERE item.available = TRUE " +
            "AND (lower(item.name) LIKE %?1% " +
            "OR lower(item.description) LIKE %?1%)")
    List<Item> search(String text, Pageable pageable);

    @Query("SELECT item FROM Item item " +
            "WHERE item.request.id IN :ids")
    List<Item> findAllByRequestIds(@Param("ids") List<Long> ids);

    List<Item> findAllByRequestOwnerId(long ownerId);

    List<Item> findAllByRequestId(long requestId);
}
