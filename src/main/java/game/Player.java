package game;

import net.dv8tion.jda.api.entities.User;

public class Player {
    private final User player;
    private int score;

    public Player(User player) {
        this.player = player;
    }

    public User getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score+=points;
    }
}