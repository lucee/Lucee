component {
	public function test() {
		var myRecord = DnsRecordservice.new( {
			domain_id 	 :: oDnsDomain.getId(),
			type         : templateRecord.type,
			content      : newContent,
			ttl          : templateRecord.ttl,
			shortName    : templateRecord.name
		} );
	}
}