package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDTO;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    public static final long FAKE_ID = 99999L;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private RequestService service;
    @Spy
    private RequestMapper mapper;
    @Spy
    private ItemMapper itemMapper;
    private User user;
    private Item item;
    private Request request;

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
    }

    @Test
    void shouldAddRequest() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.save(any(Request.class)))
                .thenReturn(request);

        RequestDTO requestDTO = service.addRequest(user.getId(), mapper.toDTO(request));

        assertEquals(requestDTO.getId(), request.getId());
    }

    @Test
    void shouldAddRequestWithIncorrectUserId() {
        when(userRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.addRequest(FAKE_ID, mapper.toDTO(request)));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnRequestListByOwnerId() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByOwnerIdOrderByCreatedDesc(user.getId()))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequestOwnerId(user.getId()))
                .thenReturn(List.of(item));

        List<RequestDTO> requestDTOS = service.getRequestListByOwnerId(user.getId());

        assertEquals(requestDTOS.get(0).getId(), request.getId());
    }

    @Test
    void shouldReturnRequestListByIncorrectUserId() {
        when(userRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getRequestListByOwnerId(FAKE_ID));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnAllRequestList() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByPageable(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequestIds(List.of(request.getId())))
                .thenReturn(List.of(item));

        List<RequestDTO> requestDTOS = service.getAllRequestList(user.getId(),
                new MyPageRequest(0, 10, Sort.unsorted()));

        assertEquals(requestDTOS.get(0).getId(), request.getId());
    }

    @Test
    void shouldReturnAllRequestListByIncorrectUserId() {
        when(userRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getAllRequestList(FAKE_ID,
                        new MyPageRequest(0, 10, Sort.unsorted())));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnRequestById() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestId(request.getId()))
                .thenReturn(List.of(item));

        RequestDTO requestDTO = service.getRequestById(user.getId(), request.getId());

        assertEquals(requestDTO.getId(), request.getId());
    }

    @Test
    void shouldReturnRequestByIncorrectUserId() {
        when(userRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("User not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getRequestById(FAKE_ID, request.getId()));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnRequestByIncorrectRequestId() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.findById(FAKE_ID))
                .thenThrow(new NotFoundException("Request not found"));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getRequestById(user.getId(), FAKE_ID));

        assertEquals("Request not found", exception.getMessage());
    }
}