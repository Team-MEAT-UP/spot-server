package com.meetup.server.global.support.error.discord;

import com.meetup.server.global.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Profile({"prod", "stg"})
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordAlarmSender {

    @Value("${discord.error-alert-url}")
    private String errorAlertUrl;

    private static final RestClient restClient = RestClient.create();

    private final Environment environment;

    public void sendErrorAlert(Exception exception) {
        String content = ":rotating_light: [" + getEnvironment() + "] 서버 예외 발생";
        List<DiscordRequest.Embed> embeds = List.of(
                DiscordRequest.Embed.of("Exception", exception.getClass().getSimpleName()),
                DiscordRequest.Embed.of("Message", exception.getMessage()),
                DiscordRequest.Embed.of("Timestamp", TimeUtil.formatAsDateTime(LocalDateTime.now())),
                DiscordRequest.Embed.of("StackTrace", parseStackTrace(exception))
        );

        DiscordRequest discordRequest = DiscordRequest.of(content, embeds);

        try {
            restClient.post()
                    .uri(errorAlertUrl)
                    .body(discordRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("[DiscordAlarmSender] Discord 메시지 전송 실패: {}", e.getMessage());
        }
    }

    private String getEnvironment() {
        return Optional.ofNullable(environment.getActiveProfiles()[0]).map(String::toUpperCase).orElse("-");
    }

    private String parseStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 2000) break;
        }
        return sb.toString();
    }
}
