package com.doatec.controller;

import com.doatec.dto.request.CriarAdminRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.SuperAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperAdminController")
class SuperAdminControllerTest {

    @Mock
    private SuperAdminService superAdminService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private SuperAdminController controller;

    private Pessoa superAdmin;
    private User userDetails;
    private UsuarioAdminResponse adminResponse;

    @BeforeEach
    void setUp() {
        superAdmin = DoadorPF.builder()
                .id(99)
                .nome("Super Admin")
                .email("superadmin@test.com")
                .senha("password")
                .cpf("000.000.000-00")
                .role(Role.SUPER_ADMIN)
                .ativo(true)
                .build();

        userDetails = new User("superadmin@test.com", "password", List.of());

        adminResponse = UsuarioAdminResponse.builder()
                .id(1)
                .nome("Admin Teste")
                .email("admin@test.com")
                .documento("123.456.789-00")
                .telefone("11999999999")
                .endereco("12345-678")
                .tipoPessoa("DOADOR_PF")
                .role("ADMIN")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("listarAdmins")
    class ListarAdminsTests {

        @Test
        @DisplayName("Deve retornar 200 com pagina de administradores")
        void listarAdmins_retorna200ComPage() {
            Page<UsuarioAdminResponse> page = new PageImpl<>(List.of(adminResponse));
            when(superAdminService.listarAdmins(any(Pageable.class))).thenReturn(page);

            ResponseEntity<Page<UsuarioAdminResponse>> response = controller.listarAdmins(Pageable.unpaged());

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals("Admin Teste", response.getBody().getContent().get(0).nome());
            verify(superAdminService).listarAdmins(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("criarAdmin")
    class CriarAdminTests {

        @Test
        @DisplayName("Deve retornar 201 com administrador criado")
        void criarAdmin_retorna201ComAdmin() {
            when(pessoaRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdmin));
            when(superAdminService.criarAdmin(any(CriarAdminRequest.class), eq(99))).thenReturn(adminResponse);

            CriarAdminRequest request = new CriarAdminRequest(
                    "Admin Teste", "admin@test.com", "senha123",
                    "DOADOR_PF", "123.456.789-00", "ADMIN"
            );

            ResponseEntity<UsuarioAdminResponse> response = controller.criarAdmin(request, userDetails);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Admin Teste", response.getBody().nome());
            verify(pessoaRepository).findByEmail("superadmin@test.com");
            verify(superAdminService).criarAdmin(request, 99);
        }
    }

    @Nested
    @DisplayName("rebaixarAdmin")
    class RebaixarAdminTests {

        @Test
        @DisplayName("Deve retornar 200 com administrador rebaixado")
        void rebaixarAdmin_retorna200() {
            UsuarioAdminResponse rebaixado = UsuarioAdminResponse.builder()
                    .id(1)
                    .nome("Admin Teste")
                    .email("admin@test.com")
                    .documento("123.456.789-00")
                    .role("USER")
                    .ativo(true)
                    .build();

            when(pessoaRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdmin));
            when(superAdminService.rebaixarAdmin(1, 99)).thenReturn(rebaixado);

            ResponseEntity<UsuarioAdminResponse> response = controller.rebaixarAdmin(1, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("USER", response.getBody().role());
            verify(pessoaRepository).findByEmail("superadmin@test.com");
            verify(superAdminService).rebaixarAdmin(1, 99);
        }
    }

    @Nested
    @DisplayName("alterarRoleAdmin")
    class AlterarRoleAdminTests {

        @Test
        @DisplayName("Deve retornar 200 com role alterada")
        void alterarRoleAdmin_retorna200() {
            UsuarioAdminResponse alterado = UsuarioAdminResponse.builder()
                    .id(1)
                    .nome("Admin Teste")
                    .email("admin@test.com")
                    .documento("123.456.789-00")
                    .role("USER")
                    .ativo(true)
                    .build();

            when(pessoaRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdmin));
            when(superAdminService.alterarRoleAdmin(1, Role.USER, 99)).thenReturn(alterado);

            ResponseEntity<UsuarioAdminResponse> response = controller.alterarRoleAdmin(1, Role.USER, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("USER", response.getBody().role());
            verify(pessoaRepository).findByEmail("superadmin@test.com");
            verify(superAdminService).alterarRoleAdmin(1, Role.USER, 99);
        }
    }

    @Nested
    @DisplayName("alterarStatusAdmin")
    class AlterarStatusAdminTests {

        @Test
        @DisplayName("Deve lancar BusinessException quando body nao tem campo ativo")
        void alterarStatusAdmin_bodySemAtivo_lancaExcecao() {
            when(pessoaRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdmin));

            Map<String, Boolean> body = new HashMap<>();

            assertThrows(BusinessException.class,
                    () -> controller.alterarStatusAdmin(1, body, userDetails));
            verify(superAdminService, never()).alterarStatusAdmin(any(), any(), any());
        }

        @Test
        @DisplayName("Deve retornar 200 com status alterado com sucesso")
        void alterarStatusAdmin_sucesso_retorna200() {
            UsuarioAdminResponse inativado = UsuarioAdminResponse.builder()
                    .id(1)
                    .nome("Admin Teste")
                    .email("admin@test.com")
                    .documento("123.456.789-00")
                    .role("ADMIN")
                    .ativo(false)
                    .build();

            when(pessoaRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdmin));
            when(superAdminService.alterarStatusAdmin(1, false, 99)).thenReturn(inativado);

            Map<String, Boolean> body = new HashMap<>();
            body.put("ativo", false);

            ResponseEntity<UsuarioAdminResponse> response = controller.alterarStatusAdmin(1, body, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().ativo());
            verify(pessoaRepository).findByEmail("superadmin@test.com");
            verify(superAdminService).alterarStatusAdmin(1, false, 99);
        }
    }

    @Nested
    @DisplayName("excluirAdmin")
    class ExcluirAdminTests {

        @Test
        @DisplayName("Deve retornar 204 ao excluir administrador")
        void excluirAdmin_retorna204() {
            when(pessoaRepository.findByEmail("superadmin@test.com")).thenReturn(Optional.of(superAdmin));
            doNothing().when(superAdminService).excluirAdmin(1, 99);

            ResponseEntity<Void> response = controller.excluirAdmin(1, userDetails);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(pessoaRepository).findByEmail("superadmin@test.com");
            verify(superAdminService).excluirAdmin(1, 99);
        }
    }
}
