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
import org.example.HBQB;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .addOption(OptionType.STRING, "tags", "tags comma seperated see /tags (no tags default hs) ie: hs, math, science", true)
        );

        commandData.add(Commands.slash("jointeam", "Join a team if a game is about to start")
                .addOption(OptionType.STRING, "teamname", "name of team you want to join", true)
        );

        commandData.add(Commands.slash("startgame", "Start the game (locks teams)"));

        commandData.add(Commands.slash("scorecheck", "check current score"));

        commandData.add(Commands.slash("stopgame", "stop current game in channel"));

        commandData.add(Commands.slash("leaveteam", "leave your current team"));
/*
        commandData.add(Commands.slash("addquestion", "submit your own question")
                .addOption(OptionType.STRING,"question","your question", true)
                .addOption(OptionType.STRING, "answers", "your answers, comma seperated, these are the bold and underlined portion of the answer only. ie: a, b, c ",true)
                .addOption(OptionType.STRING,"displayanswer", "the full answer to show after question is finished",true)
                .addOption(OptionType.STRING, "tags", "add descriptor tags see /tags for a list. comma space seperated, ie: hs, trash",true)
        );

        commandData.add(Commands.slash("deletequestion", "delete a question from the database")
                .addOption(OptionType.STRING,"uuid", "question id given on question submission to database",true)
        );

        commandData.add(Commands.slash("searchquestionbyuuid","Search the database for a question given uuid")
                .addOption(OptionType.STRING,"uuid","uuid",true)
        );

        commandData.add(Commands.slash("searchquestionbyusersubmission", "search a questino by the user who submitted it")
                .addOption(OptionType.USER,"user","user",true)
        );

        commandData.add(Commands.slash("searchquestionbytags", "search a questino by the user who submitted it")
                .addOption(OptionType.STRING,"tags","tags comma space separated",true)
        );
*/
        commandData.add(Commands.slash("tags", "list of descriptor tags for questions"));

        commandData.add(Commands.slash("addtags", "add tag to current game")
                .addOption(OptionType.STRING, "tags", "tags comma space separated", true));

        commandData.add(Commands.slash("removetags", "remove a tag from current game")
                .addOption(OptionType.STRING, "tags", "tags comma space separated", true)
        );

        commandData.add(Commands.slash("currenttags", "display current tags in this match"));

        commandData.add(Commands.slash("listplayers", "list players in team"));

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
            //case "addquestion" -> addQuestion(event);
            case "tags" -> showTags(event);
            //case "deletequestion" -> deleteQuestion(event);
            //case "searchquestionbyuuid" -> searchQuestionByUUID(event);
            //case "searchquestionbyusersubmission" -> searchQuestionByUserSubmission(event);
            case "addtags" -> addTags(event);
            case "removetags" -> removeTags(event);
            case "currenttags" -> printCurrentGameTags(event);
            case "listplayers" -> printPlayers(event);
        }
    }

    private void printPlayers(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                event.reply(b.getTeam1Name() + " : " + b.getTeam1Members().keySet().stream().map(i -> i + ", ").collect(Collectors.joining()) + "\n" + b.getTeam2Name() + " : " + b.getTeam2Members().keySet().stream().map(i -> i + ", ").collect(Collectors.joining())).queue();
                return;
            }
        }
        event.reply("No match existing in this channel").queue();
    }

    private void printCurrentGameTags(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                event.reply(b.getTags().stream().map(i -> i.name() + ", ").collect(Collectors.joining())).queue();
                return;
            }
        }
        event.reply("No match existing in this channel").queue();
    }

    private void removeTags(SlashCommandInteractionEvent event) {

        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                var tags = parseTagsFromString(event);
                b.getTags().removeAll(tags);
                if (b.getTags().isEmpty()) {
                    event.getChannel().sendMessage("cannot remove all tags in game! Adding in hs tag").queue();
                    b.getTags().add(QuestionTags.HS);
                }
                event.reply("removed tags: " + tags.stream().map(i -> i.name() + ", ").collect(Collectors.joining())).queue();
                return;
            }
        }
        event.reply("no match existing in this channel").queue();
    }

    private void addTags(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                var tags = parseTagsFromString(event);
                b.getTags().addAll(tags);
                event.reply("added tags: " + tags.stream().map(i -> i.name() + ", ").collect(Collectors.joining())).queue();
                return;
            }
        }
        event.reply("no match existing in this channel").queue();
    }

    /*
        private void searchQuestionByUserSubmission(SlashCommandInteractionEvent event) {

        }

        private void searchQuestionByUUID(SlashCommandInteractionEvent event) {

        }

        private void deleteQuestion(SlashCommandInteractionEvent event) {

        }

        private void addQuestion(SlashCommandInteractionEvent event) {
            // reply with uuid of the submitted question
        }
    */
    private void showTags(SlashCommandInteractionEvent event) {
        event.reply("hs, ms, college, science, philosophy, literature, math, trash, history, art, geography, miscellaneous, music").queue();
    }

    private void leaveTeam(SlashCommandInteractionEvent event) {
        for (Bowl b : games) {
            if (b.getName().equals(event.getChannel())) {
                b.getTeam1Members().remove(event.getUser());
                b.getTeam2Members().remove(event.getUser());
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
                if (!b.isStarted()) {
                    b.startQuestion(event.getChannel());
                    event.reply(b.displayTeams() + "\nStarting match").queue();
                } else event.reply("Match has already started").queue();
                return;
            }
        }
        event.reply("No existing game in this channel").queue();
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

    private List<QuestionTags> parseTagsFromString(SlashCommandInteractionEvent event) {
        List<QuestionTags> tags = new ArrayList<>();

        List.of(event.getOption("tags").getAsString().split(", ")).forEach(i -> {
            switch (i) {
                case "hs" -> tags.add(QuestionTags.HS);
                case "ms" -> tags.add(QuestionTags.MS);
                case "college" -> tags.add(QuestionTags.COLLEGE);
                case "science" -> tags.add(QuestionTags.SCIENCE);
                case "philosophy" -> tags.add(QuestionTags.PHILOSOPHY);
                case "literature" -> tags.add(QuestionTags.LITERATURE);
                case "math" -> tags.add(QuestionTags.MATH);
                case "trash" -> tags.add(QuestionTags.TRASH);
                case "history" -> tags.add(QuestionTags.HISTORY);
                case "art" -> tags.add(QuestionTags.ART);
                case "geography" -> tags.add(QuestionTags.GEOGRAPHY);
                case "miscellaneous" -> tags.add(QuestionTags.MISCELLANEOUS);
                case "music" -> tags.add(QuestionTags.MUSIC);
                default ->
                        event.getChannel().sendMessage("could not find tag: " + i + " please check spelling and /tags you can add back the tag with /addTags").queue();
            }
        });

        return tags;
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

        List<QuestionTags> tags = parseTagsFromString(event);
        if (tags.isEmpty()) {
            event.getChannel().sendMessage("cannot have no tags, adding in hs tag").queue();
            tags.add(QuestionTags.HS);
        }

        games.add(new Bowl(mongo, event.getChannel(), event.getOption("team1").getAsString(), event.getOption("team2").getAsString(), tags));
        event.reply("game has been created -> **" + event.getOption("team1").getAsString() + "** vs **" + event.getOption("team2").getAsString() + "** \ntags are: " + tags.stream().map(i -> i.name() + ", ").collect(Collectors.joining())).queue();
    }

}
