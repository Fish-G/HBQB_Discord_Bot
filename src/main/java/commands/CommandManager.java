package commands;

import game.Bowl;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.example.HBQB;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    private final List<Bowl> games = HBQB.getGameList();

    MongoTemplate mongo;
    public CommandManager(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {//runs every time a bot joins the sever

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) { //runs on bot startup
        List<CommandData> commandData = new ArrayList<>();


        commandData.add(Commands.slash("makegame","Start a new game of Quiz bowl")
                .addOption(OptionType.STRING,"team1","Name of team 1", true)
                .addOption(OptionType.STRING,"team2", "Name of team 2", true)
                .addOptions(
                        new OptionData(OptionType.STRING,"difficulty", "Difficulty (default hs)")
                                .addChoice("HS","hs")
                                .addChoice("MS","ms")
                                .addChoice("Collegiate","collegiate")
                )

        );

        commandData.add(Commands.slash("jointeam", "Join a team if a game is about to start")
                .addOption(OptionType.STRING, "teamname", "name of team you want to join",true)
        );

        commandData.add(Commands.slash("startgame", "Start the game (locks teams)"));

        commandData.add(Commands.slash("scorecheck", "check current score"));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        switch (event.getName()) {
            case "makegame" -> makeGame(event);
            case "jointeam" -> joinTeam(event);
            case "startgame" -> startGame(event);
            case "scorecheck" -> displayScore(event);
        }
    }

    private void displayScore(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                //game found
                event.reply(b.getTeam1Name() + " : " + b.getTeam1Score() + "\n" + b.getTeam2Name() + " : " + b.getTeam2Score()).queue();
                return;
            }
        }
        event.reply("No existing game is happening in this channel").queue();
    }

    private void startGame(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                b.startMatch(event.getChannel());
                event.reply(b.displayTeams() + "\nStarting match").queue();
            }
        }
    }

    private void joinTeam(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                //game found
                if (event.getOption("teamname").getAsString().equals(b.getTeam1Name())) {
                    b.addTeam1(event.getUser());
                    event.reply("Successfully joined " + b.getTeam1Name()).queue();
                } else if (event.getOption("teamname").getAsString().equals(b.getTeam2Name())) {
                    b.addTeam2(event.getUser());
                    event.reply("Successfully joined " + b.getTeam2Name()).queue();
                } else {
                    event.reply("Failed to find team name, check spelling.\n team names are: " + b.getTeam1Name() + ", " + b.getTeam2Name()).queue();
                }
                return;
            }
        }
        event.reply("Failed to find match, check if a match has been created in this channel.").queue();
    }

    private void makeGame(SlashCommandInteractionEvent event) {
        if (event.getOption("team1").getAsString().equals(event.getOption("team2").getAsString())) {
            event.reply("the two teams cannot have the same name").queue();
            return;
        }

        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                event.reply("game already exists in this channel").queue();
                return;
            }
        }

        games.add(new Bowl(mongo, event.getChannel(),event.getOption("team1").getAsString(),event.getOption("team2").getAsString()));
        event.reply("game has been created -> **" + event.getOption("team1").getAsString() + "** vs **" + event.getOption("team2").getAsString() + "**").queue();
    }

}
