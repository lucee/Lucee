package lucee.runtime.util;

import java.io.InputStream;
import java.net.URL;

import lucee.commons.io.res.Resource;

/**
 * Detects the mime type of content. Lucee provides a simple built-in implementation (file extension
 * map and magic bytes), an extension (for example the Tika extension) can replace it by declaring a
 * mimetype implementation in its manifest.
 */
public interface MimeTypeDetector {

	/**
	 * detect the mime type of a resource, based on its content and name
	 *
	 * @param res resource to check
	 * @param defaultValue value returned if the type could not be detected
	 * @return mime type
	 */
	public String getMimeType(Resource res, String defaultValue);

	/**
	 * detect the mime type of a resource, based on its content and the given file name
	 *
	 * @param res resource to check
	 * @param fileName file name used instead of the name of the resource, can be null
	 * @param defaultValue value returned if the type could not be detected
	 * @return mime type
	 */
	public String getMimeType(Resource res, String fileName, String defaultValue);

	/**
	 * detect the mime type based on the content only, the extension is not checked
	 *
	 * @param barr the (first) bytes of the content
	 * @param defaultValue value returned if the type could not be detected
	 * @return mime type
	 */
	public String getMimeType(byte[] barr, String defaultValue);

	/**
	 * detect the mime type based on the content only, the extension is not checked, the stream is not
	 * closed
	 *
	 * @param is stream to read from
	 * @param defaultValue value returned if the type could not be detected
	 * @return mime type
	 */
	public String getMimeType(InputStream is, String defaultValue);

	/**
	 * detect the mime type based on a file name only
	 *
	 * @param fileName name or path of the file
	 * @param defaultValue value returned if the type could not be detected
	 * @return mime type
	 */
	public String getMimeType(String fileName, String defaultValue);

	/**
	 * detect the mime type of the content of an URL
	 *
	 * @param url url to check
	 * @param defaultValue value returned if the type could not be detected
	 * @return mime type
	 */
	public String getMimeType(URL url, String defaultValue);
}
