package co.conexia.negociacion.services.negociacion.importarTecnologia.boundary;

import co.conexia.negociacion.services.negociacion.importarTecnologia.control.CapitaRiasModalidad;
import co.conexia.negociacion.services.negociacion.importarTecnologia.Modalidad;
import co.conexia.negociacion.services.negociacion.importarTecnologia.Tecnologia;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TecnologiaEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.tecnologias.ImportarTecnologiasTransactionalServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@Remote(ImportarTecnologiasTransactionalServiceRemote.class)
public class ImportarTecnologiaTransactionalBoundary implements ImportarTecnologiasTransactionalServiceRemote {

    private static final String CDM_PYTHON = "python";
    private static final String ARCHIVO_PYTHON = "xlsx2csv.py";
    private static final String ESPACIO = " ";
    private static final String RUTA_ALMACENAMIENTO_ARCHIVOS = "/cargue_archivos/medicamentos_rias_capita/";
    private static final String RUTA_SCRIPT_PYTHON = "/scripts_NO_BORRAR/";

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private Log logger;

    @Override
    public List<ErroresImportTecnologiasRIasCapitaDto> importarTecnologia(String nombreArchivo, NegociacionDto negociacion, Integer userId, NegociacionModalidadEnum modalidad, TecnologiaEnum tecnologia) throws ConexiaBusinessException {
        String nombreArchivoNuevo;
        try {
            nombreArchivoNuevo =convertirCSV(nombreArchivo);
            limpiarVaciosEnArchivo(nombreArchivoNuevo);
        } catch (IOException |InterruptedException e) {
            logger.error("No se pudo convertir el archivo a CSV", e);
            throw new ConexiaSystemException(PreContractualMensajeErrorEnum.SYSTEM_ERROR, "No se pudo leer el archivo del servidor.");
        }
        Modalidad md = crear(modalidad);
        Tecnologia tc = md.crearTecnologia(tecnologia);
        return convertirResultado(tc.importar(
                em,userId,negociacion.getId().intValue(),nombreArchivoNuevo),
                ErroresImportTecnologiasRIasCapitaDto.class
        );
    }

    private Modalidad crear(NegociacionModalidadEnum modalidad) {
        switch (modalidad) {
            case RIAS_CAPITA:
            default:
                return new CapitaRiasModalidad();

        }
    }

    private String convertirCSV(String nombreArchivo) throws IOException, InterruptedException {
        Path rutaCarpetaScriptsPython = Paths.get(System.getProperty("user.dir"), RUTA_SCRIPT_PYTHON).resolve(ARCHIVO_PYTHON);
        String nombreArchivoNuevo = "ME-" + new Date().getTime() + ".csv";
        String comandoPython = CDM_PYTHON.concat(ESPACIO).concat(rutaCarpetaScriptsPython.toString())
                .concat(ESPACIO).concat(String.valueOf(Paths.get(RUTA_ALMACENAMIENTO_ARCHIVOS).resolve(nombreArchivo)))
                .concat(ESPACIO).concat(String.valueOf(Paths.get(RUTA_ALMACENAMIENTO_ARCHIVOS).resolve(nombreArchivoNuevo)));

        Process p = Runtime.getRuntime().exec(comandoPython);
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException("Python command exited with " + exitCode);

        }
        return nombreArchivoNuevo;
    }

    private void limpiarVaciosEnArchivo(String nombreArchivo) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(new String[] {"sed","-i","/;;;;;/d",String.valueOf(Paths.get(RUTA_ALMACENAMIENTO_ARCHIVOS).resolve(nombreArchivo))});
        p.waitFor();
    }

    private <T> List<T> convertirResultado(List<?> listA, Class<T> clazz) {
        return listA.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

}