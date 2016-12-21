interface {
	/**
	* label of the mail server.
	* @return get the label of the mail server.
	*/
	public string function getLabel();
	/**
	* description of the mail server.
	* @return get the description of the mail server.
	*/
	public string function getDescription();
	
	/**
	* host name of the mail server.
	* @return get the host name of the mail server.
	*/
	public string function getHost();
	
	/**
	* Port of the mail server.
	* @return get the Port of the mail server.
	*/
	public number function getPort();

	/**
	* Enable Transport Layer Security.
	* @return do enable Transport Layer Security.
	*/
	public boolean function useTLS();

	/**
	* Enable secure connections via SSL.
	* @return do enable secure connections via SSL.
	*/
	public boolean function useSSL();

	/**
	* Returns shortname for this mail server.
	* @return do return shortname for this mail server.
	* */
	public string function getShortName();
}