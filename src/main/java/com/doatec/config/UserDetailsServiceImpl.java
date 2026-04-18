package com.doatec.config;

import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Pessoa pessoa = pessoaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!pessoa.getAtivo()) {
            throw new UsernameNotFoundException("Usuário desativado: " + email);
        }

        return new User(
                pessoa.getEmail(),
                pessoa.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + pessoa.getRole().name()))
        );
    }
}