package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemWithCommentsDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private List<CommentDto> comments;
}