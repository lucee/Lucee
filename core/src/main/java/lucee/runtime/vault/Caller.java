package lucee.runtime.vault;

public class Caller {

	public static StackTraceElement caller(int index) {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		return st[index];
	}

}
