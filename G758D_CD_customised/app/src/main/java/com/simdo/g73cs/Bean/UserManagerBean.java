package com.simdo.g73cs.Bean;

import org.json.JSONException;
import org.json.JSONObject;

public class UserManagerBean {
	public UserManagerBean() {
		this.account = "000000000000000";
		this.description = "000000";
	}


	String account;
	String description;

	public UserManagerBean(String account, String description) {
		this.account = account;
		this.description = description;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static UserManagerBean fromJson(JSONObject jsonObject) {
		UserManagerBean imsiBean = new UserManagerBean();
		try {
			if (jsonObject.has("ACCOUNT")) {
				imsiBean.setAccount(jsonObject.getString("ACCOUNT"));
			}
			if (jsonObject.has("DESCRIPTION")) {
				imsiBean.setDescription(jsonObject.getString("DESCRIPTION"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return imsiBean;
	}
}
