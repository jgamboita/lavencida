package co.conexia.negociacion.wap.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.ReaderBuilder;
import org.jxls.reader.ReaderConfig;
import org.jxls.reader.XLSReader;
import org.primefaces.model.UploadedFile;
import org.xml.sax.SAXException;

import com.conexia.logfactory.Log;


/**
 *
 * @author Javier Sanchez
 *
 */
public class XlxsFiles implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = -8011789198753189018L;

  @Inject
  private Log log;
//  //@Inject
//  //@UserInfo
//  //private IUsuarioAplicacion usuario;
//  @Inject
//  private UtilsFiles utilsFiles;
//  @Inject
//  @Config
//  private String pathFiles;
//  @Inject
//  private MessagesUtils messagesUtils;
// // @Inject
//  //@CnxI18n
// // transient ResourceBundle resourceBundle;
//
  private static final String FOLDER_TEMPLATES = "/xlxsTemplates/";

  public void importar(UploadedFile file, String xmlFile, ConcurrentHashMap<String, Object> beans) throws IOException, SAXException, InvalidFormatException{
    StringBuilder xmlFileObtener = new StringBuilder(FOLDER_TEMPLATES).append(xmlFile);
    InputStream xmlInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFileObtener.toString());
    InputStream xlsInputStream = file.getInputstream();
    XLSReader reader = ReaderBuilder.buildFromXML(xmlInputStream);
    log.info("Reading the data...");
    ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes(true);
    ReaderConfig.getInstance().setSkipErrors( true );
    reader.read(xlsInputStream, beans);
    log.info("End the data...");
  }



}
