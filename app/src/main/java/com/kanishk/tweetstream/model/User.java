package com.kanishk.tweetstream.model;

public class User {
	
	private String screen_name;
	
	private String name;
	
	private String profile_image_url;

	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getProfile_image_url() {
		return profile_image_url;
	}

	public void setProfile_background_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
