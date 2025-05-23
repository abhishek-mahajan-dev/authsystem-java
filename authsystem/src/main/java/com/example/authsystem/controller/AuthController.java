
package com.example.authsystem.controller;
import com.example.authsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.Data; 
import lombok.extern.slf4j.Slf4j; 

@Data 
class RegistrationRequest 
{
    private String name;
    private String email;
    
}

@Controller
@Slf4j 
public class AuthController 
{

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegister() 
    {
        log.info("AuthController.showRegister() - Displaying registration page."); 
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegistrationRequest request, Model model)
     {
        log.info("AuthController.register() - Received registration request for Name: {}, Email: {}", request.getName(), request.getEmail());
        try 
        {
            String result = userService.registerUser(request.getName(), request.getEmail());
            if (result.startsWith("Registration successful")) 
            {
                model.addAttribute("message", "Registration successful. Please check your email to set your password.");
                log.info("AuthController.register() - Registration successful for email: {}", request.getEmail());
                return "register-success";
            } 
            else
             {
                model.addAttribute("error", result);
                log.warn("AuthController.register() - Registration failed for email {}: {}", request.getEmail(), result);
                return "register";
            }
        } catch (RuntimeException e) 
        {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            log.error("AuthController.register() - RuntimeException during registration for email {}: {}", request.getEmail(), e.getMessage(), e);
            return "register";
        }
    }

    @GetMapping("/set-password")
    public String showSetPassword(@RequestParam String token, Model model) 
    {
        log.info("AuthController.showSetPassword() - Displaying set password page for token: {}", token);
        model.addAttribute("token", token);
        return "set-password";
    }

    @PostMapping("/set-password")
    public String setPassword(@RequestParam String token, @RequestParam String password, Model model)
     {
        log.info("AuthController.setPassword() - Attempting to set password for token: {}", token);
        String result = userService.savePassword(token, password);
        if (result.equals("Password set successfully")) 
        {
            model.addAttribute("message", "Password set successfully. You can now login.");
            model.addAttribute("redirect", true);
            log.info("AuthController.setPassword() - Password set successfully for token: {}", token);
            return "set-password";
        } else 
        {
            model.addAttribute("error", result);
            model.addAttribute("token", token);
            log.warn("AuthController.setPassword() - Failed to set password for token {}: {}", token, result);
            return "set-password";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "checkMail", required = false) String checkMail,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) 
                        {
        log.info("AuthController.login() - Displaying login page. checkMail: {}, logout: {}", checkMail, logout);
        if (checkMail != null) {
            model.addAttribute("message", "Check your email to set your password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out.");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard()
     {
        log.info("AuthController.dashboard() - Displaying dashboard page.");
        return "dashboard";
    }


    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() 
    {
        log.info("AuthController.showForgotPasswordPage() - Displaying forgot password page.");
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPasswordRequest(@RequestParam("email") String email, Model model) 
    {
        log.info("AuthController.processForgotPasswordRequest() - Received forgot password request for email: {}", email);
        String message = userService.createPasswordResetTokenForUser(email);
        model.addAttribute("message", message);
        log.info("AuthController.processForgotPasswordRequest() - Message for email {}: {}", email, message);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) 
    {
        log.info("AuthController.showResetPasswordPage() - Displaying reset password page for token: {}", token);
        String validationResult = userService.resetPassword(token, null, null);
        if (validationResult.equals("Invalid or expired password reset token."))
         {
            model.addAttribute("message", validationResult);
            log.warn("AuthController.showResetPasswordPage() - Invalid or expired token: {}", token);
            return "message";
        }
        model.addAttribute("token", token);
        log.info("AuthController.showResetPasswordPage() - Valid token found: {}", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmNewPassword") String confirmNewPassword,
                                       Model model,
                                       RedirectAttributes redirectAttributes) 
        {
        log.info("AuthController.processResetPassword() - Attempting to reset password for token: {}", token);
        String result = userService.resetPassword(token, newPassword, confirmNewPassword);

        if (result.equals("Your password has been successfully reset.")) 
        {
            redirectAttributes.addFlashAttribute("message", result);
            log.info("AuthController.processResetPassword() - Password successfully reset for token: {}", token);
            return "redirect:/login";
        } 
        else {
            model.addAttribute("error", result);
            model.addAttribute("token", token);
            log.warn("AuthController.processResetPassword() - Password reset failed for token {}: {}", token, result);
            return "reset-password";
        }
    }
}