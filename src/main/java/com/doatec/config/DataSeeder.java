package com.doatec.config;

import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.DoadorPJ;
import com.doatec.model.account.Role;
import com.doatec.repository.AlunoRepository;
import com.doatec.repository.PessoaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
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
    private static final String ALUNO_RA = "1190482321008";

    private final PessoaRepository pessoaRepository;
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(PessoaRepository pessoaRepository, AlunoRepository alunoRepository,
                      PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate) {
        this.pessoaRepository = pessoaRepository;
        this.alunoRepository = alunoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        fixRoleCheckConstraint();
        fixLogAcaoCheckConstraint();
        seedSuperAdmin();
        seedAlunoTeste();
        seedDoadoresTeste();
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
        var alunoExistente = alunoRepository.findByEmail(ALUNO_EMAIL);
        if (alunoExistente.isPresent()) {
            Aluno aluno = alunoExistente.get();
            if ("593364".equals(aluno.getRa())) {
                if (alunoRepository.existsByRaAndIdNot(ALUNO_RA, aluno.getId())) {
                    log.warn("RA fixo do aluno de teste já está em uso; RA anterior preservado");
                } else {
                    aluno.setRa(ALUNO_RA);
                    alunoRepository.save(aluno);
                    log.info("RA antigo do aluno de teste atualizado para {}", ALUNO_RA);
                }
            }
            return;
        }

        String ra = ALUNO_RA;
        if (alunoRepository.existsByRa(ra)) {
            log.warn("Aluno de teste não criado: RA {} já cadastrado", ra);
            return;
        }
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
    private void seedDoadoresTeste() {
        String senha = "123456";
        String emailPf = "pessoa@gmail.com";
        String emailPj = "empresa@gmail.com";

        atualizarEmailTeste("doador.pf@doatec.test", emailPf, "doador_pf", "cpf", "34439757715", senha);
        atualizarEmailTeste("pessoa@teste.com", emailPf, "doador_pf", "cpf", "34439757715", senha);
        atualizarEmailTeste("doador.pj@doatec.test", emailPj, "doador_pj", "cnpj", "79914219000156", senha);
        atualizarEmailTeste("empresa@teste.com", emailPj, "doador_pj", "cnpj", "79914219000156", senha);

        if (!seedExiste(emailPf, "doador_pf", "cpf", "34439757715")) {
            pessoaRepository.save(DoadorPF.builder()
                    .nome("Marina Oliveira")
                    .email(emailPf)
                    .senha(passwordEncoder.encode(senha))
                    .cpf("34439757715")
                    .endereco("17404542").logradouro("Rua das Flores").numero("123")
                    .bairro("Centro").cidade("São Manuel").estado("SP")
                    .telefone("(14) 99999-1234").role(Role.USER).ativo(true).build());
            log.info("Doador pessoa física de teste criado: {}", emailPf);
        }

        if (!seedExiste(emailPj, "doador_pj", "cnpj", "79914219000156")) {
            pessoaRepository.save(DoadorPJ.builder()
                    .nome("Empresa Horizonte")
                    .razaoSocial("Horizonte Tecnologia Ltda")
                    .email(emailPj)
                    .senha(passwordEncoder.encode(senha))
                    .cnpj("79914219000156")
                    .endereco("17404542").logradouro("Rua das Palmeiras").numero("456")
                    .bairro("Centro").cidade("São Manuel").estado("SP")
                    .telefone("(14) 3333-4567").role(Role.USER).ativo(true).build());
            log.info("Doador pessoa jurídica de teste criado: {}", emailPj);
        }
    }

    private void atualizarEmailTeste(String anterior, String novo, String tabela, String coluna, String documento, String senha) {
        String sql = "UPDATE pessoa SET email = ?, senha = ? WHERE email = ? "
                + "AND NOT EXISTS (SELECT 1 FROM pessoa WHERE email = ?) "
                + "AND id IN (SELECT id FROM " + tabela + " WHERE REPLACE(REPLACE(REPLACE("
                + coluna + ", '.', ''), '-', ''), '/', '') = ?)";
        int atualizados = jdbcTemplate.update(sql, novo, passwordEncoder.encode(senha), anterior, novo, documento);
        if (atualizados > 0) log.info("E-mail da conta de teste atualizado: {}", novo);
    }

    private boolean seedExiste(String email, String tabela, String coluna, String documento) {
        Integer emails = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pessoa WHERE email = ?", Integer.class, email);
        String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE REPLACE(REPLACE(REPLACE(" + coluna
                + ", '.', ''), '-', ''), '/', '') = ?";
        Integer documentos = jdbcTemplate.queryForObject(sql, Integer.class, documento);
        if (emails != null && emails > 0 || documentos != null && documentos > 0) {
            log.info("Doador de teste não criado: e-mail ou documento já cadastrado ({})", email);
            return true;
        }
        return false;
    }

}
