package game;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.*;
import java.util.concurrent.*;

public class Bowl {

    private final MessageChannel name;
    private final String team1Name;
    private final String team2Name;
    private int team1Score;
    private int team2Score;

    private final List<String> team1Members = new ArrayList<>();
    private final List<String> team2members = new ArrayList<>();

    ScheduledExecutorService questionMessager = Executors.newScheduledThreadPool(1);

    // current question
    Queue<String> question = new LinkedList<>();

    //current possible answers
    List<String> answers = new ArrayList<>();


    public Bowl(MessageChannel name, String team1Name, String team2Name) {
        this.name = name;
        this.team1Name = team1Name;
        this.team2Name = team2Name;

    }

    public MessageChannel getName() {
        return name;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public int getTeam1Score() {
        return team1Score;
    }

    public int getTeam2Score() {
        return team2Score;
    }

    public void addTeam1(String playerName) {
        team1Members.add(playerName);
    }

    public void addTeam2(String playerName) {
        team2members.add((playerName));
    }

    public void startMatch(MessageChannel channel) {
        startQuestion(channel);
    }

    public void checkAnswer(String buzz) {
        if (answers.contains(buzz)) {
            name.sendMessage("correct").queue();
            sf.cancel(true);
            question.clear();


        } else name.sendMessage("Incorrect").queue();

    }

    ScheduledFuture<?> sf;
    private void printClue(MessageChannel channel) {
        if (question.isEmpty()) return;

        sf = channel.sendMessage(question.remove()).queueAfter(5, TimeUnit.SECONDS, (response) -> printClue(channel));
    }

    private void startQuestion(MessageChannel channel) {
        //queue up next question and answer
        //load question from database, split into sentences
        question.addAll(List.of("In a novel by an author with this first name, the magician Signor Brunoni and the scandalous Captain Brown disrupt a society of unwed, elderly “Amazons.” In a work by another author with this first name, the title character ends up marrying her blinded cousin Romney in Florence. Margaret Hale intervenes in strikes at a cotton mill in a work by an author with this first name; that author of Cranford and North and South had the last name (*) Gaskell. Another author with this first name wrote Aurora Leigh and a collection of works imploring “Yes, call me by my pet name” and vowing “I shall but love thee better after death.” For 10 points, give this first name of an author who wrote “How do I love thee? Let me count the ways” in Sonnets from the Portuguese.".split("\\.")));

        answers.add("bingus");

        printClue(channel);
    }

    public void endMatch() {
        questionMessager.shutdownNow();
    }

    public String displayTeams() {
        StringBuilder b = new StringBuilder();
        b.append(team1Name).append("\n");
        team1Members.forEach(i -> b.append(i).append(", "));
        b.append(team2Name).append("\n");
        team2members.forEach(i -> b.append(i).append(", "));
        return b.toString();
    }
}
