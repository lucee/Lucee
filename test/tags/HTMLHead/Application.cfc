component {

	this.name	=	Hash( GetCurrentTemplatePath() );

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}