package com.example.project.repositories;

import com.example.project.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
