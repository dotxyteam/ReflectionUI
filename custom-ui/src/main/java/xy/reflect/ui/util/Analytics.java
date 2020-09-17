/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class allows to optionally gather the application usage statistics.
 */
public class Analytics {
	
	public static final Analytics DEFAULT = new Analytics(); 

	private final String WEBSITE_URL = "http://javacollection.net/reflectionui";
	private final String TRACKINGS_DELIVERY_URL = WEBSITE_URL
			+ "/analytics/forward-http-to-google-analytics.php";
	private final int TRACKINGS_TRANSMISSION_PACKET_SIZE = 1000;
	private final String[] NEW_LINE_SEQUENCES = new String[] { "\r\n", "\n",
			"\r" };
	private final String TRACKING_CATEGORY_PREFIX = "ReflectionUI";

	private Thread regularSender;
	private boolean initialized;
	private List<Tracking> trackings = Collections
			.synchronizedList(new ArrayList<Tracking>());

	public void initialize() {
		if (initialized) {
			return;
		}
		startRegularSender();
		initialized = true;
	}

	public void shutdown() {
		if (!initialized) {
			return;
		}
		regularSender.interrupt();
		try {
			regularSender.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		try {
			sendAllTrackings();
		} catch (Throwable t) {
			logError(t);
		}
		initialized = false;
	}

	private void sleepSafely(long durationMilliseconds) {
		try {
			Thread.sleep(durationMilliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void startRegularSender() {
		regularSender = new Thread(Analytics.class.getName() + "RegularSender") {
			@Override
			public synchronized void run() {
				while (true) {
					sleepSafely(1000);
					if (isInterrupted()) {
						break;
					}
					if (trackings.size() >= TRACKINGS_TRANSMISSION_PACKET_SIZE) {
						sendAllTrackings();
					}
				}
			}
		};
		regularSender.setDaemon(true);
		regularSender.setPriority(Thread.MIN_PRIORITY);
		regularSender.start();
	}

	private String escapeNewLines(String s) {
		StringBuilder result = new StringBuilder();
		char lastC = 0;
		for (char currentC : s.toCharArray()) {
			char standardC = standardizeNewLineSequences(lastC, currentC);
			try {
				if (standardC == 0) {
					continue;
				} else if (standardC == '\n') {
					result.append("\\n");
				} else if (standardC == '\\') {
					result.append("\\\\");
				} else {
					result.append(standardC);
				}
			} finally {
				lastC = currentC;
			}
		}
		return result.toString();
	}

	private char standardizeNewLineSequences(char lastC, char c) {
		for (String newLineSequence : NEW_LINE_SEQUENCES) {
			if (newLineSequence.equals("" + lastC + c)) {
				return 0;
			}
		}
		for (String newLineSequence : NEW_LINE_SEQUENCES) {
			if (newLineSequence.startsWith("" + c)) {
				return '\n';
			}
		}
		return c;
	}

	public void track(String event, String... details) {
		for (int i = 0; i < details.length; i++) {
			details[i] = escapeNewLines(details[i]);
		}
		logInfo("Tracking: " + event + " " + Arrays.toString(details));
		trackings.add(new Tracking(new Date(), event, details));
	}

	private void logInfo(String s) {
		System.out.println(s);
	}

	private void logError(Throwable t) {
		System.err.println(t);
	}

	public synchronized void sendAllTrackings() {
		logInfo("Delivering trackings");
		try {
			for (Tracking tracking : trackings) {
				sendTracking(tracking.getDateTime(), tracking.getUsed(),
						tracking.getDetails());
			}
		} catch (Exception e) {
			logError(e);
		} finally {
			trackings.clear();
		}
	}

	private void sendTracking(Date when, String used, String... details) {
		used = anonymize(used);
		for (int i = 0; i < details.length; i++) {
			details[i] = anonymize(details[i]);
		}
		Map<String, String> arguments = new HashMap<String, String>();
		StringBuilder categoryId = new StringBuilder();
		{
			categoryId.append(TRACKING_CATEGORY_PREFIX);
			categoryId.append("-");
			categoryId.append("b" + BuildProperties.get().getId());
			arguments.put("ec", categoryId.toString());
		}
		arguments.put("ea", used);
		arguments.put("qt",
				Long.toString((new Date().getTime() - when.getTime())));
		if (details.length > 0) {
			arguments.put("el", stringJoin(Arrays.asList(details), " - "));
		}
		try {
			arguments.put("cid", hexEncode(System.getProperty("user.name")));
		} catch (Exception e) {
			arguments.put("cid", "UnknownHost");
		}
		try {
			sendHttpPost(new URL(TRACKINGS_DELIVERY_URL), arguments);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private InputStream sendHttpPost(URL url, Map<String, String> arguments)
			throws IOException {
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		List<String> postBuilder = new ArrayList<String>();
		for (Map.Entry<String, String> entry : arguments.entrySet())
			postBuilder.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
					+ URLEncoder.encode(entry.getValue(), "UTF-8"));
		byte[] post = stringJoin(postBuilder, "&").getBytes("UTF-8");
		int length = post.length;
		http.setFixedLengthStreamingMode(length);
		http.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		http.setConnectTimeout(1000);
		http.connect();
		OutputStream outputStream = http.getOutputStream();
		outputStream.write(post);
		return http.getInputStream();
	}

	private String stringJoin(Collection<?> col, String delim) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> iter = col.iterator();
		if (iter.hasNext())
			sb.append(iter.next().toString());
		while (iter.hasNext()) {
			sb.append(delim);
			sb.append(iter.next().toString());
		}
		return sb.toString();
	}

	private String hexEncode(String s) {
		StringBuilder result = new StringBuilder();
		for (byte b : s.getBytes()) {
			result.append(Integer.toHexString(unsignedByte(b)));
		}
		return result.toString();
	}

	private int unsignedByte(byte b) {
		return 0xFF & (int) b;
	}

	private String anonymize(String message) {
		message = message.replaceAll("([Pp]assword)=.*", "$1=**********");
		message = message.replaceAll("([Ll]ogin)=.*", "$1=**********");
		message = message.replaceAll("(user\\..*)=.*", "$1=**********");
		return message;
	}

	public class Tracking implements Serializable {

		private static final long serialVersionUID = 1L;

		private long id;

		private Date dateTime;

		private String used;

		private String[] details;

		public Tracking() {
		}

		public Tracking(Date dateTime, String used, String[] details) {
			super();
			this.dateTime = dateTime;
			this.used = used;
			this.details = details;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public Date getDateTime() {
			return dateTime;
		}

		public void setDateTime(Date dateTime) {
			this.dateTime = dateTime;
		}

		public String getUsed() {
			return used;
		}

		public void setUsed(String used) {
			this.used = used;
		}

		public String[] getDetails() {
			return details;
		}

		public void setDetails(String[] details) {
			this.details = details;
		}

		@Override
		public String toString() {
			return "Tracking [dateTime=" + dateTime + ", used=" + used
					+ ", details=" + Arrays.toString(details) + "]";
		}

	}

	public static void main(String[] args) {
		DEFAULT.initialize();
		DEFAULT.track("used", "details");
		DEFAULT.track("used");
		DEFAULT.shutdown();
	}

}
