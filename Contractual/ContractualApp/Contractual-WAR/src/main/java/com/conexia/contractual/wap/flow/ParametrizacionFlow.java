package com.conexia.contractual.wap.flow;

import java.io.Serializable;
import javax.enterprise.inject.Produces;
import javax.faces.flow.Flow;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.FlowBuilderParameter;
import javax.faces.flow.builder.FlowDefinition;

/**
 * Flujo de parametrizacion.
 *
 * @author jlopez
 */
public class ParametrizacionFlow implements Serializable {

//    @Produces
//    @FlowDefinition
//    public Flow defineFlowListarSedes(@FlowBuilderParameter FlowBuilder flowBuilder) {
//        String flowId = "listarSedesFlow";	// id del flujo
//        flowBuilder.id("", flowId);	// set del id del flujo
//
//        // se crea el nodo inicial del flujo
//        flowBuilder.viewNode(flowId, "/parametrizacion/parametrizarContrato.xhtml").markAsStartNode();
//
//        // inicializador para el flujo 
//        flowBuilder.initializer("#{parametrizarContratoController.iniciar}");
//
//        // Nodo para el envio del flujo
//        flowBuilder.flowCallNode("call-parametrizarTecnologiasFlow")
//                .flowReference("", "parametrizarTecnologiasFlow")
//                .outboundParameter("sede", "#{parametrizarContratoController.sede}")
//                .outboundParameter("origin", flowId);
//        return flowBuilder.getFlow();
//    }
//
//    @Produces
//    @FlowDefinition
//    public Flow defineFlowParametrizarTecnologias(@FlowBuilderParameter FlowBuilder flowBuilder) {
//        String flowId = "parametrizarTecnologiasFlow";	// id del flujo
//        flowBuilder.id("", flowId);	// set del id del flujo
//        
//        // Seteo el valor del origen sobre un atributo propio del flujo
//        flowBuilder.inboundParameter("origin", "#{flowScope.origin}");
//        flowBuilder.inboundParameter("sede", "#{parametrizarTecnologiasController.sede}");
//
//         // se crea el nodo inicial del flujo
//        flowBuilder.viewNode(flowId, "/parametrizacion/parametrizarTecnologias.xhtml").markAsStartNode();
//        flowBuilder.returnNode(flowId+"-exitTo-listarSedesFlow").fromOutcome("listarSedesFlow");
//        flowBuilder.initializer("#{parametrizarTecnologiasController.init}");
//        return flowBuilder.getFlow();
//    }

}
