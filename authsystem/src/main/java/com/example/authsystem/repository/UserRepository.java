package com.example.authsystem.repository;
import com.example.authsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> 
{
    Optional<User> findByEmail(String email);
}