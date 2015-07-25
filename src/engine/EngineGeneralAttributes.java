package engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import network.MessageOutputWriter;

import board.Constants;

public class EngineGeneralAttributes {

	// Unsere Spielfarbe
	private int color = Constants.Color.NONE;

	// The move time for the game.
	private int tcmove = 1200000000;
	// The starting reserve time.
	private int tcreserve = 60000;
	// The percent of unused time added to the reserve. The initial value is 100
	// percent. 0 means no unused time is added to the reserve.
	private int tcpercent = 100;
	// The maximum reserve time.
	private int tcmax = Integer.MAX_VALUE;
	// Time limit for the total length of the game.
	private int tctotal = Integer.MAX_VALUE;
	// Maximum number of moves the game can last.
	private int tcturns = Integer.MAX_VALUE;
	// Maximum time a single move can last.
	private int tcturntime = 5;
	// Amount of reserve time for gold.
	private int greserve = 6000;
	// Amount of reserve time for silver.
	private int sreserve = 6000;
	// Amount of time used on gold's last turn.
	private int gused = 0;
	// Amount of time used on silver's last turn.
	private int sused = 0;
	// Amount of time used on the last turn.
	private int lastmoveused = 0;
	// Amount of time used so far on the current turn.
	private int moveused = 0;
	// Opponent's name.
	private String opponent = "Opponent";
	// Opponent's current rating.
	private int opponent_rating = 1400;
	// Engine's current rating.
	private int rating = 1400;
	// Current game is rated. 1 is rated, 0 is unrated. The default is rated.
	private int rated = 1;
	// Event the current game is a part of.
	private String event = "";
	// Size in megabytes of the hash table.
	private int hash = 1000;
	// If set to a positive number the depth in steps to search a position. An
	// engine may honor a minimum depth of 4. A value of 0 or less indicates no
	// fixed depth.
	private int depth = 10;

	/*************************************************************************************************************************/
	/**
	 * Attribute, die für die Engine relevant sind aus einem Konfigurationsfile
	 * laden!
	 */
	/*************************************************************************************************************************/

	// Der Typ der Engine (alphabeta ...)
	private int engineType = 1;
	// Search-depth. Entspricht der variable 'this.depth' und wird nur genutzt,
	// wenn != -1.
	private int searchDepth = -1;
	// Move-Ordering aktiv?
	private boolean moveOrdering = true;
	// Memory nutzen?
	private boolean useMemory = true;
	// Parallele Berechnung?
	private boolean useParallelization = false;
	// Eine Instanz des MessageWriters zum Loggen von Nachrichten.
	private MessageOutputWriter messageWriter;

	/* Konstruktor */
	public EngineGeneralAttributes(MessageOutputWriter messageWriter) {
		this.messageWriter = messageWriter;
	}

