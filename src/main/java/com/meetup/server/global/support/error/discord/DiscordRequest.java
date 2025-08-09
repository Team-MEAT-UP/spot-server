package com.meetup.server.global.support.error.discord;

import java.util.List;

public record DiscordRequest(
        String content,
        List<Embed> embeds
) {

    public static DiscordRequest of(String content, List<Embed> embeds) {
        return new DiscordRequest(content, embeds);
    }

    public record Embed(String title, String description) {

        public static Embed of(String title, String description) {
            return new Embed(title, description);
        }
    }
}
