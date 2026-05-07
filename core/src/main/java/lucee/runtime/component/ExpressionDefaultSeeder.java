package lucee.runtime.component;

import lucee.runtime.PageContext;

/** Implemented by emitted CFC classes that declare cfproperty expression-form defaults. */
public interface ExpressionDefaultSeeder {

	void _seedExpressionDefaults(PageContext pc) throws Throwable;
}
