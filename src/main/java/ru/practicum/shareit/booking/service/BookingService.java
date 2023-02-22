package ru.practicum.shareit.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDTO;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.BookingDateTimeException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingService(UserRepository userRepository,
                          ItemRepository itemRepository,
                          BookingRepository bookingRepository,
                          BookingMapper bookingMapper) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
    }

    public BookingDTO addBooking(long userId, BookingDTO bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        validate(bookingDto, booker, item);

        Booking booking = bookingMapper.toModel(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        log.info("Adding booking");

        return bookingMapper.toDTO(bookingRepository.save(booking));
    }

    public BookingDTO changeStatus(long userId, long bookingId, boolean status) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (checkOwnerItem(owner, booking)) {
            if (status && booking.getStatus().equals(BookingStatus.APPROVED)) {
                throw new BadRequestException("Booking is already APPROVED");
            } else if (status) {
                booking.setStatus(BookingStatus.APPROVED);
                bookingRepository.save(booking);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
                bookingRepository.save(booking);
            }
        } else {
            throw new NotFoundException("it's not the owner of the item");
        }
        return bookingMapper.toDTO(booking);
    }

    public BookingDTO getBooking(long userId, long bookingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (checkOwnerItem(user, booking) || checkBookerItem(user, booking)) {
            return bookingMapper.toDTO(bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new NotFoundException("Booking not found")));
        } else {
            throw new NotFoundException("User must be the owner or booker item");
        }
    }

    public List<BookingDTO> getAllByBookerId(long userId, String state, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        switch (state) {
            case "ALL":
                return bookingMapper.toDTOList(bookingRepository.findAllByBookerIdOrderByStartDesc(userId, pageable));
            case "CURRENT":
                return bookingMapper.toDTOList(bookingRepository.findAllByBookerCurrentState(userId, LocalDateTime.now(), pageable));
            case "PAST":
                return bookingMapper.toDTOList(bookingRepository.findAllByBookerPastState(userId, LocalDateTime.now(), pageable));
            case "FUTURE":
                return bookingMapper.toDTOList(bookingRepository.findAllByBookerFutureState(userId, LocalDateTime.now(), pageable));
            case "WAITING":
                return bookingMapper.toDTOList(bookingRepository.findAllByBookerAndStatus(userId, BookingStatus.WAITING, pageable));
            case "REJECTED":
                return bookingMapper.toDTOList(bookingRepository.findAllByBookerAndStatus(userId, BookingStatus.REJECTED, pageable));
            default:
                throw new UnsupportedStatusException("Incorrect state");
        }
    }

    public List<BookingDTO> getAllByOwnerId(long userId, String state, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        switch (state) {
            case "ALL":
                return bookingMapper.toDTOList(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, pageable));
            case "CURRENT":
                return bookingMapper.toDTOList(bookingRepository.findAllByOwnerCurrentState(userId, LocalDateTime.now(), pageable));
            case "PAST":
                return bookingMapper.toDTOList(bookingRepository.findAllByOwnerPastState(userId, LocalDateTime.now(), pageable));
            case "FUTURE":
                return bookingMapper.toDTOList(bookingRepository.findAllByOwnerFutureState(userId, LocalDateTime.now(), pageable));
            case "WAITING":
                return bookingMapper.toDTOList(bookingRepository.findAllByOwnerAndStatus(userId, BookingStatus.WAITING, pageable));
            case "REJECTED":
                return bookingMapper.toDTOList(bookingRepository.findAllByOwnerAndStatus(userId, BookingStatus.REJECTED, pageable));
            default:
                throw new UnsupportedStatusException("Incorrect state");
        }
    }

    private boolean checkOwnerItem(User user, Booking booking) {
        return user.equals(booking.getItem().getOwner());
    }

    private boolean checkBookerItem(User user, Booking booking) {
        return user.equals(booking.getBooker());
    }

    private void validate(BookingDTO bookingDTO, User booker, Item item) {
        if (bookingDTO.getEnd().isBefore(LocalDateTime.now()) || bookingDTO.getEnd().isBefore(bookingDTO.getStart())
                || bookingDTO.getStart().isBefore(LocalDateTime.now())) {
            throw new BookingDateTimeException("Incorrect date or time for booking");

        } else if (item.getOwner().equals(booker)) {
            throw new NotFoundException("Owner cannot book his item");

        } else if (!item.getAvailable()) {
            throw new BadRequestException("Item available should be true");
        }
    }
}
