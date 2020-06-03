package co.conexia.negociacion.services.negociacion.sedes.boundary;

import co.conexia.negociacion.services.bandeja.negociacion.sedes.control.AreaCoberturaControl;
import com.conexia.contratacion.commons.constants.enums.AreaCoberturaTipoEnum;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.AreaCoberturaSedesDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class AreaCoberturaTransactionalBoundaryTest {

    AreaCoberturaTransactionalBoundary boundary;
    EntityManager em;
    AreaCoberturaControl control;

    @BeforeEach
    public void init() {
        // Mock
        em = mock(EntityManager.class);
        control = mock(AreaCoberturaControl.class);

        // Components
        boundary = spy(AreaCoberturaTransactionalBoundary.class);

        // Mocks de los componentes
        boundary.em = em;
        boundary.areaCoberturaControl = control;
    }

    @Test
    public void testCrearSedesNegociacionCoberturaPorDefecto() {
        // Dado
        SedesNegociacionDto sedesDto = new SedesNegociacionDto();

        // Cuando
        this.boundary.crearSedesNegociacionCoberturaPorDefecto(sedesDto, true, AreaCoberturaTipoEnum.MUNICIPIOS_MANUAL, null);

        //Debe
        verify(control).crearSedesNegociacionCoberturaPorDefecto(sedesDto, true, AreaCoberturaTipoEnum.MUNICIPIOS_MANUAL, null);
    }

    @Test
    public void testActualizarMunicipioCoberturaSedesNegociacion() {
        // Dado
        Integer municipioId = 1;
        Long sedesNegociacionId = 2L;
        Boolean seleccionado = true;

        // Cuando
        //this.boundary.actualizarMunicipioCoberturaSedesNegociacion(municipioId, sedesNegociacionId, seleccionado);

        // Debe
        //verify(control).actualizarMunicipioCoberturaSedesNegociacion(municipioId, sedesNegociacionId, seleccionado);
    }

    @Test
    public void testReplicarAreaCoberturaBySedeAndNegociacionId() {
        // Dado
        Long negociacionId = 1L;
        Long sedeNegociacionId = 2L;
        List<AreaCoberturaSedesDto> areas = new ArrayList<>();
        AreaCoberturaSedesDto dto = new AreaCoberturaSedesDto();
        dto.setSeleccionado(false);
        dto.setMunicipioDto(new MunicipioDto(1, null));
        areas.add(dto);
        dto = new AreaCoberturaSedesDto();
        dto.setSeleccionado(true);
        dto.setMunicipioDto(new MunicipioDto(1, null));
        areas.add(dto);

        // Cuando
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<ArrayList<Integer>> municipiosId = ArgumentCaptor.forClass((Class<ArrayList<Integer>>) (Class) ArrayList.class);
        ArgumentCaptor<Boolean> selected = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Long> negociacionCaptorId = ArgumentCaptor.forClass(Long.class);
        when(control.consultarAreaAPropagarBySedeNegociacionId(sedeNegociacionId)).thenReturn(areas);
        //this.boundary.replicarAreaCoberturaBySedeAndNegociacionId(negociacionId, sedeNegociacionId);

        // Debe
        //verify(control, times(2)).actualizarSeleccionByMunicipiosAndNegociacion(municipiosId.capture(), negociacionCaptorId.capture(), selected.capture());
        //Long negociacionIdMetodo = negociacionCaptorId.getValue();
        //assertEquals(negociacionId, negociacionIdMetodo);
    }

    @Test
    public void testActualizarSeleccionBySedeNegociacionId() {
        // Dado
        Long sedeNegociacionId = 1L;
        Boolean seleccionado = true;

        // Cuando
        this.boundary.actualizarSeleccionBySedeNegociacionId(sedeNegociacionId, seleccionado);

        // Debe
        verify(control).actualizarSeleccionBySedeNegociacionId(sedeNegociacionId, seleccionado);
    }

    @Test
    public void testEliminarSedeNegociacionByNegociacionIdAndSedePrestador() {
        // Dado
        Long negociacionId = 1L;
        Long sedePrestadorId = 5L;

        // Cuando
        Query query = mock(Query.class);
        when(em.createNamedQuery("SedesNegociacion.deleteByNegociacionIdAndSedePrestador")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.setParameter("sedePrestadorId", sedePrestadorId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        this.boundary.eliminarSedeNegociacionByNegociacionIdAndSedePrestador(negociacionId, sedePrestadorId);

        // Debe
        verify(em).createNamedQuery("SedesNegociacion.deleteByNegociacionIdAndSedePrestador");
        verify(query).setParameter("negociacionId", negociacionId);
        verify(query).setParameter("sedePrestadorId", sedePrestadorId);
        verify(query).executeUpdate();
    }

    @Test
    public void testActualizarSeleccionByMunicipiosAndSedesNegociacion() {
        // Dado
        List<Integer> municipiosId = new ArrayList<>();
        Long sedesNegociacionId = 10L;
        boolean seleccionado = true;

        // Cuando
        this.boundary.actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosId, sedesNegociacionId, seleccionado);

        // Debe
        verify(control).actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosId, sedesNegociacionId, seleccionado);
    }

}
