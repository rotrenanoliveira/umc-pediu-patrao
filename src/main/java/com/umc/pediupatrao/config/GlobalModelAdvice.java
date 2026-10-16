package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.PerfilUsuario;
import com.umc.pediupatrao.service.UsuarioAutenticadoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public GlobalModelAdvice(UsuarioAutenticadoService usuarioAutenticadoService) {
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @ModelAttribute("ehAdmin")
    public boolean ehAdmin() {
        return temPerfil(PerfilUsuario.ADMIN);
    }

    @ModelAttribute("ehGerente")
    public boolean ehGerente() {
        return temPerfil(PerfilUsuario.ADMIN) || temPerfil(PerfilUsuario.GERENTE);
    }

    private boolean temPerfil(PerfilUsuario esperado) {
        try {
            return usuarioAutenticadoService.atual().getPerfil() == esperado;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }
}
