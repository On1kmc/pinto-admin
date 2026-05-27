package com.ivanov.pinto_admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final AdminUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model, Authentication auth) {
        model.addAttribute("users", repo.findAll());
        model.addAttribute("currentUsername", auth.getName());
        return "accounts";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam AdminRole role) {
        if (username.isBlank() || password.isBlank()) return "redirect:/accounts";
        if (repo.findByUsername(username.trim()).isPresent()) return "redirect:/accounts?error=exists";
        repo.save(new AdminUser(username.trim(), passwordEncoder.encode(password), role));
        return "redirect:/accounts";
    }

    @PostMapping("/changerole")
    public String changeRole(
            @RequestParam Long id,
            @RequestParam AdminRole role,
            Authentication auth) {
        repo.findById(id).ifPresent(user -> {
            if (!user.getUsername().equals(auth.getName())) {
                user.setRole(role);
                repo.save(user);
            }
        });
        return "redirect:/accounts";
    }

    @PostMapping("/changepassword")
    public String changePassword(
            @RequestParam Long id,
            @RequestParam String password) {
        if (password.isBlank()) return "redirect:/accounts";
        repo.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(password));
            repo.save(user);
        });
        return "redirect:/accounts";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, Authentication auth) {
        repo.findById(id).ifPresent(user -> {
            // нельзя удалить себя
            if (!user.getUsername().equals(auth.getName())) {
                repo.delete(user);
            }
        });
        return "redirect:/accounts";
    }
}
