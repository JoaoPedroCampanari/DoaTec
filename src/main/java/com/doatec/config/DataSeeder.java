package com.doatec.config;

import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.repository.AlunoRepository;
import com.doatec.repository.PessoaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String SUPER_ADMIN_EMAIL = "admin@doatec.com";
    private static final String SUPER_ADMIN_SENHA = "Admin@123";
    private static final String SUPER_ADMIN_CPF = "000.000.000-00";

    private static final String ALUNO_EMAIL = "teste@gmail.com";
    private static final String ALUNO_SENHA = "123456";
    private static final String ALUNO_CEP = "17404542";

    private final PessoaRepository pessoaRepository;
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public DataSeeder(PessoaRepository pessoaRepository, AlunoRepository alunoRepository,
                      PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate, Environment environment) {
        this.pessoaRepository = pessoaRepository;
        this.alunoRepository = alunoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        fixRoleCheckConstraint();
        seedSuperAdmin();
        seedAlunoTeste();
    }

    /**
     * Hibernate ddl-auto=update não altera check constraints existentes.
     * Este método corrige a constraint para incluir SUPER_ADMIN quando necessário.
     */
    private void fixRoleCheckConstraint() {
        try {
            // Verifica se está usando PostgreSQL
            boolean isPostgres = false;
            for (String profile : environment.getActiveProfiles()) {
                if ("docker".equals(profile)) {
                    isPostgres = true;
                    break;
                }
            }
            if (!isPostgres) return;

            // Tenta dropar e recriar a constraint
            jdbcTemplate.execute("ALTER TABLE pessoa DROP CONSTRAINT IF EXISTS pessoa_role_check");
            jdbcTemplate.execute("ALTER TABLE pessoa ADD CONSTRAINT pessoa_role_check CHECK (role IN ('USER', 'ADMIN', 'SUPER_ADMIN'))");
            log.info("Constraint pessoa_role_check atualizada para incluir SUPER_ADMIN");
        } catch (Exception e) {
            // Se a tabela não existe ainda (primeira execução), o Hibernate vai criá-la corretamente
            log.debug("Não foi possível alterar constraint pessoa_role_check (tabela pode não existir ainda): {}", e.getMessage());
        }
    }

    private void seedSuperAdmin() {
        if (!pessoaRepository.existsByRole(Role.SUPER_ADMIN)) {
            DoadorPF superAdmin = DoadorPF.builder()
                    .nome("Super Admin")
                    .email(SUPER_ADMIN_EMAIL)
                    .senha(passwordEncoder.encode(SUPER_ADMIN_SENHA))
                    .cpf(SUPER_ADMIN_CPF)
                    .role(Role.SUPER_ADMIN)
                    .ativo(true)
                    .build();

            pessoaRepository.save(superAdmin);

            log.info("═══════════════════════════════════════════════════════");
            log.info("  Super Admin criado automaticamente");
            log.info("  Email: {}", SUPER_ADMIN_EMAIL);
            log.info("  Senha: {}", SUPER_ADMIN_SENHA);
            log.info("  ⚠ Troque a senha em produção!");
            log.info("═══════════════════════════════════════════════════════");
        }
    }

    private void seedAlunoTeste() {
        if (alunoRepository.existsByEmail(ALUNO_EMAIL)) {
            log.info("Aluno de teste já existe, pulando seed...");
            return;
        }

        String ra = String.format("%d", 100000 + (int)(Math.random() * 900000));
        String telefone = String.format("(14) 9%s", 10000 + (int)(Math.random() * 90000));
        String numero = String.valueOf(10 + (int)(Math.random() * 990));

        Aluno aluno = Aluno.builder()
                .nome("Aluno Teste")
                .email(ALUNO_EMAIL)
                .senha(passwordEncoder.encode(ALUNO_SENHA))
                .ra(ra)
                .endereco(ALUNO_CEP)
                .logradouro("Rua Teste")
                .numero(numero)
                .bairro("Centro")
                .cidade("São Manuel")
                .estado("SP")
                .telefone(telefone)
                .role(Role.USER)
                .ativo(true)
                .build();

        alunoRepository.save(aluno);

        log.info("═══════════════════════════════════════════════════════");
        log.info("  Aluno de teste criado automaticamente");
        log.info("  Email: {}", ALUNO_EMAIL);
        log.info("  Senha: {}", ALUNO_SENHA);
        log.info("  RA: {}", ra);
        log.info("═══════════════════════════════════════════════════════");
    }
}
