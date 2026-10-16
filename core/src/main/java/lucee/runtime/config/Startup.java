package lucee.runtime.config;

import lucee.runtime.db.ClassDefinition;

public class Startup {
	public final ClassDefinition<?> cd;
	public final Object instance;

	public Startup(ClassDefinition<?> cd, Object instance) {
		this.cd = cd;
		this.instance = instance;
	}
}