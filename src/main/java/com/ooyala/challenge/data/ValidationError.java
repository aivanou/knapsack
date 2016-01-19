package com.ooyala.challenge.data;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ValidationError {
    private List<String> messages;

    public ValidationError(List<String> messages) {
        this.messages = messages;
    }

    public ValidationError(String message) {
        this.messages = new ArrayList<>();
        messages.add(message);
    }

    public void addMessage(String msg) {
        messages.add(msg);
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override public String toString() {
        StringBuilder bldr = new StringBuilder();
        for (String message : messages) {
            bldr.append(message);
            bldr.append("\n");
        }
        return bldr.toString();
    }
}
