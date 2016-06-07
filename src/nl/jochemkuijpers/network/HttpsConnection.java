package nl.jochemkuijpers.network;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

/**
 * Sends GET, POST and POST multipart requests over a secure HTTPS connection.
 * 
 * Please refer to the LICENSE file for the license corresponding to this code.
 * 
 * @author Jochem Kuijpers
 */
public class HttpsConnection extends Connection {
	private final static int DESTINATION_PORT = 443;

	/**
	 * @see Connection#Connection(String,String)
	 */
	public HttpsConnection(String host, String useragent) {
		super(host, DESTINATION_PORT, useragent);
	}

	/**
	 * @see Connection#Connection(String)
	 */
	public HttpsConnection(String host) {
		super(host, DESTINATION_PORT);
	}

	@Override
	protected Socket createSocket() throws IOException {
		SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		return ssf.createSocket();
	}
}
