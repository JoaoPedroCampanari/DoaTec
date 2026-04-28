package com.doatec.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:no-reply@doatec.com}")
    private String fromEmail;

    public void enviarSuporteResposta(String para, String nomeUsuario, String assunto, String resposta) {
        enviarEmail(
                para,
                "Nova resposta no seu ticket de suporte - DoaTec",
                montarHtmlSuporte(nomeUsuario, assunto, resposta)
        );
    }

    public void enviarEmailStatusDoacao(String para, String nomeUsuario, String status, String descricaoDoacao) {
        enviarEmail(
                para,
                "Status da sua doacao atualizado - DoaTec",
                montarHtmlStatusDoacao(nomeUsuario, status, descricaoDoacao)
        );
    }

    public void enviarEmailStatusSolicitacao(String para, String nomeUsuario, String status, String descricao) {
        enviarEmail(
                para,
                "Status da sua solicitacao atualizado - DoaTec",
                montarHtmlStatusSolicitacao(nomeUsuario, status, descricao)
        );
    }

    private void enviarEmail(String para, String assunto, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email enviado para {} com assunto: {}", para, assunto);
        } catch (MessagingException e) {
            log.error("Falha ao enviar email para {}: {}", para, e.getMessage());
        }
    }

    private String montarHtmlSuporte(String nome, String assunto, String resposta) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #3b82f6;">DoaTec - Suporte</h2>
                    <p>Ola, <strong>%s</strong>!</p>
                    <p>Sua solicitacao de suporte sobre "<strong>%s</strong>" recebeu uma resposta:</p>
                    <div style="background: #f0f9ff; border-left: 4px solid #3b82f6; padding: 15px; margin: 15px 0;">
                        %s
                    </div>
                    <p style="color: #6b7280; font-size: 12px;">Acesse o painel de suporte para continuar a conversa.</p>
                </body>
                </html>
                """.formatted(nome, assunto, resposta);
    }

    private String montarHtmlStatusDoacao(String nome, String status, String descricao) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #3b82f6;">DoaTec - Doacao</h2>
                    <p>Ola, <strong>%s</strong>!</p>
                    <p>O status da sua doacao "<strong>%s</strong>" foi atualizado para: <strong>%s</strong></p>
                    <p style="color: #6b7280; font-size: 12px;">Acesse a aba "Minhas Doacoes" para mais detalhes.</p>
                </body>
                </html>
                """.formatted(nome, descricao, status);
    }

    private String montarHtmlStatusSolicitacao(String nome, String status, String descricao) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #3b82f6;">DoaTec - Solicitacao</h2>
                    <p>Ola, <strong>%s</strong>!</p>
                    <p>O status da sua solicitacao "<strong>%s</strong>" foi atualizado para: <strong>%s</strong></p>
                    <p style="color: #6b7280; font-size: 12px;">Acesse a aba "Meus Pedidos" para mais detalhes.</p>
                </body>
                </html>
                """.formatted(nome, descricao, status);
    }
}
