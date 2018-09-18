package com.example.restapp.rest;

import com.example.restapp.dto.AuthenticationRequest;
import com.example.restapp.dto.AuthenticationResponse;
import com.example.restapp.dto.UserDto;
import com.example.restapp.model.User;
import com.example.restapp.repository.UserRepository;
import com.example.restapp.security.JwtTokenUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class  UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/auth")
    public ResponseEntity auth(@RequestBody AuthenticationRequest authenticationRequest) {
        User user = userRepository.findOneByEmail(authenticationRequest.getEmail());
        if (user != null && passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())) {
            String token = jwtTokenUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(token)
                    .userDto(UserDto.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .surname(user.getSurname())
                            .email(user.getEmail())
                            .userType(user.getType())
                            .build())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }


    @ApiOperation(value = "all users", notes = "get all users")
    @GetMapping
    public List<User> users() {
        return userRepository.findAll();
    }

    @GetMapping("/names")
    public List<UserDto> userNames() {
        List<User> all = userRepository.findAll();
        List<UserDto> userDtos = new LinkedList<>();
        all.forEach(e -> userDtos.add(new UserDto(e.getId(), e.getName(), e.getSurname(), e.getEmail(), e.getType())));
        return userDtos;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return userRepository.findOne(id);
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody User user) {
        if (userRepository.findOneByEmail(user.getEmail()) == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return ResponseEntity.ok("created");
        }
        return ResponseEntity.badRequest().body("User with " + user.getEmail() + " already exist");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable int id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("user with " + userDetails.getUsername() + " trying to delete user by " + id);
            userRepository.delete(id);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("user with " + id + " does not exist");
        }
    }

    @PutMapping
    public ResponseEntity updateUser(@RequestBody User user) {
        if (userRepository.exists(user.getId())) {
            userRepository.save(user);
            return ResponseEntity.ok("updated");
        }
        return ResponseEntity.badRequest().body("User with " + user.getId() + " does not exist");
    }

}
