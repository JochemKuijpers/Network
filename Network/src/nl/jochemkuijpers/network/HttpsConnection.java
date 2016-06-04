package nl.jochemkuijpers.network;

import java.io.IOException;
import java.net.UnknownHostException;

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
	 * @see Connection#Connection(String,String,boolean)
	 */
	public HttpsConnection(String host, String useragent, boolean keepalive) {
		super(host, useragent, keepalive);
	}

	/**
	 * @see Connection#Connection(String,String)
	 */
	public HttpsConnection(String host, String useragent) {
		super(host, useragent);
	}

	/**
	 * @see Connection#Connection(String)
	 */
	public HttpsConnection(String host) {
		super(host);
	}

	@Override
	protected void openSocket() throws UnknownHostException, IOException {
		if (socket != null) {
			if (socket.isConnected() && !socket.isClosed()) {
				return;
			}
		}

		SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		socket = ssf.createSocket(host, DESTINATION_PORT);
	}
}
