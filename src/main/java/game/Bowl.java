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

    private boolean power;
    //current possible answers
    List<String> answers = new ArrayList<>();


    public Bowl(MongoTemplate mongo, MessageChannel name, String team1Name, String team2Name) {
        this.mongo = mongo;
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

    public void addTeam1(User player) {
        team1Members.put(player, new Player(player));
    }

    public void addTeam2(User player) {
        team2members.put(player, new Player(player));
    }

    public void startMatch(MessageChannel channel) {
        startQuestion(channel);
    }


    private boolean acceptable(String buzz) {
        LevenshteinCalculator levenshteinCalculator = new LevenshteinCalculator();

        return false;
    }

    public void checkAnswer(String buzz, User player) {
        if (answers.contains(buzz)) {
            name.sendMessage("correct").queue();
            sf.cancel(true);
            question.clear();

            //add score to player
            if (team1Members.containsKey(player)) {
                team1Members.get(player).addScore(10);
                team1Score += 10;
            } else {
                team2members.get(player).addScore(10);
                team2Score += 10;
            }

        } else name.sendMessage("Incorrect").queue();

        if (team1Members.containsKey(player)) {
            team1Members.get(player).addScore(-5);
        } else team2members.get(player).addScore(-5);

    }

    ScheduledFuture<?> sf;

    private void printClue(MessageChannel channel) {
        if (question.isEmpty()) return;

        sf = channel.sendMessage(question.remove()).queueAfter(5, TimeUnit.SECONDS, (response) -> printClue(channel));
    }

    private void loadQuestion() {
        //load a question into the currentQuestion linked list, optional conditions like category and difficulty
        Query query = query(where("tags").is("science").and("color").is(""));
        query.fields().include("id");
        List<Question> idList = mongo.find(query, Question.class);


        Question q = mongo.findById(idList.get((int)(Math.random() * idList.size())), Question.class);

        //perform question formatting into the queue
        question.addAll(List.of(q.getQuestion().split("(?<=\\. )|(?<=\\Q.”\\E)|(?<=\\(\\*\\))")));

        //load answers
        answers = q.getAnswers();

        //how to add to the database
        mongo.save(new Question());

    }


    private void startQuestion(MessageChannel channel) {
        //queue up next question and answer
        //load question from database, split into sentences
        answers.add("bingus");

        printClue(channel);
    }

    public void endMatch() {
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
