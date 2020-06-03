package co.conexia.negociacion.services.negociacion.importarTecnologia.control;

import co.conexia.negociacion.services.negociacion.importarTecnologia.Modalidad;
import co.conexia.negociacion.services.negociacion.importarTecnologia.Tecnologia;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionRiasCapitaEnum;
import com.conexia.contractual.model.contratacion.importar.ErrorImportarMedicamentosRiasCapita;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MedicamentoModalidad implements Tecnologia {

    @Inject
    private Log logger;

    private Modalidad modalidad;

    public MedicamentoModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public MedicamentoModalidad() {
    }

    @Override
    public List<?> importar(EntityManager em, int usuarioId, int negociacionId, String nombreArchivo) throws ConexiaBusinessException {
        if ((CapitaRiasModalidad.class).isInstance(modalidad)) {
            return ejecutarProcedimiento(em, usuarioId, negociacionId, nombreArchivo);
        }
        return new ArrayList<>();
    }

    private List<ErroresImportTecnologiasRIasCapitaDto> ejecutarProcedimiento(EntityManager em, int usuarioId, int negociacionId, String nombreArchivo) throws ConexiaBusinessException {
        try {
            List resultados = em.createNamedStoredProcedureQuery(ErrorImportarMedicamentosRiasCapita.FN_IMPORTAR_MEDICAMENTOS_RIAS_CAPITA)
                    .setParameter(ErrorImportarMedicamentosRiasCapita.PARAM_NOMBRE_ARCHIVO, nombreArchivo)
                    .setParameter(ErrorImportarMedicamentosRiasCapita.PARAM_NEGOCIACION_ID, negociacionId)
                    .setParameter(ErrorImportarMedicamentosRiasCapita.PARAM_USUARIO_ID, usuarioId)
                    .getResultList();
            List<ErrorImportarMedicamentosRiasCapita> listaErroreRias = convertirResultado(resultados, ErrorImportarMedicamentosRiasCapita.class);
            return getErroresImportTecnologiasRIasCapitaDtos(listaErroreRias);
        } catch (Exception e) {
            logger.error("No se ejecuto la función fn_importar_medicamentos_rias_capita",e);
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.ARCHIVO_NO_ENCONTRADO);
        }
    }

    private static <T> List<T> convertirResultado(List<?> listA, Class<T> clazz) {
        return listA.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    private List<ErroresImportTecnologiasRIasCapitaDto> getErroresImportTecnologiasRIasCapitaDtos(List<ErrorImportarMedicamentosRiasCapita> listaErroreRias) {
        List<ErroresImportTecnologiasRIasCapitaDto> lista = new ArrayList<>();
        listaErroreRias.forEach(a -> {
            String[] parts = a.getMensaje().split("-");
            Arrays.stream(parts)
                    .filter(part -> !part.isEmpty())
                    .map(part -> new ErroresImportTecnologiasRIasCapitaDto(part, (ErrorTecnologiasNegociacionRiasCapitaEnum.findByCodigo(part)).getMensaje(),
                            a.getLinea() + 1, "", a.getCodigo(), a.getRuta(), a.getTema(), a.getRangoPoblacion()))
                    .forEachOrdered(lista::add);
        });
        return lista;
    }

}
