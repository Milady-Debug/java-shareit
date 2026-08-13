package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toEntity(ItemRequestCreateDto dto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestResponseDto toResponse(ItemRequest request, List<Item> items) {
        List<ItemShortDto> itemShorts = items.stream()
                .map(i -> new ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                .collect(Collectors.toList());
        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemShorts
        );
    }
}