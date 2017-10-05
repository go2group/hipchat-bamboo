package com.go2group.recipients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.utils.HttpUtils;

public class HipchatNotificationTransport implements NotificationTransport {
	private static final Logger log = Logger.getLogger(HipchatNotificationTransport.class);
	public static final String HIPCHAT_API_URL = "/v1/rooms/message?auth_token=";

	private final String apiToken;
	private final String serverUrl;
	private final String room;
	private final String from = "Bamboo";
	private final boolean notify;

	private final HttpClient client;

	private final ResultsSummary resultsSummary;

	public HipchatNotificationTransport(String apiToken, String serverUrl, String room, boolean notify, ResultsSummary resultsSummary) {
		this.apiToken = apiToken;
		this.serverUrl = serverUrl;
		this.room = room;
		this.notify = notify;
		this.resultsSummary = resultsSummary;
		client = new HttpClient();

		try {
			URI uri = new URI(serverUrl + HIPCHAT_API_URL);
			setProxy(client, uri.getScheme());
		} catch (URIException e) {
			log.error("Unable to set up proxy settings, invalid URI encountered: " + e);
		} catch (URISyntaxException e) {
			log.error("Unable to set up proxy settings, invalid URI encountered: " + e);
		}
	}

	@Override
	public void sendNotification(Notification notification) {

		String message = (notification instanceof Notification.HtmlImContentProvidingNotification) ? ((Notification.HtmlImContentProvidingNotification) notification)
				.getHtmlImContent() : notification.getIMContent();

		if (!StringUtils.isEmpty(message)) {
			PostMethod method = setupPostMethod();
			method.setParameter("message", message);
			if (resultsSummary != null) {
				setMessageColor(method, resultsSummary);
			} else {
				setMessageColor(method, "yellow"); // todo: might need to use
													// different color in some
													// cases
			}

			try {
				client.executeMethod(method);
			} catch (IOException e) {
				log.error("Error using Hipchat API: " + e.getMessage(), e);
			}
		}
	}

	private void setMessageColor(PostMethod method, ResultsSummary result) {
		String color = "yellow";

		if (result.getBuildState() == BuildState.FAILED) {
			color = "red";
		} else if (result.getBuildState() == BuildState.SUCCESS) {
			color = "green";
		}

		setMessageColor(method, color);
	}

	private void setMessageColor(PostMethod method, String colour) {
		method.addParameter("color", colour);
	}

	private PostMethod setupPostMethod() {
		PostMethod m = new PostMethod(serverUrl + HIPCHAT_API_URL + apiToken);
		m.addParameter("room_id", room);
		m.addParameter("from", from);
		m.addParameter("notify", (notify ? "1" : "0"));
		return m;
	}

	private static void setProxy(@NotNull final HttpClient client, @Nullable final String scheme) throws URIException {
		HttpUtils.EndpointSpec proxyForScheme = HttpUtils.getProxyForScheme(scheme);
		if (proxyForScheme != null) {
			client.getHostConfiguration().setProxy(proxyForScheme.host, proxyForScheme.port);
		}
	}
}
