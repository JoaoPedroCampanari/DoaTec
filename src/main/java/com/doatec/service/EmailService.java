package com.doatec.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.email.from:no-reply@doatec.com}")
    private String fromEmail;

    public void enviarSuporteResposta(String para, String nomeUsuario, String assunto, String resposta) {
        String texto = "Olá, " + nomeUsuario + "!\n\nSeu ticket sobre \"" + assunto + "\" recebeu uma resposta:\n\n" + resposta + "\n\nAcesse seus tickets no DoaTec para continuar a conversa.";
        String corpo = "<p>Olá, <strong>" + escapar(nomeUsuario) + "</strong>!</p>"
                + "<p>Seu ticket sobre <strong>" + escapar(assunto) + "</strong> recebeu uma resposta:</p>"
                + caixa(escapar(resposta).replace("\n", "<br>"))
                + "<p>Acesse seus tickets no DoaTec para continuar a conversa.</p>";
        enviar(para, "Nova resposta no seu ticket de suporte - DoaTec", "Atualização do suporte", texto, corpo);
    }

    public void enviarEmailStatusDoacao(String para, String nomeUsuario, String status, String descricaoDoacao) {
        String texto = "Olá, " + nomeUsuario + "!\n\nO status da sua doação \"" + descricaoDoacao + "\" foi atualizado para: " + status + ".\n\nAcesse Minhas Doações no DoaTec para mais detalhes.";
        String corpo = "<p>Olá, <strong>" + escapar(nomeUsuario) + "</strong>!</p>"
                + "<p>O status da sua doação <strong>" + escapar(descricaoDoacao) + "</strong> foi atualizado para:</p>"
                + caixa(escapar(status)) + "<p>Acesse <strong>Minhas Doações</strong> no DoaTec para mais detalhes.</p>";
        enviar(para, "Status da sua doação atualizado - DoaTec", "Atualização da doação", texto, corpo);
    }

    public void enviarEmailStatusSolicitacao(String para, String nomeUsuario, String status, String descricao) {
        String texto = "Olá, " + nomeUsuario + "!\n\nO status da sua solicitação \"" + descricao + "\" foi atualizado para: " + status + ".\n\nAcesse Meus Pedidos no DoaTec para mais detalhes.";
        String corpo = "<p>Olá, <strong>" + escapar(nomeUsuario) + "</strong>!</p>"
                + "<p>O status da sua solicitação <strong>" + escapar(descricao) + "</strong> foi atualizado para:</p>"
                + caixa(escapar(status)) + "<p>Acesse <strong>Meus Pedidos</strong> no DoaTec para mais detalhes.</p>";
        enviar(para, "Status da sua solicitação atualizado - DoaTec", "Atualização da solicitação", texto, corpo);
    }

    public void enviarCodigoRedefinicaoSenha(String para, String codigo) {
        enviarCodigo(para, codigo, "Redefinição de senha - DoaTec", "Redefina sua senha",
                "Use o código abaixo para redefinir a senha da sua conta DoaTec.");
    }

    public void enviarCodigoCadastro(String para, String codigo) {
        enviarCodigo(para, codigo, "Confirme seu e-mail para criar sua conta - DoaTec", "Confirme seu e-mail",
                "Use o código abaixo para confirmar seu e-mail e continuar seu cadastro no DoaTec.");
    }

    public void enviarCodigoAlteracaoEmail(String para, String codigo) {
        enviarCodigo(para, codigo, "Confirme seu novo e-mail - DoaTec", "Confirme seu novo e-mail",
                "Use o código abaixo para confirmar a alteração do e-mail da sua conta DoaTec.");
    }

    private void enviarCodigo(String para, String codigo, String assunto, String titulo, String introducao) {
        String texto = introducao + "\n\nSeu código de confirmação é " + codigo + ". Ele expira em 10 minutos.\n\nSe você não solicitou este código, ignore este e-mail.";
        String corpo = "<p>" + escapar(introducao) + "</p>"
                + "<div style=\"margin:24px 0;padding:18px;background:#eff6ff;border-radius:8px;text-align:center;\">"
                + "<span style=\"font-size:30px;font-weight:700;letter-spacing:6px;color:#185cb2;\">" + escapar(codigo) + "</span></div>"
                + "<p>Este código expira em <strong>10 minutos</strong>.</p>"
                + "<p style=\"color:#64748b;font-size:14px;\">Se você não solicitou este código, ignore este e-mail.</p>";
        enviar(para, assunto, titulo, texto, corpo);
    }

    private void enviar(String para, String assunto, String titulo, String texto, String conteudo) {
        String html = """
                <!doctype html><html lang="pt-BR"><body style="margin:0;padding:0;background:#f3f6fa;font-family:Arial,Helvetica,sans-serif;color:#243247;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f3f6fa;"><tr><td align="center" style="padding:32px 12px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px;background:#ffffff;border:1px solid #e2e8f0;border-radius:12px;">
                <tr><td style="padding:28px 32px 22px;border-bottom:1px solid #e8eef5;"><img src="cid:doatec-logo" alt="DoaTec" width="180" style="display:block;width:180px;max-width:100%%;height:auto;border:0;"></td></tr>
                <tr><td style="padding:28px 32px 32px;font-size:16px;line-height:1.6;"><h1 style="margin:0 0 20px;color:#123b6c;font-size:23px;line-height:1.3;">%s</h1>%s</td></tr>
                <tr><td style="padding:20px 32px;border-top:1px solid #e8eef5;color:#64748b;font-size:12px;line-height:1.6;">DoaTec · Tecnologia que conecta e transforma<br>Esta é uma mensagem automática do DoaTec.</td></tr>
                </table></td></tr></table></body></html>
                """.formatted(escapar(titulo), conteudo);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_RELATED, "UTF-8");
            helper.setFrom(fromEmail, "DoaTec");
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(texto + "\n\nDoaTec · Tecnologia que conecta e transforma\nEsta é uma mensagem automática do DoaTec.", html);
            helper.addInline("doatec-logo", new ClassPathResource("static/doatec_logo.png"), "image/png");
            mailSender.send(message);
            log.info("E-mail DoaTec enviado com assunto: {}", assunto);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new MailPreparationException("Não foi possível preparar o e-mail do DoaTec.", e);
        }
    }

    private String escapar(String valor) { return HtmlUtils.htmlEscape(valor == null ? "" : valor); }
    private String caixa(String conteudo) {
        return "<div style=\"margin:20px 0;padding:16px 18px;background:#eff6ff;border-left:4px solid #2674c9;border-radius:4px;color:#173c69;\">" + conteudo + "</div>";
    }
}
