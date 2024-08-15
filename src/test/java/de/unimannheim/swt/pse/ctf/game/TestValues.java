package de.unimannheim.swt.pse.ctf.game;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.unimannheim.swt.pse.ctf.game.map.MapTemplate;
import de.unimannheim.swt.pse.ctf.game.state.GameState;
import de.unimannheim.swt.pse.ctf.game.state.Move;
import de.unimannheim.swt.pse.ctf.game.state.Piece;
import de.unimannheim.swt.pse.ctf.game.state.Team;

/**
 * Class used to generate test GameStates and MapTemplates
 * @author sistumpf
 */
public class TestValues {
	/**
	 * Creates a test GameState from the example Map. 
	 * @return GameState
	 */
	public static GameState getEmptyTestState() {
		Team team1 = new Team();
		team1.setBase(new int[] {0,0});
		team1.setColor("red");
		team1.setFlags(1);
		team1.setId("0");

		Team team2 = new Team();
		team2.setBase(new int[] {9,9});
		team2.setColor("blue");
		team2.setFlags(1);
		team2.setId("1");

		Piece[] pieces1 = new Piece[0];
		team1.setPieces(pieces1);

		Piece[] pieces2 = new Piece[0];
		team2.setPieces(pieces2);

		Move lastMove = new Move();
		lastMove.setNewPosition(null);
		lastMove.setPieceId(null);

		GameState testState = new GameState();
		testState.setCurrentTeam(1);
		String[][] example = new String[][] {
			{"b:0","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","",""},
			{"","","","","","","","","","b:1"}
		};
		testState.setGrid(example);
		testState.setLastMove(lastMove);
		testState.setTeams(new Team[]{team1, team2});

		return testState;
	}
	
	/**
	 * Creates a test GameState from the example Map. 
	 * @return GameState
	 */
	public static GameState getTestState() {
		MapTemplate mt = getTestTemplate();
		Team team1 = new Team();
		team1.setBase(new int[] {0,0});
		team1.setColor("red");
		team1.setFlags(mt.getFlags());
		team1.setId("0");

		Team team2 = new Team();
		team2.setBase(new int[] {9,9});
		team2.setColor("blue");
		team2.setFlags(mt.getFlags());
		team2.setId("1");

		Piece[] pieces1 = new Piece[8];
		for(int i=0; i<8; i++) {
			pieces1[i] = new Piece(); 
			pieces1[i].setDescription(mt.getPieces()[1]);
			pieces1[i].setId("p:0_"+(i+1));
			if(i<2)
				pieces1[i].setPosition(new int[] {1,4+i});
			else
				pieces1[i].setPosition(new int[] {2,i});    
			pieces1[i].setTeamId(team1.getId());
		}
		team1.setPieces(pieces1);

		Piece[] pieces2 = new Piece[8];
		for(int i=0; i<8; i++) {
			pieces2[i] = new Piece(); 
			pieces2[i].setDescription(mt.getPieces()[1]);
			pieces2[i].setId("p:1_"+(i+1));
			if(i<6)
				pieces2[i].setPosition(new int[] {7,2+i});
			else
				pieces2[i].setPosition(new int[] {8,i-2});  
			pieces2[i].setTeamId(team2.getId());
		}
		team2.setPieces(pieces2);

		Move lastMove = new Move();
		lastMove.setNewPosition(null);
		lastMove.setPieceId(null);

		GameState testState = new GameState();
		testState.setCurrentTeam(0);
		String[][] example = new String[][] {
			{"b:0","","","","","","","","",""},
			{"","","","",pieces1[0].getId(),pieces1[1].getId(),"","","",""},
			{"","",pieces1[2].getId(),pieces1[3].getId(),pieces1[4].getId(),pieces1[5].getId(),pieces1[6].getId(),pieces1[7].getId(),"",""},
			{"","","","","","","","","",""},
			{"","","","","","","","b","",""},
			{"","","","b","","","","","",""},
			{"","","","","","","","","",""},
			{"","",pieces2[0].getId(),pieces2[1].getId(),pieces2[2].getId(),pieces2[3].getId(),pieces2[4].getId(),pieces2[5].getId(),"",""},
			{"","","","",pieces2[6].getId(),pieces2[7].getId(),"","","",""},
			{"","","","","","","","","","b:1"}
		};
		testState.setGrid(example);
		testState.setLastMove(lastMove);
		testState.setTeams(new Team[]{team1, team2});

		return testState;
	}

