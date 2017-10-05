package com.go2group.recipients;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.notification.NotificationRecipient;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.notification.recipients.AbstractNotificationRecipient;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugin.descriptor.NotificationRecipientModuleDescriptor;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.event.Event;
import com.go2group.hipchat.components.HipChatProxyClient;
import com.go2group.hipchat.components.HipChatProxyClient.JSONString;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HipchatRecipient extends AbstractNotificationRecipient implements NotificationRecipient.RequiresPlan,
		NotificationRecipient.RequiresResultSummary, NotificationRecipient.RequiresEvent {
	private static final Logger log = Logger.getLogger(HipchatRecipient.class);
	private static final String ROOMS = "rooms";
	private static final String NOTIFY_USERS = "notifyUsers";
	private static final String HIPCHAT_API_TOKEN = "hipchat.api.token";
	private static final String HIPCHAT_SERVER_URL = "hipchat.server.url";

	private String[] rooms = null;
	private String[] selectedRooms = null;
	private boolean notify = false;

	private TemplateRenderer templateRenderer;
	private AdministrationConfigurationManager administrationConfigurationManager;
	private HipChatProxyClient hipChatProxyClient;

	private ImmutablePlan plan;
	private ResultsSummary resultsSummary;
	private Event event;

	@Override
	public void populate(@NotNull Map<String, String[]> params) {
		if (params.containsKey(ROOMS)) {
			this.setRooms(params.get(ROOMS));
		}

		this.notify = params.containsKey(NOTIFY_USERS);
	}

	@Override
	public void init(@Nullable String configurationData) {
		if (configurationData != null) {
			int firstIdx = configurationData.indexOf('|');
			if (firstIdx > 0) {
				notify = configurationData.substring(0, firstIdx).equals("true");
				selectedRooms = configurationData.substring(firstIdx + 1).split(",");
			}
		}
	}

	@NotNull
	@Override
	public String getRecipientConfig() {
		// We can do this because API tokens don't have | in them, but it's
		// pretty dodge. Better to JSONify or something?
		return String.valueOf(notify) + '|' + (rooms != null ? Joiner.on(",").join(rooms) : "");
	}

	@NotNull
	@Override
	public String getEditHtml() {
		String editTemplateLocation = ((NotificationRecipientModuleDescriptor) getModuleDescriptor()).getEditTemplate();
		return templateRenderer.render(editTemplateLocation, populateContext());
	}

	private Map<String, Object> populateContext() {
		Map<String, Object> context = Maps.newHashMap();
		final AdministrationConfiguration administrationConfiguration = administrationConfigurationManager
				.getAdministrationConfiguration();
		String token = administrationConfiguration.getSystemProperty(HIPCHAT_API_TOKEN);
		String serverUrl = administrationConfiguration.getSystemProperty(HIPCHAT_SERVER_URL);
		JSONString roomString = hipChatProxyClient.getRooms(serverUrl, token);
		JSONObject roomObj;
		try {
			roomObj = new JSONObject(roomString.toString());
			JSONArray roomArray = roomObj.getJSONArray(ROOMS);
			rooms = new String[roomArray.length()];
			for (int i = 0; i < roomArray.length(); i++) {
				rooms[i] =  roomArray.getJSONObject(i).getString("name");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (rooms != null) {
			context.put(ROOMS, rooms);
		}
		if (selectedRooms != null){
			context.put("selectedRooms", selectedRooms);
		} else {
			context.put("selectedRooms", new String[0]);
		}
		context.put(NOTIFY_USERS, notify);
		return context;
	}

	@NotNull
	@Override
	public String getViewHtml() {
		String editTemplateLocation = ((NotificationRecipientModuleDescriptor) getModuleDescriptor()).getViewTemplate();
		return templateRenderer.render(editTemplateLocation, populateContext());
	}

	@NotNull
	@Override
	public List<NotificationTransport> getTransports() {
		final AdministrationConfiguration administrationConfiguration = administrationConfigurationManager
				.getAdministrationConfiguration();
		String token = administrationConfiguration.getSystemProperty(HIPCHAT_API_TOKEN);
		String serverUrl = administrationConfiguration.getSystemProperty(HIPCHAT_SERVER_URL);
		List<NotificationTransport> list = Lists.newArrayList();
		for (String room : selectedRooms) {
			list.add(new HipchatNotificationTransport(token, serverUrl, room, notify, resultsSummary));
		}
		return list;
	}

	@Override
	public void setEvent(@org.jetbrains.annotations.Nullable final Event event) {
		this.event = event;
	}

	@Override
	public void setResultsSummary(@org.jetbrains.annotations.Nullable final ResultsSummary resultsSummary) {
		this.resultsSummary = resultsSummary;
	}

	// -----------------------------------Dependencies
	public void setTemplateRenderer(TemplateRenderer templateRenderer) {
		this.templateRenderer = templateRenderer;
	}

	public void setAdministrationConfigurationManager(
			AdministrationConfigurationManager administrationConfigurationManager) {
		this.administrationConfigurationManager = administrationConfigurationManager;
	}

	public void setHipChatProxyClient(HipChatProxyClient hipChatProxyClient) {
		this.hipChatProxyClient = hipChatProxyClient;
	}

	public String[] getRooms() {
		return rooms;
	}

	public void setRooms(String[] rooms) {
		this.rooms = rooms;
	}

	public String[] getSelectedRooms() {
		return selectedRooms;
	}

	public void setSelectedRooms(String[] selectedRooms) {
		this.selectedRooms = selectedRooms;
	}

	@Override
	public void setPlan(ImmutablePlan plan) {
		this.plan = plan;
	}
}
