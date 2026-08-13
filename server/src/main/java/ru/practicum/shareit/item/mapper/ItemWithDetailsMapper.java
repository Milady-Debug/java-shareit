package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemWithDetailsMapper {
    public static ItemWithBookingsAndCommentsDto toDto(
            Item item,
            List<Comment> comments,
            Booking lastBooking,
            Booking nextBooking
    ) {
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        BookingShortDto lastDto = lastBooking != null
                ? new BookingShortDto(lastBooking.getId(), lastBooking.getStart())
                : null;
        BookingShortDto nextDto = nextBooking != null
                ? new BookingShortDto(nextBooking.getId(), nextBooking.getStart())
                : null;

        return new ItemWithBookingsAndCommentsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                lastDto,
                nextDto,
                commentDtos
        );
    }
}