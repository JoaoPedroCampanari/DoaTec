package com.doatec.controller;
import com.doatec.model.account.DoadorPF;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AlteracaoEmailControllerTest {
    @Test void confirmaSomenteComSenhaECodigo() {
        PessoaRepository repo = mock(PessoaRepository.class);
        EmailService mail = mock(EmailService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        AlteracaoEmailController controller = new AlteracaoEmailController(repo, encoder, mail);
        DoadorPF pessoa = new DoadorPF("12345678900");
        pessoa.setId(3); pessoa.setEmail("antigo@example.com"); pessoa.setSenha(encoder.encode("senha123"));
        when(repo.findByEmail("antigo@example.com")).thenReturn(Optional.of(pessoa));
        User user = new User("antigo@example.com", "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        MockHttpSession sessao = new MockHttpSession();
        assertThrows(ResponseStatusException.class, () -> controller.solicitar(user, new AlteracaoEmailController.Solicitacao("novo@example.com", "errada"), sessao));
        verifyNoInteractions(mail);
        controller.solicitar(user, new AlteracaoEmailController.Solicitacao("novo@example.com", "senha123"), sessao);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mail).enviarCodigoAlteracaoEmail(eq("novo@example.com"), captor.capture());
        String codigo = captor.getValue();
        assertEquals("antigo@example.com", pessoa.getEmail());
        assertThrows(ResponseStatusException.class, () -> controller.confirmar(user, new AlteracaoEmailController.Confirmacao("incorreto"), sessao));
        assertEquals("antigo@example.com", pessoa.getEmail());
        controller.confirmar(user, new AlteracaoEmailController.Confirmacao(codigo), sessao);
        assertEquals("novo@example.com", pessoa.getEmail());
        assertTrue(sessao.isInvalid());
        verify(repo).saveAndFlush(pessoa);
    }
}
