package com.zi.vu.depro.model;

/**
 * Describes what a message sent to chatGPT means.
 * Will be used together with:
 * @see Message
 */
public enum Role {

    /**
     * When this role is chosen, chatGPT will know that it is a question from a user.
     */
    user,
    /**
     * When this role is chosen, chatGPT will know that it is a content from a programmer of the chat's behaviour.
     */
    system

}
