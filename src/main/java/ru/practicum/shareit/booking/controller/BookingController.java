package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDTO;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.MyPageRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDTO createBookings(@RequestHeader(SHARER_USER_ID) long userId,
                                     @Valid @RequestBody BookingDTO bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDTO changeStatus(@RequestHeader(SHARER_USER_ID) long userId,
                                   @PathVariable long bookingId, @RequestParam(name = "approved") boolean status) {
        return bookingService.changeStatus(userId, bookingId, status);
    }

    @GetMapping("/{bookingId}")
    public BookingDTO getBookings(@RequestHeader(SHARER_USER_ID) long userId,
                                  @PathVariable long bookingId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDTO> getAll(@RequestHeader(SHARER_USER_ID) long userId,
                                   @RequestParam(defaultValue = "ALL") String state,
                                   @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                   @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        return bookingService.getAllByBookerId(userId, state, new MyPageRequest(from, size, Sort.unsorted()));
    }

    @GetMapping("/owner")
    public List<BookingDTO> getAllByOwner(@RequestHeader(SHARER_USER_ID) long userId,
                                          @RequestParam(defaultValue = "ALL") String state,
                                          @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                          @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        return bookingService.getAllByOwnerId(userId, state, new MyPageRequest(from, size, Sort.unsorted()));
    }
}
