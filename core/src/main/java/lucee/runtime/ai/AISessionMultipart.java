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
public interface AISessionMultipart extends AISession {

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

	// FUTURE use inte loader interface AISessionMultiParts and forget AISessionMultipart
	public static AISessionMultipart toAISessionMultipart(AISession ais) {
		// AISessionMultiParts
		if (ais instanceof AISessionMultipart) return (AISessionMultipart) ais;

		// Try to detect AISessionMultiParts *without* a hard dependency
		try {
			ClassLoader cl = ais.getClass().getClassLoader();
			Class<?> multiPartsClass = Class.forName("lucee.runtime.ai.AISessionMultiParts", false, cl);

			if (multiPartsClass.isInstance(ais)) {
				// IMPORTANT: createAISessionMultipartWrap must NOT use AISessionMultiParts
				// in its signature or imports – just take AISession or Object.
				return AISessionMultipartWrapper.createAISessionMultipartWrap(ais);
			}
		}
		catch (ClassNotFoundException e) {
			// Older loader: interface doesn't exist at all → just ignore and fall through
		}

		// Fallback: whatever makes sense in your case
		// (throw, wrap as “single part”, etc.)
		throw new IllegalArgumentException("AISession is neither AISessionMultipart nor (optionally) AISessionMultiParts: " + ais.getClass().getName());
	}
}