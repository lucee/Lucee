package lucee.runtime.ai;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

/**
 * The AIEngine interface defines the core functionalities required for an AI engine, including
 * initialization, invocation, and lifecycle management methods.
 */
public interface AIEngine {

	/**
	 * Initializes the AI engine with the specified properties.
	 *
	 * @param properties a Struct containing the properties for initialization.
	 * @return an instance of the AIEngine after initialization.
	 * @throws PageException if an error occurs during initialization.
	 */
	AIEngine init(AIEngineFactory factory, Struct properties, String initalMessage) throws PageException;

	/**
	 * Invokes the AI engine with the specified request.
	 *
	 * @param req the Request object containing the questions to be processed.
	 * @return a Response object containing the answers from the AI engine.
	 * @throws PageException if an error occurs during invocation.
	 */
	Response invoke(String message) throws PageException;

	public Conversation[] getHistory();

	public AIEngineFactory getFactory();

	public String getId();
}
