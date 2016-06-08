package nl.jochemkuijpers.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Various utility methods used in this package.
 * 
 * Please refer to the LICENSE file for the license corresponding to this code.
 * 
 * @author Jochem Kuijpers
 */
public class NetworkUtils {
	private final static String ENCODING = StandardCharsets.UTF_8.name();

	/**
	 * Reads a line ending with "\r\n" or "\n" from an InputStream without
	 * buffering.
	 * 
	 * "\r" is not seen as a line ending unless followed by "\n", so the
	 * returned string may contain "\r" characters if they are not followed up
	 * by "\n"
	 * 
	 * @param in
	 *            input stream
	 * @return the next line excluding newline characters
	 * @throws IOException
	 *             if an error occurred
	 */
	public static String readLineUnbuffered(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) > 0) {
			if (b == '\r') {
				if ((b = in.read()) > 0) {
					if (b == '\n') {
						break;
					} else {
						out.write('\r');
						out.write(b);
						continue;
					}
				} else {
					out.write('\r');
					break;
				}
			}
			if (b == '\n') {
				break;
			}
			out.write(b);
		}
		try {
			return out.toString(ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(ENCODING + " not supported.");
		}
	}

	/**
	 * Creates a query string from a field name to value mapping, without ?
	 * prefix.
	 * 
	 * @param fields
	 *            field name to value mapping
	 * @return query string
	 */
	public static String mapToQueryString(Map<String, String> fields) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Entry<String, String> entry : fields.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append('&');
			}
			sb.append(NetworkUtils.urlEncode(entry.getKey()));
			sb.append('=');
			sb.append(NetworkUtils.urlEncode(entry.getValue()));
		}
		return sb.toString();
	}

	/**
	 * URL-encodes a string. UTF-8 encoding is assumed.
	 * 
	 * @param str
	 *            input string
	 * @return url-encoded string
	 */
	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(ENCODING + " not supported.");
		}
	}
}
