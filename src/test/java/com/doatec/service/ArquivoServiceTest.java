package com.doatec.service;

import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Role;
import com.doatec.model.arquivo.ArquivoUpload;
import com.doatec.model.donation.Doacao;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArquivoServiceTest {
    ArquivoUploadRepository arquivos = mock(ArquivoUploadRepository.class);
    PessoaRepository pessoas = mock(PessoaRepository.class);
    DoacaoRepository doacoes = mock(DoacaoRepository.class);
    SolicitacaoHardwareRepository solicitacoes = mock(SolicitacaoHardwareRepository.class);
    ArquivoService service = new ArquivoService(arquivos, pessoas, doacoes, solicitacoes);
    DoadorPF dono, outro, admin, superadmin;
    String nome = "12345678-1234-1234-1234-123456789abc.png";
    @BeforeEach void setup() {
        dono = pessoa(1, Role.USER); outro = pessoa(2, Role.USER);
        admin = pessoa(3, Role.ADMIN); superadmin = pessoa(4, Role.SUPER_ADMIN);
    }
    private DoadorPF pessoa(int id, Role role) { DoadorPF p = new DoadorPF("123"); p.setId(id); p.setRole(role); return p; }
    @Test void guardaNomeOriginalSemCaminho() {
        when(pessoas.findByEmail("dono")).thenReturn(Optional.of(dono));
        service.registrar(nome, "dono", "image/png", "C:\\pasta\\comprovante.png");
        var captor = org.mockito.ArgumentCaptor.forClass(ArquivoUpload.class);
        verify(arquivos).saveAndFlush(captor.capture());
        assertEquals("comprovante.png", captor.getValue().getNomeOriginal());
    }
    @Test void donoEAdministradoresLeemOutroUsuarioNao() {
        when(arquivos.findById(nome)).thenReturn(Optional.of(new ArquivoUpload(nome, dono, "image/png")));
        when(pessoas.findByEmail("dono")).thenReturn(Optional.of(dono));
        when(pessoas.findByEmail("outro")).thenReturn(Optional.of(outro));
        when(pessoas.findByEmail("admin")).thenReturn(Optional.of(admin));
        when(pessoas.findByEmail("super")).thenReturn(Optional.of(superadmin));
        assertTrue(service.podeLer(nome, "dono"));
        assertFalse(service.podeLer(nome, "outro"));
        assertTrue(service.podeLer(nome, "admin"));
        assertTrue(service.podeLer(nome, "super"));
    }
    @Test void naoPermiteVincularArquivoDeOutraPessoaOuUrlExterna() {
        when(arquivos.findById(nome)).thenReturn(Optional.of(new ArquivoUpload(nome, dono, "image/png")));
        assertDoesNotThrow(() -> service.validarVinculo("/uploads/" + nome, dono));
        assertThrows(ResponseStatusException.class, () -> service.validarVinculo("/uploads/" + nome, outro));
        assertThrows(ResponseStatusException.class, () -> service.validarVinculo("https://externo.test/foto.png", dono));
    }
    @Test void arquivosAntigosSomenteParaDonoDoRegistro() {
        String url = "/uploads/" + nome;
        when(arquivos.findById(nome)).thenReturn(Optional.empty());
        when(pessoas.findByEmail("dono")).thenReturn(Optional.of(dono));
        when(pessoas.findByEmail("outro")).thenReturn(Optional.of(outro));
        when(doacoes.findByDoadorId(1)).thenReturn(List.of(Doacao.builder().urlFoto("/uploads/outro.jpg, " + url).build()));
        assertTrue(service.podeLer(nome, "dono"));
        assertFalse(service.podeLer(nome, "outro"));
        assertDoesNotThrow(() -> service.validarVinculo(url, dono));
    }
    @Test void comprovanteAntigoPertenceAoAluno() {
        when(arquivos.findById(nome)).thenReturn(Optional.empty());
        when(pessoas.findByEmail("dono")).thenReturn(Optional.of(dono));
        when(solicitacoes.findByAlunoId(1)).thenReturn(List.of(SolicitacaoHardware.builder().urlComprovante("/uploads/" + nome).build()));
        assertTrue(service.podeLer(nome, "dono"));
    }
}
