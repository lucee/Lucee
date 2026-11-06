
package lucee.transformer;

import java.util.List;

import lucee.transformer.statement.Statement;

/**
 * Body tag (Statement collector)
 */
public interface Body extends Statement {

	/**
	 * adds a statement to the Page
	 * 
	 * @param statement
	 */
	public abstract void addFirst(Statement statement);

	public abstract void addStatement(Statement statement);

	/**
	 * returns all statements
	 * 
	 * @return the statements
	 */
	public abstract boolean hasStatements();

	public abstract List<Statement> getStatements();

	/**
	 * move all statements to target body
	 * 
	 * @param trg
	 */
	public abstract void moveStatmentsTo(Body trg);

	/**
	 * returns if body has content or not
	 * 
	 * @return is empty
	 */
	public abstract boolean isEmpty();

	public void addPrintOut(Factory f, String str, Position start, Position end);

	/**
	 * Adds bare template text. {@code raw} is the original source of that text, with no surrounding quotes.
	 * A null {@code raw} uses {@code str}.
	 */
	public void addPrintOut(Factory f, String str, String raw, Position start, Position end);

	public void remove(Statement stat);
}