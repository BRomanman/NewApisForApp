package com.clinica.api.personal_service.security;

import com.clinica.api.personal_service.model.Administrador;
import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Usuario;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String nombre;
    private final String apellido;
    private final LocalDate fechaNacimiento;
    private final String telefono;
    private final String roleName;
    private final Long doctorId;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(
        Long id,
        String email,
        String password,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String telefono,
        String roleName,
        Long doctorId,
        Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.roleName = roleName;
        this.doctorId = doctorId;
        this.authorities = authorities;
    }

    public static CustomUserDetails fromUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario, "Usuario must not be null");
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "paciente";
        return new CustomUserDetails(
            usuario.getId(),
            usuario.getCorreo(),
            usuario.getContrasena(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getFechaNacimiento(),
            usuario.getTelefono(),
            rol,
            null,
            buildAuthorities(rol)
        );
    }

    public static CustomUserDetails fromDoctor(Doctor doctor) {
        Objects.requireNonNull(doctor, "Doctor must not be null");
        String rol = doctor.getRol() != null ? doctor.getRol().getNombre() : "doctor";
        return new CustomUserDetails(
            doctor.getId(),
            doctor.getCorreo(),
            doctor.getContrasena(),
            doctor.getNombre(),
            doctor.getApellido(),
            doctor.getFechaNacimiento(),
            doctor.getTelefono(),
            rol,
            doctor.getId(),
            buildAuthorities(rol)
        );
    }

    public static CustomUserDetails fromAdministrador(Administrador administrador) {
        Objects.requireNonNull(administrador, "Administrador must not be null");
        String rol = administrador.getRol() != null ? administrador.getRol().getNombre() : "administrador";
        return new CustomUserDetails(
            administrador.getId(),
            administrador.getCorreo(),
            administrador.getContrasena(),
            administrador.getNombre(),
            administrador.getApellido(),
            administrador.getFechaNacimiento(),
            administrador.getTelefono(),
            rol,
            null,
            buildAuthorities(rol)
        );
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(String role) {
        String normalized = role != null ? role.toUpperCase().replace(" ", "_") : "PACIENTE";
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + normalized));
    }

    public Long getUserId() {
        return id;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRoleName() {
        return roleName;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
