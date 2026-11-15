package com.doatec.service;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    private JavaMailSender sender;
    private EmailService service;
    private MimeMessage message;

    @BeforeEach void setup() {
        sender = mock(JavaMailSender.class);
        message = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(message);
        service = new EmailService(sender);
        ReflectionTestUtils.setField(service, "fromEmail", "no-reply@doatec.com");
    }

    @Test void codigoUsaTextoHtmlELogoIncorporada() throws Exception {
        service.enviarCodigoCadastro("aluno@example.com", "12345678");
        message.saveChanges();
        assertTrue(contem(message, "text/plain", "12345678"));
        assertTrue(contem(message, "text/html", "cid:doatec-logo"));
        assertTrue(temLogo(message));
        assertTrue(message.isMimeType("multipart/related"));
        Multipart raiz = (Multipart) message.getContent();
        boolean logoNaRaiz = false;
        for (int i = 0; i < raiz.getCount(); i++) {
            BodyPart parte = raiz.getBodyPart(i);
            String[] id = parte.getHeader("Content-ID");
            if (id != null && id.length > 0 && id[0].contains("doatec-logo")) logoNaRaiz = true;
        }
        assertTrue(logoNaRaiz);
        verify(sender).send(message);
    }

    @Test void respostaEscapaConteudoDeUsuario() throws Exception {
        service.enviarSuporteResposta("aluno@example.com", "Ana", "Ajuda", "<script>alert(1)</script>");
        message.saveChanges();
        assertTrue(contem(message, "text/html", "&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(temLogo(message));
    }

    private boolean contem(Part parte, String tipo, String texto) throws Exception {
        if (parte.isMimeType(tipo)) return parte.getContent().toString().contains(texto);
        if (parte.isMimeType("multipart/*")) {
            Multipart multi = (Multipart) parte.getContent();
            for (int i=0; i<multi.getCount(); i++) if (contem(multi.getBodyPart(i), tipo, texto)) return true;
        }
        return false;
    }
    private boolean temLogo(Part parte) throws Exception {
        String[] id = parte.getHeader("Content-ID");
        if (id != null && id.length > 0 && id[0].contains("doatec-logo") && parte.isMimeType("image/png")) return true;
        if (parte.isMimeType("multipart/*")) {
            Multipart multi = (Multipart) parte.getContent();
            for (int i=0; i<multi.getCount(); i++) if (temLogo(multi.getBodyPart(i))) return true;
        }
        return false;
    }
}
