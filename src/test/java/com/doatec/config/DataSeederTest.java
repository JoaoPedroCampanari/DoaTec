package com.doatec.config;

import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataSeederTest {
    private final PessoaRepository pessoas = mock(PessoaRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void criaSuperAdminComDadosConfigurados() {
        when(encoder.encode("senha-de-teste")).thenReturn("senha-criptografada");
        new DataSeeder(pessoas, encoder, "admin@exemplo.com", "senha-de-teste").run();

        ArgumentCaptor<Pessoa> captor = ArgumentCaptor.forClass(Pessoa.class);
        verify(pessoas).save(captor.capture());
        assertEquals("admin@exemplo.com", captor.getValue().getEmail());
        assertEquals("senha-criptografada", captor.getValue().getSenha());
        assertEquals(Role.SUPER_ADMIN, captor.getValue().getRole());
    }

    @Test
    void preservaSuperAdminExistente() {
        when(pessoas.existsByRole(Role.SUPER_ADMIN)).thenReturn(true);
        new DataSeeder(pessoas, encoder, "novo@exemplo.com", "outra-senha").run();
        verify(pessoas, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void naoCriaContaSemCredenciaisCompletas() {
        new DataSeeder(pessoas, encoder, "admin@exemplo.com", "").run();
        verify(pessoas, never()).save(any());
        verifyNoInteractions(encoder);
    }
}
