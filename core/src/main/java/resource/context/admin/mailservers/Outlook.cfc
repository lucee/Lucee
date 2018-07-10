component implements="MailServer" {
	/**
	* label of the mail server.
	* @return get the label of the mail server.
	*/
	public string function getLabel() {return "Outlook Mail (Hotmail)";}

	/**
	* description of the mail server.
	* @return get the description of the mail server.
	*/
	public string function getDescription() {return "Outlook Mail offers a rich email experience on the web and can be accessed via IMAP, POP and Exchange ActiveSync. Outlook Mail automatically bans spam, and the web interface collects clutter and is smart about large file attachments.";}

	/**
	* host name of the mail server.
	* @return get the host name of the mail server.
	*/
	public string function getHost() {return "smtp-mail.outlook.com";}
	
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
	public boolean function useSSL() {return true;}

	/**
	* Returns shortname for this mail server.
	* @return do return shortname for this mail server.
	* */
	public string function getShortName() {return "Outlook";}
}