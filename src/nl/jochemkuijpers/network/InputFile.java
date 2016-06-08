package nl.jochemkuijpers.network;

/**
 * A helper class to upload files using the POST multipart methods.
 * 
 * Please refer to the LICENSE file for the license corresponding to this code.
 * 
 * @author Jochem Kuijpers
 */
public class InputFile {
	private final String fileName;
	private final String contentType;
	private final byte[] content;

	/**
	 * Create an input file to send via
	 * {@link Connection#post(String, java.util.Map, java.util.Map)} or
	 * {@link Connection#post(String, java.util.Map, String, InputFile)}
	 * 
	 * @param fileName
	 *            file name, need not match the local file name
	 * @param contentType
	 *            content type. Commonly used MIME types:
	 *            <ul>
	 *            <li>application/octet-stream - arbitrary binary data</li>
	 *            <li>text/plain - text files</li>
	 *            <li>text/html - html files</li>
	 *            <li>image/png - png images</li>
	 *            <li>image/gif - gif images</li>
	 *            <li>...</li>
	 *            </ul>
	 * @param content
	 *            The binary content of the file
	 */
	public InputFile(String fileName, String contentType, byte[] content) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.content = content;
	}

	/**
	 * @return file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @return file content
	 */
	public byte[] getContent() {
		return content;
	}
}
