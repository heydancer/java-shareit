package ru.practicum.shareit.common;

import java.util.List;

public interface BaseMapper<D, M> {
    M toModel(D d);

    D toDTO(M m);

    List<D> toDTOList(List<M> mList);
}