package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.RequestDTO;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {
    private static final Logger log = LoggerFactory.getLogger(RequestController.class);
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(SHARER_USER_ID) long userId,
                                                @Valid @RequestBody RequestDTO requestDTO) {
        log.info("Create request {}, userId={}", requestDTO, userId);
        return requestClient.create(userId, requestDTO);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestList(@RequestHeader(SHARER_USER_ID) long userId) {
        log.info("Get request list userId={}", userId);
        return requestClient.getRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestList(@RequestHeader(SHARER_USER_ID) long userId,
                                                    @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                                    @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Get all request userId={}", userId);
        return requestClient.getRequestsByUserIdWithPagination(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(SHARER_USER_ID) long userId,
                                             @PathVariable long requestId) {
        log.info("Get request by id requestId={}, userId={}", requestId, userId);
        return requestClient.getRequestById(userId, requestId);
    }
}
