package lucee.runtime.script;

import java.io.PrintStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import lucee.loader.engine.CFMLEngine;

public class Main {

	private static final String USAGE = "Usage: script [-options]\n\n" + "Where options include:\n" + "-l  language\n" + "-e  code\n";

	public static void main(final String args[]) throws Exception {

		String lang = "CFML";
		String code = null;

		String arg;
		final String pw = null, key = null;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if ("-l".equals(arg)) {
				if (args.length > i + 1) lang = args[++i].trim();
			}
			else if ("-e".equals(arg)) if (args.length > i + 1) code = args[++i].trim();
		}
		final int dialect = CFMLEngine.DIALECT_CFML;
		if (code == null) printUsage("-e is missing", System.err);

		final LuceeScriptEngineFactory factory = new LuceeScriptEngineFactory();
		System.out.println(factory.getScriptEngine().eval(code));

		final ScriptEngine engine = new ScriptEngineManager().getEngineByName(lang);
		if (engine == null) System.out.println("could not load an engine with the name:" + lang);
		else System.out.println(engine.eval(code));

	}

	private static void printUsage(final String msg, final PrintStream ps) {
		ps.println();
		ps.println("Failed to execute!");
		ps.println("Reason: " + msg);
		ps.println();
		ps.print(USAGE);
		ps.flush();

		System.exit(0);
	}
}
