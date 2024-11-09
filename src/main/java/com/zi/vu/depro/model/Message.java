package com.zi.vu.depro.model;

/**
 * This is wrapper class that will contain message (request) to ChatGPT.
 * Every message should have the following 2 parameters:
 *
 * @param role        either "user" when sending what user asks (exactly what user types in the chat)
 *                    or "system" when we want to provide additional setup (like "You are English to Polish translator").
 * @param content either exact question from a user or content of how the chat should behave.
 */
public record Message(Role role, String content) {
}
