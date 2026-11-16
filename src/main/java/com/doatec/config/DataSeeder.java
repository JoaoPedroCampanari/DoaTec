package com.doatec.config;

import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.repository.PessoaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String SUPER_ADMIN_CPF = "000.000.000-00";

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String superAdminEmail;
    private final String superAdminSenha;

    public DataSeeder(PessoaRepository pessoaRepository, PasswordEncoder passwordEncoder,
                      @Value("${SUPER_ADMIN_EMAIL:}") String superAdminEmail,
                      @Value("${SUPER_ADMIN_SENHA:}") String superAdminSenha) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.superAdminEmail = superAdminEmail;
        this.superAdminSenha = superAdminSenha;
    }

    @Override
    public void run(String... args) {
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        if (pessoaRepository.existsByRole(Role.SUPER_ADMIN)) {
            return;
        }
        if (superAdminEmail.isBlank() || superAdminSenha.isBlank()) {
            log.warn("Superadministrador não criado: configure SUPER_ADMIN_EMAIL e SUPER_ADMIN_SENHA.");
            return;
        }

        DoadorPF superAdmin = DoadorPF.builder()
                .nome("Super Admin")
                .email(superAdminEmail.trim())
                .senha(passwordEncoder.encode(superAdminSenha))
                .cpf(SUPER_ADMIN_CPF)
                .role(Role.SUPER_ADMIN)
                .ativo(true)
                .build();
        pessoaRepository.save(superAdmin);
        log.info("Superadministrador criado automaticamente.");
    }
}
