package org.example;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class HBQB {
    private final String TOKEN = "MTEzNzkwMjc5OTA3NDYzOTg3Mw.GgoJFd.UCkKBjXGZWjef1ii6Ku1jCKRC8LHXUxVmIJMsg";

    private final ShardManager shardManager;

    public static void main(String[] args) {
        try {
            HBQB bot = new HBQB();
        } catch (LoginException e) {
            System.out.println("error: invalid token");
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public HBQB() throws LoginException {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(TOKEN);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("hb"));

        shardManager = builder.build();

    }
}