package commands;

import game.Bowl;
import game.QuestionTags;
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


        commandData.add(Commands.slash("makegame", "Start a new game of Quiz bowl")
                .addOption(OptionType.STRING, "team1", "Name of team 1", true)
                .addOption(OptionType.STRING, "team2", "Name of team 2", true)
                .addOption(OptionType.BOOLEAN, "hs", "High School")
                .addOption(OptionType.BOOLEAN, "ms", "Middle School")
                .addOption(OptionType.BOOLEAN, "college", "Collegiate")
                .addOption(OptionType.BOOLEAN, "science", "Science")
                .addOption(OptionType.BOOLEAN, "philosophy", "Philosophy")
                .addOption(OptionType.BOOLEAN, "literature", "Literature")
                .addOption(OptionType.BOOLEAN, "math", "Math")
                .addOption(OptionType.BOOLEAN, "trash", "Trash")
                .addOption(OptionType.BOOLEAN, "art", "Art")
                .addOption(OptionType.BOOLEAN, "geography", "Geography")
                .addOption(OptionType.BOOLEAN, "miscellaneous", "Miscellaneous")
                .addOption(OptionType.BOOLEAN, "music", "Music")
                .addOption(OptionType.BOOLEAN, "history", "History")
        );

        commandData.add(Commands.slash("jointeam", "Join a team if a game is about to start")
                .addOption(OptionType.STRING, "teamname", "name of team you want to join", true)
        );

        commandData.add(Commands.slash("startgame", "Start the game (locks teams)"));

        commandData.add(Commands.slash("scorecheck", "check current score"));

        commandData.add(Commands.slash("stopgame", "stop current game in channel"));

        commandData.add(Commands.slash("leaveteam", "leave your current team"));

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
            case "stopgame" -> stopGame(event);
            case "leaveteam" -> leaveTeam(event);
        }
    }

    private void leaveTeam(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                b.getTeam1Members().remove(event.getUser());
                b.getTeam2members().remove(event.getUser());
            }
        }
        event.reply("you have left your team").queue();
    }

    private void stopGame(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                b.endMatch();
                games.remove(b);
                event.reply("Game has been stopped").queue();
                return;
            }
        }
        event.reply("No game is currently running in this channel").queue();
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
        List<QuestionTags> tags = new ArrayList<>();
        if (event.getOption("hs") != null && event.getOption("hs").getAsBoolean()) tags.add(QuestionTags.HS);
        if (event.getOption("ms") != null && event.getOption("ms").getAsBoolean()) tags.add(QuestionTags.MS);
        if (event.getOption("college") != null && event.getOption("college").getAsBoolean()) tags.add(QuestionTags.COLLEGE);
        if (event.getOption("science") != null && event.getOption("science").getAsBoolean()) tags.add(QuestionTags.SCIENCE);
        if (event.getOption("philosophy") != null && event.getOption("philosophy").getAsBoolean()) tags.add(QuestionTags.PHILOSOPHY);
        if (event.getOption("literature") != null && event.getOption("literature").getAsBoolean()) tags.add(QuestionTags.LITERATURE);
        if (event.getOption("math") != null && event.getOption("math").getAsBoolean()) tags.add(QuestionTags.MATH);
        if (event.getOption("trash") != null && event.getOption("trash").getAsBoolean()) tags.add(QuestionTags.TRASH);
        if (event.getOption("history") != null && event.getOption("history").getAsBoolean()) tags.add(QuestionTags.HISTORY);
        if (event.getOption("art") != null && event.getOption("art").getAsBoolean()) tags.add(QuestionTags.ART);
        if (event.getOption("geography") != null && event.getOption("geography").getAsBoolean()) tags.add(QuestionTags.GEOGRAPHY);
        if (event.getOption("miscellaneous") != null && event.getOption("miscellaneous").getAsBoolean()) tags.add(QuestionTags.MISCELLANEOUS);
        if (event.getOption("music") != null && event.getOption("music").getAsBoolean()) tags.add(QuestionTags.MUSIC);

        games.add(new Bowl(mongo, event.getChannel(), event.getOption("team1").getAsString(), event.getOption("team2").getAsString(), tags));
        event.reply("game has been created -> **" + event.getOption("team1").getAsString() + "** vs **" + event.getOption("team2").getAsString() + "**").queue();
    }

}
