package com.go2group.actions;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.go2group.hipchat.components.HipChatProxyClient;
import com.go2group.hipchat.components.InvalidAuthTokenException;

public class HipChatConfigurationPlugin extends GlobalAdminAction {

	private static final String HIPCHAT_API_TOKEN = "hipchat.api.token";
	private static final String HIPCHAT_SERVER_URL = "hipchat.server.url";
	private String token;
	private String serverUrl;

	protected HipChatProxyClient hipChatProxyClient;

	@Override
	public String doInput() throws Exception {
		final AdministrationConfiguration administrationConfiguration = administrationConfigurationManager
				.getAdministrationConfiguration();
		token = administrationConfiguration.getSystemProperty(HIPCHAT_API_TOKEN);
		serverUrl = administrationConfiguration.getSystemProperty(HIPCHAT_SERVER_URL);
		if (serverUrl == null) {
			serverUrl = "https://api.hipchat.com";
		}
		return INPUT;
	}

	@Override
	public String doExecute() throws Exception {
		final AdministrationConfiguration administrationConfiguration = administrationConfigurationManager
				.getAdministrationConfiguration();
		try {
			this.hipChatProxyClient.getRooms(serverUrl, token);
			administrationConfiguration.setSystemProperty(HIPCHAT_API_TOKEN, token);
			administrationConfiguration.setSystemProperty(HIPCHAT_SERVER_URL, serverUrl);
			administrationConfigurationManager.saveAdministrationConfiguration(administrationConfiguration);
			addActionMessage("Changes has been saved successfully.");
		} catch (InvalidAuthTokenException ie) {
			ie.printStackTrace();
			addActionError("Oops! There was error retrieving the rooms with the token you supplied. Are you sure it is a valid \"admin\" token?");
		}
		return SUCCESS;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	public void setHipChatProxyClient(HipChatProxyClient hipChatProxyClient){
		this.hipChatProxyClient = hipChatProxyClient;
	}

}
