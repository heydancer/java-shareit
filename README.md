# Shareit

---

## Описание

Shareit - Микросервисное приложение, позволяющее пользователям размещать объявления для обмена товарами

## Функционал

### Два сервиса:
- основной сервис shareit-server содержить всю основную логику для работы продукта
- сервис шлюз shareit-gateway содержит контроллеры, с которыми непосредственно работают пользователи, — вместе с
  валидацией входных данных

Каждое из приложений запускается как самостоятельное Java-приложение, а их общение происходит через REST

- создание, редактирование, получение и удаление пользователей
- добавление новых вещей пользователем, редактирование и удаление
- поиск вещей в названиии/описании c использованием пагинации
- оставление комментариев
- отправление запросов на вещи
- создание бронирования
- управление статусом бронирования
- получение списка бронирований с возможностью пагинации
- получение списка бронирований владельца вещи

### Эндпоинты

#### User

| Method | Endpoint                           | Description     |
|--------|------------------------------------|-----------------|
| POST   | /users Body: {userDTO...}          | Create new user |
| GET    | /users/{userId}                    | Get user by id  |
| GET    | /users                             | Get users       |
| PATCH  | /users/{userId} Body: {userDTO...} | Update user     |
| DELETE | /users/{userId}                    | Remove user     |

#### Item

| Method | Endpoint                                      | Request Header              | Description          |
|--------|-----------------------------------------------|-----------------------------|----------------------|
| POST   | /items Body: {itemDTO...}                     | X-Sharer-User-Id = {userId} | Create new item      |
| GET    | /items/{itemId}                               | X-Sharer-User-Id = {userId} | Get item by id       |
| GET    | /items?from=...&size=...                      | X-Sharer-User-Id = {userId} | Get items by user id |
| GET    | /items/text?from...&size...                   |                             | Search item by text  |
| PATCH  | /items/{itemId} Body: {itemDTO...}            | X-Sharer-User-Id = {userId} | Updated Item         |
| DELETE | /items/{itemId}                               | X-Sharer-User-Id = {userId} | Remove Item          |
| POST   | /items/{itemId}/comment Body: {commentDTO...} | X-Sharer-User-Id = {userId} | Create Comment       |

#### Request

| Method | Endpoint                        | Request Header              | Description                         |
|--------|---------------------------------|-----------------------------|-------------------------------------|
| POST   | /requests Body: {requestDTO...} | X-Sharer-User-Id = {userId} | Create new request                  |
| GET    | /requests/{requestId}           | X-Sharer-User-Id = {userId} | Get request by id                   |
| GET    | /requests/all?from=...&size=... | X-Sharer-User-Id = {userId} | Get requests by user id with filter |
| GET    | /requests                       | X-Sharer-User-Id = {userId} | Get requests by user id             |

#### Booking (state = ALL, WAITING, APPROVED, REJECTED, CANCELED)

| Method | Endpoint                                    | Request Header              | Description                      |
|--------|---------------------------------------------|-----------------------------|----------------------------------|
| POST   | /bookings/{bookingId} Body: {requestDTO...} | X-Sharer-User-Id = {userId} | Create new booking               |
| PATCH  | /bookings/{bookingId}?approved=true/false   | X-Sharer-User-Id = {userId} | Updated status                   |
| GET    | /bookings/{bookingId}                       | X-Sharer-User-Id = {userId} | Get booking by id                |
| GET    | /bookings?state=...&from=...&size=...       | X-Sharer-User-Id = {userId} | Get bookings with filter         |
| GET    | /bookings/owner?state=...&from=...&size=... | X-Sharer-User-Id = {userId} | Get bookings by owner and filter |

### Стек
- Java 11
- Spring Boot
- Hibernate
- Maven
- Lombok
- Docker
- PostgreSQL
- StringUtils
- JUnit 5
- Mockito
- Insomnia
- Postman


