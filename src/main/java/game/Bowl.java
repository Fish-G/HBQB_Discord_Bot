package game;

import com.github.wslf.levenshteindistance.LevenshteinCalculator;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.example.Question;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.concurrent.*;


import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class Bowl {
    MongoTemplate mongo;
    private final MessageChannel name;
    private final String team1Name;
    private final String team2Name;
    private int team1Score;
    private int team2Score;

    private final HashMap<User, Player> team1Members = new HashMap<>();
    private final HashMap<User, Player> team2members = new HashMap<>();

    // current question
    Queue<String> question = new LinkedList<>();

    private boolean started = false;
    private boolean power;
    //current possible answers
    List<String> answers = new ArrayList<>();
    String answerDisplay;

    List<QuestionTags> tags;
    public Bowl(MongoTemplate mongo, MessageChannel name, String team1Name, String team2Name, List<QuestionTags> tags) {
        this.mongo = mongo;
        this.name = name;
        this.team1Name = team1Name;
        this.team2Name = team2Name;
        this.tags = tags;
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

    public void addTeam1(User player) {
        team1Members.put(player, new Player(player));
    }

    public void addTeam2(User player) {
        team2members.put(player, new Player(player));
    }

    public boolean isInPlayerList(User user) {
        return team1Members.containsKey(user) || team2members.containsKey(user);
    }

    public boolean isStarted() {
        return started;
    }

    public HashMap<User, Player> getTeam1Members() {
        return team1Members;
    }

    public HashMap<User, Player> getTeam2Members() {
        return team2members;
    }

    public List<QuestionTags> getTags() {
        return tags;
    }

    private boolean acceptable(String buzz) {
        LevenshteinCalculator levenshteinCalculator = new LevenshteinCalculator();
        for (String a : answers) {
            if (levenshteinCalculator.getLevenshteinDistance(buzz,a,false) < 3) {
                return true;
            }
        }
        return false;
    }

    public void checkAnswer(String buzz, User player) {
        if (acceptable(buzz)) {
            name.sendMessage("correct").queue();
            name.sendMessage(answerDisplay).queue();
            sf.cancel(true);
            question.clear();

            //calculate score to give
            int score = 10;
            if (power) score += 5;

            //add score to player
            if (team1Members.containsKey(player)) {
                team1Members.get(player).addScore(score);
                team1Score += score;
            } else {
                team2members.get(player).addScore(score);
                team2Score += score;
            }

            startQuestion(name);

        } else {
            name.sendMessage("Incorrect").queue();
            if (team1Members.containsKey(player)) {
                team1Members.get(player).addScore(-5);
            } else team2members.get(player).addScore(-5);
        }


    }

    ScheduledFuture<?> sf;

    private void printClue(MessageChannel channel, boolean triggerPower) {
        //in the case that we are done with the question and no correct answers
        if (question.isEmpty()) {
            sf = channel.sendMessage("time " + answerDisplay).queueAfter(5,TimeUnit.SECONDS,(response) -> startQuestion(channel));
            return;
        }

        //we want to deactivate power after the (*), so we need a mechanism to tell the next callback to deactivate power
        if (triggerPower) power = false;
        if (question.peek().endsWith("(*)")) triggerPower = true;

        boolean finalTriggerPower = triggerPower;
        sf = channel.sendMessage(question.remove()).queueAfter(5, TimeUnit.SECONDS, (response) -> printClue(channel, finalTriggerPower));
    }


    private void loadQuestion() {
        //load a question into the currentQuestion linked list, optional conditions like category and difficulty
        Query query = query(where("tags").all(tags));
        query.fields().include("id");
        List<Question> idList = mongo.find(query, Question.class);


        if (idList.isEmpty()) {
            name.sendMessage("no question found, removing all tags and adding hs tag! please readd tags with /addtags and check your combination!").queue();
            tags.clear();
            tags.add(QuestionTags.HS);

            query = query(where("tags").all(tags));
            query.fields().include("id");
            idList = mongo.find(query, Question.class);
        }

        Question q = mongo.findById(idList.get((int) (Math.random() * idList.size())).getId(), Question.class);

        //perform question formatting into the queue
        assert q != null;
        question.addAll(List.of(q.getQuestion().split("(?<=\\. )|(?<=\\Q.”\\E)|(?<=\\(\\*\\))")));

        //load answers
        answerDisplay = q.getDisplayAnswers();
        answers = q.getAnswers();
    }


    public void startQuestion(MessageChannel channel) {
        //queue up next question and answer
        //load question from database, split into sentences
        started = true;
        power = true;

        loadQuestion();
        printClue(channel, false);
    }

    public void endMatch() {
        //print out team score
        name.sendMessage(team1Name + " : " + team1Score + "\n" + team2Name + " : " + team2Score).queue();
        //print out individual scores
        team1Members.forEach((i, j) -> name.sendMessage(i.getName() + " : " + j.getScore()).queue());
        team2members.forEach((i, j) -> name.sendMessage(i.getName() + " : " + j.getScore()).queue());

        sf.cancel(true);
    }

    public String displayTeams() {
        StringBuilder b = new StringBuilder();
        b.append(team1Name).append("\n");
        team1Members.forEach((i, j) -> b.append(i.getName()).append(", "));
        b.append(team2Name).append("\n");
        team2members.forEach((i, j) -> b.append(i.getName()).append(", "));
        return b.toString();
    }
}
