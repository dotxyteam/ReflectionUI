package xy.reflect.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import xy.reflect.ui.util.SystemProperties;

public class MicroAgentExample {

	private String hawkDoamin;
	private String service;
	private String network;
	private String daemon;
	private boolean intialized = false;

	public String getHawkDoamin() {
		return hawkDoamin;
	}

	public void setHawkDoamin(String hawkDoamin) {
		this.hawkDoamin = hawkDoamin;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getDaemon() {
		return daemon;
	}

	public void setDaemon(String daemon) {
		this.daemon = daemon;
	}

	public boolean isIntialized() {
		return intialized;
	}

	public void initialize() {
		intialized = true;
	}

	public void shutdown() {
		intialized = false;
	}

	public String invoke(String microAgentName, String machineName,
			String methodName, String columnName, String... arguments) {
		return SimpleDateFormat.getTimeInstance().format(new Date());
	}

	public String listMicroAgentnames() {
		return invoke(null, null, null, null);
	}

	public static void main(String[] args) {
		System.setProperty(SystemProperties.HIDE_NULLABLE_FACETS, "true");
		ReflectionUI.main(new String[] { MicroAgentExample.class.getName() });
	}

}
