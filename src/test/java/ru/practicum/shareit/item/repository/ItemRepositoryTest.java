package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRepository itemRepository;
    private final Pageable pageable = new MyPageRequest(0, 10, Sort.unsorted());

    private final Item firstItem = Item.builder()
            .name("First Item")
            .description("First Item")
            .available(true)
            .build();

    private final Item secondItem = Item.builder()
            .name("Second Item")
            .description("Second Item")
            .available(true)
            .build();

    private final Request firstRequest = Request.builder()
            .description("First Request Description")
            .build();

    private final Request secondRequest = Request.builder()
            .description("Second Request Description")
            .build();

    @Test
    void contextLoads() {
        assertNotNull(entityManager);
    }

    @Test
    void searchTest() {
        entityManager.persist(firstItem);
        entityManager.persist(secondItem);

        List<Item> itemList = itemRepository.search("item", pageable);

        assertEquals(itemList.size(), 2);
    }

    @Test
    void findAllByRequestIdsTest() {
        firstItem.setRequest(firstRequest);
        secondItem.setRequest(secondRequest);

        entityManager.persist(firstItem);
        entityManager.persist(secondItem);
        entityManager.persist(firstRequest);
        entityManager.persist(secondRequest);

        List<Item> itemList = itemRepository
                .findAllByRequestIds(List.of(firstRequest.getId(), secondRequest.getId()));
        assertEquals(itemList.size(), 2);
    }
}