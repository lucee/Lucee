package lucee.transformer.bytecode.statement;

import java.util.List;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Body;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.statement.Statement;

/**
 * Represents a tag island in cfscript - tag code embedded between ``` delimiters
 */
public final class TagIsland extends StatementBaseNoFinal implements Body {

	private Body body;

	public TagIsland(Factory f, Position start, Position end) {
		super(f, start, end);
		this.body = new BodyBase(f);
	}

	public Body getBody() {
		return body;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		// Write out the body contents
		body.writeOut(bc);
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "TagIsland");

		// Dump body as array of statements
		Array bodyArr = new ArrayImpl();
		List<Statement> statements = body.getStatements();
		for (Statement stmt : statements) {
			Struct stmtSct = new StructImpl(Struct.TYPE_LINKED);
			stmt.dump(stmtSct);
			bodyArr.appendEL(stmtSct);
		}
		sct.setEL("body", bodyArr);
	}

	// Body interface methods - delegate to inner body
	@Override
	public void addStatement(Statement statement) {
		body.addStatement(statement);
	}

	@Override
	public void addFirst(Statement statement) {
		body.addFirst(statement);
	}

	@Override
	public List<Statement> getStatements() {
		return body.getStatements();
	}

	@Override
	public boolean hasStatements() {
		return body.hasStatements();
	}

	@Override
	public boolean isEmpty() {
		return body.isEmpty();
	}

	@Override
	public void setParent(Statement parent) {
		super.setParent(parent);
		body.setParent(parent);
	}

	@Override
	public void addPrintOut(Factory factory, String str, Position start, Position end) {
		body.addPrintOut(factory, str, start, end);
	}

	@Override
	public void moveStatmentsTo(Body trg) {
		body.moveStatmentsTo(trg);
	}

	@Override
	public void remove(Statement stat) {
		body.remove(stat);
	}
}
