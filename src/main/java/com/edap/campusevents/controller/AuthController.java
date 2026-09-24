package com.edap.campusevents.controller;

import com.edap.campusevents.dto.SignupForm;
import com.edap.campusevents.model.AppUser;
import com.edap.campusevents.model.Role;
import com.edap.campusevents.repository.UserRepository;
import com.edap.campusevents.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    // Auth is stateless JWT, not a server session, so this login endpoint isn't
    // handled by Spring Security's formLogin filter - it authenticates the
    // credentials itself, then hands the browser a token in an HttpOnly cookie.
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            response.addHeader(HttpHeaders.SET_COOKIE, jwtService.buildCookie(token).toString());
            return "redirect:/events";
        } catch (BadCredentialsException e) {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupForm form, BindingResult result) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            result.rejectValue("username", "duplicate", "That username is already taken");
        }
        if (result.hasErrors()) {
            return "auth/signup";
        }

        // Public signup always creates a regular USER account - there is no
        // self-service way to become an ADMIN. Admin access is seeded separately.
        AppUser user = new AppUser();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return "redirect:/login?registered";
    }
}
