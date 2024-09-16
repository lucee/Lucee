component extends="AI" {
	variables.fields = [
		group("Security", "Provide access credentials to connect to Gemini. This is required to authenticate your requests.")
		,field(displayName = "API Key",
			name = "apikey",
			defaultValue = "",
			required = true,
			description = "The API key required to access Gemini. You can use environment variables such as ${MY_SECRET_KEY} for secure storage.",
			type = "text"
		)
		,group("Fine-Tune", "Customize settings to fine-tune your AI session.")
		,field(displayName = "Model",
			name = "model",
			defaultValue = "",
			required = true,
			description = "Specify the model to use, for example 'gemini-large' or 'gemini-small' for Google Gemini.",
			type = "text"
		)
		,field(displayName = "System Message",
			name = "message",
			defaultValue = "",
			required = true,
			description = "The initial system message sent to the AI when starting a new session.",
			type = "textarea"
		)
		,field(displayName = "Timeout",
			name = "timeout",
			defaultValue = "2000",
			required = true,
			description = "Set the timeout duration for AI responses in milliseconds.",
			type = "select",
			values = "500,1000,2000,3000,5000,10000"
		)
	];


	public string function getClass() {
		return "lucee.runtime.ai.google.GeminiEngine";
	}

	public string function getLabel() {
		return "Gemini";
	}

	public string function getDescription() {
		return "The Gemini interface enables integration with Google's AI models, offering powerful natural language processing capabilities.";
	}	
}
