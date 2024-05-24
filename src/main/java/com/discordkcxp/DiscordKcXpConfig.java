package com.discordkcxp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("discordkcxp")
public interface DiscordKcXpConfig extends Config
{
	@ConfigSection(
		name = "Webhook URL",
		description = "The URL for the Discord webhook",
		position = 0,
		closedByDefault = false
	)
	String webhookUrl = "webhookUrl";

	@ConfigItem(
		keyName = "webhookUrl",
		name = "Discord Webhook URL",
		description = "The URL of the Discord webhook to send messages to",
		section = webhookUrl,
		position = 0
	)
	default String webhookUrl()
	{
		return "";
	}

	@ConfigSection(
			name = "Kill Count Settings",
			description = "The Settings for the kill count notifications",
			position = 1,
			closedByDefault = false
	)
	String kcSettings = "kcSettings";

	@ConfigItem(
		keyName = "includeKillCount",
		name = "Send Kill Count Notifications",
		description = "Send a Discord message when you a kill count gets updated. Requires the option Filter out boss kill-count: with spam-filter to be deactivated in your settings",
		section = kcSettings,
		position = 0
	)
	default boolean includeKillCount() {
		return true;
	}

	@ConfigSection(
			name = "Experience Gains Settings",
			description = "The Settings for the Exp gains notifications",
			position = 2,
			closedByDefault = false
	)
	String xpSettings = "xpSettings";

	@ConfigItem(
			keyName = "includeXpGains",
			name = "Send Xp Gains Notifications",
			description = "Send a Discord message when you pass the set xp threshold.",
			section = xpSettings,
			position = 0
	)
	default boolean includeXpGains() {
		return true;
	}

	@ConfigItem(
			keyName = "include99",
			name = "Send lvl 99 Notifications",
			description = "Send a Discord message when you achieve a level 99.",
			section = xpSettings,
			position = 1
	)
	default boolean include99() {
		return true;
	}

	@ConfigItem(
		keyName = "experienceThreshold",
		name = "Experience Threshold",
		description = "The amount of experience to gain before sending a notification",
		section = xpSettings,
		position = 2
	)
	default int experienceThreshold()
	{
		return 5000;
	}
}
