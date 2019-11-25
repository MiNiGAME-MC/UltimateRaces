package net.ryu.ultimateraces.api;

import net.ryu.ultimateraces.managers.UsersData;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface UltimateRacesAPI {

    /**
     * Allows you to access to the users
     * data stored within the plug-in.
     * @param uuid = player's unique id
     * @param action
     */
    void getUser(UUID uuid, Consumer<UsersData> action);

    /**
     * Allows you to get all the users
     * stored within the plug-in.
     * @return
     */
    Map<UUID, UsersData> getUsers();
}
