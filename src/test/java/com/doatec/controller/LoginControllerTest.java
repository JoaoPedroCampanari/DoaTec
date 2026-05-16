package com.doatec.controller;

import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginController")
class LoginControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginController loginController;

    private Pessoa usuario;

    @BeforeEach
    void setUp() {
        usuario = new DoadorPF("123.456.789-00");
        usuario.setId(1);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario@teste.com");
        usuario.setTelefone("11999999999");
        usuario.setAtivo(true);

        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("loginUser endpoint")
    class LoginUserTests {

        @Test
        @DisplayName("Autenticacao bem-sucedida retorna 200 com UserLoginResponse")
        void autenticacaoBemSucedida() {
            LoginRequest loginRequest = new LoginRequest("usuario@teste.com", "senha123");
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "usuario@teste.com", "senha123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(pessoaRepository.findByEmail("usuario@teste.com"))
                    .thenReturn(Optional.of(usuario));

            ResponseEntity<?> responseEntity = loginController.loginUser(loginRequest, request, response);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertInstanceOf(UserLoginResponse.class, responseEntity.getBody());

            UserLoginResponse body = (UserLoginResponse) responseEntity.getBody();
            assertEquals(1, body.id());
            assertEquals("Usuario Teste", body.nome());
            assertEquals("usuario@teste.com", body.email());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(securityContextRepository).saveContext(any(), any(), any());
        }

        @Test
        @DisplayName("Credenciais invalidas retorna 401")
        void credenciaisInvalidas() {
            LoginRequest loginRequest = new LoginRequest("usuario@teste.com", "senhaErrada");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais invalidas"));

            ResponseEntity<?> responseEntity = loginController.loginUser(loginRequest, request, response);

            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
            assertEquals("Email ou senha incorretos.", responseEntity.getBody());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verifyNoInteractions(pessoaRepository);
            verifyNoInteractions(securityContextRepository);
        }
    }
}
