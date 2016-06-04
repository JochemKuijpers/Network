package nl.jochemkuijpers.network;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sends GET, POST and POST multipart requests.
 * 
 * Please refer to the LICENSE file for the license corresponding to this code.
 * 
 * @author Jochem Kuijpers
 */
public abstract class Connection {
	private final static String DEFAULT_USER_AGENT = "Mozilla/5.0 (nl.jochemkuijpers.network 1.0)";
	private final static boolean DEFAULT_KEEPALIVE = false;

	protected final String host;
	protected Socket socket;

	private final String useragent;
	private String status;
	private Map<String, String> responseHeaders;
	private final Map<String, String> customHeaders;
	private final boolean keepalive;

	/**
	 * Set up a connection with a specified destination host and user agent.
	 * 
	 * @param host
	 *            destination host
	 * @param useragent
	 *            user agent
	 * @param keepalive
	 *            whether or not to keep the socket open after each request.
	 *            This could lead to problems if the content-length header was
	 *            not properly set by the server.
	 */
	protected Connection(String host, String useragent, boolean keepalive) {
		this.host = host;
		this.socket = null;
		this.useragent = useragent;
		this.status = null;
		this.responseHeaders = null;
		this.customHeaders = new HashMap<String, String>();
		this.keepalive = keepalive;
	}

	/**
	 * Set up a connection with a specified destination host and user agent.
	 * 
	 * @param host
	 *            destination host
	 * @param useragent
	 *            user agent
	 */
	protected Connection(String host, String useragent) {
		this(host, useragent, DEFAULT_KEEPALIVE);
	}

	/**
	 * Set up a connection with a specified destination host
	 * 
	 * @param host
	 *            destination host
	 */
	protected Connection(String host) {
		this(host, DEFAULT_USER_AGENT, DEFAULT_KEEPALIVE);
	}

	/**
	 * Open the socket as specified by the implementing child class. A new
	 * socket should be created if the socket member is null or otherwise
	 * unavailable.
	 * 
	 * @throws IOException
	 *             if an IO error occurred
	 * @throws UnknownHostException
	 *             if the host could not be resolved
	 */
	protected abstract void openSocket() throws UnknownHostException,
			IOException;

