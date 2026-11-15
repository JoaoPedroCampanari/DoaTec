package com.doatec.service;

import com.doatec.model.account.DoadorPF;
import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedefinicaoSenhaServiceTest {
    PessoaRepository pessoas;
    EmailService mail;
    BCryptPasswordEncoder encoder;
    MockHttpServletRequest request;
    RedefinicaoSenhaService service;
    DoadorPF conta;

    @BeforeEach void iniciar() {
        pessoas = mock(PessoaRepository.class);
        mail = mock(EmailService.class);
        encoder = new BCryptPasswordEncoder(4);
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        service = new RedefinicaoSenhaService(pessoas, mail, encoder, request);
        conta = new DoadorPF("12345678900");
        conta.setId(1); conta.setEmail("aluno@example.com"); conta.setSenha(encoder.encode("senha-antiga")); conta.setAtivo(true);
    }
    private String solicitarCodigo() {
        when(pessoas.findByEmailIgnoreCase("aluno@example.com")).thenReturn(Optional.of(conta));
        service.solicitar("ALUNO@example.com");
        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(mail).enviarCodigoRedefinicaoSenha(eq("aluno@example.com"), codigo.capture());
        return codigo.getValue();
    }
    @Test void alteraSenhaSomenteComCodigoValidoEUsoUnico() {
        String codigo = solicitarCodigo();
        when(pessoas.findByIdForPasswordReset(1)).thenReturn(Optional.of(conta));
        String original = conta.getSenha();
        assertThrows(ResponseStatusException.class, () -> service.confirmar("errado", "senha-nova"));
        assertEquals(original, conta.getSenha());
        service.confirmar(codigo, "senha-nova");
        assertTrue(encoder.matches("senha-nova", conta.getSenha()));
        verify(pessoas).saveAndFlush(conta);
        assertThrows(ResponseStatusException.class, () -> service.confirmar(codigo, "outra-senha"));
    }
    @Test void codigoNaoFuncionaEmOutraSessao() {
        String codigo = solicitarCodigo();
        request.setSession(new org.springframework.mock.web.MockHttpSession());
        assertThrows(ResponseStatusException.class, () -> service.confirmar(codigo, "senha-nova"));
        verify(pessoas, never()).saveAndFlush(any());
    }
    @Test void bloqueiaCodigoDepoisDeCincoErros() {
        String codigo = solicitarCodigo();
        for (int i = 0; i < 5; i++) assertThrows(ResponseStatusException.class, () -> service.confirmar("errado", "senha-nova"));
        assertThrows(ResponseStatusException.class, () -> service.confirmar(codigo, "senha-nova"));
        verify(pessoas, never()).saveAndFlush(any());
    }
    @Test void emailDesconhecidoNaoEnviaCodigo() {
        when(pessoas.findByEmailIgnoreCase("inexistente@example.com")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.solicitar("inexistente@example.com"));
        verifyNoInteractions(mail);
        assertThrows(ResponseStatusException.class, () -> service.confirmar("12345678", "senha-nova"));
    }
    @Test void invalidaPedidoSeSenhaFoiAlterada() {
        String codigo = solicitarCodigo();
        conta.setSenha(encoder.encode("mudou-depois"));
        when(pessoas.findByIdForPasswordReset(1)).thenReturn(Optional.of(conta));
        assertThrows(ResponseStatusException.class, () -> service.confirmar(codigo, "senha-nova"));
        verify(pessoas, never()).saveAndFlush(any());
    }
}
