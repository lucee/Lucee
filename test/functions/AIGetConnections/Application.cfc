component {

	this.name = "AIGetConnections-test";

	this.ai = {
		"aigetconnections-test": {
			"class": "lucee.runtime.ai.openai.OpenAIEngine",
			"default": "aigetconnectionstest",
			"custom": {
				"type": "openai",
				"secretKey": "aigetconnections-secret-value",
				"model": "aigetconnections-model",
				"conversationSizeLimit": 7,
				"connectTimeout": 1234,
				"socketTimeout": 5678
			}
		},
		"aigetconnections-nodefault": {
			"class": "lucee.runtime.ai.openai.OpenAIEngine",
			"custom": {
				"type": "openai",
				"secretKey": "aigetconnections-secret-value",
				"model": "aigetconnections-model-2"
			}
		}
	};

	// replaces the connection with the same name from the config (aimock), when there is one
	if ( len( url.override ?: "" ) ) {
		this.name &= "-override";
		this.ai[ url.override ] = {
			"class": "lucee.runtime.ai.openai.OpenAIEngine",
			"custom": { "type": "openai", "secretKey": "aigetconnections-secret-value", "model": "aigetconnections-override" }
		};
	}

}
