package com.doatec.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para acesso ao token CSRF.
 */
@RestController
public class CsrfController {
    /**
     * Retorna o token CSRF usado nas requisições.
     */
    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
