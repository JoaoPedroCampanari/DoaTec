package com.doatec.integration;

import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.repository.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PessoaRepository pessoaRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    // ==================== HELPERS DE AUTENTICAÇÃO ====================

    protected String loginAs(String email, String senha) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("JSESSIONID").getValue();
    }

    protected String loginAsUser() throws Exception {
        return loginAs("user@test.com", "123456");
    }

    protected String loginAsAdmin() throws Exception {
        return loginAs("admin@test.com", "Admin@123");
    }

    protected String loginAsSuperAdmin() throws Exception {
        return loginAs("superadmin@test.com", "SuperAdmin@123");
    }

    // ==================== FIXTURES DE TESTE ====================

    protected DoadorPF criarDoadorPF(String email, String nome) {
        String cpfUnico = String.format("%03d.%03d.%03d-%02d",
                (email.hashCode() % 1000 + 1000) % 1000,
                (email.hashCode() % 1000 + 2000) % 1000,
                (email.hashCode() % 1000 + 3000) % 1000,
                (email.hashCode() % 100 + 100) % 100);
        DoadorPF doador = DoadorPF.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .cpf(cpfUnico)
                .telefone("11999999999")
                .endereco("12345-678")
                .logradouro("Rua Teste")
                .numero("100")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .role(Role.USER)
                .ativo(true)
                .build();
        return pessoaRepository.save(doador);
    }

    protected Aluno criarAluno(String email, String nome) {
        String raUnico = String.format("%06d", Math.abs(email.hashCode()) % 1000000);
        Aluno aluno = Aluno.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .ra(raUnico)
                .telefone("11999999999")
                .endereco("12345-678")
                .logradouro("Rua Teste")
                .numero("100")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .role(Role.USER)
                .ativo(true)
                .build();
        return pessoaRepository.save(aluno);
    }

    protected DoadorPF criarAdmin(String email, String nome) {
        String cpfUnico = String.format("%03d.%03d.%03d-%02d",
                (email.hashCode() % 1000 + 4000) % 1000,
                (email.hashCode() % 1000 + 5000) % 1000,
                (email.hashCode() % 1000 + 6000) % 1000,
                (email.hashCode() % 100 + 200) % 100);
        DoadorPF admin = DoadorPF.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("Admin@123"))
                .cpf(cpfUnico)
                .telefone("11988888888")
                .endereco("12345-678")
                .logradouro("Rua Admin")
                .numero("200")
                .bairro("Jardim")
                .cidade("São Paulo")
                .estado("SP")
                .role(Role.ADMIN)
                .ativo(true)
                .build();
        return pessoaRepository.save(admin);
    }

    protected DoadorPF criarSuperAdmin(String email, String nome) {
        String cpfUnico = String.format("%03d.%03d.%03d-%02d",
                (email.hashCode() % 1000 + 7000) % 1000,
                (email.hashCode() % 1000 + 8000) % 1000,
                (email.hashCode() % 1000 + 9000) % 1000,
                (email.hashCode() % 100 + 300) % 100);
        DoadorPF superAdmin = DoadorPF.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode("SuperAdmin@123"))
                .cpf(cpfUnico)
                .telefone("11977777777")
                .endereco("12345-678")
                .logradouro("Rua SuperAdmin")
                .numero("300")
                .bairro("Vila")
                .cidade("São Paulo")
                .estado("SP")
                .role(Role.SUPER_ADMIN)
                .ativo(true)
                .build();
        return pessoaRepository.save(superAdmin);
    }
}
