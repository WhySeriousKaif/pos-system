package com.molla.service.impl;

import com.molla.configuration.JwtProvider;
import com.molla.exceptions.UserException;
import com.molla.model.User;
import com.molla.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.molla.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService{

    private final UserRepository userRepository ;

    private final JwtProvider jwtProvider ;

//    public UserServiceImp(UserRepository userRepository, JwtProvider jwtProvider) {
//        this.userRepository = userRepository;
//        this.jwtProvider = jwtProvider;
//    }

    @Override
    public User getUserFromJwt(String jwt) throws UserException {
        String email=jwtProvider.getEmailFromToken(jwt);
        User user=userRepository.findByEmail(email);
        if(user==null){
            throw new UserException("User not found");
        }
        return user;
    }

    @Override
    public User getCurrentUser() throws UserException {

        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByEmail(email);
        if(user==null){
            throw new UserException("User not found");

        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) throws UserException {
        User user=userRepository.findByEmail(email);
        if(user==null){
            throw new UserException("User not found");

        }
        return user;
    }

    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
