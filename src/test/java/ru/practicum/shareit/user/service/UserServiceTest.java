package ru.practicum.shareit.user.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    public static final long FAKE_ID = 99999L;
    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserService service;

    @Spy
    private UserMapper mapper;

    private User user;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@yandex.ru")
                .build();
    }

    @Test
    void shouldCreateUser() {
        when(repository.save(any(User.class))).thenReturn(user);

        UserDTO userDTO = service.addUser(mapper.toDTO(user));

        assertEquals(1, userDTO.getId());
        assertEquals("Test User", userDTO.getName());
        assertEquals("test@yandex.ru", userDTO.getEmail());
    }

    @Test
    void shouldReturnUserById() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO userDTO = service.getUserById(1L);

        assertEquals(1, userDTO.getId());
        assertEquals("Test User", userDTO.getName());
        assertEquals("test@yandex.ru", userDTO.getEmail());
    }

    @Test
    void shouldReturnThrowUserByFakeId() {
        when(repository.findById(FAKE_ID)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getUserById(FAKE_ID));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyUsers() {
        when(repository.findAll()).thenReturn(List.of());

        List<UserDTO> userDTOs = service.getAllUsers();

        assertEquals(0, userDTOs.size());
    }

    @Test
    void shouldReturnAllUsers() {
        when(repository.findAll())
                .thenReturn(List.of(user));

        List<UserDTO> userDTOs = service.getAllUsers();

        assertEquals(1, userDTOs.size());
        assertEquals(1, userDTOs.get(0).getId());
        assertEquals("Test User", userDTOs.get(0).getName());
        assertEquals("test@yandex.ru", userDTOs.get(0).getEmail());
    }

    @Test
    void shouldUpdateUser() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(repository.save(any(User.class)))
                .thenReturn(user);

        UserDTO userDTO = mapper.toDTO(user);
        UserDTO updatedUser = service.updateUser(userDTO.getId(), userDTO);

        assertEquals(updatedUser.getId(), userDTO.getId());
        assertEquals(updatedUser.getName(), userDTO.getName());
        assertEquals(updatedUser.getEmail(), userDTO.getEmail());
    }

    @Test
    void shouldUpdateUserWithEmptyNameAndEmail() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        UserDTO userDTO = mapper.toDTO(user);
        userDTO.setEmail(StringUtils.EMPTY);
        userDTO.setName(StringUtils.EMPTY);

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateUser(userDTO.getId(), userDTO));

        assertEquals("Name and Email cannot be empty", exception.getMessage());
    }

    @Test
    void shouldUpdateUserWithEmptyEmail() {
        UserDTO userForUpdate = UserDTO.builder()
                .email(StringUtils.EMPTY)
                .build();

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateUser(user.getId(), userForUpdate));

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void shouldUpdateUserWithEmptyName() {
        UserDTO userForUpdate = UserDTO.builder()
                .name(StringUtils.EMPTY)
                .build();

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        final ValidationException exception = assertThrows(ValidationException.class,
                () -> service.updateUser(user.getId(), userForUpdate));

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void shouldUpdateUserName() {
        UserDTO userForUpdate = UserDTO.builder()
                .name("New name")
                .build();

        when(repository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        UserDTO updatedUser = service.updateUser(user.getId(), userForUpdate);

        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        assertEquals("New name", updatedUser.getName());
    }

    @Test
    void shouldUpdateUserEmail() {
        UserDTO userForUpdate = UserDTO.builder()
                .email("newemail@gmail.com")
                .build();

        when(repository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        UserDTO updatedUser = service.updateUser(user.getId(), userForUpdate);

        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(user.getName(), updatedUser.getName());
        assertEquals("newemail@gmail.com", updatedUser.getEmail());
    }

    @Test
    void shouldReturnThrowIfUserUpdate() {
        when(repository.findById(FAKE_ID))
                .thenReturn(Optional.empty());

        UserDTO userDTO = mapper.toDTO(user);

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.updateUser(FAKE_ID, userDTO));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldRemoveUserById() {
        when(repository.findById(user.getId()))
                .thenReturn(Optional.ofNullable(user));

        service.removeUserById(user.getId());

        verify(repository, times(1)).deleteById(user.getId());
    }

    @Test
    void shouldReturnThrowIfUserRemove() {
        when(repository.findById(FAKE_ID))
                .thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.removeUserById(FAKE_ID));

        assertEquals("User not found", exception.getMessage());
    }
}