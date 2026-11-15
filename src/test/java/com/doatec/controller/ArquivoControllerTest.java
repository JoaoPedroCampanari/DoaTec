package com.doatec.controller;
import com.doatec.service.ArquivoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArquivoControllerTest {
    @TempDir Path diretorio;
    @Test void exigeAutenticacaoEAutorizacaoAntesDeEntregarArquivo() throws Exception {
        ArquivoService service = mock(ArquivoService.class);
        ArquivoController controller = new ArquivoController(service, diretorio.toString());
        String nome = "12345678-1234-1234-1234-123456789abc.png";
        Files.write(diretorio.resolve(nome), new byte[]{1,2,3});
        User usuario = new User("user@test.com", "senha", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(401, controller.ler(nome, null).getStatusCode().value());
        assertEquals(404, controller.ler(nome, usuario).getStatusCode().value());
        when(service.podeLer(nome, "user@test.com")).thenReturn(true);
        var resposta = controller.ler(nome, usuario);
        assertEquals(200, resposta.getStatusCode().value());
        assertEquals("no-store, private", resposta.getHeaders().getCacheControl());
        assertEquals(404, controller.ler("../segredo.txt", usuario).getStatusCode().value());
    }
}
