package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByOwnerIdOrderByCreatedDesc(long userId);

    @Query("SELECT request FROM Request AS request " +
            "WHERE request.owner.id <> ?1")
    List<Request> findAllByPageable(long userId, Pageable pageable);
}
