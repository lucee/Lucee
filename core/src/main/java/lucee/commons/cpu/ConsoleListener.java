package lucee.commons.cpu;

import java.util.Iterator;
import java.util.List;

import lucee.aprint;
import lucee.commons.cpu.CPULogger.StaticData;

public class ConsoleListener implements Listener {

	private boolean showStacktrace;

	public ConsoleListener(boolean showStacktrace) {
		this.showStacktrace = showStacktrace;
	}

	@Override
	public void listen(List<StaticData> staticData) {

		Iterator<StaticData> it = staticData.iterator();
		StaticData data;
		aprint.e("----------------------------------------");
		while (it.hasNext()) {
			data = it.next();
			aprint.e("-----");
			aprint.e("name: " + data.name);
			// print.e("time: " + data.time);
			aprint.e("percentage: " + data.getPercentage());
			if (showStacktrace) aprint.e(data.getStacktrace());
			// print.e("total: " + data.total);
		}
	}

}
