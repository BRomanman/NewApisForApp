package citas_service_nuevo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import citas_service_nuevo.model.Cita;
import citas_service_nuevo.repository.CitaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaService citaService;

    @Test
    @DisplayName("findAll delega en el repositorio")
    void findAll_returnsRepositoryData() {
        List<Cita> citas = List.of(new Cita());
        when(citaRepository.findAll()).thenReturn(citas);

        List<Cita> result = citaService.findAll();

        assertThat(result).isEqualTo(citas);
        verify(citaRepository).findAll();
    }

    @Test
    @DisplayName("findById retorna la entidad cuando existe")
    void findById_returnsEntity() {
        Cita cita = new Cita();
        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));

        Cita result = citaService.findById(5L);

        assertThat(result).isSameAs(cita);
        verify(citaRepository).findById(5L);
    }

    @Test
    @DisplayName("findById lanza EntityNotFoundException cuando no existe la cita")
    void findById_throwsWhenMissing() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citaService.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cita no encontrada");
        verify(citaRepository).findById(99L);
    }

    @Test
    @DisplayName("save delega en el repositorio")
    void save_delegatesToRepository() {
        Cita cita = new Cita();
        when(citaRepository.save(cita)).thenReturn(cita);

        Cita result = citaService.save(cita);

        assertThat(result).isSameAs(cita);
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("deleteById lanza EntityNotFoundException si no existe la cita")
    void deleteById_throwsWhenMissing() {
        when(citaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citaService.deleteById(1L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cita no encontrada");
        verify(citaRepository).findById(1L);
    }

    @Test
    @DisplayName("deleteById elimina la cita existente")
    void deleteById_deletesWhenFound() {
        Cita cita = new Cita();
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        citaService.deleteById(1L);

        verify(citaRepository).findById(1L);
        verify(citaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("findProximasByUsuario delega en el repositorio con la fecha actual")
    void findProximasByUsuario_delegatesToRepository() {
        when(citaRepository.findByIdUsuarioAndFechaCitaGreaterThanEqual(anyLong(), any(LocalDate.class)))
            .thenReturn(List.of());

        citaService.findProximasByUsuario(7L);

        verify(citaRepository).findByIdUsuarioAndFechaCitaGreaterThanEqual(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("findProximasByDoctor delega en el repositorio con la fecha actual")
    void findProximasByDoctor_delegatesToRepository() {
        when(citaRepository.findByIdDoctorAndFechaCitaGreaterThanEqual(anyLong(), any(LocalDate.class)))
            .thenReturn(List.of());

        citaService.findProximasByDoctor(3L);

        verify(citaRepository).findByIdDoctorAndFechaCitaGreaterThanEqual(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("findByDoctorAndFecha delega en el repositorio")
    void findByDoctorAndFecha_delegatesToRepository() {
        LocalDate fecha = LocalDate.of(2025, 2, 1);
        when(citaRepository.findByIdDoctorAndFechaCita(5L, fecha)).thenReturn(List.of());

        citaService.findByDoctorAndFecha(5L, fecha);

        verify(citaRepository).findByIdDoctorAndFechaCita(5L, fecha);
    }

    @Test
    @DisplayName("isDisponible lanza cuando la cita no existe")
    void isDisponible_throwsWhenMissing() {
        when(citaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citaService.isDisponible(2L))
            .isInstanceOf(EntityNotFoundException.class);
        verify(citaRepository).findById(2L);
    }

    @Test
    @DisplayName("isDisponible devuelve true cuando la cita está disponible")
    void isDisponible_returnsTrueWhenAvailable() {
        Cita cita = new Cita();
        cita.setDisponible(true);
        cita.setEstado("Disponible");
        when(citaRepository.findById(anyLong())).thenReturn(Optional.of(cita));

        boolean disponible = citaService.isDisponible(3L);

        assertThat(disponible).isTrue();
        verify(citaRepository).findById(3L);
    }

    @Test
    @DisplayName("cancelarCita libera la cita y actualiza los campos")
    void cancelarCita_updatesFields() {
        Cita cita = new Cita();
        cita.setEstado("Confirmado");
        cita.setIdUsuario(8L);
        cita.setDisponible(false);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita result = citaService.cancelarCita(1L);

        assertThat(result.getEstado()).isEqualTo("Disponible");
        assertThat(result.getIdUsuario()).isNull();
        assertThat(result.getDisponible()).isTrue();
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("reservar asigna usuario y marca la cita como confirmada cuando está disponible")
    void reservar_updatesFieldsWhenAvailable() {
        Cita cita = new Cita();
        cita.setDisponible(true);
        cita.setEstado("Disponible");
        when(citaRepository.findById(4L)).thenReturn(Optional.of(cita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita reservada = citaService.reservar(4L, 20L);

        assertThat(reservada.getIdUsuario()).isEqualTo(20L);
        assertThat(reservada.getEstado()).isEqualTo("Confirmado");
        assertThat(reservada.getDisponible()).isFalse();
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("reservar lanza IllegalStateException cuando la cita no está disponible")
    void reservar_throwsWhenNotAvailable() {
        Cita cita = new Cita();
        cita.setDisponible(false);
        cita.setEstado("Confirmado");
        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> citaService.reservar(5L, 1L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("isDisponible devuelve false cuando el estado no es Disponible")
    void isDisponible_returnsFalseWhenStateDiffers() {
        Cita cita = new Cita();
        cita.setDisponible(true);
        cita.setEstado("Confirmado");
        when(citaRepository.findById(6L)).thenReturn(Optional.of(cita));

        boolean disponible = citaService.isDisponible(6L);

        assertThat(disponible).isFalse();
    }
}
