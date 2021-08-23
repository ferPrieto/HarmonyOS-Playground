package com.fprieto.wearable.model;

public class DataMessage {
    private String messageType;
    private PlayerCommand playerCommand;

    public PlayerCommand getPlayerCommand() {
        return playerCommand;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setPlayerCommand(PlayerCommand playerCommand) {
        this.playerCommand = playerCommand;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
