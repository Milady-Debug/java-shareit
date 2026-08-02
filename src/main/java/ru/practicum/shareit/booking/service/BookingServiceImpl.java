package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Владелец не может бронировать свою вещь");
        }
        if (!item.isAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может подтверждать бронирование");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Статус бронирования уже изменён");
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updated = bookingRepository.save(booking);
        return BookingMapper.toBookingResponse(updated);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("У вас нет доступа к этому бронированию");
        }
        return BookingMapper.toBookingResponse(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsForCurrentUser(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByBooker(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый state: " + state);
        }
        return bookings.stream().map(BookingMapper::toBookingResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsForOwner(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByOwnerId(userId);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(userId, Status.REJECTED);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByOwner(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findPastByOwner(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByOwner(userId, now);
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый state: " + state);
        }
        return bookings.stream().map(BookingMapper::toBookingResponse).collect(Collectors.toList());
    }
}
