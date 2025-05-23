
package com.example.authsystem.service;
import com.example.authsystem.model.User;
import com.example.authsystem.model.VerificationToken;
import com.example.authsystem.repository.UserRepository;
import com.example.authsystem.repository.VerificationTokenRepository;
import com.example.authsystem.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService implements UserDetailsService 
{

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailUtil emailUtil;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException 
    {
        logger.info("UserService - loadUserByUsername - Attempting to load user by email: [{}]", email);
        logger.info("UserService - loadUserByUsername - Email parameter: [{}]", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        logger.info("UserService - loadUserByUsername - findByEmail result: {}", userOptional);

        if (!userOptional.isPresent())
         {
            logger.info("UserService - loadUserByUsername - User not found with email: [{}]", email);
           
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();
        logger.info("UserService - loadUserByUsername - Found user: {}, Enabled: {}", user.getEmail(), user.isEnabled());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(), 
                true,
                true, 
                true, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional
    public String registerUser(String name, String email) 
    {
        logger.info("UserService.registerUser() - Attempting to register user with Email: {}", email);
        if (userRepository.findByEmail(email).isPresent()) 
        {
            logger.warn("UserService.registerUser() - User with this email already exists: {}", email);
            return "User with this email already exists";
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(null); 
        user.setEnabled(false);
        User savedUser = userRepository.save(user); 
        logger.info("UserService.registerUser() - User saved with ID: {}", savedUser.getId());

        String token = UUID.randomUUID().toString();
        createVerificationToken(savedUser, token); 
        logger.info("UserService.registerUser() - Verification token created for user ID: {}", savedUser.getId());

        String verificationLink = "http://localhost:8080/set-password?token=" + token;
        try 
        {
            logger.info("UserService.registerUser() - Sending verification email to: {}, link: {}", email, verificationLink);
            emailUtil.sendVerificationEmail(email, "Set Your Password", verificationLink);
            logger.info("UserService.registerUser() - Verification email sent successfully to {}", email);
        } catch (Exception e) 
        {
            logger.error("UserService.registerUser() - Error sending verification email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email. Registration failed.", e);
        }
        logger.info("UserService.registerUser() - Registration successful for email: {}", email);
        return "Registration successful. Please check your email to set your password.";
    }

    public void createVerificationToken(User user, String token)
     {
        logger.info("UserService.createVerificationToken() - User ID: {}, Token: {}", user.getId(), token);
        VerificationToken vToken = new VerificationToken();
        vToken.setToken(token);
        vToken.setUser(user);
        vToken.setExpiryDate(LocalDateTime.now().plusMinutes(30)); 
        tokenRepository.save(vToken);
        logger.info("UserService.createVerificationToken() - Verification token saved with ID: {}", vToken.getId());
    }

    @Transactional
    public String savePassword(String token, String password)
     {
        logger.info("UserService.savePassword() - Attempting to save password for token: {}", token);
        VerificationToken verificationToken = tokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null) 
        {
            logger.warn("UserService.savePassword() - Verification token is NULL for token: {}", token);
            return "Invalid or expired token";
        }
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) 
        {
            logger.warn("UserService.savePassword() - Token expired for token: {}, Expiry Date: {}", token, verificationToken.getExpiryDate());
            return "Invalid or expired token";
        }
        logger.info("UserService.savePassword() - Found verification token for user ID: {}", verificationToken.getUser().getId());

        User userFromToken = verificationToken.getUser();
        logger.info("UserService.savePassword() - User ID from token: {}", userFromToken.getId());
        User existingUser = userRepository.findById(userFromToken.getId()).orElse(null);

        if (existingUser != null) 
        {
            logger.info("UserService.savePassword() - Found existing user: {}", existingUser.getEmail());
            existingUser.setPassword(passwordEncoder.encode(password));
            existingUser.setEnabled(true);
            logger.info("UserService.savePassword() - Setting password and enabling user: {}", existingUser.getEmail());
            userRepository.save(existingUser);
            logger.info("UserService.savePassword() - User password saved successfully for: {}", existingUser.getEmail());
            tokenRepository.delete(verificationToken);
            logger.info("UserService.savePassword() - Token deleted for user: {}", existingUser.getEmail());
            return "Password set successfully";
        } else 
        {
            logger.error("UserService.savePassword() - User not found for token: {}", token);
            return "Error: User not found for the provided token.";
        }
    }

    public User findByEmail(String email) 
    {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public String createPasswordResetTokenForUser(String email)
     {
        logger.info("UserService.createPasswordResetTokenForUser() - Attempting to create password reset token for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) 
        {
            logger.warn("UserService.createPasswordResetTokenForUser() - User not found for email: {}", email);
            return "If an account with that email exists, a password reset link has been sent.";
        }
        User user = userOptional.get();
        String token = UUID.randomUUID().toString();

        tokenRepository.deleteByUser(user); 
        logger.info("UserService.createPasswordResetTokenForUser() - Deleted existing tokens for user ID: {}", user.getId());

        createVerificationToken(user, token); 
        logger.info("UserService.createPasswordResetTokenForUser() - Password reset token created for user ID: {}", user.getId());

        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        try 
        {
            logger.info("UserService.createPasswordResetTokenForUser() - Sending password reset email to: {}, link: {}", email, resetLink);
            emailUtil.sendVerificationEmail(email, "Password Reset Request", resetLink);
            logger.info("UserService.createPasswordResetTokenForUser() - Password reset email sent successfully to {}", email);
        } catch (Exception e) 
        {
            logger.error("UserService.createPasswordResetTokenForUser() - Error sending password reset email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email.", e);
        }
        return "If an account with that email exists, a password reset link has been sent.";
    }

    @Transactional
    public String resetPassword(String token, String newPassword, String confirmNewPassword)
     {
        logger.info("UserService.resetPassword() - Attempting to reset password for token: {}", token);
        if (newPassword == null || confirmNewPassword == null || !newPassword.equals(confirmNewPassword))
         {
            logger.warn("UserService.resetPassword() - Passwords do not match or are null for token: {}", token);
            return "Passwords do not match.";
        }

        VerificationToken verificationToken = tokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) 
        {
            logger.warn("UserService.resetPassword() - Invalid or expired password reset token: {}", token);
            return "Invalid or expired password reset token.";
        }

        User user = verificationToken.getUser();
        if (user == null) 
        {
            logger.error("UserService.resetPassword() - User associated with token {} not found.", token);
            return "Error: User associated with token not found.";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setEnabled(true);
        userRepository.save(user);
        logger.info("UserService.resetPassword() - Password successfully reset for user: {}", user.getEmail());

        tokenRepository.delete(verificationToken);
        logger.info("UserService.resetPassword() - Password reset token deleted for user: {}", user.getEmail());

        return "Your password has been successfully reset.";
    }
}