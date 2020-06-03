package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaConsultaContratoDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionConsultaContratoDto;
import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.logfactory.Log;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EliminarNegociacionControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private Log log;

    public String getNegociacionHabilitarContratoMaximoTiempoVencido() {
        try{
            String sql = "SELECT valor FROM configuracion.properties WHERE clave = :clave";
            return (String) em
                    .createNativeQuery(sql)
                    .setParameter("clave","negociacion_habilitar_contrato_maximo_tiempo_vencido")
                    .getSingleResult();
        }catch(Exception e){
            log.error("No existe el parametro negociacion_habilitar_contrato_maximo_tiempo_vencido", e);
            return "0 DAY";
        }
    }

    public List<NegociacionConsultaContratoDto> consultarContratos(FiltroBandejaConsultaContratoDto dto) {
        List<NegociacionConsultaContratoDto> negociacionConsultaContratoDto = new ArrayList<>();
        StringBuilder consulta = new StringBuilder();

        try {
            String negociacionHabilitarContratoMaximoTiempoVencido = getNegociacionHabilitarContratoMaximoTiempoVencido();

            consulta.append("select \n"
                    + "consulta.contrato_id,\n"
                    + "consulta.solicitud_id,\n"
                    + "consulta.numero_negociacion,\n"
                    + "consulta.numero_contrato,\n"
                    + "consulta.razon_social,\n"
                    + "consulta.nit,\n"
                    + "consulta.tipo_contrato,\n"
                    + "consulta.regional,\n"
                    + " consulta.modalidad,\n"
                    + "consulta.regimen,\n"
                    + "consulta.tipo_subsidiado subsidiado,\n"
                    + "consulta.fecha_inicio,\n"
                    + "consulta.fecha_fin, "
                    + "consulta.responsable_creacion,\n"
                    + "consulta.fecha_creacion,\n"
                    + "consulta.estado_legalizacion_id estado_legalizacion_id,\n"
                    + "consulta.estado_contrato,\n"
                    + "consulta.fecha_fin,\n"
                    + "consulta.negociacion_borrada,\n"
                    + "consulta.contrato_borrado,\n"
                    + "consulta.tipo_identificacion,\n"
                    + "count(distinct sp.id) as sedes_contratadas\n"
                    + "from(\n"
                    + "   select\n"
                    + "   sc.id solicitud_id,\n"
                    + "         n.id numero_negociacion,\n"
                    + "          c.numero_contrato,\n"
                    + "          case when sc.prestador_id is null then (select nombre from contratacion.prestador where id = n.prestador_id) \n"
                    + "               else (select nombre from contratacion.prestador where id = sc.prestador_id)  \n"
                    + "               end razon_social,\n"
                    + "           case when sc.prestador_id is null then (select numero_documento from contratacion.prestador where id = n.prestador_id) \n"
                    + "               else (select numero_documento from contratacion.prestador where id = sc.prestador_id)  \n"
                    + "               end nit,\n"
                    + "               \n"
                    + "          case when sc.prestador_id is null then  n.prestador_id\n"
                    + "               else sc.prestador_id \n"
                    + "               end id_prestador,\n"
                    + "          c.tipo_contrato,\n"
                    + "          c.id contrato_id,\n"
                    + "         (select descripcion from maestros.regional where id = c.regional_id) regional,\n"
                    + "          n.tipo_modalidad_negociacion modalidad,\n"
                    + "       n.tipo_negociacion,\n"
                    + "       n.estado_negociacion,\n"
                    + "       n.regimen,\n"
                    + "       c.tipo_subsidiado,\n"
                    + "       c.fecha_inicio,\n"
                    + "       case when c.user_id is null then (select name from \"security\".\"user\" where id = n.user_id)\n"
                    + "       else (select name from \"security\".\"user\" where id = c.user_id) \n"
                    + "       end responsable_creacion,\n"
                    + "       sc.estado_legalizacion_id,\n"
                    + "       c.fecha_creacion,\n"
                    + "        CASE \n"
                    + "   WHEN c.fecha_fin >= now()  THEN 'VIGENTE'\n"
                    + "   when c.fecha_fin <= now()  THEN 'VENCIDO'\n"
                    + "   when c.fecha_fin is null then null\n"
                    + "   END as estado_contrato,\n"
                    + "          sc.id contrato_solicitud_id,\n"
                    + "          c.fecha_fin,\n"
                    + "          n.deleted negociacion_borrada,\n"
                    + "          C.deleted contrato_borrado,\n"
                    + "           case when sc.prestador_id is null then (select tipo_identificacion_id from contratacion.prestador where id = n.prestador_id) \n"
                    + "               else (select tipo_identificacion_id from contratacion.prestador where id = sc.prestador_id)  \n"
                    + "               end tipo_identificacion\n"
                    + "   from contratacion.negociacion n\n"
                    + "  left join contratacion.solicitud_contratacion sc on   n.id = sc.negociacion_id\n"
                    + "  left join contratacion.contrato c on c.solicitud_contratacion_id = sc.id) \n"
                    + "  consulta "
                    + "  inner join contratacion.sede_prestador sp on sp.prestador_id = consulta.id_prestador\n"
                    + " where (\n"
                    + " consulta.estado_contrato = 'VIGENTE'  or consulta.estado_contrato is null \n"
                    + " or (consulta.estado_contrato = 'VENCIDO' and consulta.estado_legalizacion_id = 'LEGALIZADA' and consulta.fecha_fin >= cast((now() - interval '" + negociacionHabilitarContratoMaximoTiempoVencido +"') as date) ) \n"
                    + ") \n"
                    + " and ((consulta.contrato_borrado is null  or consulta.contrato_borrado = :deleted) and (consulta.negociacion_borrada is null  or consulta.negociacion_borrada = :deleted))");
            if (!dto.getNumeroContrato().isEmpty()) {
                consulta.append(" and consulta.numero_contrato =:numeroContrato ");
            }
            if (Objects.nonNull(dto.getRazonSocial())) {
                consulta.append(" and consulta.razon_social ilike :razonSocial ");
            }
            if (Objects.nonNull(dto.getNumeroDocumento())) {
                consulta.append(" and  consulta.nit =:identificacion ");
            }
            if (Objects.nonNull(dto.getNumeroNegociacion())) {
                consulta.append(" and consulta.numero_negociacion =:nro_negociacion ");
            }
            if (Objects.nonNull(dto.getModalidad())) {
                consulta.append(" and consulta.modalidad =:tipoModalidad ");
            }
            if (Objects.nonNull(dto.getTipoIdentificacionDto())) {
                consulta.append(" and consulta.tipo_identificacion =:tipoIdentificacion ");
            }
            if (EstadoLegalizacionEnum.CONTRATO_SIN_VB.name().equals(dto.getEstadoContrato())
                    || EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR.name().equals(dto.getEstadoContrato())
                    || EstadoLegalizacionEnum.LEGALIZADA.name().equals(dto.getEstadoContrato())) {
                consulta.append(" and consulta.estado_legalizacion_id =:estadoContrato ");
            } else {
                consulta.append("and (consulta.estado_legalizacion_id in('" + EstadoLegalizacionEnum.CONTRATO_SIN_VB.name() + "','" + EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR.name() + "','" + EstadoLegalizacionEnum.LEGALIZADA.name() + "') or consulta.estado_legalizacion_id is null )");
            }
            consulta.append("group by \n"
                    + " 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21");

            Query query = em.createNativeQuery(consulta.toString(), "Negociacion.habilitarEliminarNegociacionMappging");

             query.setParameter("deleted", false);

            if (!dto.getNumeroContrato().isEmpty()) {
                query.setParameter("numeroContrato", dto.getNumeroContrato());
            }
            if (Objects.nonNull(dto.getRazonSocial())) {
                query.setParameter("razonSocial", "%"+ dto.getRazonSocial()+ "%");
            }
            if (Objects.nonNull(dto.getNumeroDocumento())) {
                query.setParameter("identificacion", dto.getNumeroDocumento());
            }
            if (Objects.nonNull(dto.getNumeroNegociacion())) {
                query.setParameter("nro_negociacion", dto.getNumeroNegociacion());
            }
            if (Objects.nonNull(dto.getModalidad())) {
                query.setParameter("tipoModalidad", dto.getModalidad());
            }
            if (Objects.nonNull(dto.getTipoIdentificacionDto())) {
                query.setParameter("tipoIdentificacion", dto.getTipoIdentificacionDto().getId());
            }
            if (EstadoLegalizacionEnum.CONTRATO_SIN_VB.name().equals(dto.getEstadoContrato())
                    || EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR.name().equals(dto.getEstadoContrato())
                    || EstadoLegalizacionEnum.LEGALIZADA.name().equals(dto.getEstadoContrato())) {
                query.setParameter("estadoContrato", dto.getEstadoContrato().toUpperCase());
            }
            negociacionConsultaContratoDto = query.getResultList();

            if (negociacionConsultaContratoDto.isEmpty()) {
                if (!dto.getNumeroContrato().isEmpty()) {
                    negociacionConsultaContratoDto = consultarContratosUrgencias(dto);
                }
            }

        } catch (Exception e) {
            log.error("Error en la clase EliminarNegociacionControl metodo consultarContratos()", e);
        }
        return negociacionConsultaContratoDto;
    }

    public List<NegociacionConsultaContratoDto> consultarContratosUrgencias(FiltroBandejaConsultaContratoDto dto) {
        List<NegociacionConsultaContratoDto> negociacionConsultaContratoDto = new ArrayList<>();
        StringBuilder consulta = new StringBuilder();
        try {
            consulta.append("  select \n"
                    + "  cu.id contrato_id,\n"
                    + "  cu.numero_contrato_urgencias numero_contrato,\n"
                    + "  cu.regimen,\n"
                    + "   p.numero_documento,\n"
                    + "   p.tipo_identificacion_id, "
                    + "   cu.tipo_contrato,\n"
                    + "   p.nombre,\n"
                    + "   r.descripcion,\n"
                    + "   cu.tipo_subsidiado,\n"
                    + "   cu.fecha_inicio,\n"
                    + "   u.\"name\",\n"
                    + "   cu.fecha_creacion,\n"
                    + "   count(distinct sp.id) as sedes_contratadas,\n"
                    + "  CASE \n"
                    + "          WHEN cu.fecha_fin >= now()  THEN 'VIGENTE' \n"
                    + "          ELSE 'VENCIDO' \n"
                    + "      END as estado_contrato \n"
                    + "  from contratacion.contrato_urgencias cu\n"
                    + "  inner join contratacion.prestador p on cu.prestador_id = p.id\n"
                    + "  inner join  maestros.regional r on cu.regional_id = r.id\n"
                    + "  inner join \"security\".\"user\" u  on cu.user_id = u.id\n"
                    + "  inner join contratacion.sede_prestador sp on sp.prestador_id = p.id\n"
                    + "  where \n"
                    + "  CASE \n"
                    + "      WHEN cu.fecha_fin >= now()  THEN 'VIGENTE' \n"
                    + "       ELSE 'VENCIDO' \n"
                    + "      end = 'VIGENTE' \n "
                    + " and case when cu.deleted is null or cu.deleted = false then false end = :deleted");
            if (!dto.getNumeroContrato().isEmpty()) {
                consulta.append(" and cu.numero_contrato_urgencias =:numero_contrato_urgencias ");
            }
            if (!dto.getRazonSocial().isEmpty()) {
                consulta.append(" and p.nombre ilike :razonSocial ");
            }
            if (!dto.getNumeroDocumento().isEmpty()) {
                consulta.append(" and  p.numero_documento =:identificacion ");
            }
            if (dto.getTipoIdentificacionDto() != null) {
                consulta.append(" and p.tipo_identificacion_id =:tipoIdentificacion ");
            }
            consulta.append("group by\n"
                    + "    cu.id,\n"
                    + "    cu.numero_contrato_urgencias,\n"
                    + "     cu.regimen,\n"
                    + "     p.numero_documento,\n"
                    + "   cu.tipo_contrato,\n"
                    + "   p.nombre,\n"
                    + "   r.descripcion,\n"
                    + "   cu.tipo_subsidiado,\n"
                    + "   cu.fecha_inicio,\n"
                    + "   u.\"name\",\n"
                    + "   cu.fecha_creacion,\n"
                    + "   p.tipo_identificacion_id, "
                    + "   cu.fecha_fin");
            Query query = em.createNativeQuery(consulta.toString(), "ContratoUrgencias.eliminarContratoUrgenciasMappging");

            query.setParameter("deleted", false);

            if (!dto.getNumeroContrato().isEmpty()) {
                query.setParameter("numero_contrato_urgencias", dto.getNumeroContrato());
            }
            if (!dto.getRazonSocial().isEmpty()) {
                query.setParameter("razonSocial", dto.getRazonSocial());
            }
            if (!dto.getNumeroDocumento().isEmpty()) {
                query.setParameter("identificacion", dto.getNumeroDocumento());
            }
            if (dto.getTipoIdentificacionDto() != null) {
                query.setParameter("tipoIdentificacion", dto.getTipoIdentificacionDto().getId());
            }
            negociacionConsultaContratoDto = query.getResultList();

        } catch (Exception e) {
            log.error("Error en la clase EliminarNegociacionControl metodo consultarContratosUrgencias()", e);
        }
        return negociacionConsultaContratoDto;
    }

    public Boolean habilitarContrato(NegociacionConsultaContratoDto selDTo) {
        try {
            habilitarContrato(selDTo.getContratoId());
            return true;
        } catch (Exception e) {
            this.log.error("Se presento un error al eliminar la negociacion ", e);
        }
        return false;
    }

    public String eliminaContrato(NegociacionConsultaContratoDto eliminarContratoDto) {
        String eliminarContrato = "";
        Boolean autorizacionesAsociadas = false;
        Boolean radicacionesAsociadas = false;
        try {
                // valida si un contrato tiene autorizaciones asociadas
                if (Objects.nonNull(eliminarContratoDto.getNumeroContrato()) && validaAutorizacionesAsociadas(eliminarContratoDto.getNumeroContrato())) {
                    eliminarContrato = "validaAutorizacionesAsociadas";
                } else {
                    autorizacionesAsociadas = true;
                }
                // valida si un contrato tiene radicaciones asociadas
                if (Objects.nonNull(eliminarContratoDto.getContratoId()) && validaRadicacionesAsociadas(eliminarContratoDto.getContratoId())) {
                    eliminarContrato = "validaRadicacionesAsociadas";
                } else {
                    radicacionesAsociadas = true;
                }
                // valida (autorizacionesAsociadas = true) el contrato no tiene autorizaciones o radicaciones asociadas
                if (autorizacionesAsociadas && radicacionesAsociadas) {
                    // validacion cuando la negociacion esta asociada a un contrato
                    if (eliminarContratoDto.getNumeroNegociacion() != null) {
                        // se hace borrado logico contrato negociacion
                        if(Objects.nonNull(eliminarContratoDto.getContratoId())){
                            this.borradoLogicoContrato(eliminarContratoDto.getContratoId());
                        }
                        // se hace borrado logico solicitud negociacion
                        if(Objects.nonNull(eliminarContratoDto.getNumeroNegociacion())&&Objects.nonNull(eliminarContratoDto.getContratoId())){
                            this.borradoLogicoSolicitudContratacion(eliminarContratoDto.getNumeroNegociacion(),eliminarContratoDto.getContratoId());
                        }
                    //se hace borrado logico a la negociacion y los registros que estan asociados a la misma
                        List<String> regimenes = consultarRegimenContratoByNegociacionId(eliminarContratoDto.getNumeroNegociacion());
                        if(regimenes.isEmpty()){
                            this.borradoLogicoRegistrosAsociadosNegociacion(new Long(eliminarContratoDto.getNumeroNegociacion()));
                        }else if(regimenes.size() == 1){
                            this.cambiarRegimenNegociacion(eliminarContratoDto.getNumeroNegociacion(),regimenes.get(0));
                        }
                    }else {
                        // si el contrato no tiene una negociacion asociada se determina que es contrato de urgencias (URG)
                        // se hace borrado logico contrato urgencias
                        if(Objects.nonNull(eliminarContratoDto.getContratoId())){
                            this.borradoLogicoContratoUrgencias(eliminarContratoDto.getContratoId());
                        }
                    }
                    eliminarContrato = "oK";
                }
        } catch (Exception e) {
           log.error("Error en la clase EliminarNegociacionControl metodo eliminaContrato()", e);
            eliminarContrato = "";
        }
        return eliminarContrato;
    }

    private void cambiarRegimenNegociacion(Integer numeroNegociacion, String regimen) {
        em.createNativeQuery("UPDATE contratacion.negociacion SET regimen = :regimen WHERE  id = :numero_negociacion")
                .setParameter("regimen", regimen)
                .setParameter("numero_negociacion",numeroNegociacion)
                .executeUpdate();
    }

    private Boolean validaAutorizacionesAsociadas(String numeroContrato) {
        boolean autorizacionesAsociadas = false;
        try {
            if(Objects.isNull(numeroContrato)){
                return autorizacionesAsociadas;
            }
            List<String> obj = em.createNativeQuery("select "
                    + " ic.contrato_id from prevalidacion_rips.informacion_cargue ic,liquidacion.cuenta cu,contratacion.contrato c "
                    + " where ic.cuenta_id = cu.id "
                    + " and c.id = ic.contrato_id"
                    + " and c.numero_contrato =:numeroContrato ")
                    .setParameter("numeroContrato", numeroContrato)
                    .getResultList();
            if (!obj.isEmpty()) {
                autorizacionesAsociadas = true;
            }
        } catch (Exception e) {
            this.log.error("Se presento un error a validad las autorizaciones ", e);
        }
        return autorizacionesAsociadas;
    }

    private Boolean validaRadicacionesAsociadas(long contratoId) {
        Boolean radicacionesAsociadas = false;
        try {
            List<String> cuenta = em.createNativeQuery(
                    " select id from liquidacion.cuenta where "
                            + "(contrato_id = :contrato_id or contrato_urgencias_id = :contrato_id)"
                            + " and  estado_cuenta != 'anulado'")
                    .setParameter("contrato_id", contratoId)
                    .getResultList();

            List<String> informacionCargue = em.createNativeQuery(
                    " select id from prevalidacion_rips.informacion_cargue where "
                            + "(contrato_id = :contrato_id or contrato_urgencias_id = :contrato_id)"
                            + " and  (deleted = false or deleted is null) ")
                    .setParameter("contrato_id", contratoId)
                    .getResultList();

            if (!cuenta.isEmpty() || !informacionCargue.isEmpty()) {
                radicacionesAsociadas = true;
            }
        } catch (Exception e) {
            this.log.error("Se presento un error a validad las autorizaciones ", e);
        }
        return radicacionesAsociadas;
    }

    private void habilitarContrato(Long id) {
        em.createNativeQuery("UPDATE contratacion.solicitud_contratacion sc SET estado_legalizacion_id = :estadoNegociacion " +
                "FROM contratacion.contrato con " +
                "WHERE con.solicitud_contratacion_id = sc.id AND con.id = :contrato_id")
                .setParameter("estadoNegociacion", EstadoLegalizacionEnum.CONTRATO_SIN_VB.name()).setParameter("contrato_id", id).executeUpdate();
    }

    private void borradoLogicoSolicitudContratacion(Integer id,Long contratoId) {
        em.createNativeQuery("UPDATE contratacion.solicitud_contratacion sc SET deleted = :deleted FROM contratacion.contrato con WHERE con.solicitud_contratacion_id = sc.id AND sc.negociacion_id = :negociacion_id AND con.id = :contrato_id")
                .setParameter("deleted", true).setParameter("negociacion_id", id).setParameter("contrato_id",contratoId).executeUpdate();
    }

    private void borradoLogicoContrato(Long id) {
        em.createNativeQuery("update contratacion.contrato set deleted =:deleted where  id =:id ")
                .setParameter("id", id).setParameter("deleted", true).executeUpdate();
    }

    private void borradoLogicoContratoUrgencias(Long id) {
        em.createNativeQuery("update contratacion.contrato_urgencias set deleted = :deleted where id = :contrato_id ")
                .setParameter("deleted", true).setParameter("contrato_id", id).executeUpdate();
    }

    private void borradoLogicoRegistrosAsociadosNegociacion(Long id) {
        // se actualiza negociacion
        em.createNativeQuery("update contratacion.negociacion set deleted = :deleted where id = :negociacion_id ")
                .setParameter("deleted", true).setParameter("negociacion_id", id).executeUpdate();

        // se actualiza negociacion municipio
        em.createNamedQuery("NegociacionMunicipio.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza sedes negociacion
        em.createNamedQuery("SedesNegociacion.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();

        // se actualiza reglas negociacion
        em.createNamedQuery("ReglaNegociacion.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza afiliado no procesado grupo etareo
        em.createNamedQuery("AfiliadoNoProcesado.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza negociacion ria
        em.createNamedQuery("NegociacionRia.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza incentivo
        em.createNamedQuery("Incentivo.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza modelo
        em.createNamedQuery("Modelo.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza negociacion rango poblacion
        em.createNamedQuery("NegociacionRangoPoblacion.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();

        // se actualiza negociacion invitacion negociacion
        em.createNamedQuery("InvitacionNegociacion.updateDeletedRegistro")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();

        // se actualiza negociacion legalizacion contrato
        em.createNativeQuery("update contratacion.legalizacion_contrato set deleted =:deleted where contrato_id in (select c.id from contratacion.contrato c inner join contratacion.solicitud_contratacion sc on c.solicitud_contratacion_id = sc.id where sc.negociacion_id = :negociacionId) ")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza negociacion area cobertura contrato
        em.createNativeQuery("update contratacion.area_cobertura_contrato set deleted =:deleted where sede_contrato_id in (select sc.id from contratacion.sede_contrato sc inner join contratacion.contrato c on c.id =  sc.contrato_id inner join contratacion.solicitud_contratacion sco on c.solicitud_contratacion_id = sco.id where sco.negociacion_id =:negociacionId)")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza negociacion procedimiento contrato
        em.createNativeQuery("update contratacion.procedimiento_contrato set deleted =:deleted where sede_contrato_id in (select sc.id from contratacion.sede_contrato sc inner join contratacion.contrato c on c.id =  sc.contrato_id inner join contratacion.solicitud_contratacion sco on c.solicitud_contratacion_id = sco.id where sco.negociacion_id =:negociacionId) ")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza paguete contrtato
        em.createNativeQuery("update contratacion.paquete_contrato set deleted =:deleted where sede_contrato_id in (select sc.id from contratacion.sede_contrato sc inner join contratacion.contrato c on c.id =  sc.contrato_id inner join contratacion.solicitud_contratacion sco on c.solicitud_contratacion_id = sco.id where sco.negociacion_id =:negociacionId) ")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();
        // se actualiza sede contrato
        em.createNativeQuery("update contratacion.sede_contrato set deleted =:deleted where contrato_id in (select c.id from contratacion.contrato c inner join contratacion.solicitud_contratacion sc on c.solicitud_contratacion_id = sc.id where sc.negociacion_id =:negociacionId) ")
                .setParameter("deleted", true)
                .setParameter("negociacionId", id)
                .executeUpdate();

    }


    public List<String> consultarRegimenContratoByNegociacionId(Integer negociacionId) {
        Query query = em.createNativeQuery("SELECT sc.regimen FROM contratacion.negociacion ne " +
                "JOIN contratacion.solicitud_contratacion sc ON(sc.negociacion_id = ne.id) " +
                "JOIN contratacion.contrato con ON(con.solicitud_contratacion_id = sc.id) " +
                "WHERE ne.id = :id_negociacion AND con.deleted = :deleted");

        query.setParameter("deleted", false);
        query.setParameter("id_negociacion", negociacionId);

        return (List<String>) query.getResultList();
    }
}
