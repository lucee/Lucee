package lucee.commons.cpu;

import java.util.List;

import lucee.commons.cpu.CPULogger.StaticData;

public interface Listener {

	public void listen(List<StaticData> list);

}
