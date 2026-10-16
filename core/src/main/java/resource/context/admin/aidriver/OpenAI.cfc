component extends="AI" {
	variables.fields = [
		group("Endpoint", "Define a predefined or custom endpoint for your OpenAI connection.")
		,field(displayName = "Type",
			name = "type",
			defaultValue = "other",
			required = true,
			description = "Select one of the predefined endpoints for this interface, or choose 'other' to specify a custom URL below.",
			type = "select",
			values = "other,chatgpt,deepseek,grok,ollama,perplexity"
		)
		,field(displayName = "URL",
			name = "url",
			defaultValue = "",
			required = false,
			description = "Custom URL/Endpoint for the OpenAI REST API, including host, port, and script path, such as [http://localhost:11434/v1/].",
			type = "text"
		)
		,group("Security", "Provide access credentials for the AI engine. This is required for OpenAI, but may not be necessary for Ollama.")
		,field(displayName = "Secret Key",
			name = "secretKey",
			defaultValue = "",
			required = true,
			description = "Secret key for accessing the AI engine. You can use environment variables like this: ${MY_SECRET_KEY}.",
			type = "password"
		)
		,group("Fine-Tune", "Customize settings to fine-tune your AI session.")
		,field(displayName = "Model",
			name = "model",
			defaultValue = "",
			required = false,
			description = "Specify the model to use, e.g., 'gpt-4o-mini' for OpenAI or 'gemma2' for Ollama. If no model is defined, Lucee picks one. in edit view of this page, you get a list of all avilable models.",
			type = "text"
		)
		,field(displayName = "System Message",
			name = "message",
			defaultValue = "",
			required = true,
			description = "Initial system message sent to the AI when initializing a session.",
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
		return "lucee.runtime.ai.openai.OpenAIEngine";
	}

	public string function getLabel() {
		return "OpenAI";
	}

	public string function getLabelLong() {
		return "OpenAI (ChatGPT, Copilot, Deepseek, Grok, Ollama, Perplexity)";
	}

	public string function getDescription() {
		return "Connect to OpenAI's models (https://platform.openai.com) and other AI providers that support the OpenAI-compatible REST API (like Copilot, Deepseek, Grok, Ollama, Perplexity). For Copilot use ""other"", because every user has a custom URL."
	}
}