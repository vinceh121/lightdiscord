package me.vinceh121.lightdiscord;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class LightDiscord implements ModInitializer, DedicatedServerModInitializer {
	public static final Logger LOG = LoggerFactory.getLogger(LightDiscord.class);

	@Override
	public void onInitialize() {
	}

	@Override
	public void onInitializeServer() {
		try {
			DiscordWebHooks.load();
		} catch (IOException e) {
			LOG.error("Failed to load LightDiscord config", e);
		}

		DiscordWebHooks.postWebhook(new WebhookBody(DiscordWebHooks.getMessage("startup", "Server is online"),
				DiscordWebHooks.getMessage("username", "Minecraft"),
				DiscordWebHooks.getProperty("defaultAvatar", DiscordWebHooks.DEFAULT_AVATAR)));

		ServerPlayConnectionEvents.INIT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
			final GameProfile profile = handler.player.getGameProfile();
			final String username = profile.getName();

			DiscordWebHooks.postWebhook(new WebhookBody(
					DiscordWebHooks.getMessage("join", "%s joined the game").formatted(username), username, profile));
		});

		ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
			final GameProfile profile = handler.player.getGameProfile();
			final String username = profile.getName();

			DiscordWebHooks.postWebhook(new WebhookBody(
					DiscordWebHooks.getMessage("left", "%s left the game").formatted(username), username, profile));
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			DiscordWebHooks.postWebhook(new WebhookBody(DiscordWebHooks.getMessage("startup", "Server is offline"),
					DiscordWebHooks.getMessage("username", "Minecraft"),
					DiscordWebHooks.getProperty("defaultAvatar", DiscordWebHooks.DEFAULT_AVATAR)));
		}));
	}
}
