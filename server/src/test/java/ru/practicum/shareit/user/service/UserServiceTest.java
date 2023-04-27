package ru.practicum.shareit.user.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
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
        when(repository.save(any(User.class)))
                .thenReturn(user);

        UserDTO userDTO = service.addUser(mapper.toDTO(user));

        Assertions.assertEquals(1, userDTO.getId());
        Assertions.assertEquals("Test User", userDTO.getName());
        Assertions.assertEquals("test@yandex.ru", userDTO.getEmail());
    }

    @Test
    void shouldCreateUserAndCheckRepositoryMethodCalls() {
        when(repository.save(any(User.class)))
                .thenReturn(user);

        service.addUser(mapper.toDTO(user));

        verify(repository, times(1))
                .save(any(User.class));
    }

    @Test
    void shouldReturnUserById() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDTO userDTO = service.getUserById(1L);

        Assertions.assertEquals(1, userDTO.getId());
        Assertions.assertEquals("Test User", userDTO.getName());
        Assertions.assertEquals("test@yandex.ru", userDTO.getEmail());
    }

    @Test
    void shouldReturnUserByIdAndCheckRepositoryMethodCalls() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        service.getUserById(1L);

        verify(repository, times(1))
                .findById(1L);
    }

    @Test
    void shouldReturnThrowUserByFakeId() {
        when(repository.findById(FAKE_ID)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getUserById(FAKE_ID));

        Assertions.assertEquals("User not found", exception.getMessage());
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
        Assertions.assertEquals(1, userDTOs.get(0).getId());
        Assertions.assertEquals("Test User", userDTOs.get(0).getName());
        Assertions.assertEquals("test@yandex.ru", userDTOs.get(0).getEmail());
    }

    @Test
    void shouldReturnAllUsersAndCheckRepositoryMethodCalls() {
        when(repository.findAll())
                .thenReturn(List.of(user));

        service.getAllUsers();

        verify(repository, times(1))
                .findAll();
    }

    @Test
    void shouldUpdateUser() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(repository.save(any(User.class)))
                .thenReturn(user);

        UserDTO userDTO = mapper.toDTO(user);
        UserDTO updatedUser = service.updateUser(userDTO.getId(), userDTO);

        Assertions.assertEquals(updatedUser.getId(), userDTO.getId());
        Assertions.assertEquals(updatedUser.getName(), userDTO.getName());
        Assertions.assertEquals(updatedUser.getEmail(), userDTO.getEmail());
    }

    @Test
    void shouldUpdateUserAndCheckRepositoryMethodCalls() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(repository.save(any(User.class)))
                .thenReturn(user);

        UserDTO userDTO = mapper.toDTO(user);
        service.updateUser(userDTO.getId(), userDTO);

        verify(repository, times(1))
                .findById(anyLong());
        verify(repository, times(1))
                .save(any(User.class));
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

        Assertions.assertEquals("Name and Email cannot be empty", exception.getMessage());
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

        Assertions.assertEquals("Email cannot be empty", exception.getMessage());
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

        Assertions.assertEquals("Name cannot be empty", exception.getMessage());
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

        Assertions.assertEquals(user.getId(), updatedUser.getId());
        Assertions.assertEquals(user.getEmail(), updatedUser.getEmail());
        Assertions.assertEquals("New name", updatedUser.getName());
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

        Assertions.assertEquals(user.getId(), updatedUser.getId());
        Assertions.assertEquals(user.getName(), updatedUser.getName());
        Assertions.assertEquals("newemail@gmail.com", updatedUser.getEmail());
    }

    @Test
    void shouldReturnThrowIfUserUpdate() {
        when(repository.findById(FAKE_ID))
                .thenReturn(Optional.empty());

        UserDTO userDTO = mapper.toDTO(user);

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.updateUser(FAKE_ID, userDTO));

        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldRemoveUserByIdAndCheckRepositoryMethodCalls() {
        when(repository.findById(user.getId()))
                .thenReturn(Optional.ofNullable(user));

        service.removeUserById(user.getId());

        verify(repository, times(1))
                .findById(user.getId());
        verify(repository, times(1))
                .deleteById(user.getId());
    }

    @Test
    void shouldReturnThrowIfUserRemove() {
        when(repository.findById(FAKE_ID))
                .thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.removeUserById(FAKE_ID));

        Assertions.assertEquals("User not found", exception.getMessage());
    }
}