	/**
	 * Läd ein paar Konfigurationen für die Engine aus einem Conf-file.
	 */
	public void loadEngineConfigFromFile() {

		InputStream fis;
		BufferedReader br;
		String line;

		try {
			fis = new FileInputStream(Constants.Files.CONFFILE);

			br = new BufferedReader(new InputStreamReader(fis,
					Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {

				/* Engine-Typ */
				if (line.matches("engine = (minimax|alphabeta|negascout|mtdf|mcts|iterativedeepening)")) {
					String[] tmpArray = line.split(" ");

					if (tmpArray[2].equals("minimax")) {
						engineType = Constants.EngineType.MINIMAX;
					} else if (tmpArray[2].equals("alphabeta")) {
						engineType = Constants.EngineType.ALPHABETA;
					} else if (tmpArray[2].equals("iterativedeepening")) {
						engineType = Constants.EngineType.ITERATIVEDEEPENING;
					} else if (tmpArray[2].equals("negascout")) {
						engineType = Constants.EngineType.NEGASCOUT;
					} else if (tmpArray[2].equals("mtdf")) {
						engineType = Constants.EngineType.MTDF;
					} else if (tmpArray[2].equals("mcts")) {
						engineType = Constants.EngineType.MCTS;
					}
					messageWriter.writeLog("log Engine set to " + tmpArray[2]);
				}

				/* Iterationstiefe festlegen */
				if (line.matches("depth = [0-9]+")) {
					searchDepth = Integer.parseInt(line.split(" ")[2]);
					depth = searchDepth;
					messageWriter.writeLog("log search depth set to "
							+ line.split(" ")[2]);
				}

				/* Move-Ordering festlegen */
				if (line.matches("moveordering = (true|false)")) {
					if (line.split(" ")[2].equals("false")) {
						messageWriter.writeLog("log set moveOrdering to false");
						this.moveOrdering = false;
					} else if (line.split(" ")[2].equals("true")) {
						messageWriter.writeLog("log set moveOrdering to true");
						this.moveOrdering = true;
					}
				}

				/* Memory-Nutzung festlegen */
				if (line.matches("usememory = (true|false)")) {
					if (line.split(" ")[2].equals("false")) {
						messageWriter.writeLog("log set useMemory to false");
						this.useMemory = false;
					} else {
						messageWriter.writeLog("log set useMemory to true");
						this.useMemory = true;
					}
				}

				/* Memory-Nutzung festlegen */
				if (line.matches("useparallelization = (true|false)")) {
					if (line.split(" ")[2].equals("false")) {
						messageWriter.writeLog("log set parallelization to false");
						this.useParallelization = false;
					} else {
						messageWriter.writeLog("log set parallelization to true");
						this.useParallelization = true;
					}
				}
				
				
			}

			// Done with the file
			br.close();

		} catch (Exception e) {
		}
	}

	/**
	 * Speichert die globalen Attribute und Optionen
	 */
	public void setOption(String name, String value) {
		name = name.toLowerCase();

		messageWriter.writeLog("log set Option '" + name + " = " + value + "'");

		if (name.equals("tcmove")) {
			this.tcmove = Integer.parseInt(value);
		} else if (name.equals("tcreserve")) {
			this.tcreserve = Integer.parseInt(value);
		} else if (name.equals("tcpercent")) {
			if (!value.equals(""))
				this.tcpercent = Integer.parseInt(value);
		} else if (name.equals("tcmax")) {
			this.tcmax = Integer.parseInt(value);
		} else if (name.equals("tctotal")) {
			this.tctotal = Integer.parseInt(value);
		} else if (name.equals("tcturns")) {
			this.tcturns = Integer.parseInt(value);
		} else if (name.equals("tcturntime")) {
			this.tcturntime = Integer.parseInt(value);
		} else if (name.equals("greserve") || name.equals("wreserve")) {
			this.greserve = Integer.parseInt(value);
		} else if (name.equals("sreserve") || name.equals("breserve")) {
			this.sreserve = Integer.parseInt(value);
		} else if (name.equals("gused")) {
			this.gused = Integer.parseInt(value);
		} else if (name.equals("sused")) {
			this.sused = Integer.parseInt(value);
		} else if (name.equals("lastmoveused")) {
			this.lastmoveused = Integer.parseInt(value);
		} else if (name.equals("moveused")) {
			this.moveused = Integer.parseInt(value);
		} else if (name.equals("opponent")) {
			if (!value.equals(""))
				this.opponent = value;
		} else if (name.equals("opponent_rating")) {
			if (!value.equals(""))
				this.opponent_rating = Integer.parseInt(value);
		} else if (name.equals("rating")) {
			if (!value.equals(""))
				this.rating = Integer.parseInt(value);
		} else if (name.equals("rated")) {
			if (!value.equals(""))
				this.rated = Integer.parseInt(value);
		} else if (name.equals("event")) {
			this.event = value;
		} else if (name.equals("hash")) {
			this.hash = Integer.parseInt(value);
		} else if (name.equals("depth")) {
			/*
			 * Die Einstellung des eigenen Config-Files ist immer im Recht, vor
			 * dem Controller!!!
			 */
			if (this.searchDepth == -1)
				this.depth = Integer.parseInt(value);
		} else {
			messageWriter.sendMessage("log WARNING: Option '" + name + " = "
					+ value + "' not recognized.");
		}

	}

	/**
	 * Mit welcher Farbe wir überhaupt spielen.
	 * 
	 * @param color
	 *            die Farbe.
	 */
	public void setColorToPlay(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public int getTcmove() {
		return tcmove;
	}

	public int getTcreserve() {
		return tcreserve;
	}

	public int getTcpercent() {
		return tcpercent;
	}

	public int getTcmax() {
		return tcmax;
	}

	public int getTctotal() {
		return tctotal;
	}

	public int getTcturns() {
		return tcturns;
	}

	public int getTcturntime() {
		return tcturntime;
	}

	public int getGreserve() {
		return greserve;
	}

	public int getSreserve() {
		return sreserve;
	}

	public int getGused() {
		return gused;
	}

	public int getSused() {
		return sused;
	}

	public int getLastmoveused() {
		return lastmoveused;
	}

	public int getMoveused() {
		return moveused;
	}

	public String getOpponent() {
		return opponent;
	}

	public int getOpponent_rating() {
		return opponent_rating;
	}

	public int getRating() {
		return rating;
	}

	public int getRated() {
		return rated;
	}

	public String getEvent() {
		return event;
	}

	public int getHash() {
		return hash;
	}

	public int getDepth() {
		return depth;
	}

	public int getEngineType() {
		return engineType;
	}

	public int getSearchDepth() {
		return searchDepth;
	}

	public boolean getMoveOrdering() {
		return moveOrdering;
	}

	public boolean getUseMemory() {
		return useMemory;
	}
	
	public boolean getUseParallelization() {
		return useParallelization;
	}
	
	public MessageOutputWriter getWriter() {
		return this.messageWriter;
	}
}
