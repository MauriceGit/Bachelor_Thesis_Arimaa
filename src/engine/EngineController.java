package engine;

import memory.RepetitionCounter;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveGenerator;
import board.MoveSpecial;
import board.Piece;
import board.Position;

public class EngineController {

	/* Instanz auf die Engine */
	private Engine engine;

	/* Instanz des zugehörigen Threads */
	private Thread engineThread;

	/* Instanz auf die Zeitverwaltung der Engine */
	private EngineTimeController engineTimeController;

	/* Thread mit Zeitverwaltung der Engine */
	private Thread timeControllerThread;

	/* Attribute der Engine */
	private EngineGeneralAttributes engineGeneralAttributes;

	/* Instanz des Message-Writers */
	private MessageOutputWriter messageWriter;

	/* Die korrekte momentane Boardkonfiguration. Korrekter Zustand! */
	private Bitboard board;
	
	/* Instanz für das Zählen und behalten von Spielfeldsituationen für die Repetition-Rule */
	private RepetitionCounter repetitionCounter;

	public EngineController(EngineGeneralAttributes engineGeneralAttributes,
			MessageOutputWriter messageWriter) {

		/* Konfigurieren der Search Engine. */
		switch (engineGeneralAttributes.getEngineType()) {
		case Constants.EngineType.MINIMAX:
			engine = new Minimax(messageWriter, engineGeneralAttributes);
			messageWriter.sendMessage("log engine type: MINIMAX. Depth: "
					+ engineGeneralAttributes.getDepth());
			break;
		case Constants.EngineType.ALPHABETA:
			engine = new AlphaBeta(messageWriter, engineGeneralAttributes);
			messageWriter.sendMessage("log engine type: ALPHABETA. Depth: "
					+ engineGeneralAttributes.getDepth() + ". MoveOrdering: "
					+ engineGeneralAttributes.getMoveOrdering() + ". Memory: "
					+ engineGeneralAttributes.getUseMemory() + ". Parallelization: "
					+ engineGeneralAttributes.getUseParallelization() + ".");
			break;
		case Constants.EngineType.ITERATIVEDEEPENING:
			engine = new IterativeDeepening(messageWriter, engineGeneralAttributes);
			messageWriter.sendMessage("log engine type: ITERATIVEDEEPENING. Depth: "
					+ engineGeneralAttributes.getDepth() + ". MoveOrdering: "
					+ engineGeneralAttributes.getMoveOrdering() + ". Memory: "
					+ engineGeneralAttributes.getUseMemory() + ". Parallelization: "
					+ engineGeneralAttributes.getUseParallelization() + ".");
			break;
		case Constants.EngineType.NEGASCOUT:
		case Constants.EngineType.MTDF:
		case Constants.EngineType.MCTS:
			engine = new NegaScout(messageWriter, engineGeneralAttributes);
			messageWriter.sendMessage("log engine type: NEGASCOUT. Depth: "
					+ engineGeneralAttributes.getDepth());
		}

		engineTimeController = new EngineTimeController(engine,
				engineGeneralAttributes, messageWriter);
		this.engineGeneralAttributes = engineGeneralAttributes;
		this.messageWriter = messageWriter;
		this.board = new Bitboard(engineGeneralAttributes);
		
		this.repetitionCounter = new RepetitionCounter();

	}

	/**
	 * Aktualisiert den Boardzustand mit einem neuen Move
	 * 
	 * @param move
	 *            Der Move
	 */
	public void makeMove(String move) {
		char[] splitMove = move.toCharArray();

		int color = (splitMove[0] >= 65 && splitMove[0] <= 90) ? Constants.Color.GOLD
				: Constants.Color.SILVER; // Großbuchstaben == Gold, alles
											// andere == Silber
		int type = -1;
		int col = splitMove[1] - 97;
		int row = splitMove[2] - 48 - 1; // -48 für Konversion char->int, -1 um
											// von der Rownum auf den Index zu
											// kommen.

		switch (splitMove[0]) {
		case 'E':
		case 'e':
			type = Constants.Type.ELEPHANT;
			break;
		case 'M':
		case 'm':
			type = Constants.Type.CAMEL;
			break;
		case 'H':
		case 'h':
			type = Constants.Type.HORSE;
			break;
		case 'D':
		case 'd':
			type = Constants.Type.DOG;
			break;
		case 'C':
		case 'c':
			type = Constants.Type.CAT;
			break;
		case 'R':
		case 'r':
			type = Constants.Type.RABBIT;
			break;
		default:
			messageWriter.writeLog("log Figure type '" + splitMove[0]
					+ "' to set/move not recognized!");
		}

		/* Initial Placement! */
		if (move.length() == 3) {

			board.setPieceAtPosition(new Piece(new Position(col, row), color,
					type));

			/* Bewegen von Figuren: */
		} else if (move.length() == 4) {

			int colDir = 0;
			int rowDir = 0;

			/* Entfernt die Figur von seiner Ursprünglichen Position */
			board.unsetPieceAtPosition(new Piece(new Position(col, row), color,
					type));

			if (splitMove[3] != 'x') {
				switch (splitMove[3]) {
				case 'n':
					rowDir = 1;
					break;
				case 's':
					rowDir = -1;
					break;
				case 'w':
					colDir = -1;
					break;
				case 'e':
					colDir = 1;
					break;
				}

				/* Setzt die Figur an seine neue Position */
				board.setPieceAtPosition(new Piece(new Position(col + colDir,
						row + rowDir), color, type));
			}
		} else {
			messageWriter.writeLog("log Move '" + move + "' not recognized!");
		}

		/*
		 * Überprüfung, ob eine Figur in ein Trap gelaufen ist oder dort alleine
		 * gelassen wurde. --> Aus dem Spiel entfernen!
		 */
		board.removePiecesFromTraps();
	}

