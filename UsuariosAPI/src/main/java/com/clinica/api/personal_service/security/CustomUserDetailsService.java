package com.clinica.api.personal_service.security;

import com.clinica.api.personal_service.repository.AdministradorRepository;
import com.clinica.api.personal_service.repository.DoctorRepository;
import com.clinica.api.personal_service.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final DoctorRepository doctorRepository;
    private final AdministradorRepository administradorRepository;

    public CustomUserDetailsService(
        UsuarioRepository usuarioRepository,
        DoctorRepository doctorRepository,
        AdministradorRepository administradorRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.doctorRepository = doctorRepository;
        this.administradorRepository = administradorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByCorreo(username)
            .map(CustomUserDetails::fromUsuario)
            .or(() -> doctorRepository.findByCorreoAndActivoTrue(username).map(CustomUserDetails::fromDoctor))
            .or(() -> administradorRepository.findByCorreoAndActivoTrue(username).map(CustomUserDetails::fromAdministrador))
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
