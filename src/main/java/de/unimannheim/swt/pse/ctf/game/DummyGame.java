package de.unimannheim.swt.pse.ctf.game;

import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Team;

import java.util.Date;

/**
 * This is a dummy game that does nothing. Remove this class and provide your own implementation of {@link Game}.
 */
public class DummyGame implements Game {

    private GameState gameState;

    @Override
    public GameState create(MapTemplate template) {
        GameState gameState = new GameState();
        gameState.setGrid(new String[template.getGridSize()[0]][template.getGridSize()[1]]);
        gameState.setTeams(new Team[template.getTeams()]);

        this.gameState = gameState;

        return this.gameState;
    }

    @Override
    public GameState getCurrentGameState() {
        return gameState;
    }

    @Override
    public Team joinGame(String teamId) {
        Team team = new Team();
        team.setId(teamId);

        return team;
    }

    @Override
    public int getRemainingTeamSlots() {
        return 0;
    }

    @Override
    public void makeMove(Move move) {

    }

    @Override
    public int getRemainingGameTimeInSeconds() {
        return 0;
    }

    @Override
    public int getRemainingMoveTimeInSeconds() {
        return 0;
    }

    @Override
    public void giveUp(String teamId) {

    }

    @Override
    public boolean isValidMove(Move move) {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isGameOver() {
        return false;
    }

    @Override
    public String[] getWinner() {
        return new String[0];
    }

    @Override
    public Date getStartedDate() {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }

    @Override
    public int getTurnTimeLimit() {
        return 0;
    }
}
