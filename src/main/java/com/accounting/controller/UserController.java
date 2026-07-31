package com.accounting.controller;

import com.accounting.dto.UserDto;
import com.accounting.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserProfile(
        @RequestAttribute("userId") Long userId
    ) {
        log.info("Get profile request for user={}", userId);
        UserDto user = userService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }
}
