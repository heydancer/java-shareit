package ru.practicum.shareit.item.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDTO;
import ru.practicum.shareit.item.dto.ItemDTO;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       BookingRepository bookingRepository,
                       CommentRepository commentRepository,
                       RequestRepository itemRequestRepository,
                       BookingMapper bookingMapper,
                       CommentMapper commentMapper,
                       ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = itemRequestRepository;
        this.bookingMapper = bookingMapper;
        this.commentMapper = commentMapper;
        this.itemMapper = itemMapper;
    }

    public ItemDTO addItem(long userId, ItemDTO itemDTO) {
        validate(itemDTO, userId);

        Item item;
        Request request = null;

        if (itemDTO.getRequestId() != null) {
            request = requestRepository.findById(itemDTO.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
        }

        log.info("Adding item");

        if (request != null) {
            item = itemRepository.save(itemMapper.toModel(itemDTO, request));
            itemDTO.setId(item.getId());
            itemDTO.setRequestId(request.getId());
        } else {
            item = itemRepository.save(itemMapper.toModel(itemDTO));
            itemDTO.setId(item.getId());
        }

        return itemDTO;
    }

    public CommentDTO addComment(long userId, long itemId, CommentDTO commentDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        List<Booking> bookings = bookingRepository.findAllByBookerAndItem(userId, itemId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new BadRequestException("Booking not made");
        } else {
            log.info("Adding comment");

            Comment comment = commentMapper.toModel(commentDTO);
            comment.setAuthor(user);
            comment.setItem(item);
            comment.setCreated(LocalDateTime.now());

            return commentMapper.toDTO(commentRepository.save(comment));
        }
    }

    public ItemDTO getById(long userId, long itemId) {
        log.info("Getting item with ID: {}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        ItemDTO itemDTO = itemMapper.toDTO(item);

        if (item.getOwner().getId() == userId) {
            setBooking(itemDTO, itemId);
        }

        itemDTO.setComments(commentMapper.toDTOList(commentRepository.findAllByItemId(itemId)));

        return itemDTO;
    }

    public List<ItemDTO> getItemsByUserId(long userId, Pageable pageable) {

        log.info("Getting all items by user ID: {}", userId);

        List<ItemDTO> items = itemMapper.toDTOList(itemRepository.findAllByOwnerIdOrderByIdAsc(userId, pageable));
        items.forEach(itemDTO -> setBooking(itemDTO, itemDTO.getId()));

        return items;
    }

    public List<ItemDTO> getItemsByText(String text, Pageable pageable) {
        log.info("Getting all items by text: {}", text);

        if (StringUtils.isEmpty(text)) {
            return List.of();
        }

        return itemMapper.toDTOList(itemRepository.search(text.toLowerCase(), pageable));
    }

    public ItemDTO updateItem(long userId, long itemId, ItemDTO itemDto) {
        Item item = itemMapper.toModel(itemDto);
        checkForUpdate(userId, itemId, item);

        log.info("Updating item with ID: {}", itemId);

        return itemMapper.toDTO(itemRepository.save(item));
    }

    public void removeItemById(long userId, long itemId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        itemRepository.deleteById(itemId);
    }

    private void validate(ItemDTO itemDTO, long userId) {
        if (itemDTO.getAvailable() == null) {
            throw new BadRequestException("Available cannot be null");
        } else if (itemDTO.getDescription() == null) {
            throw new BadRequestException("Description cannot be null");
        } else {
            itemDTO.setOwner(userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found")));
        }
    }

    private void checkForUpdate(long userId, long itemId, Item item) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item itemBeforeUpdate = itemRepository
                .findById(itemId).orElseThrow(() -> new NotFoundException("Item not found"));

        if (itemBeforeUpdate.getOwner().equals(user)) {
            item.setId(itemId);
            item.setOwner(user);

            if (item.getName() == null && item.getDescription() == null) {
                item.setName(itemBeforeUpdate.getName());
                item.setDescription(itemBeforeUpdate.getDescription());

            } else if (item.getName() == null && item.getAvailable() == null) {
                item.setName(itemBeforeUpdate.getName());
                item.setAvailable(itemBeforeUpdate.getAvailable());

            } else if (item.getDescription() == null && item.getAvailable() == null) {
                item.setDescription(itemBeforeUpdate.getDescription());
                item.setAvailable(itemBeforeUpdate.getAvailable());
            }

        } else {
            throw new NotFoundException("Item does not belong to the user");
        }
    }

    private void setBooking(ItemDTO itemDTO, long itemId) {
        Optional<Booking> lastBooking = bookingRepository.findLastBooking(itemId, LocalDateTime.now());
        Optional<Booking> nextBooking = bookingRepository.findNextBooking(itemId, LocalDateTime.now());

        lastBooking.ifPresent(booking -> itemDTO.setLastBooking(bookingMapper.toSimpleDTO(booking)));
        nextBooking.ifPresent(booking -> itemDTO.setNextBooking(bookingMapper.toSimpleDTO(booking)));
    }
}
