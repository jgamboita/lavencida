package com.conexia.contractual.wap.controller.legalizacion;

import com.conexia.contractual.wap.facade.legalizacion.ParametrizacionMinutaFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContenidoMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.GestionMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
@URLMappings(mappings = {
    @URLMapping(id = "actualizarMinuta", pattern = "/legalizacion/actualizarMinuta/#{minutaController.minutaId}", viewId = "/legalizacion/minuta.page"),
    @URLMapping(id = "crearMinuta", pattern = "/legalizacion/crearMinuta", viewId = "/legalizacion/minuta.page")
})
public class MinutaController implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Utilitarias de faces.
     */
    @Inject
    private FacesUtils facesUtils;
    
    @Inject
    private FacesMessagesUtils messagesUtils;

    /**
     * Facade de parametrizacion.
     */
    @Inject
    private ParametrizacionMinutaFacade parametrizacionFacade;

    /**
     * Arbol de contenidos.
     */
    private TreeNode contenidos;

    /**
     * Nodo seleccionado en el arbol de minutas.
     */
    private TreeNode selectedNode;

    /**
     * Arbol de variables.
     */
    private TreeNode variables;

    /**
     * Gestion minuta dto.
     */
    private GestionMinutaDto gestionMinutaDto;

    /**
     * Variable que carga los contenidos de la minuta segun el item
     * seleccionado.
     */
    private List<ContenidoMinutaDto> contenidosMinuta;

    /**
     * Detalles de la minuta.
     */
    private List<MinutaDetalleDto> detalles;

    /**
     * Minuta.
     */
    private MinutaDto minuta;
    
    /**
     * Modalidad de negociacion
     */
    private NegociacionModalidadEnum modalidadNegociacion;
    /**
     * Id de la minuta.
     */
    private Long minutaId;

    /**
     * Mensaje a visualizar cuando se elimina un item de la minuta.
     */
    private String mensajeEliminar;

    /**
     * Sesion de usuario activa
     */
    @Inject
    @UserInfo
    private UserApp user;

    @PostConstruct
    public void init() {
        gestionMinutaDto = new GestionMinutaDto();
    }

    public void agregaHijos(DefaultTreeNode nodoActual, Long padreId, int nivel) {
        for (MinutaDetalleDto det : this.detalles) {
            if (det.getContenidoMinuta().getNivel() == (nivel + 1)) {
                if (det.getPadreId().longValue() == padreId && padreId != null) {
                    DefaultTreeNode nodoPapa = new DefaultTreeNode(det, nodoActual);
                    this.agregaHijos(nodoPapa, det.getId(), det.getContenidoMinuta().getNivel());
                }
            }

        }
    }

    public void agregarContenido() {
        if (selectedNode == null) {
            selectedNode = this.contenidos;
        }
        if(this.getGestionMinutaDto().getTituloContenido().hashCode() ==0){
        	this.messagesUtils.addError("Por favor ingrese el nombre del contenido.");
            return;
        }
        if (this.getGestionMinutaDto().getContenidoSeleccionado() == null) {
            this.messagesUtils.addError("Por favor ingrese el tipo del contenido.");
            return;
        }
        MinutaDetalleDto detalleSeleccionado = (MinutaDetalleDto) selectedNode.getData();
        MinutaDetalleDto minutaGuardar = new MinutaDetalleDto();
        minutaGuardar.setContenidoMinuta(this.getGestionMinutaDto().getContenidoSeleccionado());
        minutaGuardar.setMinutaId(detalleSeleccionado.getMinutaId());
        minutaGuardar.setOrdinal(selectedNode.getChildCount() + 1);
        minutaGuardar.setPadreId(detalleSeleccionado.getId());
        minutaGuardar.setTitulo(this.getGestionMinutaDto().getTituloContenido());
        try {
            if (this.parametrizacionFacade.validaExistenciaNombreMinutaDetalle(minutaGuardar) > 0) {
                this.getGestionMinutaDto().setTituloContenido("");
                this.messagesUtils.addError("El nombre del item ya existe por favor cambielo.");
            } else {
                MinutaDetalleDto minuta = this.parametrizacionFacade.guardaMinutaDetalle(minutaGuardar);
                new DefaultTreeNode(minuta, selectedNode);
                this.getGestionMinutaDto().setTituloContenido("");
                this.getGestionMinutaDto().setContenidoSeleccionado(null);
                this.messagesUtils.addInfo("Se guardo correctamente el ítem de la minuta.");
                RequestContext.getCurrentInstance().execute("PF('crearContenidoDialog').hide();");
            }
        } catch (final Exception e) {
            this.messagesUtils.addError("Se presentó un error al guardar el detalle de la minuta.");
            RequestContext.getCurrentInstance().execute("PF('crearContenidoDialog').hide();");
        }
    }

    public void duplicar() {
    	FacesContext facesContext = FacesContext.getCurrentInstance();
    	Flash flash = facesContext.getExternalContext().getFlash();
    	flash.setKeepMessages(true);
    	flash.setRedirect(true);
    	minuta.setMinutasDetalle(this.parametrizacionFacade.obtieneDetalleMinuta(minuta.getId()));      
    	facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"La minuta se duplicó exitosamente", "")); 
        facesUtils.urlRedirect("/legalizacion/actualizarMinuta/" + this.parametrizacionFacade.duplicarMinuta(minuta).getId()); 
    }
    
    public void eliminarContenido() {
        try {
            this.parametrizacionFacade.eliminarMinutaDetalle((MinutaDetalleDto) selectedNode.getData());
            selectedNode.getChildren().clear();
            selectedNode.getParent().getChildren().remove(selectedNode);
            selectedNode.setParent(null);
            selectedNode = null;
        } catch (Exception e) {
            this.messagesUtils.addError("Se generó un error al eliminar el detalle de la minuta.");
        }
    }

    public void gestionarMinuta(MinutaDto minutaDto) {
        facesUtils.urlRedirect(minutaDto == null ? "/legalizacion/crearMinuta"
                : "/legalizacion/actualizarMinuta/" + minutaDto.getId());
    }

    public void guardaContenido() {
        try {
            if (this.getGestionMinutaDto().getMinutaDetalle().getOrdinal() > 0) {
                this.parametrizacionFacade.actualizarMinutaDetalle(this.getGestionMinutaDto().getMinutaDetalle());
                this.messagesUtils.addInfo("Se guardo correctamente el contenido del detalle de la minuta.");
            } else {
                this.messagesUtils.addError("No se puede editar contenido al ítem seleccionado.");
            }
        } catch (final Exception e) {
            this.messagesUtils.addError("Se generó un error al guardar el contenido del detalle de la minuta.");
        }

    }

    public void generaMensajeEliminar() {
        MinutaDetalleDto minuta = (MinutaDetalleDto) selectedNode.getData();
        if (minuta.getContenidoMinuta().getNivel() > 0) {
            this.mensajeEliminar = "Desea Eliminar " + minuta.getTitulo();
            if (selectedNode.getChildCount() > 0) {
                this.mensajeEliminar += " y sus " + selectedNode.getChildCount() + " hijos";
            }
            RequestContext.getCurrentInstance().execute("PF('confirmarEliminarDialog').show();");
        } else {
            this.messagesUtils.addError("No se puede eliminar la Minuta " + minuta.getTitulo());
        }
    }

    public void guardarMinuta() {
        try {
            if (minuta.getId() == null) {
                this.minuta = this.parametrizacionFacade.guardarMinuta(this.minuta);
                contenidos = new DefaultTreeNode(null);
                new DefaultTreeNode(new MinutaDetalleDto(0, minuta.getNombre(),
                        minuta.getId(), ""), contenidos);
                cargarArbolVariables();
                this.gestionarMinuta(this.minuta);
                this.parametrizacionFacade.guardarHistorialMinuta(user.getId(), minuta.getId(), "CREAR");
            } else {
                this.parametrizacionFacade.actualizarMinuta(this.minuta);
                this.parametrizacionFacade.guardarHistorialMinuta(user.getId(), minuta.getId(), "MODIFICAR");
            }
            this.messagesUtils.addInfo("Se ha guardado correctamente.");
        } catch (ConexiaBusinessException cbe) {
            this.messagesUtils.addWarning("Nombre de minuta ya existe");
        } catch (Exception ex) {
            this.messagesUtils.addError("Se generó un error guardando la minuta.");
        }
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.getGestionMinutaDto().setMinutaDetalle((MinutaDetalleDto) event.getTreeNode().getData());
    }

    public void actualizarMinutaDetalleTitulo() {
        try {
            this.getGestionMinutaDto().getMinutaDetalleEditar().setTitulo(this.getGestionMinutaDto().getTituloMinutaDetalle());
            if (this.getGestionMinutaDto().getTituloMinutaDetalle().length() < 5 || this.getGestionMinutaDto().getTituloMinutaDetalle().length() > 50) {
                this.messagesUtils.addError("El titulo del ítem debe tener entre 5 y 50 caracteres.");
                return;
            }
            if (this.parametrizacionFacade.validaExistenciaNombreMinutaDetalle(this.getGestionMinutaDto().getMinutaDetalleEditar()) > 0) {
                this.messagesUtils.addError("El nombre del item ya existe por favor cambielo.");
            } else {
                this.parametrizacionFacade.actualizarMinutaDetalleTitulo(this.getGestionMinutaDto().getMinutaDetalleEditar());
                ((MinutaDetalleDto) selectedNode.getData()).setTitulo(this.getGestionMinutaDto().getTituloMinutaDetalle());
                this.messagesUtils.addInfo("Se actualizó el titulo del ítem de la minuta.");
                this.getGestionMinutaDto().setTituloMinutaDetalle("");
                RequestContext.getCurrentInstance().execute("PF('editarDetalleMinutaDialog').hide();");
            }
        } catch (final Exception e) {
            this.messagesUtils.addError("Se generó un error actualizando el titulo del ítem de la minuta.");
        }

    }

    public void onDragDrop(TreeDragDropEvent event) throws Exception {

        //Nodo que se mueve y su nueva ubicacion.
        TreeNode dragNode = event.getDragNode();
        //Nodo del que se mueve del nodo.
        TreeNode dropNode = event.getDropNode();
        MinutaDetalleDto minDetalleDtoDrag = (MinutaDetalleDto) dragNode.getData();
        MinutaDetalleDto minDetalleDtoDrop = (MinutaDetalleDto) dropNode.getData();
        try {
            dragDropNivel(dragNode, dropNode, minDetalleDtoDrag, minDetalleDtoDrop);
        } catch (final Exception e) {
            this.cargarArbolMinutas();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "El item " + minDetalleDtoDrag.getDescripcion() + " no puede moverse.", "");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }

    }

    private void dragDropNivel(TreeNode dragNode, TreeNode dropNode, MinutaDetalleDto minDetalleDtoDrag, MinutaDetalleDto minDetalleDtoDrop) throws Exception {
        if (minDetalleDtoDrag.getContenidoMinuta().getNivel() == (minDetalleDtoDrop.getContenidoMinuta().getNivel() + 1)) {
            final String[] nodo = dragNode.getRowKey().split("_", (minDetalleDtoDrag.getContenidoMinuta().getNivel() + 1));
            Integer posicionNodo = new Integer(nodo[minDetalleDtoDrag.getContenidoMinuta().getNivel()]);
            final MinutaDetalleDto minutaDetalleAnterior = new MinutaDetalleDto();
            minutaDetalleAnterior.setId(minDetalleDtoDrag.getId());
            minutaDetalleAnterior.setPadreId(minDetalleDtoDrag.getPadreId());
            minutaDetalleAnterior.setMinutaId(minDetalleDtoDrag.getMinutaId());
            minDetalleDtoDrag.setOrdinal(posicionNodo + 1);
            if (minDetalleDtoDrop.getId() != null) {
                minDetalleDtoDrag.setPadreId(minDetalleDtoDrop.getId());
            }
            minDetalleDtoDrag.setMinutaDetalleAnteriorDto(minutaDetalleAnterior);
            this.parametrizacionFacade.actualizarOrdinalMinutaDetalle(minDetalleDtoDrag);
        } else {
            throw new Exception("No se puede mover el nodo al lugar seleccionado.");
        }
    }

    public void obtieneContenidosMinuta() {
        if (selectedNode == null) {
            selectedNode = this.contenidos;
        }
        MinutaDetalleDto min;
        min = (MinutaDetalleDto) selectedNode.getData();
        if (min != null) {
            if (min.getContenidoMinuta().isTieneHijos()) {
                this.contenidosMinuta = this.parametrizacionFacade.obtieneContenidosMinutaPorNivel(min.getContenidoMinuta().getNivel());
                this.visualizarContenidosMinuta(selectedNode);
                RequestContext.getCurrentInstance().execute("PF('crearContenidoDialog').show();");
            } else {
                this.messagesUtils.addError("El item seleccionado no puede tener items asociados.");
            }

        }
    }

    public void editarMinutaDetalle() {
        if (selectedNode == null) {
            selectedNode = this.contenidos;
        }
        this.gestionMinutaDto.setMinutaDetalleEditar((MinutaDetalleDto) selectedNode.getData());
        this.gestionMinutaDto.setTituloMinutaDetalle(this.gestionMinutaDto.getMinutaDetalleEditar().getTitulo());
        if (this.gestionMinutaDto.getMinutaDetalleEditar() != null && this.gestionMinutaDto.getMinutaDetalleEditar().getTitulo() != null
                && this.gestionMinutaDto.getMinutaDetalleEditar().getId() != null) {
            RequestContext.getCurrentInstance().execute("PF('editarDetalleMinutaDialog').show();");
        }
    }

    private void visualizarContenidosMinuta(TreeNode treeNodes) {
        for (TreeNode treeNode : treeNodes.getChildren()) {
            MinutaDetalleDto minutaDetalleDto = (MinutaDetalleDto) treeNode.getData();
            if (minutaDetalleDto.getContenidoMinuta().isUnico()) {
                this.contenidosMinuta.remove(minutaDetalleDto.getContenidoMinuta());
            }
        }
    }

    public void regresar() {
        facesUtils.urlRedirect("/legalizacion/minutas.page");
    }

    public TreeNode getSelectedNode() {
        this.getGestionMinutaDto().setMinutaDetalle(this.selectedNode == null ? null : (MinutaDetalleDto) this.selectedNode.getData());
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
        this.getGestionMinutaDto().setMinutaDetalle(this.selectedNode == null ? null
                : (MinutaDetalleDto) this.selectedNode.getData());
        RequestContext.getCurrentInstance().update("formContrato:editor");
    }

    private void cargarArbolMinutas() {
        if (minuta == null) {
            minuta = this.parametrizacionFacade.obtenerMinuta(minutaId);
        }
        detalles = this.parametrizacionFacade.obtieneDetalleMinuta(minutaId);
        contenidos = new DefaultTreeNode(null);
        DefaultTreeNode node = new DefaultTreeNode(new MinutaDetalleDto(0,
                minuta.getNombre(), minuta.getId(), this.parametrizacionFacade.generarVistaPrevia(minutaId)), contenidos);
        node.setExpanded(true);
        node.setSelected(true);
        detalles.stream().filter((detalle) -> (detalle.getContenidoMinuta().getNivel() == 1)).forEach((detalle) -> {
            DefaultTreeNode nodoActual = new DefaultTreeNode(detalle, node);
            this.agregaHijos(nodoActual, detalle.getId(), detalle.getContenidoMinuta().getNivel());
        });
    }

    private void cargarArbolVariables() {
        variables = new DefaultTreeNode(null);
        //this.parametrizacionFacade.listarVariables(minuta.getModalidad().getId());
       
        TreeNode contratante = new DefaultTreeNode(TipoVariableEnum.CONTRATANTE, variables);
        TreeNode contratista = new DefaultTreeNode(TipoVariableEnum.CONTRATISTA, variables);
        TreeNode tabla = new DefaultTreeNode(TipoVariableEnum.TABLA, variables);
        TreeNode contrato = new DefaultTreeNode(TipoVariableEnum.CONTRATO, variables);
        for (VariableDto v : this.parametrizacionFacade.listarVariables(minuta.getModalidad().getId())){
            switch (v.getTipoVariable()) {
                case CONTRATANTE:
                    new DefaultTreeNode(v, contratante);
                    break;
                case CONTRATISTA:
                    new DefaultTreeNode(v, contratista);
                    break;
                case TABLA:
                    new DefaultTreeNode(v, tabla);
                    break;
                case CONTRATO:
                    new DefaultTreeNode(v, contrato);
                    break;
            }
        }
    }

    public MinutaDto getMinuta() {
        if (minutaId != null && minuta == null) {
            minuta = this.parametrizacionFacade.obtenerMinuta(minutaId);
        } else if (minuta == null) {
            minuta = new MinutaDto();
        }
        return minuta;
    }

    public void setMinutaId(Long minutaId) {
        this.minutaId = minutaId;
        if (minutaId != null) {
            cargarArbolMinutas();
            cargarArbolVariables();
        }
    }

    public List<MinutaDto> getMinutas() {
        return this.parametrizacionFacade.listarMinutas();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public TreeNode getContenidos() {
        return contenidos;
    }

    public void setContenidos(TreeNode contenidos) {
        this.contenidos = contenidos;
    }

    public void setMinuta(MinutaDto minuta) {
        this.minuta = minuta;
    }

    public Long getMinutaId() {
        return minutaId;
    }

    public TreeNode getVariables() {
        return variables;
    }

    public void setVariables(TreeNode variables) {
        this.variables = variables;
    }

    public List<MinutaDetalleDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<MinutaDetalleDto> detalles) {
        this.detalles = detalles;
    }

    public List<ContenidoMinutaDto> getContenidosMinuta() {
        return contenidosMinuta;
    }

    public void setContenidosMinuta(List<ContenidoMinutaDto> contenidosMinuta) {
        this.contenidosMinuta = contenidosMinuta;
    }

    public String getMensajeEliminar() {
        return mensajeEliminar;
    }

    public void setMensajeEliminar(String mensajeEliminar) {
        this.mensajeEliminar = mensajeEliminar;
    }

    /**
     * @return the gestionMinutaDto
     */
    public GestionMinutaDto getGestionMinutaDto() {
        return gestionMinutaDto;
    }

    /**
     * @param gestionMinutaDto the gestionMinutaDto to set
     */
    public void setGestionMinutaDto(GestionMinutaDto gestionMinutaDto) {
        this.gestionMinutaDto = gestionMinutaDto;
    }
   

    public NegociacionModalidadEnum getModalidadNegociacion() {
        return modalidadNegociacion;
    }

    public void setModalidadNegociacion(NegociacionModalidadEnum modalidadNegociacion) {
        this.modalidadNegociacion = modalidadNegociacion;
    }
}
