package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@Service
@RequiredArgsConstructor
public class BookingGatewayServiceImpl implements BookingGatewayService {

    private final BookingClient bookingClient;

    @Override
    public ResponseEntity<Object> getBookings(long userId, String stateParam, Integer from, Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @Override
    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
        return bookingClient.bookItem(userId, requestDto);
    }

    @Override
    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @Override
    public ResponseEntity<Object> approveBooking(long userId, Long bookingId, boolean approved) {
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @Override
    public ResponseEntity<Object> getOwnerBookings(long userId, String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getOwnerBookings(userId, state);
    }
}