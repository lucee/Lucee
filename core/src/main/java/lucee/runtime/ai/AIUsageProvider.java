package lucee.runtime.ai;

/**
 * Optional interface for a {@link Response} that can tell the detailed token usage (LDEV-6527).
 * Responses that do not implement it only tell the total with {@link Response#getTotalTokenUsed()}.
 */
public interface AIUsageProvider {

	/**
	 * @return the usage reported by the provider, null if the provider did not report any usage
	 */
	public AIUsage getUsage();

	/**
	 * @return the model the provider says it used, null if not reported
	 */
	public String getResponseModel();
}
