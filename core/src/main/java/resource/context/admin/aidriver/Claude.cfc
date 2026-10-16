component extends="AI" {
	variables.fields = [
		group("Endpoint", "Configure the endpoint for your Claude API connection.")
		
		,field(displayName = "URL",
			name = "url",
			defaultValue = "https://api.anthropic.com/v1/",
			required = false,
			description = "Custom URL for the Claude API. Only required if using a custom environment.",
			type = "text"
		)
		,group("Authentication", "Provide your Anthropic API key for authentication.")
		,field(displayName = "API Key",
			name = "apiKey",
			defaultValue = "",
			required = true,
			description = "Your Anthropic API key. You can use environment variables like this: ${ANTHROPIC_API_KEY}.",
			type = "password"
		)
		,group("Configuration", "Customize your Claude integration settings.")
		,field(displayName = "Model",
			name = "model",
			defaultValue = "claude-3-sonnet-20240229",
			required = true,
			description = "Specify the Claude model to use. See https://docs.anthropic.com/claude/docs/models-overview for available models.",
			type = "text"
		)
		,field(displayName = "System Message",
			name = "message",
			defaultValue = "",
			required = true,
			description = "Initial system message for the conversation.",
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
		return "lucee.runtime.ai.anthropic.ClaudeEngine";
	}

	public string function getLabel() {
		return "Claude";
	}

	public string function getDescription() {
		return "Connect to Anthropic's Claude AI models (https://claude.ai/) through their official API.";
	}
}