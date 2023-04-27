package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase
class RequestRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RequestRepository requestRepository;
    private final Pageable pageable = new MyPageRequest(0, 10, Sort.unsorted());

    private final User user = User.builder()
            .name("Test User")
            .email("Test Description")
            .build();

    private final Request request = Request.builder()
            .owner(user)
            .build();

    @Test
    void contextLoadsTest() {
        assertNotNull(entityManager);
    }

    @Test
    void findAllByPageableTest() {
        entityManager.persist(user);
        entityManager.persist(request);

        List<Request> requestList = requestRepository
                .findAllByPageable(99999, pageable);

        assertEquals(requestList.size(), 1);
    }
}