package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService - Envio de emails")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@doatec.com");
    }

    @Test
    @DisplayName("enviarSuporteResposta envia email corretamente")
    void enviarSuporteRespostaFunciona() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);

        assertDoesNotThrow(() -> {
            emailService.enviarSuporteResposta(
                    "usuario@teste.com",
                    "João",
                    "Hardware solicitado",
                    "Seu pedido foi aprovado."
            );
        });

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailStatusDoacao envia email corretamente")
    void enviarEmailStatusDoacaoFunciona() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);

        assertDoesNotThrow(() -> {
            emailService.enviarEmailStatusDoacao(
                    "doador@teste.com",
                    "Maria",
                    "FINALIZADO",
                    "Doação de 5 notebooks"
            );
        });

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailStatusSolicitacao envia email corretamente")
    void enviarEmailStatusSolicitacaoFunciona() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);

        assertDoesNotThrow(() -> {
            emailService.enviarEmailStatusSolicitacao(
                    "aluno@teste.com",
                    "Carlos",
                    "APROVADA",
                    "Solicitação de hardware"
            );
        });

        verify(mailSender).send(mimeMessage);
    }
}
