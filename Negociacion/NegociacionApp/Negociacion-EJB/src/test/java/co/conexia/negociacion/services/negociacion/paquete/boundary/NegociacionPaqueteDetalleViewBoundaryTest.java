package co.conexia.negociacion.services.negociacion.paquete.boundary;

import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.mockito.Mockito.*;

public class NegociacionPaqueteDetalleViewBoundaryTest {

    NegociacionPaqueteDetalleViewBoundary boundary;
    EntityManager em;

    @BeforeEach
    public void init() {
        // Mock
        em = mock(EntityManager.class);

        // Components
        boundary = spy(NegociacionPaqueteDetalleViewBoundary.class);

        // Mocks de los components
        boundary.em = em;
    }

    @Test
    public void testConsultarInformacionBasicaByPaqueteId() {
        // Dado
        Long sedeNegId = 1L;

        // Cuando
        TypedQuery<PaquetePortafolioDto> query = mock(TypedQuery.class);
        when(em.createNamedQuery("PaquetePortafolio.findDtoPaquetesByPortafolioId", PaquetePortafolioDto.class)).thenReturn(query);
        when(query.setParameter("paqueteId", sedeNegId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);
        this.boundary.consultarInformacionBasicaByPaqueteId(sedeNegId);

        // Debe
        verify(em).createNamedQuery("PaquetePortafolio.findDtoPaquetesByPortafolioId", PaquetePortafolioDto.class);
        verify(query).setParameter("paqueteId", sedeNegId);
        verify(query).getSingleResult();
    }


}
