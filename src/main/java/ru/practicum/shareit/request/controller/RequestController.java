package ru.practicum.shareit.request.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
public class RequestController {

    private static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public RequestDTO createRequest(@RequestHeader(SHARER_USER_ID) long userId,
                                    @Valid @RequestBody RequestDTO itemRequestDTO) {

        return requestService.addRequest(userId, itemRequestDTO);
    }

    @GetMapping
    public List<RequestDTO> getRequestList(@RequestHeader(SHARER_USER_ID) long userId) {
        return requestService.getRequestListByOwnerId(userId);
    }


    @GetMapping("/all")
    public List<RequestDTO> getAllRequestList(@RequestHeader(SHARER_USER_ID) long userId,
                                              @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
                                              @Positive @RequestParam(defaultValue = "10", required = false) Integer size) {

        return requestService.getAllRequestList(userId, new MyPageRequest(from, size, Sort.unsorted()));
    }

    @GetMapping("/{requestId}")
    public RequestDTO getRequest(@RequestHeader(SHARER_USER_ID) long userId,
                                 @PathVariable long requestId) {

        return requestService.getRequestById(userId, requestId);
    }
}
