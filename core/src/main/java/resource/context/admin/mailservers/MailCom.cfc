component implements="MailServer" {
	/**
	* label of the mail server.
	* @return get the label of the mail server.
	*/
	public string function getLabel() {return "MailCom";}

	/**
	* description of the mail server.
	* @return get the description of the mail server.
	*/
	public string function getDescription() {return "Mail.com is a web portal and web-based email service provider owned by the German internet company United Internet. It offers news articles and videos and a free webmail application with unlimited storage";}

	/**
	* host name of the mail server.
	* @return get the host name of the mail server.
	*/
	public string function getHost() {return "smtp.mail.com";}
	
	/**
	* Port of the mail server.
	* @return get the Port of the mail server.
	*/
	public number function getPort() {return 587;}

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
	public string function getShortName() {return "MailCom";}
}