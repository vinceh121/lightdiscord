package me.vinceh121.lightdiscord;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;

public record WebhookBody(String content, String username, @SerializedName("avatar_url") String avatarUrl) {
	public WebhookBody(String content, String username, GameProfile profile) {
		this(content, username, DiscordWebHooks.getAvatar(profile));
	}
}
