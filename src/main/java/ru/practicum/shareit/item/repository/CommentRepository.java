package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(long itemId);
}
