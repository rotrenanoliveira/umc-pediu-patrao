package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.PerfilUsuario;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {
    private final UsuarioRepository usuarios;
    public UsuarioAutenticadoService(UsuarioRepository usuarios) { this.usuarios = usuarios; }
    public Usuario atual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return usuarios.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não encontrado."));
    }
    public void exigirGerente() {
        PerfilUsuario perfil = atual().getPerfil();
        if (perfil != PerfilUsuario.GERENTE && perfil != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Esta operação exige perfil GERENTE ou ADMIN.");
        }
    }
}
