component extends="AI" {
	variables.fields = [

		group("Endpoint", "Define a predefined or custom endpoint for your OpenAI connection.")
		/*,field(displayName = "URL",
			name = "url",
			defaultValue = "",
			required = false,
			description = "Custom URL/Endpoint for the Gemini REST API, including host, port, and script path, such as [https://generativelanguage.googleapis.com/v1/].",
			type = "text"
		)*/
		,field(displayName = "Enable Beta Access",
            name = "beta",
            defaultValue = "true",
            required = false,
            description = "Unlock access to the latest pre-release models and emerging capabilities. 
			Enable this option if you intend to use features that are currently in the preview phase. Ignored with custom URL.",
            type = "checkbox"
        )
		,group("Security", "Provide access credentials to connect to Gemini. This is required to authenticate your requests.")
		,field(displayName = "API Key",
			name = "apikey",
			defaultValue = "",
			required = true,
			description = "The API key required to access Gemini. You can use environment variables such as ${MY_SECRET_KEY} for secure storage.",
			type = "password"
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
        ,field(displayName = "Conversation History Limit",
            name = "conversationSizeLimit",
            defaultValue = "100",
            required = true,
            description = "Maximum number of question-answer pairs to keep in the conversation history. Once reached, older messages will be removed.",
            type = "select",
            values = "10,50,100,200,500"
        )
        ,field(displayName = "Temperature",
            name = "temperature",
            defaultValue = "0.7",
            required = false,
            description = "Controls response randomness (0.0 to 1.0). Lower values make responses more focused and deterministic, higher values make them more creative and varied.",
            type = "select",
            values = "0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0"
        )
		,group("Timeout", "")
		,field(displayName = "Connect Timeout",
			name = "connectTimeout",
			defaultValue = "2000",
			required = true,
			description = "Maximum time to establish initial connection (milliseconds).",
			type = "select", 
			values = "500,1000,2000,3000,5000"
		)
		,field(displayName = "Socket Timeout", 
			name = "socketTimeout",
			defaultValue = "20000",
			required = true,
			description = "Maximum time between data packets after connection (milliseconds).",
			type = "select",
			values = "5000,10000,20000,30000,60000,120000,180000,300000"
		)
	];


	public string function getClass() {
		return "lucee.runtime.ai.google.GeminiEngine";
	}

	public string function getLabel() {
		return "Gemini";
	}

	public string function getDescription() {
		return "Connect to Google's Gemini AI models (https://ai.google.dev) through their official API.";
	}
}
