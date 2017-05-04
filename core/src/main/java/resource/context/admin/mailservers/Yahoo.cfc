component implements="MailServer" {
	/**
	* label of the mail server.
	* @return get the label of the mail server.
	*/
	public string function getLabel() {return "Yahoo! Mail";}

	/**
	* description of the mail server.
	* @return get the description of the mail server.
	*/
	public string function getDescription() {return "Yahoo! Mail is your ubiquitous email program on the web and mobile devices with ample storage, SMS texting, social networking and instant messaging to boot.";}

	/**
	* host name of the mail server.
	* @return get the host name of the mail server.
	*/
	public string function getHost() {return "smtp.mail.yahoo.com";}
	
	/**
	* Port of the mail server.
	* @return get the Port of the mail server.
	*/
	public number function getPort() {return 465;}

	/**
	* Enable Transport Layer Security.
	* @return do enable Transport Layer Security.
	*/
	public boolean function useTLS() {return false;}

	/**
	* Enable secure connections via SSL.
	* @return do enable secure connections via SSL.
	*/
	public boolean function useSSL() {return false;}

	/**
	* Returns shortname for this mail server.
	* @return do return shortname for this mail server.
	* */
	public string function getShortName() {return "Yahoo";}
}