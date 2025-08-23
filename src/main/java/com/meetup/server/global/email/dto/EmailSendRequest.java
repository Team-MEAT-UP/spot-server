package com.meetup.server.global.email.dto;

public record EmailSendRequest(
        String emailAddress,
        String subject,
        String content
) {
    public static EmailSendRequest of(String emailAddress, String subject, String content) {
        return new EmailSendRequest(emailAddress, subject, content);
    }
}