	  /**
	   * Returns the test MapTemplate.
	   *
	   * @return MapTemplate
	   */
	  static MapTemplate getTestTemplate() {
	    String mapString = "{\"gridSize\":[10,10],\"teams\":2,\"flags\":1,\"pieces\":[{\"type\":\"Pawn\",\"attackPower\":1,\"count\":10,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":1,\"down\":0,\"upLeft\":1,\"upRight\":1,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Rook\",\"attackPower\":5,\"count\":2,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":0,\"upRight\":0,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Knight\",\"attackPower\":3,\"count\":2,\"movement\":{\"shape\":{\"type\":\"lshape\"}}},{\"type\":\"Bishop\",\"attackPower\":3,\"count\":2,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":0,\"down\":0,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"Queen\",\"attackPower\":5,\"count\":1,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"King\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":1,\"right\":1,\"up\":1,\"down\":1,\"upLeft\":1,\"upRight\":1,\"downLeft\":1,\"downRight\":1}}}],\"blocks\":0,\"placement\":\"symmetrical\",\"totalTimeLimitInSeconds\":-1,\"moveTimeLimitInSeconds\":-1}\r\n";
	    Gson gson = new Gson();
	    new TypeToken<>() {}.getType(); 
	    return gson.fromJson(mapString, MapTemplate.class);
	  }
	  
