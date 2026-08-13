package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @RequestBody ItemDto dto) {
        return itemService.createItem(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto dto) {
        return itemService.updateItem(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsAndCommentsDto getWithDetails(@RequestHeader(USER_ID_HEADER) Long userId,
                                                         @PathVariable Long itemId) {
        return itemService.getItemWithDetails(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingsAndCommentsDto> getOwnWithDetails(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.getItemsWithDetailsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto dto) {
        return itemService.addComment(userId, itemId, dto);
    }
}