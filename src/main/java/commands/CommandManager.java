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

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    private final List<Bowl> games = new ArrayList<>();


    @Override
    public void onGuildJoin(GuildJoinEvent event) {//runs every time a bot joins the sever

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) { //runs on bot startup
        List<CommandData> commandData = new ArrayList<>();


        commandData.add(Commands.slash("makeGame","Start a new game of Quiz bowl")
                .addOptions(
                        new OptionData(OptionType.STRING,"difficulty", "Difficulty (default hs)")
                                .addChoice("HS","hs")
                                .addChoice("MS","ms")
                                .addChoice("Collegiate","collegiate")
                )
                .addOption(OptionType.STRING,"team1","Name of team 1", true)
                .addOption(OptionType.STRING,"team2", "Name of team 2", true)
        );

        commandData.add(Commands.slash("joinTeam", "Join a team if a game is about to start")
                .addOption(OptionType.STRING, "teamName", "name of team you want to join")
        );
        // when makeGame command is called, update joinTeam with the teamnames, otherwise say that no game is going on

        commandData.add(Commands.slash("startGame", "Start the game (locks teams)"));

        commandData.add(Commands.slash("scoreCheck", "check current score"));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (event.getGuild() == null) return;

        switch (event.getName()) {
            case "makeGame" -> makeGame(event);
            case "joinTeam" -> joinTeam(event);
            case "startGame" -> startGame(event);
            case "scoreCheck" -> displayScore(event);
        }
    }

    private void displayScore(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel().getName())) {
                //game found
                event.reply(b.getTeam1Name() + " : " + b.getTeam1Score() + "\n" + b.getTeam2Name() + " : " + b.getTeam2Score()).queue();
                return;
            }
        }
        event.reply("No existing game is happening in this channel").queue();
    }

    private void startGame(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel().getName())) {
                b.startMatch();
                event.reply(b.displayTeams() + "\nStarting match").queue();
            }
        }
    }

    private void joinTeam(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel().getName())) {
                //game found
                if (event.getOption("teamName").getAsString().equals(b.getTeam1Name())) {
                    b.addTeam1(event.getUser().getName());
                    event.reply("Successfully joined " + b.getTeam1Name()).queue();
                } else if (event.getOption("teamName").getAsString().equals(b.getTeam2Name())) {
                    b.addTeam2(event.getUser().getName());
                    event.reply("Successfully joined " + b.getTeam2Name()).queue();
                } else {
                    event.reply("Failed to find team name, check spelling.\n team names are: " + b.getTeam1Name() + ", " + b.getTeam2Name()).queue();
                }
            }
        }
    }

    private void makeGame(SlashCommandInteractionEvent event) {
        games.add(new Bowl(event.getChannel().getName(),event.getOption("team1").getAsString(),event.getOption("team2").getAsString()));
        event.reply("game has been created").queue();
    }

}
