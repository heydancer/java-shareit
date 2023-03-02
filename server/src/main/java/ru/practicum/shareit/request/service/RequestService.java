package ru.practicum.shareit.request.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDTO;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestService.class);
    private final RequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final RequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;

    @Autowired
    public RequestService(RequestRepository itemRequestRepository,
                          UserRepository userRepository,
                          RequestMapper itemRequestMapper,
                          ItemMapper itemMapper,
                          ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRequestMapper = itemRequestMapper;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
    }

    public RequestDTO addRequest(long userId, RequestDTO requestDTO) {
        User user = checkUser(userId);

        Request request = itemRequestMapper.toModel(requestDTO);
        request.setCreated(LocalDateTime.now());
        request.setOwner(user);

        log.info("Create item request");

        return itemRequestMapper.toDTO(itemRequestRepository.save(request));
    }

    public List<RequestDTO> getRequestListByOwnerId(long userId) {
        checkUser(userId);

        List<RequestDTO> requests = itemRequestMapper.toDTOList(itemRequestRepository
                .findAllByOwnerIdOrderByCreatedDesc(userId));

        List<ItemDTO> itemList = itemMapper.toDTOList(itemRepository
                .findAllByRequestOwnerId(userId));

        log.info("Get item request list");

        return getRequestDTOs(requests, itemList);
    }

    public List<RequestDTO> getAllRequestList(long userId, Pageable pageable) {
        checkUser(userId);

        List<RequestDTO> requests = itemRequestRepository.findAllByPageable(userId, pageable)
                .stream()
                .map(itemRequestMapper::toDTO)
                .collect(Collectors.toList());

        List<ItemDTO> itemList = itemMapper.toDTOList(itemRepository.findAllByRequestIds(requests
                .stream()
                .map(RequestDTO::getId)
                .collect(Collectors.toList())));

        log.info("Get all item request");

        return getRequestDTOs(requests, itemList);
    }

    public RequestDTO getRequestById(long userId, long requestId) {
        checkUser(userId);

        Request request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<ItemDTO> items = itemMapper.toDTOList(itemRepository.findAllByRequestId(requestId));

        RequestDTO requestDto = itemRequestMapper.toDTO(request);
        requestDto.setItems(items);

        return requestDto;
    }

    private List<RequestDTO> getRequestDTOs(List<RequestDTO> requests, List<ItemDTO> items) {
        Map<Long, RequestDTO> requestDTOMap = new HashMap<>();

        for (RequestDTO request : requests) {
            requestDTOMap.put(request.getId(), request);
        }

        for (ItemDTO itemDTO : items) {
            if (requestDTOMap.containsKey(itemDTO.getRequestId())) {
                requestDTOMap.get(itemDTO.getRequestId()).getItems().add(itemDTO);
            }
        }

        return new ArrayList<>(requestDTOMap.values());
    }

    private User checkUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
