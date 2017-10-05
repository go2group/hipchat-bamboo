package com.go2group.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bamboo.notification.NotificationRule;
import com.atlassian.bamboo.notification.NotificationSet;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.go2group.hipchat.components.HipChatProxyClient;
import com.go2group.hipchat.components.InvalidAuthTokenException;
import com.go2group.hipchat.components.HipChatProxyClient.JSONString;

public class Announcements extends GlobalAdminAction {

	private static final String HIPCHAT_API_TOKEN = "hipchat.api.token";
	private static final String HIPCHAT_SERVER_URL = "hipchat.server.url";
	private static final String RECIPIENT_TYPE = "com.go2group.HipChat4Bamboo:hichat.recipient";
	private static final String ROOMS = "rooms";
	private static Logger log = Logger.getLogger(Announcements.class);

	private String token;
	private String serverUrl;

	private String[] rooms;
	private String announcement;
	private String color = "purple";
	private String roomOption;

	private HipChatProxyClient hipChatProxyClient;

	@Override
	public String doInput() throws Exception {
		final AdministrationConfiguration administrationConfiguration = administrationConfigurationManager
				.getAdministrationConfiguration();
		token = administrationConfiguration.getSystemProperty(HIPCHAT_API_TOKEN);
		serverUrl = administrationConfiguration.getSystemProperty(HIPCHAT_SERVER_URL);
		try {
			populateRooms(serverUrl, token);
		} catch (InvalidAuthTokenException ie) {
			ie.printStackTrace();
			addActionError("Oops! There was error retrieving the rooms with the token you supplied. Are you sure it is a valid \"admin\" token?");
		}
		return INPUT;
	}

	private void populateRooms(String serverUrl, String token) {
		JSONString roomString = hipChatProxyClient.getRooms(serverUrl, token);
		JSONObject roomObj;
		try {
			roomObj = new JSONObject(roomString.toString());
			JSONArray roomArray = roomObj.getJSONArray(ROOMS);
			rooms = new String[roomArray.length()];
			for (int i = 0; i < roomArray.length(); i++) {
				rooms[i] = roomArray.getJSONObject(i).getString("name");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			rooms = new String[0];
		}
	}

	@Override
	public String doExecute() throws Exception {
		if (announcement != null) {
			if ("all".equals(roomOption)) {
				JSONString roomString = hipChatProxyClient.getRooms(serverUrl, token);
				JSONObject rooms;
				try {
					rooms = new JSONObject(roomString.toString());
					if (rooms != null) {
						JSONArray roomArray = rooms.getJSONArray("rooms");
						for (int i = 0; i < roomArray.length(); i++) {
							JSONObject room = roomArray.getJSONObject(i);
							this.hipChatProxyClient.notifyRoom(room.getString("room_id"), announcement, color, token,
									serverUrl);
						}
					}
				} catch (JSONException e) {
					log.error("Error parsing room json:" + roomString.toString(), e);
					e.printStackTrace();
				}
			} else if ("subscribed".equals(roomOption)) {
				Set<String> rooms = new HashSet<String>();
				List<TopLevelPlan> plans = getPlanManager().getAllPlans();
				for (TopLevelPlan topLevelPlan : plans) {
					NotificationSet notificationSet = topLevelPlan.getNotificationSet();
					Set<NotificationRule> rules = notificationSet.getNotificationRules();
					for (NotificationRule rule : rules) {
						if (rule.getRecipientType().equals(RECIPIENT_TYPE)) {
							String recipient = rule.getRecipient();
							if (recipient != null) {
								int firstIdx = recipient.indexOf('|');
								if (firstIdx > 0) {
									String[] selectedRooms = recipient.substring(firstIdx + 1).split(",");
									for (String room : selectedRooms) {
										rooms.add(room);
									}
								}
							}
						}
					}
				}
				for (String room : rooms) {
					this.hipChatProxyClient.notifyRoom(room, announcement, color, token, serverUrl);
				}
			} else {
				if (rooms != null) {
					for (String room : rooms) {
						hipChatProxyClient.notifyRoom(room, announcement, color, token, serverUrl);
					}
				}
			}
			addActionMessage("Message sent successfully.");
		}
		populateRooms(serverUrl, token);
		return SUCCESS;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setHipChatProxyClient(HipChatProxyClient hipChatProxyClient) {
		this.hipChatProxyClient = hipChatProxyClient;
	}

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String[] getRooms() {
		return rooms;
	}

	public void setRooms(String[] rooms) {
		this.rooms = rooms;
	}

	public String getRoomOption() {
		return roomOption;
	}

	public void setRoomOption(String roomOption) {
		this.roomOption = roomOption;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
