package co.conexia.negociacion.wap.facade.comite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.comite.ActaComiteDto;
import com.conexia.contratacion.commons.dto.comite.CompromisoComiteDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.comite.ActaComiteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.comite.ActaComiteViewServiceRemote;
import com.conexia.negociacion.definitions.comite.GestionComiteViewServiceRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.webutils.i18n.CnxI18n;

public class ActaComiteFacade implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6352488708275358005L;

	@Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
	
	@Inject
	@CnxService
	private ActaComiteTransactionalServiceRemote actaComiteTxBundary;
	
	@Inject
	@CnxService
	private ActaComiteViewServiceRemote actaComiteBundary;
	
	@Inject
	@CnxService
	private GestionComiteViewServiceRemote gestionComiteBundary;
	
	public void registrarActaComite(ActaComiteDto acta)
			throws ConexiaBusinessException {
		for (CompromisoComiteDto compromiso : acta.getCompromisos()) {
			List<String> mensajes = new ArrayList<String>(3);
			
			if (compromiso.getTarea() == null) {
				mensajes.add(resourceBundle.getString("acta_comite_msg_compromiso_tarea"));
			}
			
			if (compromiso.getFechaCompromiso() == null) {
				mensajes.add(resourceBundle.getString("acta_comite_msg_compromiso_fecha"));
			}
			
			if (compromiso.getResponsable() == null) {
				mensajes.add(resourceBundle.getString("acta_comite_msg_compromiso_responsable"));
			}
			
			if ( !(mensajes.isEmpty()) ){
				throw new ConexiaBusinessException(
						PreContractualMensajeErrorEnum.ACTA_COMITE_FORM_ERROR,
						mensajes.toArray(new String[0]));
			}			
		}
		
		actaComiteTxBundary.registrarActa(acta);
	}
	
	/**
	 * Genera el documento pdf que representa el acta de finalizacion de comite
	 * @param long1 
	 * 
	 * @return una instancia de {@link DefaultStreamedContent}
	 * @throws IOException 
	 * @throws ConexiaBusinessException 
	 * @throws FileNotFoundException 
	 */
	public StreamedContent imprimirActa(Long comiteId) throws JRException, IOException, ConexiaBusinessException {
		
		StreamedContent content = null;
			
		InputStream in = FacesContext
				.getCurrentInstance()
				.getExternalContext()
				.getResourceAsStream(
						"/WEB-INF/classes/acta_comite.jasper");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("comiteId", comiteId);
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(in, parametros);
		JasperExportManager.exportReportToPdfStream(jasperPrint, out);
			
		in.close();		
		in = new ByteArrayInputStream(out.toByteArray());
		
        content = new DefaultStreamedContent(in, "application/pdf", "acta_comite_" + comiteId + ".pdf");
		

        in.close();
        out.close();
        
		return content;
	}

	public ActaComiteDto obtenerActaPorComiteId(Long comiteId) {
		return actaComiteBundary.obtenerActaPorComiteId(comiteId);
	}

	public List<CompromisoComiteDto> obtenerCompromisosPorComiteId(Long comiteId) {		
		return actaComiteBundary.obtenerCompromisosPorComiteId(comiteId);
	}
}
