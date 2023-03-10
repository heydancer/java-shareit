package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.BaseMapper;
import ru.practicum.shareit.item.dto.CommentDTO;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper implements BaseMapper<CommentDTO, Comment> {
    @Override
    public Comment toModel(CommentDTO commentDTO) {
        return Comment.builder()
                .text(commentDTO.getText())
                .build();
    }

    @Override
    public CommentDTO toDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    @Override
    public List<CommentDTO> toDTOList(List<Comment> comments) {
        return comments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
