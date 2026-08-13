package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
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
    public BookingResponseDto createBooking(Long userId, BookingDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (item.getOwner().getId().equals(userId))
            throw new ValidationException("Владелец не может бронировать свою вещь");
        if (!item.isAvailable())
            throw new ValidationException("Вещь недоступна");

        Booking booking = BookingMapper.toBooking(dto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        return BookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(userId))
            throw new ForbiddenException("Только владелец может подтверждать");
        if (booking.getStatus() != Status.WAITING)
            throw new ValidationException("Статус уже изменён");
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId))
            throw new ForbiddenException("Нет доступа");
        return BookingMapper.toBookingResponse(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsForCurrentUser(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return getBookingsByState(userId, state, false).stream()
                .map(BookingMapper::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsForOwner(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return getBookingsByState(userId, state, true).stream()
                .map(BookingMapper::toBookingResponse)
                .collect(Collectors.toList());
    }

    private List<Booking> getBookingsByState(Long userId, BookingState state, boolean isOwner) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                return isOwner ? bookingRepository.findAllByOwnerId(userId)
                        : bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case WAITING:
                return isOwner ? bookingRepository.findByOwnerIdAndStatus(userId, Status.WAITING)
                        : bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED:
                return isOwner ? bookingRepository.findByOwnerIdAndStatus(userId, Status.REJECTED)
                        : bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            case CURRENT:
                return isOwner ? bookingRepository.findCurrentByOwner(userId, now)
                        : bookingRepository.findCurrentByBooker(userId, now);
            case PAST:
                return isOwner ? bookingRepository.findPastByOwner(userId, now)
                        : bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE:
                return isOwner ? bookingRepository.findFutureByOwner(userId, now)
                        : bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            default:
                throw new IllegalArgumentException("Неподдерживаемый state: " + state);
        }
    }
}