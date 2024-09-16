component extends="AI" {
	variables.fields = [
		group("Endpoint", "Define a predefined or custom endpoint for your OpenAI connection.")
		,field(displayName = "Type",
			name = "type",
			defaultValue = "other",
			required = true,
			description = "Select one of the predefined endpoints for this interface, or choose 'other' to specify a custom URL below.",
			type = "select",
			values = "other,chatgpt,ollama"
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
			type = "text"
		)
		,group("Fine-Tune", "Customize settings to fine-tune your AI session.")
		,field(displayName = "Model",
			name = "model",
			defaultValue = "",
			required = true,
			description = "Specify the model to use, e.g., 'gpt-4o-mini' for OpenAI or 'gemma2' for Ollama.",
			type = "text"
		)
		,field(displayName = "System Message",
			name = "message",
			defaultValue = "",
			required = true,
			description = "Initial system message sent to the AI when initializing a session.",
			type = "textarea"
		)
		,field(displayName = "Timeout",
			name = "timeout",
			defaultValue = "2000",
			required = true,
			description = "Set the session timeout duration in milliseconds.",
			type = "select",
			values = "500,1000,2000,3000,5000,10000"
		)
	];


	public string function getClass() {
		return "lucee.runtime.ai.openai.OpenAIEngine";
	}

	public string function getLabel() {
		return "OpenAI";
	}

	public string function getDescription() {
		return "The OpenAI interface enables integration with AI models like ChatGPT but also supports other engines like Ollama.";
	}
}
