package com.discordkcxp;

import com.google.inject.Provides;
import javax.inject.Inject;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.Client;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Skill;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(
	name = "Discord KC and XP Notifier"
)

public class DiscordKcXpPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DiscordKcXpConfig config;

	private int previousTotalXp = 0;
	private final Map<Skill, Integer> previousSkillLevels = new EnumMap<>(Skill.class);

	private final OkHttpClient httpClient = new OkHttpClient();

	@Provides
	DiscordKcXpConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordKcXpConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		previousTotalXp = getTotalExperience();
		initializePreviousSkillLevels();
		log.info("Discord KC XP - started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		httpClient.dispatcher().executorService().shutdown();
		log.info("Discord KC XP - stopped!");
	}

	private Instant loginTime;

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();

		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (loginTime == null)
			{
				loginTime = Instant.now();
			}
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.LOGGING_IN)
		{
			loginTime = null;
		}
	}

	public void onWorldChanged(WorldChanged event)
	{
		loginTime = Instant.now();
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{

		//Prevents a bug where a bunch of notifications get sent when the player logins
		if (hasJustLoggedIn()){
			return;
		}

		//Initialize PLayer name
		String playerName = client.getLocalPlayer().getName();

		//Check if the Xp threshold has been passed
		if(config.includeXpGains()){
			int currentTotalXp = getTotalExperience();
			int experienceThreshold = config.experienceThreshold();
			int previousThreshold = previousTotalXp / experienceThreshold;
			int currentThreshold = currentTotalXp / experienceThreshold;

			if (currentThreshold > previousThreshold)
			{
				String message = String.format("%s's total XP just passed %d experience!", playerName, currentTotalXp);
				log.info(message);
				sendDiscordWebhook(message);
			}

			// Update the previous total XP and skill levels
			previousTotalXp = currentTotalXp;
		}


		// Check for level 99 achievement
		if(config.include99()){
			Skill skill = statChanged.getSkill();
			int previousLevel = previousSkillLevels.getOrDefault(skill, 0);
			int currentLevel = client.getRealSkillLevel(skill);

			if (currentLevel == 99 && previousLevel < 99)
			{
				String message = String.format("%s just achieved level 99 in %s!", playerName, skill.getName());
				log.info(message);
				sendDiscordWebhook(message);
			}

			previousSkillLevels.put(skill, currentLevel);
		}
	}


	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if(config.includeKillCount()){
			String playerName = client.getLocalPlayer().getName();

			if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE)
			{
				return;
			}

			String message = chatMessage.getMessage();
			String regex = "Your .* count is:";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(message);

			if (matcher.find())
			{
				log.info("Kill count message detected: {}", message);
				sendDiscordWebhook(String.format("%s kill count update: %s", playerName, message));
			}
		}
	}

	private void initializePreviousSkillLevels()
	{
		for (Skill skill : Skill.values())
		{
			previousSkillLevels.put(skill, client.getRealSkillLevel(skill));
		}
	}

	private int getTotalExperience()
	{
		int totalXp = 0;
		for (Skill skill : Skill.values())
		{
			totalXp += client.getSkillExperience(skill);
		}
		return totalXp;
	}

	private void sendDiscordWebhook(String message)
	{
		String webhookUrl = config.webhookUrl();
		if (webhookUrl.isEmpty())
		{
			log.warn("Discord webhook URL is not configured.");
			return;
		}

		RequestBody body = new FormBody.Builder()
				.add("content", message)
				.build();

		Request request = new Request.Builder()
				.url(webhookUrl)
				.post(body)
				.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.error("Failed to send message to Discord webhook", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				if (!response.isSuccessful())
				{
					log.error("Unexpected response from Discord webhook: {}", response);
				}
				response.close();
			}
		});
	}

	public boolean hasJustLoggedIn()
	{
		if (loginTime == null)
		{
			return false;
		}

		Duration durationSinceLogin = Duration.between(loginTime, Instant.now());
		return durationSinceLogin.getSeconds() < 3;
	}
}
