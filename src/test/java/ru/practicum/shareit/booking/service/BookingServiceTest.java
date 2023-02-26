package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDTO;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.BookingDateTimeException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    public static final long FAKE_ID = 99999L;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingService service;
    @Spy
    private BookingMapper bookingMapper;
    private Booking booking;
    private User booker;
    private User owner;
    private User another;
    private Item item;

    @BeforeEach
    void beforeEach() {
        booker = User.builder()
                .id(1L)
                .name("Test Booker")
                .email("test@yandex.ru")
                .build();

        owner = User.builder()
                .id(2L)
                .name("Test Owner")
                .email("owner@yandex.ru")
                .build();

        another = User.builder()
                .id(3L)
                .name("Another User")
                .email("another@yandex.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void shouldAddBooking() {
        BookingDTO bookingDTO = bookingMapper.toDTO(booking);

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDTO createdBooking = service.addBooking(booker.getId(), bookingDTO);

        assertEquals(createdBooking.getId(), bookingDTO.getId());
        assertEquals(createdBooking.getStart(), bookingDTO.getStart());
        assertEquals(createdBooking.getEnd(), bookingDTO.getEnd());
        assertEquals(createdBooking.getBooker(), bookingDTO.getBooker());
        assertEquals(createdBooking.getItem(), bookingDTO.getItem());
        assertEquals(createdBooking.getStatus(), bookingDTO.getStatus());
    }

    @Test
    void shouldAddBookingAndCheckRepositoryMethodCalls() {
        BookingDTO bookingDTO = bookingMapper.toDTO(booking);

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        service.addBooking(booker.getId(), bookingDTO);

        verify(userRepository, times(1))
                .findById(booker.getId());
        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    @Test
    void shouldAddBookingWithIncorrectDate() {
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStart(LocalDateTime.now().plusDays(1));
        BookingDTO bookingDTO = bookingMapper.toDTO(booking);

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        final BookingDateTimeException exception = assertThrows(BookingDateTimeException.class,
                () -> service.addBooking(booker.getId(), bookingDTO));

        assertEquals("Incorrect date or time for booking", exception.getMessage());
    }

    @Test
    void shouldAddBookingWithOwnerBooker() {
        item.setOwner(booker);
        BookingDTO bookingDTO = bookingMapper.toDTO(booking);

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addBooking(booker.getId(), bookingDTO));

        assertEquals("Owner cannot book his item", exception.getMessage());
    }

    @Test
    void shouldAddBookingWithItemAvailableFalse() {
        item.setAvailable(false);
        BookingDTO bookingDTO = bookingMapper.toDTO(booking);

        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        final BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.addBooking(booker.getId(), bookingDTO));

        assertEquals("Item available should be true", exception.getMessage());
    }

    @Test
    void shouldChangeStatusApproved() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDTO updatedBooking = service.changeStatus(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, updatedBooking.getStatus());
    }

    @Test
    void shouldChangeStatusReject() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDTO updatedBooking = service.changeStatus(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, updatedBooking.getStatus());
    }

    @Test
    void shouldChangeStatusAndCheckRepositoryMethodCalls() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        service.changeStatus(owner.getId(), booking.getId(), false);

        verify(userRepository, times(1))
                .findById(owner.getId());
        verify(bookingRepository, times(1))
                .findById(booking.getId());
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    @Test
    void shouldChangeStatusAlreadyApproved() {
        booking.setStatus(BookingStatus.APPROVED);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        final BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.changeStatus(owner.getId(), booking.getId(), true));

        assertEquals("Booking is already APPROVED", exception.getMessage());
    }

    @Test
    void shouldChangeStatusWithIncorrectUser() {
        when(userRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.changeStatus(FAKE_ID, booking.getId(), true));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldChangeStatusWithIncorrectBooking() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("Booking not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.changeStatus(owner.getId(), FAKE_ID, true));

        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void shouldChangeStatusWithNotOwnerItem() {
        item.setOwner(booker);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.changeStatus(owner.getId(), booking.getId(), false));

        assertEquals("it's not the owner of the item", exception.getMessage());
    }

    @Test
    void shouldReturnBooking() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        BookingDTO bookingDTO = service.getBooking(owner.getId(), booking.getId());
        assertEquals(bookingDTO.getId(), booking.getId());
        assertEquals(bookingDTO.getItem(), item);
        assertEquals(bookingDTO.getBooker(), booker);
    }

    @Test
    void shouldReturnBookingAndCheckRepositoryMethodCalls() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        service.getBooking(owner.getId(), booking.getId());

        verify(userRepository, times(1))
                .findById(owner.getId());
        verify(bookingRepository, times(2))
                .findById(booking.getId());
    }

    @Test
    void shouldReturnBookingWithIncorrectUserId() {
        item.setOwner(another);

        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getBooking(owner.getId(), booking.getId()));

        assertEquals("User must be the owner or booker item", exception.getMessage());
    }

    @Test
    void shouldReturnAllByBookerIdWithStateAll() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        service.getAllByBookerId(booker.getId(), "ALL",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByBookerIdWithStateCurrent() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        service.getAllByBookerId(booker.getId(), "CURRENT",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByBookerCurrentState(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByBookerIdWithStatePast() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        service.getAllByBookerId(booker.getId(), "PAST",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByBookerPastState(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByBookerIdWithStateFuture() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        service.getAllByBookerId(booker.getId(), "FUTURE",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByBookerFutureState(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByBookerIdWithStateWaiting() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        service.getAllByBookerId(booker.getId(), "WAITING",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByBookerAndStatus(booker.getId(), BookingStatus.WAITING,
                        new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void shouldReturnAllByBookerIdWithStateReject() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        service.getAllByBookerId(booker.getId(), "REJECTED",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByBookerAndStatus(booker.getId(), BookingStatus.REJECTED,
                        new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void shouldReturnAllByBookerIdWithStateIncorrect() {
        when(userRepository.findById(booker.getId()))
                .thenReturn(Optional.of(booker));

        final UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> service.getAllByBookerId(booker.getId(), "INCORRECT STATE",
                        new MyPageRequest(0, 10, Sort.unsorted())));

        assertEquals("Incorrect state", exception.getMessage());
    }

    @Test
    void shouldReturnAllByOwnerIdWithStateAll() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        service.getAllByOwnerId(owner.getId(), "ALL",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByOwnerIdWithStateCurrent() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        service.getAllByOwnerId(owner.getId(), "CURRENT",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByOwnerCurrentState(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByOwnerIdWithStatePast() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        service.getAllByOwnerId(owner.getId(), "PAST",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByOwnerPastState(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByOwnerIdWithStateFuture() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        service.getAllByOwnerId(owner.getId(), "FUTURE",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByOwnerFutureState(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void shouldReturnAllByOwnerIdWithStateWaiting() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        service.getAllByOwnerId(owner.getId(), "WAITING",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByOwnerAndStatus(owner.getId(), BookingStatus.WAITING,
                        new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void shouldReturnAllByOwnerIdWithStateReject() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        service.getAllByOwnerId(owner.getId(), "REJECTED",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(bookingRepository)
                .findAllByOwnerAndStatus(owner.getId(), BookingStatus.REJECTED,
                        new MyPageRequest(0, 10, Sort.unsorted()));
    }

    @Test
    void shouldReturnAllByOwnerIdWithStateIncorrect() {
        when(userRepository.findById(owner.getId()))
                .thenReturn(Optional.of(owner));

        final UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> service.getAllByOwnerId(owner.getId(), "INCORRECT STATE",
                        new MyPageRequest(0, 10, Sort.unsorted())));

        assertEquals("Incorrect state", exception.getMessage());
    }
}