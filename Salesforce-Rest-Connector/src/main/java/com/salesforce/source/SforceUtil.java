package com.salesforce.source;

import java.util.Properties;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.salesforce.Token;
import org.salesforce.Util;

import com.google.gson.Gson;

public class SforceUtil extends Util {

	private Properties _props = new Properties();

	private String _accessToken = "";

	public SforceUtil(Properties properties) {
		this._props = properties;
	}

	@Override
	public String queryUrl() {
		return _props.getProperty("QueryUrl", "/services/data/v47.0/query/?q=");
	}
	
	@Override
	public String baseUrl() {
		return _props.getProperty("BaseUrl", "/services/data/v47.0/sobjects/"); 
	}
	
	@Override
	public String getHost() {
		return _props.getProperty("host", "https://ap2.salesforce.com");
	}

	@Override
	public String getAccessToken() {

		if (_accessToken.equals("")) {
			try {
				String UserName = _props.getProperty("username");
				String PassWord = _props.getProperty("password");
				String LoginURL = _props.getProperty("LoginURL", "https://login.salesforce.com");
				String GrantService = _props.getProperty("GrantService", "/services/oauth2/token?grant_type=password");
				String ClientID = _props.getProperty("ClientID");
				String ClientSecret = _props.getProperty("ClientSecret");
				String loginURL = LoginURL + GrantService + "&client_id=" + ClientID + "&client_secret=" + ClientSecret
						+ "&username=" + UserName + "&password=" + PassWord;

				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(loginURL);
				BasicResponseHandler handler = new BasicResponseHandler();
				CloseableHttpResponse response = client.execute(post);
				String body = handler.handleResponse(response);
				Gson gson = new Gson();
				Token jsonObject = gson.fromJson(body, Token.class);
				_accessToken = jsonObject.access_token();

			} catch (Exception e) {
			}
		}

		return _accessToken;
	}

}