	  /**
       * Returns the test MapTemplate.
       *
       * @return MapTemplate
       */
      static MapTemplate[] getDummyTeplates() {
        MapTemplate[] templates = new MapTemplate[5];

        String mapString =
            "{\"gridSize\":[10,10],\"teams\":2,\"flags\":1,\"pieces\":[{\"type\":\"Pawn\",\"attackPower\":1,\"count\":10,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":1,\"down\":0,\"upLeft\":1,\"upRight\":1,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Rook\",\"attackPower\":5,\"count\":2,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":0,\"upRight\":0,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Knight\",\"attackPower\":3,\"count\":2,\"movement\":{\"shape\":{\"type\":\"lshape\"}}},{\"type\":\"Bishop\",\"attackPower\":3,\"count\":2,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":0,\"down\":0,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"Queen\",\"attackPower\":5,\"count\":1,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"King\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":1,\"right\":1,\"up\":1,\"down\":1,\"upLeft\":1,\"upRight\":1,\"downLeft\":1,\"downRight\":1}}}],\"blocks\":0,\"placement\":\"symmetrical\",\"totalTimeLimitInSeconds\":-1,\"moveTimeLimitInSeconds\":-1}\r\n";
        Gson gson = new Gson();
        new TypeToken<>() {}.getType();
        templates[0] = gson.fromJson(mapString, MapTemplate.class);
        
        String mapString1 =
            "{\"gridSize\":[6,5],\"teams\":2,\"flags\":1,\"pieces\":[{\"type\":\"Pawn\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":1,\"down\":0,\"upLeft\":1,\"upRight\":1,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Knight\",\"attackPower\":3,\"count\":2,\"movement\":{\"shape\":{\"type\":\"lshape\"}}},{\"type\":\"Bishop\",\"attackPower\":3,\"count\":2,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":0,\"down\":0,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"Queen\",\"attackPower\":5,\"count\":1,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"King\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":1,\"right\":1,\"up\":1,\"down\":1,\"upLeft\":1,\"upRight\":1,\"downLeft\":1,\"downRight\":1}}}],\"blocks\":0,\"placement\":\"symmetrical\",\"totalTimeLimitInSeconds\":-1,\"moveTimeLimitInSeconds\":-1}\r\n";
        Gson gson1 = new Gson();
        new TypeToken<>() {}.getType();
        templates[1] = gson1.fromJson(mapString1, MapTemplate.class);
        
        String mapString2 =
            "{\"gridSize\":[20,20],\"teams\":3,\"flags\":1,\"pieces\":[{\"type\":\"Pawn\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":1,\"down\":0,\"upLeft\":1,\"upRight\":1,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Rook\",\"attackPower\":5,\"count\":2,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":0,\"upRight\":0,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Knight\",\"attackPower\":3,\"count\":2,\"movement\":{\"shape\":{\"type\":\"lshape\"}}},{\"type\":\"Bishop\",\"attackPower\":3,\"count\":2,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":0,\"down\":0,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"Queen\",\"attackPower\":5,\"count\":1,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"King\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":1,\"right\":1,\"up\":1,\"down\":1,\"upLeft\":1,\"upRight\":1,\"downLeft\":1,\"downRight\":1}}}],\"blocks\":0,\"placement\":\"symmetrical\",\"totalTimeLimitInSeconds\":-1,\"moveTimeLimitInSeconds\":-1}\r\n";
        Gson gson2 = new Gson();
        new TypeToken<>() {}.getType();
        templates[2] = gson2.fromJson(mapString2, MapTemplate.class);
        
        String mapString3 =
            "{\"gridSize\":[10,10],\"teams\":2,\"flags\":1,\"pieces\":[{\"type\":\"Pawn\",\"attackPower\":1,\"count\":10,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":1,\"down\":0,\"upLeft\":1,\"upRight\":1,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Rook\",\"attackPower\":5,\"count\":2,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":0,\"upRight\":0,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Knight\",\"attackPower\":3,\"count\":2,\"movement\":{\"shape\":{\"type\":\"lshape\"}}},{\"type\":\"Bishop\",\"attackPower\":3,\"count\":2,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":0,\"down\":0,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"Queen\",\"attackPower\":5,\"count\":1,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"King\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":1,\"right\":1,\"up\":1,\"down\":1,\"upLeft\":1,\"upRight\":1,\"downLeft\":1,\"downRight\":1}}}],\"blocks\":0,\"placement\":\"defensive\",\"totalTimeLimitInSeconds\":-1,\"moveTimeLimitInSeconds\":-1}\r\n";
        Gson gson3 = new Gson();
        new TypeToken<>() {}.getType();
        templates[3] = gson3.fromJson(mapString3, MapTemplate.class);

        String mapString4 =
            "{\"gridSize\":[20,10],\"teams\":2,\"flags\":1,\"pieces\":[{\"type\":\"Pawn\",\"attackPower\":1,\"count\":10,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":1,\"down\":0,\"upLeft\":1,\"upRight\":1,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Rook\",\"attackPower\":5,\"count\":2,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":0,\"upRight\":0,\"downLeft\":0,\"downRight\":0}}},{\"type\":\"Knight\",\"attackPower\":3,\"count\":2,\"movement\":{\"shape\":{\"type\":\"lshape\"}}},{\"type\":\"Bishop\",\"attackPower\":3,\"count\":2,\"movement\":{\"directions\":{\"left\":0,\"right\":0,\"up\":0,\"down\":0,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"Queen\",\"attackPower\":5,\"count\":1,\"movement\":{\"directions\":{\"left\":2,\"right\":2,\"up\":2,\"down\":2,\"upLeft\":2,\"upRight\":2,\"downLeft\":2,\"downRight\":2}}},{\"type\":\"King\",\"attackPower\":1,\"count\":1,\"movement\":{\"directions\":{\"left\":1,\"right\":1,\"up\":1,\"down\":1,\"upLeft\":1,\"upRight\":1,\"downLeft\":1,\"downRight\":1}}}],\"blocks\":0,\"placement\":\"symmetrical\",\"totalTimeLimitInSeconds\":-1,\"moveTimeLimitInSeconds\":-1}\r\n";
        Gson gson4 = new Gson();
        new TypeToken<>() {}.getType();
        templates[4] = gson4.fromJson(mapString4, MapTemplate.class);
        
        return templates;
      }
}
