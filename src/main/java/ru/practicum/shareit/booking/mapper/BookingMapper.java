package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDTO;
import ru.practicum.shareit.booking.dto.SimplifiedBookingDTO;
import ru.practicum.shareit.common.BaseMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper implements BaseMapper<BookingDTO, Booking> {
    @Override
    public Booking toModel(BookingDTO bookingDto) {
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    @Override
    public BookingDTO toDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    @Override
    public List<BookingDTO> toDTOList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SimplifiedBookingDTO toSimpleDTO(Booking booking) {
        return SimplifiedBookingDTO.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
