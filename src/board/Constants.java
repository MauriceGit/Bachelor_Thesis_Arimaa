package board;

/**
 * Klasse, die Konstanten zu Spielinformationen, wie Figuren und Farben
 * beinhaltet.
 * 
 * @author maurice
 * 
 */
public class Constants {

	/**
	 * Interne Klasse mit allen Möglichen Spielfiguren und ihren Kürzeln.
	 */
	public static class Type {
		public static final int ELEPHANT = 0;
		public static final int CAMEL = 1;
		public static final int HORSE = 2;
		public static final int DOG = 3;
		public static final int CAT = 4;
		public static final int RABBIT = 5;
		public static final int NONE = -1;
	}

	/**
	 * Der Wert einer Figur zur Evaluierung.
	 */
	public static class TypeValue {
		public static final int ELEPHANT = 16;
		public static final int CAMEL = 11;
		public static final int HORSE = 7;
		public static final int DOG = 4;
		public static final int CAT = 2;
		public static final int RABBIT = 1;
	}

	/**
	 * Legt einen Multiplier für eine Figur fest.
	 */
	public static class TypeImportance {
		public static final double ELEPHANT = 2.0;
		public static final double CAMEL = 1.8;
		public static final double HORSE = 1.6;
		public static final double DOG = 1.4;
		public static final double CAT = 1.2;
		public static final double RABBIT = 1.0;
	}

	/**
	 * Wieviel ein besetzter Trap wert ist.
	 */
	public static class TrapControl {
		public static final int CONTROL = 5;
	}

	/**
	 * Festlegung einer Wertigkeit von Strategien für die Evaluierung.
	 */
	public static class EvalValue {
		public static final double MATERIAL_MY = 10.0;
		public static final double MATERIAL_HARLOG = 220.0;
		
		public static final double POSITIONING_PIECE_SQUARE = 0.05;
		
		public static final double POSITIONING_CAMEL_GENERAL = 2.0;
		public static final double POSITIONING_CAMEL_SIDE = 5.0;
		public static final double POSITIONING_CAMEL_ELEPHANT = 10.0;
		
		public static final double POSITIONING_CAMEL_OWN_ELEPHANT = 20.0;
		
		public static final double POSITIONING_ELEPHANT_CAMEL = 3.0;
		public static final double POSITIONING_LONELY = 50.0;
		
		public static final double POSITIONING_ELEPHANT = 40.0;
		public static final double POSITIONING_CAMEL = 5.0;
		public static final double POSITIONING_HORSE = 1.0;
		public static final double POSITIONING_DOG = 1.0;
		public static final double POSITIONING_CAT = 1.0;
		public static final double POSITIONING_RABBIT = 10.0;
		
		public static final double TRAPCONTROL = 1.0;
		public static final double TRAPCONTROL_PIECE_STRENGTH = 0.02;
		public static final double TRAPCONTROL_DANGER = 500.0;
		public static final double TRAPCONTROL_FREEZING = 1.0;
		public static final double TRAPCONTROL_BLOCK_TRAP = 1.0; /* Keine Verwendung */
		public static final double TRAPCONTROL_HOME_TRAP = 3;
		public static final double TRAPCONTROL_FALSE_PROTECTION = 10.0;
		
		public static final double DEPTH_DEPENDING_RATING = 1.3;
		
		public static final double MOVEMENT = 1.0;
		public static final double MOVEMENT_ELEPHANT = 4.0;
		public static final double MOVEMENT_CAMEL = 2.0;
		public static final double MOVEMENT_HORSE = 1.5;
		public static final double MOVEMENT_DOG = 1.0;
		public static final double MOVEMENT_CAT = 1.0;
		public static final double MOVEMENT_RABBIT = 0.5;
		public static final double MOVEMENT_FREEZING = 7.0;
		
		public static final double SPEED = 0.2;
		
		public static final double ENEMY_GENERAL_CONFLICT = 4.0;
		public static final double ENEMY_CONTACT = 2.0;
		public static final double ENEMY_CONTACT_FRONT_DIRECT = 5.0;
		public static final double ENEMY_CONTACT_FRONT_FAR = 1.0;
		public static final double ENEMY_CONTACT_FRONT_INDIRECT = 1.0;
		public static final double ENEMY_CONTACT_BACK_DIRECT = 12.0;
		public static final double ENEMY_CONTACT_SIDE = 7.0;
		
		public static final double PIECE_SEPARATED = 5.0;
		public static final double PIECE_SEPARATED_2_STEP = 20.0;
		public static final double PIECE_SEPARATED_1_STEP = 10.0;
		
		public static final double LONELY_ELEPHANT = 5.0;
		public static final double PASSIVE_ELEPHANT = 5.0;
		
		public static final double TEAM_SPIRIT = 10.0;
		
		public static final double HOSTAGE_SITUATION = 170;
		public static final double FRAME_SITUATION = 170;	
		public static final double FORK_SITUATION = 170;
		
		public static final double RABBIT_WALL = 2.0;
		
		public static final double RABBIT_CROWDING = 50.0;
		
		public static final double RABBIT_2_STEP_GOAL = 10000000.0;
		public static final double RABBIT_3_STEP_GOAL = 5000000.0;
		