	/**
	 * Printet das aktuelle Board nach StdOut aus. --> Debugg-Funktion!!!!
	 */
	public void printBoard() {
		board.printBitboard();
	}

	/**
	 * Printet eine Push-Attackmap aus. --> Debugg-Funktion!!!!!
	 */
	public void printSpecialMoveMap(int col, int row) {
		Piece piece = board.findPieceAt(new Position(col, row));

		if (piece == null) {
			System.out.println("No Peace found.");
			return;
		}

		for (MoveSpecial move : board.getPieceSpecialMoveList(piece)) {
			System.out.println(move);
		}
	}

	/**
	 * Printet alle Bewegungsmöglichkeiten ab einer Spielfeldsituation aus.
	 */
	public void printColorMoves(int color) {
		MoveGenerator mg = new MoveGenerator();
		for (Move move : mg.generateAllColorMoves(board, color)) {
			messageWriter.sendMessage("log " + move);
		}
	}

	/**
	 * Beginnt ein neues Spiel. Also leeren des Boards, etc...
	 */
	public void newGame() {

		board.generateEmptyBitboard();
		board.generateMasks();

	}

	/**
	 * Printet die aktuelle Spielsituation in die Logdatei.
	 */
	public void logBitboard() {
		messageWriter.writeLog(board.toString());
	}

	/**
	 * Merkt sich den aktuellen Boardzustand, für eine Überprüfung, dass dieser
	 * maximal drei mal auftreten darf.
	 */
	public void registerBoardSituation() {

		repetitionCounter.saveBoard(board);

	}

	/**
	 * Startet die Engine als Thread mit dem dazugehörigen Time-Controller.
	 */
	public void go() {

		/********************** STOP ************************/

		engine.setPrintBestMove(false);
		engine.stopThread();
		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		engineTimeController.getOwnThread().interrupt();
		try {
			engineTimeController.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		/********************** START ***********************/

		/* Startet die Engine selber */
		engine.setPrintBestMove(true);
		engine.setBoard(board);
		
		
		engine.setRepetitionCounter(repetitionCounter);
		
		
		engine.newThread();
		engineThread = engine.getOwnThread();
		engineThread.start();

		/* Startet die Zeitkontrolle/Messung für die Engine */
		engineTimeController.newThread();
		engineTimeController.setEngineThread(engineThread);
		timeControllerThread = engineTimeController.getOwnThread();
		timeControllerThread.start();
	}

	/**
	 * Stoppt die Engine und setzt das Flag, dass das best-erreichteste Ergebnis
	 * ausgegeben wird.
	 */
	public void stop() {

		engine.setPrintBestMove(true);

		engine.stopThread();
		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		engineTimeController.stop();
		timeControllerThread.interrupt();
		try {
			timeControllerThread.join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

	}

	/**
	 * Genau, wie stop() aber ohne die Ausgabe des bestmove am Ende. Stilles
	 * Beenden.
	 */
	public void kill() {

		// Kein Ausprinten des Ergebnisses!
		engine.setPrintBestMove(false);

		// Stoppen des Engine-Threads
		engine.stopThread();
		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

		// Stoppen des Timer-Threads.
		// --------------> Unter Umständen ändern auf .interrupt(), um ihn hart
		// zu beenden (Um Verzögerung zu vermeiden, wenn der Timer nur alle paar
		// Sekunden aufgerufen wird und auf eine Beendigung checkt...)
		engineTimeController.stop();
		if (timeControllerThread != null) {
			timeControllerThread.interrupt();
			try {
				timeControllerThread.join();
			} catch (InterruptedException e) {
				messageWriter
						.sendMessage("log InterruptedException beim join.");
			}
		}
	}

	/**
	 * Ganz böses Beenden ohne Rücksicht auf Verluste!!!
	 */
	public void interrupt() {
		engine.setPrintBestMove(false);
		engine.getOwnThread().interrupt();
		if (timeControllerThread != null)
			timeControllerThread.interrupt();
	}

	public void getBestMove() {

		engine.setPrintBestMove(true);

		// sollte eig. immer tot sein hier.
		engine.stopThread();

		try {
			engine.getOwnThread().join();
		} catch (InterruptedException e) {
			messageWriter.sendMessage("log InterruptedException beim join.");
		}

	}

}
