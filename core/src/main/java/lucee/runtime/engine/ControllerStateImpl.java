package lucee.runtime.engine;

public class ControllerStateImpl implements ControllerState {

	private boolean active;

	public ControllerStateImpl(boolean active) {
		this.active = active;
	}

	@Override
	public boolean active() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