		public static final double RABBIT_FREEWAY = 30.0;
		public static final double RABBIT_FREEWAY_0 = 0.1;
		public static final double RABBIT_FREEWAY_1 = 0.5;
		public static final double RABBIT_FREEWAY_2 = 0.8;
		public static final double RABBIT_FREEWAY_3 = 1.0;
		public static final double RABBIT_FREEWAY_4 = 1.3;
		public static final double RABBIT_FREEWAY_5 = 1.6;
		public static final double RABBIT_FREEWAY_6 = 2.0;
		public static final double RABBIT_FREEWAY_7 = 4.0;
		
		public static final double SAVETY = 1.0;
	}

	public static class PieceSquareTable {
		public static final int[][] LOW = { 
				{ 2, 2, 3, 4, 5, 6, 7, 8 },
				{ 2, 2, 2, 1, 1, 2, 6, 8 }, 
				{ 2, 2, -1, 0, 0, -1, 5, 8 },
				{ 2, 1, 0, 0, 0, 0, 4, 8 }, 
				{ 2, 1, 0, 0, 0, 0, 4, 8 },
				{ 2, 2, -1, 0, 0, -1, 5, 8 }, 
				{ 2, 2, 2, 1, 1, 2, 6, 8 },
				{ 2, 2, 3, 4, 5, 6, 7, 8 } };
		public static final int[][] MIDDLE = { 
				{ 1, 2, 3, 1, 0, 0, 0, 0 },
				{ 2, 4, 6, 1, 0, 0, 0, 0 }, 
				{ 3, 6, -1, 2, 1, -1, 0, 0 },
				{ 2, 4, 5, 1, 0, 0, 0, 0 }, 
				{ 2, 4, 5, 1, 0, 0, 0, 0 },
				{ 3, 6, -1, 2, 1, -1, 0, 0 }, 
				{ 2, 4, 6, 1, 0, 0, 0, 0 },
				{ 1, 2, 3, 1, 0, 0, 0, 0 } };
		public static final int[][] HIGH = { 
				{ 0, 0, 1, 2, 2, 1, 0, 0 },
				{ 0, 2, 2, 3, 3, 2, 2, 0 }, 
				{ 1, 2, -1, 4, 4, -1, 2, 1 },
				{ 2, 3, 4, 5, 5, 4, 3, 2 }, 
				{ 2, 3, 4, 5, 5, 4, 3, 2 },
				{ 1, 2, -1, 4, 4, -1, 2, 1 }, 
				{ 0, 2, 2, 3, 3, 2, 2, 0 },
				{ 0, 0, 1, 2, 2, 1, 0, 0 } };
		public static final int[][] SPEED = { 
				{ 3, 4, 3, 2, 2, 1, 1, 1 },
				{ 3, 4, 3, 2, 2, 1, 1, 1 }, 
				{ 3, 4, -1, 2, 2, -1, 1, 1 },
				{ 3, 4, 3, 2, 2, 1, 1, 1 }, 
				{ 3, 4, 3, 2, 2, 1, 1, 1 },
				{ 3, 4, -1, 2, 2, -1, 1, 1 }, 
				{ 3, 4, 3, 2, 2, 1, 1, 1 },
				{ 3, 4, 3, 2, 2, 1, 1, 1 } };
		public static final int[][] MORE_SPEED = { 
				{ 6, 10, 9, 5, 2, 1, 1, 1 },
				{ 6, 10, 10, 5, 2, 1, 1, 1 }, 
				{ 6, 10, -1, 5, 2, -1, 1, 1 },
				{ 6, 10, 8, 5, 2, 1, 1, 1 }, 
				{ 6, 10, 8, 5, 2, 1, 1, 1 },
				{ 6, 10, -1, 5, 2, -1, 1, 1 }, 
				{ 6, 10, 10, 5, 2, 1, 1, 1 },
				{ 6, 10, 9, 5, 2, 1, 1, 1 } };
		
	}

	/**
	 * Penalty-Wert für bestimmte Konstellationen
	 */
	public static class Penalty {
		public static final int FEWRABBITS = 2;
	}

	/**
	 * Eine leere Bitmap mit 0en zum Initialisieren.
	 */
	public static class Bitmap {
		public static final long EMPTY_BITMAP = 0L;
	}

	/**
	 * Klasse mit der Zuordnung von Indizes (0..7) auf Buchstaben (a..h).
	 */
	public static class ColumnName {
		public static final char[] COLUMN = { 'a', 'b', 'c', 'd', 'e', 'f',
				'g', 'h' };
	}

	/**
	 * Interne Klasse mit den möglichen Spielfarben.
	 */
	public static class Color {
		public static final int SILVER = 0;
		public static final int GOLD = 1;
		public static final int NONE = -1;
	}

	/**
	 * Mögliche Filenamen.
	 */
	public static class Files {
		public static final String LOGFILE = "arimaa_bot.log";
		public static final String CONFFILE = "arimaa_bot.conf";
	}

	/**
	 * Interne Klasse zur Konfiguration von möglichen Engine-Typen.
	 */
	public static class EngineType {
		public static final int MINIMAX = 0;
		public static final int ALPHABETA = 1;
		public static final int NEGASCOUT = 2;
		public static final int MTDF = 3;
		public static final int MCTS = 4;
		public static final int ITERATIVEDEEPENING = 5;
	}

	/**
	 * Interne Klasse mit Movetypes
	 */
	public static class MoveType {
		public static final int NORMAL = 0;
		public static final int SPECIAL = 1;
		public static final int SCORE = 2;
	}

	/**
	 * Interne Klasse mit Node-Types für die Memory.
	 */
	public static class NodeType {
		public static final int EXACT = 0;
		public static final int ALPHA = 1;
		public static final int BETA = 2;
		public static final int NOTHING = 3;
	}

}
