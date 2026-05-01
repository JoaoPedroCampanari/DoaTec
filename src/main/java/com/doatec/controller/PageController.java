package com.doatec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/index")
    public String indexAlt() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @GetMapping("/aluno")
    public String aluno() { return "aluno"; }

    @GetMapping("/donate")
    public String donate() { return "donate"; }

    @GetMapping("/perfil")
    public String perfil() { return "perfil"; }

    @GetMapping("/meus-pedidos")
    public String meusPedidos() { return "meus-pedidos"; }

    @GetMapping("/minhas-doacoes")
    public String minhasDoacoes() { return "minhas-doacoes"; }

    @GetMapping("/suporte")
    public String suporte() { return "suporte"; }

    @GetMapping("/sobre")
    public String sobre() { return "sobre"; }

    @GetMapping("/admin")
    public String admin() { return "admin"; }
}
