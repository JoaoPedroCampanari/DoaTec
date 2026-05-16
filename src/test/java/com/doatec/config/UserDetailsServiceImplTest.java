package com.doatec.config;

import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    private Pessoa usuarioAtivo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = new DoadorPF("123.456.789-00");
        usuarioAtivo.setId(1);
        usuarioAtivo.setNome("Usuario Teste");
        usuarioAtivo.setEmail("user@test.com");
        usuarioAtivo.setSenha("senhaCriptografada");
        usuarioAtivo.setAtivo(true);
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Deve retornar UserDetails quando usuario existe e esta ativo")
        void usuarioEncontrado_retornaUserDetails() {
            when(pessoaRepository.findByEmail("user@test.com"))
                    .thenReturn(Optional.of(usuarioAtivo));

            UserDetails result = service.loadUserByUsername("user@test.com");

            assertNotNull(result);
            assertEquals("user@test.com", result.getUsername());
            assertEquals("senhaCriptografada", result.getPassword());
            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

            verify(pessoaRepository).findByEmail("user@test.com");
        }

        @Test
        @DisplayName("Deve lancar UsernameNotFoundException quando usuario nao existe")
        void usuarioNaoEncontrado_lancaExcecao() {
            when(pessoaRepository.findByEmail("naoexiste@test.com"))
                    .thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> service.loadUserByUsername("naoexiste@test.com"));

            assertTrue(ex.getMessage().contains("naoexiste@test.com"));

            verify(pessoaRepository).findByEmail("naoexiste@test.com");
        }

        @Test
        @DisplayName("Deve lancar UsernameNotFoundException quando usuario esta desativado")
        void usuarioDesativado_lancaExcecao() {
            usuarioAtivo.setAtivo(false);

            when(pessoaRepository.findByEmail("user@test.com"))
                    .thenReturn(Optional.of(usuarioAtivo));

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> service.loadUserByUsername("user@test.com"));

            assertTrue(ex.getMessage().contains("desativado"));

            verify(pessoaRepository).findByEmail("user@test.com");
        }
    }
}
