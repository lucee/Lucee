component implements="MailServer" {
	/**
	* label of the mail server.
	* @return get the label of the mail server.
	*/
	public string function getLabel() {return "iCloud Mail (me.com)";}

	/**
	* description of the mail server.
	* @return get the description of the mail server.
	*/
	public string function getDescription() {return "iCloud Mail is a free email service from Apple with ample storage, IMAP access and an elegantly functional web application.
That interface at icloud.com does not offer labels or other more advanced tools for productivity and for organizing mail, though, and does not support accessing other email accounts. POP access to iCloud Mail is missing, too.";}

	/**
	* host name of the mail server.
	* @return get the host name of the mail server.
	*/
	public string function getHost() {return "imap.mail.me.com";}
	
	/**
	* Port of the mail server.
	* @return get the Port of the mail server.
	*/
	public number function getPort() {return 993;}

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
	public string function getShortName() {return "iCloud";}
}