package com.materialmaker.controller;

import com.materialmaker.dto.ApiResponse;
import com.materialmaker.mapper.UserMapper;
import com.materialmaker.model.User;
import com.materialmaker.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserContext userContext;
    private final UserMapper userMapper;

    public UserController(UserContext userContext, UserMapper userMapper) {
        this.userContext = userContext;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ApiResponse<User> me() {
        String userId = userContext.getUserId();
        User user = userMapper.findById(userId);
        if (user == null) {
            user = new User();
            user.setId(userId);
            user.setDisplayName("demo-" + userId.substring(0, Math.min(8, userId.length())));
            userMapper.insert(user);
        }
        return ApiResponse.ok(user);
    }

    @PostMapping("/demo")
    public ApiResponse<User> demo() {
        String userId = "demo-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setId(userId);
        user.setDisplayName("演示用户");
        try {
            userMapper.insert(user);
        } catch (Exception ignored) {}
        return ApiResponse.ok(user);
    }
}
