package com.molla.service;

import java.util.List;

import com.molla.exceptions.UserException;
import com.molla.model.User;

public interface UserService {

    User getUserFromJwt(String jwt) throws UserException;
    User getCurrentUser() throws UserException;
    User getUserByEmail(String email) throws UserException;
    User getUserById(Long id);
    List<User> getAllUsers();

    
}
