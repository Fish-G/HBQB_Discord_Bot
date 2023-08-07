package game;

import java.util.ArrayList;
import java.util.List;

public class Bowl {
    private boolean started;

    private final String name;
    private final String team1Name;
    private final String team2Name;
    private int team1Score;
    private int team2Score;

    private final List<String> team1Members = new ArrayList<>();
    private final List<String> team2members = new ArrayList<>();

    public Bowl(String name, String team1Name, String team2Name) {
        this.name = name;
        this.team1Name = team1Name;
        this.team2Name = team2Name;

        started = false;
    }

    public String getName() {
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

    public Boolean getStarted() {
        return started;
    }

    public void addTeam1(String playerName) {
        team1Members.add(playerName);
    }

    public void addTeam2(String playerName) {
        team2members.add((playerName));
    }

    public void startMatch() {
        started = true;
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
