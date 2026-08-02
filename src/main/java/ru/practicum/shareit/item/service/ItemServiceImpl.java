package ru.practicum.shareit.item.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemWithCommentsMapper;
import ru.practicum.shareit.item.mapper.ItemWithDetailsMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.search(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        boolean hasBooked = bookingRepository.existsFinishedBooking(userId, itemId, LocalDateTime.now());
        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь или аренда ещё не завершена");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    @Override
    public ItemWithCommentsDto getItemWithComments(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return ItemWithCommentsMapper.toItemWithCommentsDto(item, comments);
    }

    @Override
    public List<ItemWithCommentsDto> getItemsWithCommentsByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(item -> {
                    List<Comment> comments = commentRepository.findAllByItemId(item.getId());
                    return ItemWithCommentsMapper.toItemWithCommentsDto(item, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemWithBookingsAndCommentsDto getItemWithDetails(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Комментарии
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        // Бронирования показываем только владельцу
        Booking lastBooking = null;
        Booking nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            Pageable pageable = PageRequest.of(0, 1);
            List<Booking> lastList = bookingRepository.findLastBookingsByItem(itemId, pageable);
            if (!lastList.isEmpty()) {
                lastBooking = lastList.get(0);
            }
            List<Booking> nextList = bookingRepository.findNextBookingsByItem(itemId, pageable);
            if (!nextList.isEmpty()) {
                nextBooking = nextList.get(0);
            }
        }

        return ItemWithDetailsMapper.toDto(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemWithBookingsAndCommentsDto> getItemsWithDetailsByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        return items.stream()
                .map(item -> {
                    List<Comment> comments = commentRepository.findAllByItemId(item.getId());
                    Pageable pageable = PageRequest.of(0, 1);
                    Booking lastBooking = null;
                    Booking nextBooking = null;
                    List<Booking> lastList = bookingRepository.findLastBookingsByItem(item.getId(), pageable);
                    if (!lastList.isEmpty()) {
                        lastBooking = lastList.get(0);
                    }
                    List<Booking> nextList = bookingRepository.findNextBookingsByItem(item.getId(), pageable);
                    if (!nextList.isEmpty()) {
                        nextBooking = nextList.get(0);
                    }
                    return ItemWithDetailsMapper.toDto(item, comments, lastBooking, nextBooking);
                })
                .collect(Collectors.toList());
    }

}