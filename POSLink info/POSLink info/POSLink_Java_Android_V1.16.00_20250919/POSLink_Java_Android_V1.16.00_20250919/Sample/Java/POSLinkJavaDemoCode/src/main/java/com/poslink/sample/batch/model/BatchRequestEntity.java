package com.poslink.sample.batch.model;


/**
 * Created by Leon.F on 2018/2/7.
 */

public class BatchRequestEntity {
    private String commandType;

    public BatchRequestEntity() {
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
