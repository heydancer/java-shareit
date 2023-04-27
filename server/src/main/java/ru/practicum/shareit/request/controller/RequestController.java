package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.MyPageRequest;
import ru.practicum.shareit.request.dto.RequestDTO;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private final RequestService requestService;

    @PostMapping
    public RequestDTO createRequest(@RequestHeader(SHARER_USER_ID) long userId,
                                    @RequestBody RequestDTO requestDTO) {
        return requestService.addRequest(userId, requestDTO);
    }

    @GetMapping
    public List<RequestDTO> getRequestList(@RequestHeader(SHARER_USER_ID) long userId) {
        return requestService.getRequestListByOwnerId(userId);
    }

    @GetMapping("/all")
    public List<RequestDTO> getAllRequestList(@RequestHeader(SHARER_USER_ID) long userId,
                                              @RequestParam(defaultValue = "0", required = false) Integer from,
                                              @RequestParam(defaultValue = "10", required = false) Integer size) {
        return requestService.getAllRequestList(userId, new MyPageRequest(from, size, Sort.unsorted()));
    }

    @GetMapping("/{requestId}")
    public RequestDTO getRequest(@RequestHeader(SHARER_USER_ID) long userId,
                                 @PathVariable long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}
