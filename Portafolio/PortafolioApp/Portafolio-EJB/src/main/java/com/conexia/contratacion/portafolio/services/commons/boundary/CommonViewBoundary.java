package com.conexia.contratacion.portafolio.services.commons.boundary;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.dto.DescriptivoDto;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.maestros.ClasificacionPrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.portafolio.definitions.commons.CommonViewServiceRemote;
import com.conexia.contratacion.portafolio.services.commons.controls.FileRepositoryControl;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author Andrés Mise Olivera
 */
@Stateless
@Remote(CommonViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CommonViewBoundary implements CommonViewServiceRemote {

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private FileRepositoryControl repositoryControl;

	@Override
	public List<ClasificacionPrestadorDto> listarClasificacionesPrestador() {
		return em.createNamedQuery("ClasificacionPrestador.findAll",
				ClasificacionPrestadorDto.class).getResultList();
	}

	@Override
	public List<GrupoInsumoDto> listarGruposInsumoEmpresarial() {
		return em.createNamedQuery("GrupoInsumo.findEmpresarial",
				GrupoInsumoDto.class).getResultList();
	}

	@Override
	public List<TipoIdentificacionDto> listarTiposIdentificacion() {
		return em.createNamedQuery("TipoIdentificacion.findAll",
				TipoIdentificacionDto.class).getResultList();
	}

	/**
	 * @see CommonViewServiceRemote#obtenerDocumentoAdjunto(String, String)
	 */
	@Override
	public DocumentoAdjuntoDto obtenerDocumentoAdjunto(
			DocumentoAdjuntoDto documentoAdjunto)
			throws ConexiaBusinessException {
		byte[] adjuntoByteStream = repositoryControl.obteneArchivo(
				documentoAdjunto.getNombreRepositorio(),
				(documentoAdjunto.getRepositorio() != null ? documentoAdjunto
						.getRepositorio() : repositoryControl
						.getRutaRepositorioFromBD()));

		documentoAdjunto.setBytesStream(adjuntoByteStream);
		return documentoAdjunto;
	}

	/**
	 * @see CommonViewServiceRemote#crearAdjunto(List)
	 */
	public DocumentoAdjuntoDto crearAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException {

		String repositorio = repositoryControl.getRutaRepositorioFromBD();
		String nombreArchivoRepositorio = repositoryControl.nuevoArchivo(
				adjunto.getBytesStream(), repositorio);

		adjunto.setNombreRepositorio(nombreArchivoRepositorio);
		adjunto.setRepositorio(repositorio);

		return adjunto;
	}

	/**
	 * @see {@link CommonViewServiceRemote#eliminarAdjunto(DocumentoAdjuntoDto)}
	 */
	public void eliminarAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException {

		repositoryControl.eliminarArchivo(adjunto.getNombreRepositorio(),
				(adjunto.getRepositorio() != null ? adjunto.getRepositorio()
						: repositoryControl.getRutaRepositorioFromBD()));
	}

	/**
	 *  @see {@link CommonViewServiceRemote#listarDepartamentos()}
	 */
	public List<DepartamentoDto> listarDepartamentos()
			throws ConexiaBusinessException {

		return em.createNamedQuery("Departamento.findAll", DepartamentoDto.class)
					.getResultList();
	}

	/**
	 * @see {@link CommonViewServiceRemote#listarMunicipios(DepartamentoDto)}
	 */
	public List<MunicipioDto> listarMunicipios(DepartamentoDto departamento) {
		return em.createNamedQuery("Municipio.listarByDepartamentoId", MunicipioDto.class)
					.setParameter("idDepartamento", departamento.getId())
					.getResultList();
	}
	
	
	@Override
	public List<DescriptivoDto> listarZonas() throws ConexiaBusinessException {
		return em.createNamedQuery("Zona.listarTodas", DescriptivoDto.class)
					.getResultList();
	}
}
