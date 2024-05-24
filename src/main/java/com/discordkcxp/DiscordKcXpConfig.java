package com.discordkcxp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("discordkcxp")
public interface DiscordKcXpConfig extends Config
{
	@ConfigItem(
			keyName = "webhookUrl",
			name = "Discord Webhook URL",
			description = "The URL of the Discord webhook to send messages to"
	)
	default String webhookUrl()
	{
		return "";
	}

	@ConfigItem(
			keyName = "experienceThreshold",
			name = "Experience Threshold",
			description = "The amount of experience to gain before sending a notification"
	)
	default int experienceThreshold()
	{
		return 5000;
	}
}
