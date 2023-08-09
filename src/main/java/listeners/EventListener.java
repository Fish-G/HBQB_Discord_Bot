package listeners;

import commands.CommandManager;
import game.Bowl;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.HBQB;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EventListener extends ListenerAdapter {
    private final List<Bowl> games = HBQB.getGameList();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // check if message is sent in a channel where a game is in session
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel()) && b.isInPlayerList(event.getAuthor())) {
                b.checkAnswer(event.getMessage().getContentRaw(), event.getAuthor());
            }
        }
    }
}
