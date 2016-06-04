package nl.jochemkuijpers.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Sends GET, POST and POST multipart requests over a plaintext HTTP connection.
 * 
 * Please refer to the LICENSE file for the license corresponding to this code.
 * 
 * @author Jochem Kuijpers
 */
public class HttpConnection extends Connection {
	private final static int DESTINATION_PORT = 80;

	/**
	 * @see Connection#Connection(String,String,boolean)
	 */
	public HttpConnection(String host, String useragent, boolean keepalive) {
		super(host, useragent, keepalive);
	}

	/**
	 * @see Connection#Connection(String,String)
	 */
	public HttpConnection(String host, String useragent) {
		super(host, useragent);
	}

	/**
	 * @see Connection#Connection(String)
	 */
	public HttpConnection(String host) {
		super(host);
	}

	@Override
	protected void openSocket() throws UnknownHostException, IOException {
		if (socket != null) {
			if (socket.isConnected() && !socket.isClosed()) {
				return;
			}
		}

		socket = new Socket(host, DESTINATION_PORT);
	}

}
