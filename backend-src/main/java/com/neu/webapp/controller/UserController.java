package com.neu.webapp.controller;

import com.neu.webapp.common.Result;
import com.neu.webapp.entity.User;
import com.neu.webapp.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.ok(userService.list());
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }
}
