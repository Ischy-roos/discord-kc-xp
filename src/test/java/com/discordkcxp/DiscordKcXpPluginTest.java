package com.discordkcxp;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DiscordKcXpPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DiscordKcXpPlugin.class);
		RuneLite.main(args);
	}
}