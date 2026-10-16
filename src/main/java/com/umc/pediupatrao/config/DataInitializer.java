package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.PerfilUsuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo) {
        return args -> {
            BCryptPasswordEncoder encoder = encoder();
            garantirUsuario(repo, encoder, "admin", PerfilUsuario.ADMIN);
            garantirUsuario(repo, encoder, "gerente", PerfilUsuario.GERENTE);
            garantirUsuario(repo, encoder, "atendente", PerfilUsuario.ATENDENTE);
        };
    }

    private BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    private void garantirUsuario(UsuarioRepository repo, BCryptPasswordEncoder encoder,
            String username, PerfilUsuario perfil) {
        repo.findByUsername(username).ifPresentOrElse(usuario -> {
            if (usuario.getPerfil() != perfil) {
                usuario.setPerfil(perfil);
                repo.save(usuario);
                System.out.println("Perfil de '" + username + "' corrigido para " + perfil);
            }
        }, () -> {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(encoder.encode("teste123"));
            usuario.setPerfil(perfil);
            repo.save(usuario);
            System.out.println("Usuário '" + username + "' criado com perfil " + perfil);
        });
    }
}