package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemWithCommentsMapper {

    public static ItemWithCommentsDto toItemWithCommentsDto(Item item, List<Comment> comments) {
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return new ItemWithCommentsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                commentDtos
        );
    }
}