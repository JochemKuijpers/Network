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

	public InputFile(String fileName, String contentType, byte[] content) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getContent() {
		return content;
	}
}
