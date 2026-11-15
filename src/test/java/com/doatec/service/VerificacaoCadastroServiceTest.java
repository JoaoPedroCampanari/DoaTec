package com.doatec.service;

import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificacaoCadastroServiceTest {
    @Test void codigoVinculadoAoEmailSessaoEConsumido() {
        PessoaRepository pessoas = mock(PessoaRepository.class);
        EmailService mail = mock(EmailService.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        VerificacaoCadastroService service = new VerificacaoCadastroService(pessoas, mail, new BCryptPasswordEncoder(4), request);
        assertThrows(ResponseStatusException.class, () -> service.exigirConfirmacao("aluno@example.com"));
        service.enviar("Aluno@example.com");
        assertThrows(ResponseStatusException.class, () -> service.exigirConfirmacao("aluno@example.com"));
        assertThrows(ResponseStatusException.class, () -> service.confirmar("aluno@example.com", "xxxxxxxx"));
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mail).enviarCodigoCadastro(eq("aluno@example.com"), captor.capture());
        String codigo = captor.getValue();
        service.confirmar("aluno@example.com", codigo);
        service.exigirConfirmacao("Aluno@example.com");
        assertThrows(ResponseStatusException.class, () -> service.exigirConfirmacao("outro@example.com"));
        service.consumir();
        assertThrows(ResponseStatusException.class, () -> service.exigirConfirmacao("aluno@example.com"));
    }
}
