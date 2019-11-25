package net.ryu.ultimateraces.managers;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class UsersData {
    @Getter private UUID uuid;
    @Getter @Setter private String race;
    @Getter @Setter private boolean chat;

    public UsersData(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "UsersData{" +
                "uuid=" + this.uuid +
                ", race=" + this.race +
                ", chat=" + this.chat +
                "}";
    }
}
