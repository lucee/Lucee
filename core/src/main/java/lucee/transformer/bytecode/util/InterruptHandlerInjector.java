/**
 * The goal of this helper is mostly to add a hooked bytecode to allow safe interruption of threads avoiding
 * Tomcat ThreadDeath and the thread.stop() codepath
 */
package lucee.transformer.bytecode.util;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import lucee.transformer.bytecode.util.Types;

public final class InterruptHandlerInjector {
	private static final Type TYPE_THREAD = Type.getType(Thread.class);
	private static final Type TYPE_EXCEPTION = Type.getType(InterruptedException.class);
	private static final Method METHOD_INTERRUPTED = new Method("interrupted", Type.BOOLEAN_TYPE, new Type[] {});

	public static int writeLoopInit(GeneratorAdapter adapter) {
		final int toIt = adapter.newLocal(Types.ITERATOR);
		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);
		return toIt;
	}

	public static void writeLoopBodyEnd(GeneratorAdapter adapter, int iteratorRef, Label jumpLabel, String timeoutDescription) {
		// count the loop, only check for interruptions once every 10K iterations
		adapter.iinc(iteratorRef, 1);
		adapter.loadLocal(iteratorRef);
		adapter.push(10000);
		adapter.ifICmp(Opcodes.IFLT, jumpLabel);
		// reset counter
		adapter.push(0);
		adapter.storeLocal(iteratorRef);
		// Check if the thread is interrupted
		writePreempt(adapter, jumpLabel, timeoutDescription);
	}

	public static void writeEndPreempt(GeneratorAdapter adapter, String timeoutDescription) {
		Label endPreempt = new Label();
		writePreempt(adapter, endPreempt, timeoutDescription);
		adapter.visitLabel(endPreempt);
	}

	public static void writePreempt(GeneratorAdapter adapter, Label label, String timeoutDescription) {
		// Check if the thread is interrupted
		adapter.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to endPreempt
		adapter.ifZCmp(Opcodes.IFEQ, label);
		// Thread interrupted, throw Interrupted Exception
		adapter.throwException(TYPE_EXCEPTION, "Timeout Exception ".concat(timeoutDescription));
	}
}