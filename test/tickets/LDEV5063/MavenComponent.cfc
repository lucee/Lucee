component javasettings='{maven:["software.amazon.awssdk:auth:2.31.16", "software.amazon.awssdk:identity-spi:2.31.16"]}' {

	import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

	function test(){
		return AwsBasicCredentials::create( "test", "test" ).getClass().getName();
	}

}
