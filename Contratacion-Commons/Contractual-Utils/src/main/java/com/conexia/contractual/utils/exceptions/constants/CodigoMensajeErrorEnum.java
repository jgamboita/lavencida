
package com.conexia.contractual.utils.exceptions.constants;

/**
 * Created by pbastidas on 12/11/14.
 */
public enum CodigoMensajeErrorEnum {

    SYSTEM_ERROR,

    CONT_LISTAR_SEDES_POR_PARAMETRIZAR,

    ACTUALIZAR_ORDINAL_MINUTA_DETALLE,

    LISTAR_MEDICAMENTOS_POR_PARAMETRIZAR,

    ERROR_GENERAL,

    /**
     * Enum de error para el almacenamiento del contrato.
     */

    NUMERO_CONTRATO,

    NUMERO_CONTRATO_PREFIJO,

    NUMERO_CONTRATO_REGIONAL,

    NUMERO_CONTRATO_MODALIDAD,

    NUMERO_CONTRATO_REGIMEN,

    NUMERO_CONTRATO_ANIO,

    NUMERO_CONTRATO_CONSECUTIVO,

    CONTRATO_MODALIDAD_NEGOCIACION,

    CONTRATO_CREACION,

    CONSULTA_LEGALIZACION_CONTRATO,

    GENERACION_MINUTA,

    GENERACION_PDF_MINUTA,

    MINUTA_NOMBRE,

    REFERENTE_NOMBRE,

    /**
     * Errores generales del sistema
     */

    NULL_POINTER_ERROR,

    SQL_ERROR,

	REQUEST_ERROR,

    GET_OTROSI_CURRENT_DATE,
    GET_OBJECT_EVENT_BY_NEGOTIATION_ID,
    GET_CHOOSED_TOWN_BY_NEGOTIATION_ID,
    HOW_MUCH_HIRING_REQUEST_TO_PARAMETERIZE,
    GET_HIRING_REQUEST_TO_PARAMETERIZATION,
    GET_HIRING_REQUEST_PARAMETERIZATION2,
    THERE_OTROSI_BY_NEGOTIATION_BY_ID,
    HOW_MUCH_HIRING_REQUEST_TO_VOBO,
    GET_HIRING_REQUEST_TO_VOBO,
    HAS_DRUGS,
    HAS_PROCEDURE,
    GET_NEGOTIATIONS_TRAY,
    CREATE_NEGOTIATION_OTROSI,
    FINISH_CREATE_NEGOTIATION_OTROSI,
    UPDATE_TECHNOLOGY_EXTENSION_DATE,
    FINISH_NEGOTIATION,
    ADD_OTROSI_CONTRACT_DATES,
    ADD_CONTRACT_DATES_NEGOTIATION,
    GET_END_DATE_PARENT_CONTRACT,
    VALIDATE_UNAUTHORIZED_NEGOTIATIONS_OTROSI,
    GET_TARRIF_ANNEX_DETAILS_TRAY,
    UPDATING_OTHERSI_AGGREGATE_TECHNOLOGY,
    GET_OTROSI_DRUG_TARRIF_ANNEX_BASE,
    GET_DRUG_TARRIF_ANNEX,
    GET_OTROSI_DRUG_TARRIF_ANNEX,
    GET_PGP_DRUG_TARRIF_ANNEX_BASE,
    GET_PROCEDURE_TARRIF_ANNEX,
    GET_OTROSI_PROCEDURE_TARRIF_ANNEX_BASE,
    GET_OTROSI_PROCEDURE_TARRIF_ANNEX,
    GET_PROCEDURE_PGP_TARRIF_ANNEX,
    GET_PACKAGE_TARRIF_ANNEX,
    GET_OTROSI_PACKAGE_TARRIF_ANNEX,
    GET_OTROSI_PACKAGE_TARRIF_ANNEX_BASE,
    GET_TECHNOLOGIES_PACKAGE_TARRIF_ANNEX,
    GET_PACKAGE_TARRIF_ANNEX_DETAILS,
    GET_PACKAGE_TARRIF_ANNEX_DYNAMIC,
    GET_BRIEFCASE_PACKAGE_TARRIF_ANNEX_DYNAMIC,
    GET_PACKAGE_NEGOTIATION_OBSERVATION,
    GET_BRIEFCASE_PACKAGE_OBSERVATION,
    GET_PACKAGE_NEGOTIATION_EXCLUSION,
    GET_BRIEFCASE_PACKAGE_EXCLUSION,
    GET_PACKAGE_NEGOTIATION_REQUIREMENT,
    GET_BRIEFCASE_PACKAGE_REQUIREMENT,
    GET_PACKAGE_NEGOTIATION_CAUSE_BREAKDOWN,
    GET_BRIEFCASE_PACKAGE_CAUSE_BREAKDOWN

    ;

	/**
	 * busca por id
	 * @param nameBuscado id buscado
	 * @return rol
	 */
	 public static CodigoMensajeErrorEnum findByName(final String nameBuscado) {
		 for (CodigoMensajeErrorEnum codigo : CodigoMensajeErrorEnum.values()) {
			 if (codigo.name().equals(nameBuscado)) {
				 return codigo;
			 }
		 }
		 return null;
	 }

}