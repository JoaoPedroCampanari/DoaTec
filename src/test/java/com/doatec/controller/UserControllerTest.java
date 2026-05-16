package com.doatec.controller;

import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.repository.SuporteFormularioRepository;
import com.doatec.service.DoacaoService;
import com.doatec.service.PessoaService;
import com.doatec.service.SolicitacaoService;
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
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    @Mock private PessoaService pessoaService;
    @Mock private DoacaoService doacaoService;
    @Mock private SolicitacaoService solicitacaoService;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private DoacaoRepository doacaoRepository;
    @Mock private SolicitacaoHardwareRepository solicitacaoHardwareRepository;
    @Mock private SuporteFormularioRepository suporteFormularioRepository;
    @InjectMocks private UserController controller;

    private User userDetails;
    private Pessoa usuario;
    private Pessoa adminUser;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@test.com", "password", List.of());

        usuario = new DoadorPF("123.456.789-00");
        usuario.setId(1);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("user@test.com");
        usuario.setRole(Role.USER);
        usuario.setAtivo(true);

        adminUser = new DoadorPF("999.888.777-66");
        adminUser.setId(99);
        adminUser.setNome("Admin Teste");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setAtivo(true);
    }

    // ==================== getCurrentUser ====================

    @Nested
    @DisplayName("GET /me — getCurrentUser")
    class GetCurrentUserTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            ResponseEntity<UserLoginResponse> response = controller.getCurrentUser(null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("retorna 200 com UserLoginResponse quando autenticado")
        void retorna200ComUserLoginResponse() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(doacaoRepository.countByDoadorId(1)).thenReturn(5L);
            when(solicitacaoHardwareRepository.countByAlunoId(1)).thenReturn(3L);
            when(suporteFormularioRepository.countByAutorId(1)).thenReturn(2L);

            ResponseEntity<UserLoginResponse> response = controller.getCurrentUser(userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().id());
            assertEquals("user@test.com", response.getBody().email());
            assertEquals(5L, response.getBody().totalDoacoes());
            assertEquals(3L, response.getBody().totalSolicitacoes());
            assertEquals(2L, response.getBody().totalTicketsSuporte());

            verify(pessoaRepository).findByEmail("user@test.com");
            verify(doacaoRepository).countByDoadorId(1);
            verify(solicitacaoHardwareRepository).countByAlunoId(1);
            verify(suporteFormularioRepository).countByAutorId(1);
        }
    }

    // ==================== getUserById ====================

    @Nested
    @DisplayName("GET /{id} — getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            ResponseEntity<UserLoginResponse> response = controller.getUserById(1, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("retorna 403 quando usuário normal tenta ver outro usuário")
        void retorna403QuandoUsuarioNormalTentaVerOutro() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            // usuario id=1 tenta ver id=2 → precisa checar se é admin
            when(pessoaService.findById(1)).thenReturn(usuario);

            ResponseEntity<UserLoginResponse> response = controller.getUserById(2, userDetails);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());

            verify(pessoaRepository).findByEmail("user@test.com");
            verify(pessoaService).findById(1);
        }

        @Test
        @DisplayName("retorna 200 quando usuário vê seus próprios dados")
        void retorna200QuandoUsuarioVeSeusPropriosDados() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(pessoaService.findById(1)).thenReturn(usuario);
            when(doacaoRepository.countByDoadorId(1)).thenReturn(0L);
            when(solicitacaoHardwareRepository.countByAlunoId(1)).thenReturn(0L);
            when(suporteFormularioRepository.countByAutorId(1)).thenReturn(0L);

            ResponseEntity<UserLoginResponse> response = controller.getUserById(1, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().id());
        }

        @Test
        @DisplayName("retorna 200 quando admin vê outro usuário")
        void retorna200QuandoAdminVeOutroUsuario() {
            User adminDetails = new User("admin@test.com", "password", List.of());

            when(pessoaRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(pessoaService.findById(99)).thenReturn(adminUser);
            when(pessoaService.findById(1)).thenReturn(usuario);
            when(doacaoRepository.countByDoadorId(1)).thenReturn(5L);
            when(solicitacaoHardwareRepository.countByAlunoId(1)).thenReturn(3L);
            when(suporteFormularioRepository.countByAutorId(1)).thenReturn(2L);

            ResponseEntity<UserLoginResponse> response = controller.getUserById(1, adminDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().id());

            verify(pessoaService).findById(99);
            verify(pessoaService).findById(1);
        }
    }

    // ==================== updateCurrentUser ====================

    @Nested
    @DisplayName("PUT /me — updateCurrentUser")
    class UpdateCurrentUserTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            PessoaUpdateRequest dto = PessoaUpdateRequest.builder().build();

            ResponseEntity<String> response = controller.updateCurrentUser(null, dto);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("retorna 200 com mensagem de sucesso ao atualizar perfil")
        void retorna200AoAtualizarPerfil() {
            PessoaUpdateRequest dto = PessoaUpdateRequest.builder()
                    .telefone("11999999999")
                    .build();

            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(pessoaService.updatePessoaProfile(eq(1), any(PessoaUpdateRequest.class))).thenReturn(usuario);

            ResponseEntity<String> response = controller.updateCurrentUser(userDetails, dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Perfil atualizado com sucesso"));

            verify(pessoaService).updatePessoaProfile(eq(1), any(PessoaUpdateRequest.class));
        }
    }

    // ==================== updateUser ====================

    @Nested
    @DisplayName("PUT /{id} — updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            PessoaUpdateRequest dto = PessoaUpdateRequest.builder().build();

            ResponseEntity<String> response = controller.updateUser(1, null, dto);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("retorna 403 quando usuário normal tenta editar outro")
        void retorna403QuandoUsuarioNormalTentaEditarOutro() {
            PessoaUpdateRequest dto = PessoaUpdateRequest.builder().build();

            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(pessoaService.findById(1)).thenReturn(usuario);

            ResponseEntity<String> response = controller.updateUser(2, userDetails, dto);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

            verify(pessoaRepository).findByEmail("user@test.com");
            verify(pessoaService).findById(1);
            verify(pessoaService, never()).updatePessoaProfile(anyInt(), any());
        }

        @Test
        @DisplayName("retorna 200 quando admin edita outro usuário")
        void retorna200QuandoAdminEditaOutro() {
            User adminDetails = new User("admin@test.com", "password", List.of());
            PessoaUpdateRequest dto = PessoaUpdateRequest.builder()
                    .telefone("11988887777")
                    .build();

            when(pessoaRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(pessoaService.findById(99)).thenReturn(adminUser);
            when(pessoaService.updatePessoaProfile(eq(1), any(PessoaUpdateRequest.class))).thenReturn(usuario);

            ResponseEntity<String> response = controller.updateUser(1, adminDetails, dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Perfil atualizado com sucesso"));

            verify(pessoaService).updatePessoaProfile(eq(1), any(PessoaUpdateRequest.class));
        }

        @Test
        @DisplayName("retorna 200 quando usuário edita seus próprios dados")
        void retorna200QuandoUsuarioEditaSeusPropriosDados() {
            PessoaUpdateRequest dto = PessoaUpdateRequest.builder()
                    .telefone("11977776666")
                    .build();

            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(pessoaService.updatePessoaProfile(eq(1), any(PessoaUpdateRequest.class))).thenReturn(usuario);

            ResponseEntity<String> response = controller.updateUser(1, userDetails, dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    // ==================== getMyDonations ====================

    @Nested
    @DisplayName("GET /me/donations — getMyDonations")
    class GetMyDonationsTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            ResponseEntity<List<DoacaoResponse>> response = controller.getMyDonations(null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("retorna 200 com lista de doações do usuário")
        void retorna200ComListaDeDoacoes() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(doacaoService.findDoacoesByDoadorId(1)).thenReturn(Collections.emptyList());

            ResponseEntity<List<DoacaoResponse>> response = controller.getMyDonations(userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());

            verify(doacaoService).findDoacoesByDoadorId(1);
        }
    }

    // ==================== getMySolicitacoes ====================

    @Nested
    @DisplayName("GET /me/solicitacoes — getMySolicitacoes")
    class GetMySolicitacoesTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            ResponseEntity<List<SolicitacaoResponse>> response = controller.getMySolicitacoes(null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("retorna 200 com lista de solicitações do usuário")
        void retorna200ComListaDeSolicitacoes() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(solicitacaoService.findSolicitacoesByAlunoId(1)).thenReturn(Collections.emptyList());

            ResponseEntity<List<SolicitacaoResponse>> response = controller.getMySolicitacoes(userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());

            verify(solicitacaoService).findSolicitacoesByAlunoId(1);
        }
    }

    // ==================== getUserDonations ====================

    @Nested
    @DisplayName("GET /{id}/donations — getUserDonations")
    class GetUserDonationsTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            ResponseEntity<List<DoacaoResponse>> response = controller.getUserDonations(1, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("retorna 403 quando usuário normal tenta ver doações de outro")
        void retorna403QuandoUsuarioNormalTentaVerDeOutro() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(pessoaService.findById(1)).thenReturn(usuario);

            ResponseEntity<List<DoacaoResponse>> response = controller.getUserDonations(2, userDetails);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());

            verify(pessoaService).findById(1);
            verify(doacaoService, never()).findDoacoesByDoadorId(anyInt());
        }

        @Test
        @DisplayName("retorna 200 quando admin vê doações de outro usuário")
        void retorna200QuandoAdminVeDeOutro() {
            User adminDetails = new User("admin@test.com", "password", List.of());

            when(pessoaRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(pessoaService.findById(99)).thenReturn(adminUser);
            when(doacaoService.findDoacoesByDoadorId(1)).thenReturn(Collections.emptyList());

            ResponseEntity<List<DoacaoResponse>> response = controller.getUserDonations(1, adminDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            verify(doacaoService).findDoacoesByDoadorId(1);
        }
    }

    // ==================== getUserSolicitacoes ====================

    @Nested
    @DisplayName("GET /{id}/solicitacoes — getUserSolicitacoes")
    class GetUserSolicitacoesTests {

        @Test
        @DisplayName("retorna 401 quando userDetails é nulo")
        void retorna401QuandoUserDetailsNulo() {
            ResponseEntity<List<SolicitacaoResponse>> response = controller.getUserSolicitacoes(1, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("retorna 403 quando usuário normal tenta ver solicitações de outro")
        void retorna403QuandoUsuarioNormalTentaVerDeOutro() {
            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(pessoaService.findById(1)).thenReturn(usuario);

            ResponseEntity<List<SolicitacaoResponse>> response = controller.getUserSolicitacoes(2, userDetails);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());

            verify(pessoaService).findById(1);
            verify(solicitacaoService, never()).findSolicitacoesByAlunoId(anyInt());
        }

        @Test
        @DisplayName("retorna 200 quando admin vê solicitações de outro usuário")
        void retorna200QuandoAdminVeDeOutro() {
            User adminDetails = new User("admin@test.com", "password", List.of());

            when(pessoaRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            when(pessoaService.findById(99)).thenReturn(adminUser);
            when(solicitacaoService.findSolicitacoesByAlunoId(1)).thenReturn(Collections.emptyList());

            ResponseEntity<List<SolicitacaoResponse>> response = controller.getUserSolicitacoes(1, adminDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            verify(solicitacaoService).findSolicitacoesByAlunoId(1);
        }
    }
}
