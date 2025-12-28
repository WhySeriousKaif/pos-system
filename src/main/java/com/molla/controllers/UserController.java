package com.molla.controllers;

import com.molla.mapper.UserMapper;
import com.molla.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.molla.service.UserService;
import com.molla.payload.dto.UserDto;
import com.molla.exceptions.UserException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.getUserFromJwt(jwt);
        return  ResponseEntity.ok(UserMapper.toDto(user));

    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id ) throws UserException {
        User user = userService.getUserById(id);
        if(user==null){
            throw new UserException("User not found");
        }
        return  ResponseEntity.ok(UserMapper.toDto(user));

    }
}