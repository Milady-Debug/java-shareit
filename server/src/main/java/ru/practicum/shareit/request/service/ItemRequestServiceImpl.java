package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestCreateDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new ValidationException("Описание не может быть пустым");

        ItemRequest request = ItemRequestMapper.toEntity(dto, requestor);
        request = requestRepository.save(request);
        return ItemRequestMapper.toResponse(request, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(req -> {
                    List<Item> items = itemRepository.findByRequestId(req.getId());
                    return ItemRequestMapper.toResponse(req, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequestsExceptUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return requestRepository.findAllByOrderByCreatedDesc().stream()
                .filter(req -> !req.getRequestor().getId().equals(userId))
                .map(req -> {
                    List<Item> items = itemRepository.findByRequestId(req.getId());
                    return ItemRequestMapper.toResponse(req, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return ItemRequestMapper.toResponse(request, itemRepository.findByRequestId(requestId));
    }
}