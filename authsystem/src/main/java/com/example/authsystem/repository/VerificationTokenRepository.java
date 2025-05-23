package com.example.authsystem.repository;
import com.example.authsystem.model.User; 
import com.example.authsystem.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> 
{
    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(User user);
}