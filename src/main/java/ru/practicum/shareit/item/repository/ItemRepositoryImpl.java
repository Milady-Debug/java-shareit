package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        storage.put(item.getId(), item);
        return item;
    }

    public List<Item> findAllByOwner(Long userId) {
        return storage.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Item> searchByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        return storage.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> {
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";
                    String desc = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains(lowerText) || desc.contains(lowerText);
                })
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
