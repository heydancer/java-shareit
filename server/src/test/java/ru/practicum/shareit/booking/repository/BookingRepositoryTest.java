package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
@AutoConfigureTestDatabase
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;
    private final Pageable pageable = new MyPageRequest(0, 10, Sort.unsorted());

    private final User owner = User.builder()
            .name("First User")
            .email("firstuser@yandex.ru")
            .build();

    private final User booker = User.builder()
            .name("Second User")
            .email("seconduser@yandex.ru")
            .build();

    private final Item item = Item.builder()
            .name("Item")
            .description("Item Description")
            .available(true)
            .owner(owner)
            .build();

    private final Booking booking = Booking.builder()
            .item(item)
            .booker(booker)
            .build();

    @Test
    void contextLoads() {
        assertNotNull(entityManager);
    }

    @Test
    void findAllByBookerCurrentStateTest() {
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByBookerCurrentState(booker.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAllByBookerPastStateTest() {
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByBookerPastState(booker.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAllByBookerFutureStateTest() {
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByBookerFutureState(booker.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAllByBookerAndStatusTest() {
        booking.setStatus(BookingStatus.WAITING);

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByBookerAndStatus(booker.getId(), BookingStatus.WAITING, pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAllByOwnerCurrentStateTest() {
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByOwnerCurrentState(owner.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));

    }

    @Test
    void findAllByOwnerPastStateTest() {
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByOwnerPastState(owner.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAllByOwnerFutureStateTest() {
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByOwnerFutureState(owner.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAllByOwnerAndStatusTest() {
        booking.setStatus(BookingStatus.WAITING);

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByOwnerAndStatus(owner.getId(), BookingStatus.WAITING, pageable);

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findLastBookingTest() {
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        Optional<Booking> lastBooking = bookingRepository
                .findLastBooking(item.getId(), LocalDateTime.now());

        assertTrue(lastBooking.isPresent());
        Assertions.assertEquals(lastBooking.get().getStart(), booking.getStart());
        Assertions.assertEquals(lastBooking.get().getEnd(), booking.getEnd());
    }

    @Test
    void findNextBookingTest() {
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        Optional<Booking> lastBooking = bookingRepository
                .findNextBooking(item.getId(), LocalDateTime.now());

        assertTrue(lastBooking.isPresent());
        Assertions.assertEquals(lastBooking.get().getStart(), booking.getStart());
        Assertions.assertEquals(lastBooking.get().getEnd(), booking.getEnd());
    }

    @Test
    void findAllByBookerAndItemTest() {
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking);

        List<Booking> bookingList = bookingRepository
                .findAllByBookerAndItem(booker.getId(), item.getId(), LocalDateTime.now());

        assertEquals(1, bookingList.size());
        Assertions.assertEquals(booking, bookingList.get(0));
    }
}