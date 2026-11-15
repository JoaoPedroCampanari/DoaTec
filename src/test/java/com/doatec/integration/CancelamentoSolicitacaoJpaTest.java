package com.doatec.integration;

import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.repository.EquipamentoRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import com.doatec.service.AdminService;
import com.doatec.service.EmailService;
import com.doatec.service.NotificacaoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(AdminService.class)
class CancelamentoSolicitacaoJpaTest {
    @Autowired EntityManager em;
    @Autowired AdminService service;
    @Autowired EquipamentoRepository equipamentos;
    @Autowired SolicitacaoHardwareRepository solicitacoes;
    @MockBean NotificacaoService notificacoes;
    @MockBean EmailService email;

    @Test void cancelaELiberaReservaNoBanco() {
        DoadorPF admin = DoadorPF.builder().nome("Admin").email("admin.jpa@example.com").senha("hash").cpf("12345678901").role(Role.ADMIN).ativo(true).build();
        Aluno aluno = Aluno.builder().nome("Aluno").email("aluno.jpa@example.com").senha("hash").ra("1234567890123").role(Role.USER).ativo(true).build();
        em.persist(admin); em.persist(aluno);
        SolicitacaoHardware pedido = SolicitacaoHardware.builder().aluno(aluno).status(StatusSolicitacao.APROVADA).build();
        em.persist(pedido);
        Equipamento reservado = Equipamento.builder().tipo("Notebook").descricao("Teste").estadoConservacao(EstadoConservacao.BOM)
                .status(StatusEquipamento.RESERVADO).solicitacaoDestino(pedido).alunoDestinatario(aluno)
                .dataAtribuicao(java.time.LocalDateTime.now()).build();
        em.persist(reservado); em.flush();
        Integer idPedido = pedido.getId(), idAdmin = admin.getId(), idEquipamento = reservado.getId();
        em.clear();
        service.alterarStatusSolicitacao(idPedido, StatusSolicitacao.CANCELADA, idAdmin, null);
        em.flush(); em.clear();
        assertEquals(StatusSolicitacao.CANCELADA, solicitacoes.findById(idPedido).orElseThrow().getStatus());
        Equipamento resultado = equipamentos.findById(idEquipamento).orElseThrow();
        assertEquals(StatusEquipamento.DISPONIVEL, resultado.getStatus());
        assertNull(resultado.getSolicitacaoDestino());
        assertNull(resultado.getAlunoDestinatario());
        assertNull(resultado.getDataAtribuicao());
    }
}
