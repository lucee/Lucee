package lucee.commons.io.retirement;

import java.io.OutputStream;

public interface RetireListener {
	public void retire(OutputStream os);
}
