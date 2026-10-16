package lucee.runtime.ai;

import java.util.List;

import lucee.runtime.exp.PageException;

/**
 * Extended AI session interface that supports multipart inquiries.
 * <p>
 * This interface extends AISession to allow sending multiple content parts (text, images, files) in
 * a single inquiry. Implementations can be checked at runtime to determine if multipart
 * functionality is available.
 */
public interface AISessionMultiParts extends AISession {

	/**
	 * Sends multiple content parts to the AI service and retrieves the response.
	 *
	 * @param parts List of Part objects containing text, images, or other content
	 * @return A Response object containing the AI's reply
	 * @throws PageException If the inquiry fails or multipart content is not supported
	 */
	public Response inquiry(List<Part> parts) throws PageException;

	/**
	 * Sends multiple content parts to the AI service with a streaming listener.
	 *
	 * @param parts List of Part objects containing text, images, or other content
	 * @param listener Listener for streaming responses
	 * @return A Response object containing the AI's reply
	 * @throws PageException If the inquiry fails or multipart content is not supported
	 */
	public Response inquiry(List<Part> parts, AIResponseListener listener) throws PageException;

}
