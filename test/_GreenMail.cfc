component javaSettings='{
	"maven": [
		"com.icegreen:greenmail:2.1.7"
	]
}' {
	import "com.icegreen.greenmail.util.ServerSetup";
	import "com.icegreen.greenmail.util.GreenMail";

	public any function smtpOnly( required numeric port ) {
		return new GreenMail( [ new ServerSetup( arguments.port, nullValue(), ServerSetup::PROTOCOL_SMTP ) ] );
	}

	public any function withPop( required numeric smtpPort, required numeric popPort, required numeric popsPort ) {
		return new GreenMail( [
			new ServerSetup( arguments.smtpPort, nullValue(), ServerSetup::PROTOCOL_SMTP )
			,new ServerSetup( arguments.popPort, nullValue(), ServerSetup::PROTOCOL_POP3 )
			,new ServerSetup( arguments.popsPort, nullValue(), ServerSetup::PROTOCOL_POP3S )
		] );
	}

	// greenmail counts a latch down when the SMTP manager accepts a message, so this blocks until
	// the mail lands instead of polling the mailbox
	public array function waitForIncoming( required any greenMail, required numeric count, numeric timeout = 10000 ) {
		if ( !arguments.greenMail.waitForIncomingEmail( arguments.timeout, arguments.count ) ) {
			throw "greenmail did not receive #arguments.count# message(s) within #arguments.timeout#ms";
		}
		return arguments.greenMail.getReceivedMessages();
	}

	// for mail greenmail does not own: a POP inbox on the standalone server, or .eml files on disk
	public any function waitFor( required any probe, required numeric timeout, numeric tick = 25 ) {
		var end = getTickCount() + arguments.timeout;
		while ( true ) {
			var result = arguments.probe();
			if ( !isNull( result ) ) return result;
			if ( getTickCount() > end ) throw "timed out after #arguments.timeout#ms waiting for mail";
			sleep( tick );
		}
	}
}
