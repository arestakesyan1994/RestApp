package com.example.restapp.repository;

import com.example.restapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findOneByEmail(String email);

}
