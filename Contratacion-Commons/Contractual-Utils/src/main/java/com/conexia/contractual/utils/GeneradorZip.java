package com.conexia.contractual.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Clase utilidad para crear archivos .Zip
 * 
 * @author jjoya
 *
 */
public class GeneradorZip {

	private ZipOutputStream zipOutputStream;
	private OutputStream outputStream;
	private byte[] buffer;

	public GeneradorZip(String destino) throws FileNotFoundException {
		buffer = new byte[4096];
		FileOutputStream fileOutputStream = new FileOutputStream(destino);
		zipOutputStream = new ZipOutputStream(fileOutputStream);
	}

	public GeneradorZip(OutputStream outputStream) {
		this.outputStream = outputStream;
		this.buffer = new byte[1024];
		this.zipOutputStream = new ZipOutputStream(outputStream);
	}

	/**
	 * Recibe una lista de archivos que convertira en un .zip
	 * 
	 * @param archivos
	 *            lista de archivos.
	 * @param destino
	 *            ruta y nombre del .zip a crear.
	 * @throws IOException 
	 */
	public void generarZip(Map<String, InputStream> archivos) throws IOException {
		try {
			// Se crea un FileOutputStream que contiene el nombre y direccion
			// del zip que se va a generar.
			// Se crea un ZipOutputStream que contiene el fileOutputStream
			// creado anteriormente.
			for (Entry<String, InputStream> archivo : archivos.entrySet()) {
				// Se envia el archivo, buffer y zipOutputStream.
				ejecutar(archivo.getKey(), archivo.getValue(), buffer,
						zipOutputStream);
			}
		}finally {
			zipOutputStream.close();
		}
	}

	/**
	 * Recibe una lista de archivos que convertira en un .zip
	 * 
	 * @param archivos
	 *            lista de archivos.
	 * @param destino
	 *            ruta y nombre del .zip a crear.
	 * @throws IOException 
	 */
	public void generarZip(Collection<File> archivos) throws IOException {
		try {
			// Se crea un FileOutputStream que contiene el nombre y direccion
			// del zip que se va a generar.
			// Se crea un ZipOutputStream que contiene el fileOutputStream
			// creado anteriormente.
			for (File file : archivos) {
				// Se envia el archivo, buffer y zipOutputStream.
				ejecutar(file, buffer, zipOutputStream);
			}
		}finally {
			zipOutputStream.close();
		}
	}

	/**
	 * Recibe un archivo que convertira en un .zip
	 * 
	 * @param archivo
	 *            a convertir en .zip.
	 * @param destino
	 *            ruta y nombre del .zip a crear.
	 * @throws IOException 
	 */
	public void generarZip(File archivo) throws IOException {
		try {
			ejecutar(archivo, buffer, this.zipOutputStream);
		}finally {
			zipOutputStream.close();
		}
	}

	private void ejecutar(File archivo, byte[] buffer,
			ZipOutputStream zipOutputStream) throws IOException {
		ejecutar(archivo.getName(), new FileInputStream(archivo), buffer,
				zipOutputStream);
	}

	private void ejecutar(String nombreArchivo, InputStream inputStream,
			byte[] buffer, ZipOutputStream zipOutputStream) throws IOException {
		// prepara el zipOutputStream para escribir el .zip
		zipOutputStream.putNextEntry(new ZipEntry(nombreArchivo));
		int length;
		while ((length = inputStream.read(buffer)) > 0) {
			// escirbe el .zip.
			zipOutputStream.write(buffer, 0, length);
		}

		zipOutputStream.closeEntry();
		inputStream.close();
	}

	/**
	 * Recibe una lista de archivos que convertira en un .zip
	 * 
	 * @param archivos
	 *            lista de archivos.
	 * @param destino
	 *            ruta y nombre del .zip a crear.
	 * @throws IOException 
	 */
	public GeneradorZip crearArchivo(String nombreArchivo,
			InputStream inputStream) throws IOException {
			// Se envia el archivo, buffer y zipOutputStream.
		ejecutar(nombreArchivo, inputStream, buffer, zipOutputStream);
		return this;
	}

	public GeneradorZip crearDirectorio(String nombreDirectorio)
			throws IOException {
		zipOutputStream.putNextEntry(new ZipEntry(nombreDirectorio));
		zipOutputStream.closeEntry();

		return this;
	}

	public void finalizar() throws IOException {
		zipOutputStream.close();
	}

	public GeneradorZip crearArchivo(String nombreArchivo, File file)
			throws IOException {
		return crearArchivo(nombreArchivo, new FileInputStream(file));
	}

}