	/**
	 * Reads the response from an InputStream. The response should contain a
	 * Content-Length header or an empty array is returned, even if the
	 * InputStream contained data.
	 * 
	 * This method will close the socket is keepalive was not set to true in the
	 * constructor, and will do so anyway if the received response did not have
	 * a valid Content-Length header.
	 * 
	 * @param in
	 *            socket InputStream
	 * @return a byte array with the content (no headers) of the response
	 * @throws IOException
	 *             if an error occurred
	 */
	private byte[] readResponse(InputStream in) throws IOException {
		status = NetworkUtils.readLineUnbuffered(in);
		responseHeaders = new HashMap<String, String>();

		String headerline;
		String[] header;
		while (!(headerline = NetworkUtils.readLineUnbuffered(in)).isEmpty()) {
			header = headerline.split(":", 2);
			if (header.length != 2) {
				continue; // illegal header?
			}
			responseHeaders.put(header[0].toLowerCase(), header[1].trim());
		}

		String contentLength = responseHeaders.get("content-length");
		if (contentLength == null) {
			socket.close();
			return new byte[0];
		}

		long length = 0;
		try {
			length = Long.valueOf(contentLength);
		} catch (NumberFormatException e) {
			socket.close();
			return new byte[0];
		}

		if (length <= 0) {
			socket.close();
			return new byte[0];
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int len;
		for (long n = 0; n < length; n += bufferSize) {
			len = in.read(buffer, 0, bufferSize);
			if (len <= 0) {
				break;
			}
			out.write(buffer, 0, (int) Math.min(len, length - n));
		}

		if (!keepalive) {
			socket.close();
		}

		return out.toByteArray();
	}

	/**
	 * Writes the custom headers to a string and returns it.
	 * 
	 * @return the custom headers.
	 */
	private String getCustomHeaders() {
		StringBuilder sb = new StringBuilder();

		for (Entry<String, String> entry : customHeaders.entrySet()) {
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
			sb.append("\r\n");
		}

		return sb.toString();
	}

	/**
	 * @return a sufficiently unique boundary string for multipart requests
	 */
	private String generateBoundary() {
		return "--------------------------------boundary-"
				+ (System.currentTimeMillis() ^ System.nanoTime());
	}

	/**
	 * Builds the body of a multipart requests with the specified boundary
	 * string, form fields and file fields.
	 * 
	 * @param boundary
	 *            a valid multipart boundary string
	 * @param formFields
	 *            a mapping from field name to field value. These should not be
	 *            url-encoded.
	 * @param fileFields
	 *            a mapping from field name to InputFile. These values should
	 *            not be url-encoded.
	 * @return the multipart request body
	 * @throws IOException
	 *             if an error occurred
	 */
	private byte[] buildMultipart(String boundary,
			Map<String, String> formFields, Map<String, InputFile> fileFields)
			throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				buffer, StandardCharsets.UTF_8));

		// fields
		for (Entry<String, String> entry : formFields.entrySet()) {
			writer.write("--" + boundary + "\r\n");
			writer.write("Content-Disposition: form-data; name=\"");
			writer.write(NetworkUtils.urlEncode(entry.getKey()));
			writer.write("\";\r\n");
			writer.write("Content-type: text/plain; charset=utf-8\r\n\r\n");

			writer.write(entry.getValue());
			writer.write("\r\n");
		}

		// files
		for (Entry<String, InputFile> entry : fileFields.entrySet()) {
			InputFile file = entry.getValue();

			writer.write("--" + boundary + "\r\n");
			writer.write("Content-Disposition: form-data; name=\"");
			writer.write(NetworkUtils.urlEncode(entry.getKey()));
			writer.write("\"; filename=\"");
			writer.write(NetworkUtils.urlEncode(file.getFileName()));
			writer.write("\"\r\n");
			writer.write("Content-Type: ");
			writer.write(file.getContentType());
			writer.write("\r\n\r\n");

			writer.flush();

			// binary write
			buffer.write(file.getContent());
			buffer.flush();

			writer.write("\r\n");
		}
		writer.write("--" + boundary + "--\r\n");
		writer.flush();
		return buffer.toByteArray();
	}

	/**
	 * @return The status line (e.g. HTTP/1.1 200 OK) of the last response
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the headers of the last response
	 */
	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	/**
	 * Set a custom header for all future requests. These can be cleared with
	 * unsetHeader()
	 * 
	 * @param field
	 *            the header field name
	 * @param content
	 *            the header field content
	 * @throws IllegalArgumentException
	 *             when field equals host, content-length, content-type or
	 *             user-agent. Set the user-agent via the constructor.
	 */
	public void setHeader(String field, String content) {
		if (field.equalsIgnoreCase("host")
				|| field.equalsIgnoreCase("content-lenght")
				|| field.equalsIgnoreCase("content-type")
				|| field.equalsIgnoreCase("user-agent")) {
			throw new IllegalArgumentException(field + " header cannot be set");
		}

		customHeaders.put(field.toLowerCase(), content.trim());
	}

	/**
	 * Unsets a custom header for all future requests. If the field was never
	 * set, nothing happens.
	 * 
	 * @param field
	 *            the header field name.
	 */
	public void unsetHeader(String field) {
		customHeaders.remove(field.toLowerCase());
	}

	/**
	 * Performs a GET request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * @param path
	 *            a valid path without first slash. To request the root path,
	 *            set an empty string. Should be properly url-encoded.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] get(String path) throws IOException {
		openSocket();

		OutputStream out = socket.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

		writer.write("GET /" + path + " HTTP/1.1\r\n");
		writer.write("User-Agent: " + useragent + "\r\n");
		writer.write("Host: " + host + "\r\n");
		if (customHeaders.size() > 0) {
			writer.write(getCustomHeaders());
		}
		writer.write("\r\n");
		writer.flush();

		return readResponse(socket.getInputStream());
	}

	/**
	 * Performs a GET request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * @param path
	 *            a valid path without first slash and without query string. To
	 *            request the root path, set an empty string. Should be properly
	 *            url-encoded.
	 * @param query
	 *            a query string with or without starting ?. Should be properly
	 *            url-encoded.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] get(String path, String query) throws IOException {
		if (query.startsWith("?")) {
			return get(path + query);
		}
		return get(path + '?' + query);
	}

	/**
	 * Performs a GET request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * @param path
	 *            a valid path without first slash and without query string. To
	 *            request the root path, set an empty string. Should be properly
	 *            url-encoded.
	 * @param fields
	 *            a mapping from field name to field value. These should not be
	 *            url-encoded.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] get(String path, Map<String, String> fields)
			throws IOException {
		String query = NetworkUtils.mapToQueryString(fields);
		return get(path + '?' + query);
	}

	/**
	 * Performs a POST request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * Sends data as Content-Type: application/x-www-form-urlencoded
	 * 
	 * @param path
	 *            a valid path without first slash and without query string. To
	 *            request the root path, set an empty string. Should be properly
	 *            url-encoded.
	 * @param fields
	 *            a mapping from field name to field value. These should not be
	 *            url-encoded.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] post(String path, Map<String, String> fields)
			throws IOException {
		openSocket();

		OutputStream out = socket.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

		byte[] content = NetworkUtils.mapToQueryString(fields).getBytes(
				StandardCharsets.UTF_8);

		writer.write("POST /" + path + " HTTP/1.1\r\n");
		writer.write("User-Agent: " + useragent + "\r\n");
		writer.write("Host: " + host + "\r\n");
		writer.write("Content-Type: application/x-www-form-urlencoded\r\n");
		writer.write("Content-Length: " + content.length + "\r\n");
		if (customHeaders.size() > 0) {
			writer.write(getCustomHeaders());
		}
		writer.write("\r\n");
		writer.flush();

		out.write(content);
		out.flush();

		return readResponse(socket.getInputStream());
	}

	/**
	 * Performs a POST request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * Sends data as Content-Type: application/x-www-form-urlencoded
	 * 
	 * @param path
	 *            a valid path without first slash and without query string. To
	 *            request the root path, set an empty string. Should be properly
	 *            url-encoded.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] post(String path) throws IOException {
		return post(path, new HashMap<String, String>(0));
	}

	/**
	 * Performs a POST request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * Sends data as Content-Type: multipart/form-data
	 * 
	 * @param path
	 *            a valid path without first slash and without query string. To
	 *            request the root path, set an empty string. Should be properly
	 *            url-encoded.
	 * @param formFields
	 *            a mapping from field name to field value. These should not be
	 *            url-encoded.
	 * @param fileFields
	 *            a mapping from field name to InputFile. These values should
	 *            not be url-encoded.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] post(String path, Map<String, String> formFields,
			Map<String, InputFile> fileFields) throws IOException {
		openSocket();

		OutputStream out = socket.getOutputStream();
		out = System.out;
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

		String boundary = generateBoundary();
		byte[] content = buildMultipart(boundary, formFields, fileFields);

		writer.write("POST /" + path + " HTTP/1.1\r\n");
		writer.write("User-Agent: " + useragent + "\r\n");
		writer.write("Host: " + host + "\r\n");
		writer.write("Content-Type: multipart/form-data; boundary=" + boundary
				+ "\r\n");
		writer.write("Content-Length: " + content.length + "\r\n");
		if (customHeaders.size() > 0) {
			writer.write(getCustomHeaders());
		}
		writer.write("\r\n");
		writer.flush();

		out.write(content);
		out.flush();

		return readResponse(socket.getInputStream());
	}

	/**
	 * Performs a POST request and returns the response body as a byte array and
	 * sets the status and responseHeader members.
	 * 
	 * Sends data as Content-Type: multipart/form-data
	 * 
	 * @param path
	 *            a valid path without first slash and without query string. To
	 *            request the root path, set an empty string. Should be properly
	 *            url-encoded.
	 * @param formFields
	 *            a mapping from field name to field value. These should not be
	 *            url-encoded.
	 * @param fileFieldName
	 *            the name of the field to use for the file to be sent.
	 * @param file
	 *            the file to be sent.
	 * @return response body as a byte array, or empty byte array.
	 * @throws IOException
	 *             if an error occurred
	 */
	public byte[] post(String path, Map<String, String> formFields,
			String fileFieldName, InputFile file) throws IOException {
		Map<String, InputFile> fileFields = new HashMap<String, InputFile>();
		fileFields.put(fileFieldName, file);

		return post(path, formFields, fileFields);
	}

	/**
	 * Prints the status of the last received response and its headers.
	 */
	public void debugPrint() {
		if (status == null) {
			System.out.println();
			System.out.println("-- DEBUG OUTPUT: NO REQUEST MADE YET --");
			System.out.println();
			return;
		}

		System.out.println();
		System.out.println("-- START DEBUG OUTPUT --");
		System.out.println("Status of last received response: " + status);
		System.out.println("Headers: ");
		for (Entry<String, String> entry : responseHeaders.entrySet()) {
			System.out.println(" " + entry.getKey() + ": " + entry.getValue());
		}
		System.out.println("-- END DEBUG OUTPUT --");
		System.out.println();
	}
}
