package com.slg.module.config;

@ConfigManager.DataConfig(path = "group.cfg")
public class GroupConfig extends GameConfig {
    @ConfigManager.Id
    private final int groupId;
    private final String groupName;

    public GroupConfig() {
        this.groupId = 0;
        this.groupName = "";
    }


    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return this.groupId + ":" + this.groupName;
    }
}
