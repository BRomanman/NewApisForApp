package com.clinica.api.personal_service.security;

import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.EmpleadoTipo;
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
    private final EmpleadoTipo tipo;
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
        EmpleadoTipo tipo,
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
        this.tipo = tipo;
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
            null,
            buildAuthorities(rol)
        );
    }

    public static CustomUserDetails fromEmpleado(Empleado empleado) {
        Objects.requireNonNull(empleado, "Empleado must not be null");
        String rol = empleado.getRol() != null ? empleado.getRol().getNombre() : "doctor";
        return new CustomUserDetails(
            empleado.getId(),
            empleado.getCorreo(),
            empleado.getContrasena(),
            empleado.getNombre(),
            empleado.getApellido(),
            empleado.getFechaNacimiento(),
            empleado.getTelefono(),
            rol,
            empleado.getTipo(),
            empleado.getTipo() == EmpleadoTipo.DOCTOR ? empleado.getId() : null,
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

    public EmpleadoTipo getTipo() {
        return tipo;
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
