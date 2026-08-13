package ru.practicum.shareit.booking.service;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

public interface BookingGatewayService {

    ResponseEntity<Object> getBookings(long userId, String stateParam, Integer from, Integer size);

    ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto);

    ResponseEntity<Object> getBooking(long userId, Long bookingId);

    ResponseEntity<Object> approveBooking(long userId, Long bookingId, boolean approved);

    ResponseEntity<Object> getOwnerBookings(long userId, String stateParam);
}