package com.conexia.contratacion.portafolio.services.transactional.prestador.controls;

import com.conexia.contratacion.portafolio.services.commons.controls.FileRepositoryControl;
import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contractual.model.contratacion.DocumentacionPrestador;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Andrés Mise Olivera
 */
public class PrestadorControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private FileRepositoryControl repositoryControl;

    public Integer actualizarPrestador(PrestadorDto prestador) {
        return this.em.createNamedQuery("Prestador.updateBasicDataById")
                .setParameter("clasePrestadorId", prestador.getClasePrestadorEnum().getId())
                .setParameter("tipoIdentificacionId", prestador.getTipoIdentificacion().getId())
                .setParameter("numeroDocumento", prestador.getNumeroDocumento())
                .setParameter("clasificacionPrestadorId", prestador.getClasificacionPrestador().getId())
                .setParameter("nombre", prestador.getNombre())
                .setParameter("correoElectronico", prestador.getCorreoElectronico())
                .setParameter("nombreContactoAdministrativo", prestador.getContactoAdministrativo())
                .setParameter("celularContactoAdministrativo", prestador.getCelularContactoAdministrativo())
                .setParameter("telFijoContactoAdministrativo", prestador.getTelefonoContactoAdministrativo())
                .setParameter("sitioWeb", prestador.getSitioWeb())
                .setParameter("mesesVigencia", prestador.getMesesVigencia())
                .setParameter("estadoPrestador", prestador.getEstadoPrestador())
                .setParameter("fechaInicioVigencia", prestador.getFechaInicioVigencia())
                .setParameter("prestadorId", prestador.getId())
                .setParameter("empresaSocialEstado", prestador.getEmpresaSocialEstado())                
                .executeUpdate();
    }

    public void asociarArchivos(PrestadorDto prestador, Map<TipoDocumentoEnum, Object[]> files) {
        DocumentacionPrestador documentacionPrestador;
        for (Map.Entry<TipoDocumentoEnum, Object[]> entry : files.entrySet()) {
            Object[] value = entry.getValue();
            documentacionPrestador = new DocumentacionPrestador(null, 
                    (String) value[2], (String) value[0], 
                    repositoryControl.nuevoArchivo((byte[]) value[1],
                    repositoryControl.getRutaRepositorioFromBD()),
                    (new Prestador(prestador.getId())), entry.getKey());
            this.em.persist(documentacionPrestador);
        }
    }

    public void guardarPrestador(PrestadorDto prestador) {
        List<Long> ids = new ArrayList<>(0);
        prestador.getGrupos().stream().forEach((gi) -> {
            ids.add(gi.getId());
        });
        Prestador p = new Prestador(prestador.getClasePrestadorEnum().getId(),
                prestador.getTipoIdentificacion().getId(),
                prestador.getNumeroDocumento(),
                prestador.getClasificacionPrestador().getId(),
                prestador.getNombre(),
                prestador.getCorreoElectronico(),
                prestador.getContactoAdministrativo(),
                prestador.getCelularContactoAdministrativo(),
                prestador.getTelefonoContactoAdministrativo(),
                prestador.getSitioWeb(),
                prestador.getEstadoPrestador(),
                prestador.getMesesVigencia(), ids, 
                prestador.getEmpresaSocialEstado());
        this.em.persist(p);
        prestador.setId(p.getId());
    }

}
