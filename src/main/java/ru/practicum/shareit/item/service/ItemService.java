package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getItemsByOwner(Long userId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);

    ItemWithCommentsDto getItemWithComments(Long itemId);

    List<ItemWithCommentsDto> getItemsWithCommentsByOwner(Long userId);

    ItemWithBookingsAndCommentsDto getItemWithDetails(Long itemId, Long userId);

    List<ItemWithBookingsAndCommentsDto> getItemsWithDetailsByOwner(Long userId);
}
