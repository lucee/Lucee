component {

	this.name = "LDEV6580";

	// .invalid never resolves (RFC 2606), so without the proxy every request fails with UnknownHostException
	// on 8.0 Claude and Gemini only use the url with type "other", otherwise they call the real API
	this.ai = {
		"ldev6580-openai": {
			"class": "lucee.runtime.ai.openai.OpenAIEngine",
			"custom": { "url": "http://ldev6580-openai.invalid/v1/", "secretKey": "none", "model": "none", "connectTimeout": 2000, "socketTimeout": 2000 }
		},
		"ldev6580-claude": {
			"class": "lucee.runtime.ai.anthropic.ClaudeEngine",
			"custom": { "type": "other", "url": "http://ldev6580-claude.invalid/v1/", "apiKey": "none", "model": "none", "connectTimeout": 2000, "socketTimeout": 2000 }
		},
		"ldev6580-gemini": {
			"class": "lucee.runtime.ai.google.GeminiEngine",
			"custom": { "type": "other", "url": "http://ldev6580-gemini.invalid/v1/", "apiKey": "none", "model": "none", "connectTimeout": 2000, "socketTimeout": 2000 }
		}
	};

}
