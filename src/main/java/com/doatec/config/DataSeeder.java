package com.doatec.config;

import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.repository.PessoaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String SUPER_ADMIN_CPF = "000.000.000-00";

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final String superAdminEmail;
    private final String superAdminSenha;

    public DataSeeder(PessoaRepository pessoaRepository, PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate,
                      @Value("${SUPER_ADMIN_EMAIL:}") String superAdminEmail,
                      @Value("${SUPER_ADMIN_SENHA:}") String superAdminSenha) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.superAdminEmail = superAdminEmail;
        this.superAdminSenha = superAdminSenha;
    }

    @Override
    public void run(String... args) {
        fixDoacaoStatusCheckConstraint();
        fixSolicitacaoStatusCheckConstraint();
        jdbcTemplate.update("UPDATE doacao SET status = ? WHERE status = ?", "EM_TRIAGEM", "AGENDANDO_COLETA");
        fixRoleCheckConstraint();
        fixLogAcaoCheckConstraint();
        seedSuperAdmin();
    }

    private void fixSolicitacaoStatusCheckConstraint() {
        Integer existentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name = 'solicitacao_hardware' AND constraint_name = 'solicitacao_hardware_status_check'", Integer.class);
        if (existentes != null && existentes > 0) {
            jdbcTemplate.execute("ALTER TABLE solicitacao_hardware DROP CONSTRAINT solicitacao_hardware_status_check");
            jdbcTemplate.execute("ALTER TABLE solicitacao_hardware ADD CONSTRAINT solicitacao_hardware_status_check CHECK (status IN ('EM_ANALISE','APROVADA','REJEITADA','CONCLUIDA','CANCELADA'))");
        }
    }

    private void fixDoacaoStatusCheckConstraint() {
        Integer existentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name = 'doacao' AND constraint_name = 'doacao_status_check'", Integer.class);
        if (existentes != null && existentes > 0) {
            jdbcTemplate.execute("ALTER TABLE doacao DROP CONSTRAINT doacao_status_check");
            jdbcTemplate.execute("ALTER TABLE doacao ADD CONSTRAINT doacao_status_check CHECK (status IN (" +
                    "'EM_TRIAGEM','EM_ANALISE','REJEITADA','RECUSADO','APROVADO','FINALIZADO','RECEBIDO','AGUARDANDO_COLETA'," +
                    "'AGUARDANDO_ENTREGA','ENTREGUE','CANCELADA','AGENDANDO_COLETA','COLETA_AGENDADA','RETIRADO'))");
        }
    }

    /**
     * Hibernate ddl-auto=update não altera check constraints existentes.
     * Este método corrige a constraint para incluir SUPER_ADMIN quando necessário.
     */
    private void fixRoleCheckConstraint() {
        try {

            // Tenta dropar e recriar a constraint
            jdbcTemplate.execute("ALTER TABLE pessoa DROP CONSTRAINT IF EXISTS pessoa_role_check");
            jdbcTemplate.execute("ALTER TABLE pessoa ADD CONSTRAINT pessoa_role_check CHECK (role IN ('USER', 'ADMIN', 'SUPER_ADMIN'))");
            log.info("Constraint pessoa_role_check atualizada para incluir SUPER_ADMIN");
        } catch (Exception e) {
            // Se a tabela não existe ainda (primeira execução), o Hibernate vai criá-la corretamente
            log.debug("Não foi possível alterar constraint pessoa_role_check (tabela pode não existir ainda): {}", e.getMessage());
        }
    }

    /**
     * Recria a check constraint de {@code log_acao.acao} para incluir os novos valores
     * do enum AcaoTipo (CRIAR_EQUIPAMENTO, EDITAR_EQUIPAMENTO, DELETAR_EQUIPAMENTO).
     * Hibernate ddl-auto=update não atualiza check constraints existentes.
     */
    private void fixLogAcaoCheckConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE log_acao DROP CONSTRAINT IF EXISTS log_acao_acao_check");
            jdbcTemplate.execute(
                "ALTER TABLE log_acao ADD CONSTRAINT log_acao_acao_check CHECK (acao IN (" +
                "'APROVAR_DOACAO','REJEITAR_DOACAO','ALTERAR_STATUS_DOACAO'," +
                "'APROVAR_SOLICITACAO','REJEITAR_SOLICITACAO','CONCLUIR_SOLICITACAO','ALTERAR_STATUS_SOLICITACAO'," +
                "'RESPONDER_SUPORTE','DESATIVAR_USUARIO','REATIVAR_USUARIO'," +
                "'CRIAR_EQUIPAMENTO','EDITAR_EQUIPAMENTO','DELETAR_EQUIPAMENTO'))"
            );
            log.info("Constraint log_acao_acao_check atualizada para incluir CRUD de Equipamento");
        } catch (Exception e) {
            log.debug("Não foi possível alterar constraint log_acao_acao_check: {}", e.getMessage());
        }
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
