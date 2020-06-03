package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import java.util.List;

public interface DerogacionTecnologias {

    List<String> contieneDerogados(NegociacionDto negociacionDto);
}
