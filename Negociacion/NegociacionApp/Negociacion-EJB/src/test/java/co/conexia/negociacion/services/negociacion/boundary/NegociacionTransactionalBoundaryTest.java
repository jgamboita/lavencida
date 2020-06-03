package co.conexia.negociacion.services.negociacion.boundary;

import co.conexia.negociacion.services.negociacion.control.NegociacionControl;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.negociacion.InvitacionNegociacion;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.InvitacionNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class NegociacionTransactionalBoundaryTest {

    NegociacionTransactionalBoundary boundary;
    EntityManager em;
    NegociacionControl control;

    @BeforeEach
    public void init() {
        // Mock entityManager
        em = mock(EntityManager.class);
        control = mock(NegociacionControl.class);

        //componentes
        boundary = spy(new NegociacionTransactionalBoundary(em, control));
    }

    @Test
    public void testCrearNegociacion() {
        // Dado
        NegociacionDto negociacion = new NegociacionDto();
        negociacion.setTipoModalidadNegociacion(NegociacionModalidadEnum.EVENTO);
        negociacion.setRegimen(RegimenNegociacionEnum.CONTRIBUTIVO);
        negociacion.setComplejidad(ComplejidadNegociacionEnum.ALTA);
        negociacion.setPoblacion(100);
        negociacion.setPrestador(new PrestadorDto(5L));
        negociacion.setEstadoNegociacion(EstadoNegociacionEnum.EN_TRAMITE);
        negociacion.setFechaCreacion(new Date());
        negociacion.setUsuarioCreacionId(new Integer("5"));

        // Cuando
        boundary.crearNegociacion(negociacion);

        // Debe
        ArgumentCaptor<Negociacion> persisted = ArgumentCaptor.forClass(Negociacion.class);
        verify(em, atLeastOnce()).persist(persisted.capture());

        //se valida que la informacion sea la misma
        assertNotNull(persisted.getValue());
    }

    @Test
    public void testEliminarNegociacion() {
        // Dado
        Long negociacionId = 1L;

        // Cuando
        Query query = mock(Query.class);

        when(em.createNamedQuery("NegociacionRiaActividadMeta.borrarPorNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionPaqueteObservacion.deleteAllObservacionNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionPaqueteExclusion.deleteAllExclusionNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionPaqueteCausaRuptura.deleteAllCausaRupturaNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.deleteAllRequerimientoTecnicoNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionPaquete.deleteAllByNegociacion")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionMedicamento.deleteAllByNegociacion")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionProcedimiento.deleteAllByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionServicio.deleteByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionCapitulo.deleteByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionGrupoTerapeutico.deleteByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("NegociacionRiaRangoPoblacion.deletePoblacionAsociadaGrupoEtareo")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("AfiliadoNoProcesado.deleteByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("NegociacionRiaRangoPoblacion.borrarPorNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("NegociacionRia.borrarNegociacionRiaPorNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("SedeNegociacionCategoriaMedicamento.deleteByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNativeQuery("DELETE FROM contratacion.afiliado_no_procesado_grupo_etareo WHERE negociacion_id=:negociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNativeQuery("DELETE FROM contratacion.poblacion_ria_grupo_etareo WHERE negociacion_id=:negociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("Negociacion.deleteAfiliadosNegociacionPgpByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("NegociacionMunicipio.borrarMunicipiosNegociacion")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        
        when(em.createNamedQuery("ReglaNegociacion.eliminarReglasByNegociacionId")).thenReturn(query);
        when(query.setParameter("negociacionId", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(em.createNamedQuery("Negociacion.deleteNegociacionById")).thenReturn(query);
        when(query.setParameter("idNegociacion", negociacionId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        this.boundary.eliminarNegociacion(negociacionId);

        //Debe
        InOrder inOrder = inOrder(em, query);
        inOrder.verify(em)
                .createNamedQuery("NegociacionRiaActividadMeta.borrarPorNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionPaqueteObservacion.deleteAllObservacionNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionPaqueteExclusion.deleteAllExclusionNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionPaqueteCausaRuptura.deleteAllCausaRupturaNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.deleteAllRequerimientoTecnicoNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionPaquete.deleteAllByNegociacion");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionProcedimiento.deleteAllByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionServicio.deleteByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionCapitulo.deleteByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionGrupoTerapeutico.deleteByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("NegociacionRiaRangoPoblacion.deletePoblacionAsociadaGrupoEtareo");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("AfiliadoNoProcesado.deleteByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("NegociacionRiaRangoPoblacion.borrarPorNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("NegociacionRia.borrarNegociacionRiaPorNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("SedeNegociacionCategoriaMedicamento.deleteByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNativeQuery("DELETE FROM contratacion.afiliado_no_procesado_grupo_etareo WHERE negociacion_id=:negociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNativeQuery("DELETE FROM contratacion.poblacion_ria_grupo_etareo WHERE negociacion_id=:negociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("Negociacion.deleteAfiliadosNegociacionPgpByNegociacionId");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("NegociacionMunicipio.borrarMunicipiosNegociacion");
        inOrder.verify(query)
                .setParameter("negociacionId", negociacionId);
        inOrder.verify(query)
                .executeUpdate();

        inOrder.verify(em)
                .createNamedQuery("Negociacion.deleteNegociacionById");
        inOrder.verify(query)
                .setParameter("idNegociacion", negociacionId);
        inOrder.verify(query)
                .executeUpdate();
    }

    @Test
    public void testTerminarBaseNegociacion() {
        // Dado
        NegociacionDto negociacion = new NegociacionDto();
        negociacion.setId(1L);
        negociacion.setComplejidad(ComplejidadNegociacionEnum.ALTA);
        negociacion.setSedePrincipal(new SedePrestadorDto(1L));

        List<SedesNegociacionDto> sedes = new ArrayList<>();
        sedes.add(new SedesNegociacionDto(1L, 1L));

        // Cuando
        when(control.consultarSedesNegociacion(negociacion.getId())).thenReturn(sedes);
        this.boundary.terminarBaseNegociacion(negociacion, 1);

        // Debe
        ArgumentCaptor<Long> sedeId = ArgumentCaptor.forClass(Long.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<ArrayList<Integer>> complejidades = ArgumentCaptor.forClass((Class<ArrayList<Integer>>) (Class) ArrayList.class);
        ArgumentCaptor<NegociacionModalidadEnum> modalidad = ArgumentCaptor.forClass(NegociacionModalidadEnum.class);

//		verify(control, atLeastOnce()).consultarSedesNegociacion(sedeId.capture());
//		verify(control, atLeastOnce()).copiarMedicamentosNegociacion(sedeId.capture(), sedeId.capture());
//		verify(control, atLeastOnce()).copiarPaquetesNegociacion(sedeId.capture(), sedeId.capture());
//		verify(control, atLeastOnce()).copiarServiciosNegociacion(sedeId.capture(), sedeId.capture(), complejidades.capture(), modalidad.capture());
//		verify(control, atLeastOnce()).copiarProcedimientosNegociacion(sedeId.capture(), sedeId.capture(), complejidades.capture(), modalidad.capture());
//		verify(control, atLeastOnce()).copiarPaquetesMedicamento(sedeId.capture());
//		verify(control, atLeastOnce()).copiarPaquetesProcedimiento(sedeId.capture());
    }

    @Test
    public void testCrearInvitacionNegociacion() {
        // Dado
        InvitacionNegociacionDto invitacionDto = new InvitacionNegociacionDto();
        invitacionDto.setEstado(EstadoInvitacionNegociacionEnum.PENDIENTE_ENVIO);
        invitacionDto.setFechaCreacion(new Date());
        invitacionDto.setFechaHoraCita(new Date());
        invitacionDto.setMensaje("Hola Mundo");
        invitacionDto.setNegociacionId(1L);
        invitacionDto.setUserId(new Integer("1"));
        invitacionDto.setCorreo("jjoya@conexia.com");

        // Cuando
        this.boundary.crearInvitacionNegociacion(invitacionDto);

        // Debe
        ArgumentCaptor<InvitacionNegociacion> persisted = ArgumentCaptor
                .forClass(InvitacionNegociacion.class);
        verify(em, atLeastOnce()).persist(persisted.capture());

        EstadoInvitacionNegociacionEnum estado = persisted.getValue()
                .getEstado();
        assertNotNull(estado);
        assertEquals(estado, invitacionDto.getEstado());
    }

    @Test
    public void testActualizarInvitacionNegociacion() {
        // Dado
        InvitacionNegociacionDto invitacionDto = new InvitacionNegociacionDto();
        invitacionDto.setId(1L);
        invitacionDto.setEstado(EstadoInvitacionNegociacionEnum.PENDIENTE_ENVIO);
        invitacionDto.setFechaCreacion(new Date());
        invitacionDto.setFechaHoraCita(new Date());
        invitacionDto.setMensaje("Hola Mundo");
        invitacionDto.setNegociacionId(1L);
        invitacionDto.setUserId(new Integer("1"));
        invitacionDto.setCorreo("elPri@conexia.com");

        InvitacionNegociacion invitacion = new InvitacionNegociacion();

        // Cuando
        when(em.find(InvitacionNegociacion.class, invitacionDto.getId())).thenReturn(invitacion);
        this.boundary.actualizarInvitacionNegociacion(invitacionDto);

        // Debe
        ArgumentCaptor<InvitacionNegociacion> persisted = ArgumentCaptor
                .forClass(InvitacionNegociacion.class);
        verify(em, atLeastOnce()).merge(persisted.capture());

        String correo = persisted.getValue()
                .getCorreo();
        assertNotNull(correo);
        assertEquals(correo, invitacionDto.getCorreo());
    }

}
