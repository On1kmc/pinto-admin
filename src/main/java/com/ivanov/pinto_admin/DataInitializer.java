package com.ivanov.pinto_admin;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Создаёт начальные учётные записи при первом запуске (если таблица пуста).
 * При последующих запусках не трогает данные.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        repo.save(new AdminUser("leo",    passwordEncoder.encode("PntLi$$03"),    AdminRole.MAIN));
        repo.save(new AdminUser("admin2", passwordEncoder.encode("ndI23L$sl69h"), AdminRole.REGULAR));
        repo.save(new AdminUser("On1k",   passwordEncoder.encode("admin"),         AdminRole.REGULAR));
    }
}
