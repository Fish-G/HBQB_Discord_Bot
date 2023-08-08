package org.example;

import commands.CommandManager;
import game.Bowl;
import io.github.cdimascio.dotenv.Dotenv;
import listeners.EventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

@Component
public class HBQB {
    private final Dotenv config;
    private final ShardManager shardManager;
    private static final List<Bowl> gameList = new ArrayList<>();
    public static List<Bowl> getGameList() {
        return gameList;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Dotenv getConfig() {
        return config;
    }

    public HBQB(MongoTemplate mongo) throws LoginException { // no calls to hbqb how do we know where mongo even is?

        config = Dotenv.configure().load();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("hb"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);

        shardManager = builder.build();

        //register listeners
        shardManager.addEventListener(new EventListener(), new CommandManager(mongo));
    }

}