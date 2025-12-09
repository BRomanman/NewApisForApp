package com.clinica.api.personal_service.security;

import com.clinica.api.personal_service.repository.EmpleadoRepository;
import com.clinica.api.personal_service.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;

    public CustomUserDetailsService(
        UsuarioRepository usuarioRepository,
        EmpleadoRepository empleadoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByCorreo(username)
            .map(CustomUserDetails::fromUsuario)
            .or(() -> empleadoRepository.findByCorreoAndActivoTrue(username).map(CustomUserDetails::fromEmpleado))
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
