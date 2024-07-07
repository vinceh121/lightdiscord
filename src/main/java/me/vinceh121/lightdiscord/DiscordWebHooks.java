package me.vinceh121.lightdiscord;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;

public class DiscordWebHooks {
	public static final String DEFAULT_AVATAR = "https://i.vinceh121.me/YHod4Tr8.png";
	public static final String DEFAULT_PLAYER_AVATAR_URL = "https://mc-heads.net/avatar/{uuid}/128";
	private static final Logger LOG = LogManager.getLogger(DiscordWebHooks.class);
	private static final Path CONFIG_PATH = Path.of("config", "lightdiscord.properties");
	private static final Properties CONFIG = new Properties();
	private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	private static final Gson GSON = new Gson();

	private static boolean ready;

	public static void postWebhook(WebhookBody body) {
		if (!ready) {
			LOG.warn("Trying to post webhook message when client isn't ready. Check config.");
			return;
		}

		final String bodyStr = GSON.toJson(body);
		final HttpRequest req = HttpRequest.newBuilder(URI.create(CONFIG.getProperty("webhook")))
				.POST(BodyPublishers.ofString(bodyStr))
				.header("user-agent", "LightDiscord/SNAPSHOT")
				.header("content-type", "application/json")
				.build();

		CLIENT.sendAsync(req, BodyHandlers.discarding()).whenCompleteAsync((res, t) -> {
			if (t != null) {
				LOG.error("Failed to post Webhook message", t);
			}
		});
	}

	public static String getAvatar(GameProfile profile) {
		return getProperty("avatar_url", DEFAULT_PLAYER_AVATAR_URL)
				.replaceAll(Pattern.quote("{uuid}"), profile.getId().toString())
				.replaceAll(Pattern.quote("{username}"), profile.getName());
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return Boolean.parseBoolean(getProperty(key, Boolean.toString(defaultValue)));
	}

	public static String getMessage(String key, String defaultValue) {
		return getProperty("message." + key, defaultValue);
	}

	public static String getProperty(String key) {
		return CONFIG.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return CONFIG.getProperty(key, defaultValue);
	}

	public static void load() throws IOException {
		if (Files.notExists(CONFIG_PATH)) {
			Files.writeString(CONFIG_PATH, """
					webhook=
					""");
		} else {
			CONFIG.load(Files.newInputStream(CONFIG_PATH));

			if (isValidUrl(CONFIG.getProperty("webhook"))) {
				ready = true;
			}
		}
	}

	private static boolean isValidUrl(String url) {
		if (url == null) {
			return false;
		}

		try {
			URI.create(url).toURL();
			return true;
		} catch (IllegalArgumentException | MalformedURLException e) {
			return false;
		}
	}
}
