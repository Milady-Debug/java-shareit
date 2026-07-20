package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findById(Long id);

    Item save(Item item);

    List<Item> findAllByOwner(Long userId);

    List<Item> searchByText(String text);

    void delete(Long id);

    boolean existsById(Long id);
}
