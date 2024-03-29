package co.conexia.contractual.services.common.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.repository.access.FileRepositoryAccessConfiguration;
import com.conexia.repository.access.FileRepositoryAccessManager;
import com.conexia.repository.access.StoredFile;
import com.conexia.repository.exception.FileRepositoryException;
import com.conexia.repository.provider.impl.LocalDiskProvider;
import com.conexia.repository.provider.impl.LocalDiskProviderConfiguration;

/**
 * Control para el manejo de repositorios de archivos
 *
 * @author jjoya
 *
 */
public class FileRepositoryControl {
	
    @Inject
    PreContractualExceptionUtils exceptionUtils;

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;
    
    @Inject
    private Log LOGGER;

    private FileRepositoryAccessManager respositoryAccessManager;

    /**
     * Genera consulta para traer el path
     *
     * @param query SqtrinBulder con el query
     * @return Path
     */
    public String getRutaRepositorioFromBD() {
        String sql = "SELECT valor FROM configuracion.properties WHERE clave = :clave";
        try {
            return (String) em
                    .createNativeQuery(sql)
                    .setParameter(
                            "clave",
                            "rep_arch_fileRepositoryPath"
                            + System.getProperty("os.name"))
                    .getSingleResult();
        } catch (NoResultException e) {
        	LOGGER.error("ERROR_REPOSITORIO_ARCHIVOS OS NAME ->"+System.getProperty("os.name") , e);
        	throw this.exceptionUtils.createSystemException(PreContractualMensajeErrorEnum.ERROR_REPOSITORIO_ARCHIVOS);
        }
    }

    public String nuevoArchivo(byte[] fileBytesStream, String path) {
        try {
        	LOGGER.info("PATH:"+ path);
            configure(path);
            StoredFile storedFile = respositoryAccessManager
                    .storeFile(fileBytesStream);

            return storedFile.getFilename();
        } catch (FileRepositoryException e) {
        	LOGGER.error("Nuevo Archivo:", e);
            throw this.exceptionUtils.createSystemException(PreContractualMensajeErrorEnum.ERROR_REPOSITORIO_ARCHIVOS);
        }
    }

    public byte[] obteneArchivo(String nombreArchivo, String path) throws ConexiaBusinessException {
        try {
            configure(path);
            return respositoryAccessManager.retrieveFile(new StoredFile(nombreArchivo));
        } catch (FileNotFoundException e) {
            LOGGER.error("Error obteniendo el archivo. ",e);
            throw this.exceptionUtils.createBusinessException(PreContractualMensajeErrorEnum.ARCHIVO_NO_ENCONTRADO);
        } catch (FileRepositoryException e) {
            LOGGER.error("Error mapeando el archivo. ",e);
            throw this.exceptionUtils.createSystemException(PreContractualMensajeErrorEnum.ERROR_REPOSITORIO_ARCHIVOS);
        }
    }

    public void eliminarArchivo(String nombreArchivo, String path) {
        try {
            configure(path);
            respositoryAccessManager.deleteFile(nombreArchivo);
        } catch (IOException | FileRepositoryException e) {
            throw this.exceptionUtils
                    .createSystemException(PreContractualMensajeErrorEnum.ERROR_REPOSITORIO_ARCHIVOS);
        }
    }

    void configure(String path) throws ConexiaSystemException {
        boolean directorioExiste = false;
        File directorio;

        try {
            directorio = new File(path);
            directorioExiste = directorio.exists();

            if (!(directorioExiste)) {
                directorioExiste = directorio.mkdirs();

            }
        } catch (SecurityException e) {
        	LOGGER.error("CONFIGURE:", e);
            throw this.exceptionUtils.createSystemException(PreContractualMensajeErrorEnum.ERROR_REPOSITORIO_ARCHIVOS);
        }

        if (directorioExiste) {
            LocalDiskProvider provider = new LocalDiskProvider();
            LocalDiskProviderConfiguration configuration = new LocalDiskProviderConfiguration();
            provider.setConfiguration(configuration);
            configuration.setPath(path);

            FileRepositoryAccessConfiguration<LocalDiskProviderConfiguration> repositoryAccessConfiguration = new FileRepositoryAccessConfiguration<LocalDiskProviderConfiguration>();
            repositoryAccessConfiguration.setEncodedFileNameLength(30);
            repositoryAccessConfiguration.setFileRepositoryProvider(provider);

            respositoryAccessManager = new FileRepositoryAccessManager();
            respositoryAccessManager.configure(repositoryAccessConfiguration);
        }
    }
}
