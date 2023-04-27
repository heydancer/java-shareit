package ru.practicum.shareit.item.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.SimplifiedBookingDTO;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.MyPageRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    public static final long FAKE_ID = 99999L;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemService service;

    @Spy
    private ItemMapper itemMapper;

    @Spy
    private CommentMapper commentMapper;

    @Spy
    private BookingMapper bookingMapper;
    private Item item;
    private User user;
    private Request request;
    private Comment comment;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@yandex.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .request(request)
                .build();

        request = Request.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .owner(user)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Test Comment")
                .author(user)
                .item(item)
                .build();
    }

    @Test
    void shouldCreateItem() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        ItemDTO itemDTO = service.addItem(user.getId(), itemMapper.toDTO(item));

        Assertions.assertEquals(user.getId(), itemDTO.getId());
        Assertions.assertEquals("Test Item", itemDTO.getName());
        Assertions.assertEquals("Test Description", itemDTO.getDescription());
        Assertions.assertTrue(itemDTO.getAvailable());
    }

    @Test
    void shouldCreateItemAndCheckRepositoryMethodCalls() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        service.addItem(user.getId(), itemMapper.toDTO(item));

        verify(userRepository, times(1))
                .findById(user.getId());
        verify(itemRepository, times(1))
                .save(any(Item.class));
    }

    @Test
    void shouldCreateItemWithNullAvailable() {
        item.setAvailable(null);

        final BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.addItem(user.getId(), itemMapper.toDTO(item)));

        Assertions.assertEquals("Available cannot be null", exception.getMessage());
    }

    @Test
    void shouldCreateItemWithNullDescription() {
        item.setDescription(null);

        final BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.addItem(user.getId(), itemMapper.toDTO(item)));

        Assertions.assertEquals("Description cannot be null", exception.getMessage());
    }

    @Test
    void shouldCreateItemWithFakeUserId() {
        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addItem(FAKE_ID, itemMapper.toDTO(item)));

        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldCreateItemWithRequest() {
        item.setRequest(request);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        ItemDTO itemDTO = service.addItem(user.getId(), itemMapper.toDTO(item));

        Assertions.assertEquals(user.getId(), itemDTO.getId());
        Assertions.assertEquals(request.getId(), itemDTO.getRequestId());
        Assertions.assertEquals("Test Item", itemDTO.getName());
        Assertions.assertEquals("Test Description", itemDTO.getDescription());
        Assertions.assertTrue(itemDTO.getAvailable());
    }

    @Test
    void shouldCreateItemWithNotFoundRequest() {
        item.setRequest(request);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addItem(user.getId(), itemMapper.toDTO(item)));

        Assertions.assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void shouldCreateCommentWithEmptyBookings() {
        CommentDTO commentDTO = commentMapper.toDTO(comment);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerAndItem(anyLong(), anyLong(), any()))
                .thenReturn(List.of());

        final BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.addComment(user.getId(), item.getId(), commentDTO));

        Assertions.assertEquals("Booking not made", exception.getMessage());
    }

    @Test
    void shouldCreateComment() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .item(item)
                .booker(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(8))
                .item(item)
                .booker(user)
                .build();

        CommentDTO commentDTO = commentMapper.toDTO(comment);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerAndItem(anyLong(), anyLong(), any()))
                .thenReturn(List.of(booking1, booking2));
        when(commentMapper.toModel(commentDTO))
                .thenReturn(comment);
        when(commentRepository.save(comment))
                .thenReturn(comment);

        CommentDTO createdComment = service.addComment(user.getId(), item.getId(), commentDTO);

        Assertions.assertEquals(createdComment.getAuthorName(), user.getName());
    }

    @Test
    void shouldCreateCommentAndCheckRepositoryMethodCalls() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .item(item)
                .booker(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(8))
                .item(item)
                .booker(user)
                .build();

        CommentDTO commentDTO = commentMapper.toDTO(comment);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerAndItem(anyLong(), anyLong(), any()))
                .thenReturn(List.of(booking1, booking2));
        when(commentMapper.toModel(commentDTO))
                .thenReturn(comment);
        when(commentRepository.save(comment))
                .thenReturn(comment);

        service.addComment(user.getId(), item.getId(), commentDTO);

        verify(userRepository, times(1))
                .findById(user.getId());
        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(bookingRepository, times(1))
                .findAllByBookerAndItem(anyLong(), anyLong(), any());
        verify(commentRepository, times(1))
                .save(comment);
    }

    @Test
    void shouldReturnItemByIdWithOwner() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .item(item)
                .booker(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(8))
                .item(item)
                .booker(user)
                .build();

        List<CommentDTO> comments = commentMapper.toDTOList(List.of(comment));

        SimplifiedBookingDTO simplifiedBookingDTO1 = bookingMapper.toSimpleDTO(booking1);
        SimplifiedBookingDTO simplifiedBookingDTO2 = bookingMapper.toSimpleDTO(booking2);

        item.setOwner(user);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findLastBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking1));
        when(bookingRepository.findNextBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking2));
        when(bookingMapper.toSimpleDTO(booking1))
                .thenReturn(simplifiedBookingDTO1);
        when(bookingMapper.toSimpleDTO(booking2))
                .thenReturn(simplifiedBookingDTO2);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(comment));

        ItemDTO itemDTO = service.getById(user.getId(), item.getId());

        Assertions.assertEquals(itemDTO.getId(), 1L);
        Assertions.assertEquals(itemDTO.getName(), item.getName());
        Assertions.assertEquals(itemDTO.getDescription(), item.getDescription());
        Assertions.assertEquals(itemDTO.getLastBooking(), simplifiedBookingDTO1);
        Assertions.assertEquals(itemDTO.getNextBooking(), simplifiedBookingDTO2);
        Assertions.assertEquals(itemDTO.getComments(), comments);
    }

    @Test
    void shouldReturnItemByIdWithOwnerAndCheckRepositoryMethodCalls() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .item(item)
                .booker(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(8))
                .item(item)
                .booker(user)
                .build();

        SimplifiedBookingDTO simplifiedBookingDTO1 = bookingMapper.toSimpleDTO(booking1);
        SimplifiedBookingDTO simplifiedBookingDTO2 = bookingMapper.toSimpleDTO(booking2);

        item.setOwner(user);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findLastBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking1));
        when(bookingRepository.findNextBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking2));
        when(bookingMapper.toSimpleDTO(booking1))
                .thenReturn(simplifiedBookingDTO1);
        when(bookingMapper.toSimpleDTO(booking2))
                .thenReturn(simplifiedBookingDTO2);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(comment));

        service.getById(user.getId(), item.getId());

        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(bookingRepository, times(1))
                .findLastBooking(anyLong(), any());
        verify(bookingRepository, times(1))
                .findNextBooking(anyLong(), any());
        verify(commentRepository, times(1))
                .findAllByItemId(item.getId());
    }

    @Test
    void shouldReturnItemById() {
        List<CommentDTO> comments = commentMapper.toDTOList(List.of(comment));

        item.setOwner(user);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(comment));

        ItemDTO itemDTO = service.getById(FAKE_ID, item.getId());

        Assertions.assertEquals(itemDTO.getId(), 1L);
        Assertions.assertEquals(itemDTO.getName(), item.getName());
        Assertions.assertEquals(itemDTO.getDescription(), item.getDescription());
        Assertions.assertEquals(itemDTO.getComments(), comments);
        assertNull(itemDTO.getLastBooking());
        assertNull(itemDTO.getNextBooking());
    }

    @Test
    void shouldReturnItemByIdAndCheckRepositoryMethodCalls() {
        item.setOwner(user);

        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(List.of(comment));

        service.getById(FAKE_ID, item.getId());

        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(commentRepository, times(1))
                .findAllByItemId(item.getId());
    }

    @Test
    void shouldReturnItemByFakeId() {
        when(itemRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("Item not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getById(user.getId(), FAKE_ID));

        Assertions.assertEquals("Item not found", exception.getMessage());
    }

    @Test
    void shouldReturnItemsByUserId() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .item(item)
                .booker(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(8))
                .item(item)
                .booker(user)
                .build();

        item.setOwner(user);

        SimplifiedBookingDTO simplifiedBookingDTO1 = bookingMapper.toSimpleDTO(booking1);
        SimplifiedBookingDTO simplifiedBookingDTO2 = bookingMapper.toSimpleDTO(booking2);

        when(itemRepository.findAllByOwnerIdOrderByIdAsc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(item));
        when(bookingRepository.findLastBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking1));
        when(bookingRepository.findNextBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking2));
        when(bookingMapper.toSimpleDTO(booking1))
                .thenReturn(simplifiedBookingDTO1);
        when(bookingMapper.toSimpleDTO(booking2))
                .thenReturn(simplifiedBookingDTO2);

        List<ItemDTO> items = service.getItemsByUserId(user.getId(),
                new MyPageRequest(0, 10, Sort.unsorted()));

        Assertions.assertEquals(items.get(0).getId(), item.getId());
        Assertions.assertEquals(items.get(0).getName(), item.getName());
        Assertions.assertEquals(items.get(0).getLastBooking(), simplifiedBookingDTO1);
        Assertions.assertEquals(items.get(0).getNextBooking(), simplifiedBookingDTO2);
    }

    @Test
    void shouldReturnItemsByUserIdAndCheckRepositoryMethodCalls() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .item(item)
                .booker(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(8))
                .item(item)
                .booker(user)
                .build();

        item.setOwner(user);

        SimplifiedBookingDTO simplifiedBookingDTO1 = bookingMapper.toSimpleDTO(booking1);
        SimplifiedBookingDTO simplifiedBookingDTO2 = bookingMapper.toSimpleDTO(booking2);

        when(itemRepository.findAllByOwnerIdOrderByIdAsc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(item));
        when(bookingRepository.findLastBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking1));
        when(bookingRepository.findNextBooking(anyLong(), any()))
                .thenReturn(Optional.of(booking2));
        when(bookingMapper.toSimpleDTO(booking1))
                .thenReturn(simplifiedBookingDTO1);
        when(bookingMapper.toSimpleDTO(booking2))
                .thenReturn(simplifiedBookingDTO2);

        service.getItemsByUserId(user.getId(),
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(itemRepository, times(1))
                .findAllByOwnerIdOrderByIdAsc(anyLong(), any(PageRequest.class));
        verify(bookingRepository, times(1))
                .findLastBooking(anyLong(), any());
        verify(bookingRepository, times(1))
                .findNextBooking(anyLong(), any());
    }

    @Test
    void shouldReturnItemsByUserIdWithoutBookings() {
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(item));

        List<ItemDTO> items = service.getItemsByUserId(user.getId(),
                new MyPageRequest(0, 10, Sort.unsorted()));

        Assertions.assertEquals(items.get(0).getId(), item.getId());
        Assertions.assertEquals(items.get(0).getName(), item.getName());
        assertNull(items.get(0).getLastBooking());
        assertNull(items.get(0).getNextBooking());
    }

    @Test
    void shouldReturnItemsByText() {
        when(itemRepository.search(anyString(), any(PageRequest.class)))
                .thenReturn(List.of(item));

        List<ItemDTO> items = service.getItemsByText("Test Item",
                new MyPageRequest(0, 10, Sort.unsorted()));

        assertEquals(items.size(), 1);
        Assertions.assertEquals(items.get(0).getId(), item.getId());
        Assertions.assertEquals(items.get(0).getName(), item.getName());
    }

    @Test
    void shouldReturnItemsByTextAndCheckRepositoryMethodCalls() {
        when(itemRepository.search(anyString(), any(PageRequest.class)))
                .thenReturn(List.of(item));

        service.getItemsByText("Test Item",
                new MyPageRequest(0, 10, Sort.unsorted()));

        verify(itemRepository, times(1))
                .search(anyString(), any(PageRequest.class));
    }

    @Test
    void shouldReturnEmptyItemsByText() {
        List<ItemDTO> items = service.getItemsByText(StringUtils.EMPTY,
                new MyPageRequest(0, 10, Sort.unsorted()));

        assertEquals(items.size(), 0);
    }

    @Test
    void shouldUpdateItemAvailableStatus() {
        item.setOwner(user);

        ItemDTO itemForUpdate = ItemDTO.builder()
                .available(false)
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        ItemDTO updatedItem = service.updateItem(user.getId(), item.getId(), itemForUpdate);

        Assertions.assertEquals(item.getId(), updatedItem.getId());
        Assertions.assertFalse(updatedItem.getAvailable());
    }

    @Test
    void shouldUpdateItemAndCheckRepositoryMethodCalls() {
        item.setOwner(user);

        ItemDTO itemForUpdate = ItemDTO.builder()
                .available(false)
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        service.updateItem(user.getId(), item.getId(), itemForUpdate);

        verify(userRepository, times(1))
                .findById(user.getId());
        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(itemRepository, times(1))
                .save(any((Item.class)));
    }

    @Test
    void shouldUpdateItemDescription() {
        item.setOwner(user);

        ItemDTO itemForUpdate = ItemDTO.builder()
                .description("New Description")
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        ItemDTO updatedItem = service.updateItem(user.getId(), item.getId(), itemForUpdate);

        Assertions.assertEquals(item.getId(), updatedItem.getId());
        Assertions.assertEquals("New Description", updatedItem.getDescription());
    }

    @Test
    void shouldUpdateItemName() {
        item.setOwner(user);

        ItemDTO itemForUpdate = ItemDTO.builder()
                .name("New Name")
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        ItemDTO updatedItem = service.updateItem(user.getId(), item.getId(), itemForUpdate);

        Assertions.assertEquals(item.getId(), updatedItem.getId());
        Assertions.assertEquals("New Name", updatedItem.getName());
    }

    @Test
    void shouldUpdateItemWithIncorrectUserId() {
        User anotherUser = User.builder()
                .id(FAKE_ID)
                .build();

        ItemDTO itemForUpdate = ItemDTO.builder()
                .name("New Name")
                .build();

        item.setOwner(user);

        when(userRepository.findById(anotherUser.getId()))
                .thenReturn(Optional.of(anotherUser));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.updateItem(FAKE_ID, item.getId(), itemForUpdate));

        Assertions.assertEquals("Item does not belong to the user", exception.getMessage());
    }

    @Test
    void shouldRemoveItemByIdWithIncorrectUserId() {
        when(userRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.removeItemById(FAKE_ID, item.getId()));

        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldRemoveItemByIdAndCheckRepositoryMethodCalls() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        service.removeItemById(user.getId(), item.getId());

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).deleteById(item.getId());
    }
}