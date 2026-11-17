package com.doatec.integration;

import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.model.chat.ContextoChat;
import com.doatec.model.chat.MensagemChat;
import com.doatec.repository.MensagemChatRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MensagemChatLeituraJpaTest {
    @Autowired EntityManager em;
    @Autowired MensagemChatRepository mensagens;

    @Test
    void somenteMensagensDaPessoaNaoLidasAparecemELeituraNaoAfetaNovasMensagens() {
        DoadorPF pessoa = DoadorPF.builder().nome("Doador").email("doador-chat@example.com").senha("hash").cpf("12345678901").role(Role.USER).ativo(true).build();
        DoadorPF admin = DoadorPF.builder().nome("Admin").email("admin-chat@example.com").senha("hash").cpf("12345678902").role(Role.ADMIN).ativo(true).build();
        em.persist(pessoa);
        em.persist(admin);
        MensagemChat primeira = MensagemChat.builder().conteudo("Olá").remetente(pessoa).contexto(ContextoChat.DOACAO).referenciaId(7).build();
        em.persist(primeira);
        em.persist(MensagemChat.builder().conteudo("Resposta").remetente(admin).contexto(ContextoChat.DOACAO).referenciaId(8).build());
        MensagemChat posterior = MensagemChat.builder().conteudo("Outra mensagem").remetente(pessoa).contexto(ContextoChat.DOACAO).referenciaId(7).build();
        em.persist(posterior);
        em.flush();
        assertEquals(1, mensagens.buscarConversasNaoLidas(Role.USER).size());
        assertEquals(1, mensagens.marcarLidasPelaEquipe(ContextoChat.DOACAO, 7, primeira.getId(), Role.USER));
        assertEquals(1, mensagens.buscarConversasNaoLidas(Role.USER).size());
        assertEquals(1, mensagens.marcarLidasPelaEquipe(ContextoChat.DOACAO, 7, posterior.getId(), Role.USER));
        assertTrue(mensagens.buscarConversasNaoLidas(Role.USER).isEmpty());
    }
}
