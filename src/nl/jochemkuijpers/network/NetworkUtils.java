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
	
	public static String readLineUnbuffered(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) > 0) {
			if (b == '\r') {
				in.read();
				break;
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

	public static String urlEncode(String key) {
		try {
			return URLEncoder.encode(key, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(ENCODING + " not supported.");
		}
	}
}
