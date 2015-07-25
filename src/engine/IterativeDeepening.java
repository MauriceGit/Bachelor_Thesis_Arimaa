package engine;

import java.util.LinkedList;

import evaluation.Evaluation;
import evaluation.SimpleEvaluation;

import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveScore;
import memory.RepetitionCounter;
import memory.Transposition;
import network.MessageOutputWriter;

public class IterativeDeepening extends Engine {

	/**
	 * Private Instanz der Memory-Haltung (Transposition-Table)
	 */
	private Transposition transposition;

	public IterativeDeepening(MessageOutputWriter messageWriter,
			EngineGeneralAttributes generalAttributes) {
		super(messageWriter, generalAttributes);

		this.transposition = new Transposition(generalAttributes.getHash());
		this.transposition.initTranspsition();
	}

	/**
	 * Errechnet den Alpha-Beta-Wert für eine bestimmte Rekursionstiefe eines
	 * Root-Knotens.
	 * 
	 * @param board
	 *            Der Root-Knoten
	 * @param depth
	 *            Rekursionstiefe
	 * @param maximizingPlayer
	 *            Der Spieler, für den der beste Zug ermittelt werden soll
	 * @param color
	 *            Die jeweilige Farbe
	 * @param moveNr
	 *            Überprüfung, dass 4 Züge pro Spieler möglich sind.
	 */
	public LinkedList<Move> iterativeDeepening(Bitboard board) {

		LinkedList<Move> resultList = null;
		LinkedList<Move> tmpResultList = null;
		// AlphaBetaParallel alphaBeta = new AlphaBetaParallel(messageWriter,
		// generalAttributes);
		AlphaBeta alphaBeta = new AlphaBeta(messageWriter, generalAttributes);

		/*
		 * Der Alphabeta-Instanz muss ein ein eigener Time-Controller
		 * mitgegegeben werden, sodass Zeitkontrolle auch dort stattfindet!
		 */
		EngineTimeController timeController = new EngineTimeController(
				alphaBeta, generalAttributes, messageWriter);
		timeController.setEngineThread(null);
		timeController.newThread();
		Thread timeControllerThread = timeController.getOwnThread();
		timeControllerThread.start();

		// Bitboard oldBoard = board.cloneBitboard();
		long oldBoardHash = super.repetitionCounter.getBoardHash(board);

		alphaBeta.setOldBoardHash(oldBoardHash);
		alphaBeta.setRepetitionCounter(super.repetitionCounter);
		alphaBeta.setMoveOrdering(new MoveOrdering(generalAttributes,
				messageWriter));

		SimpleEvaluation eval = new SimpleEvaluation(generalAttributes);
		// int oldScore = eval.evaluateBoardState(oldBoard);
		// alphaBeta.setOldScore(oldScore);
		alphaBeta.setEvaluation(eval);

		int initDepth = 0;
		int finalDepth = generalAttributes.getDepth();

		for (int depth = initDepth; depth <= finalDepth; depth++) {

			messageWriter.sendMessage("log Iterative deepening at depth = "
					+ depth);

			tmpResultList = null;

			tmpResultList = alphaBeta.alphaBeta(board, depth, true,
					generalAttributes.getColor(), 4, -1000000000, 1000000000,
					generalAttributes.getUseMemory(),
					generalAttributes.getMoveOrdering(), true, depth, false,
					generalAttributes.getUseParallelization(), false);

			if (!super.isAllowedToRun) {
				break;
			}

			resultList = tmpResultList;
		}

		// System.out.println(resultList);

		timeControllerThread.interrupt();

		super.leafNodesVisited = alphaBeta.getNodesVisited();

		return resultList;
	}

	@Override
	public void run() {

		super.leafNodesVisited = 0;

		long startTime = System.currentTimeMillis();

		/*
		 * Dem Board eine Instanz des Transposition-Tables mitgeben, damit damit
		 * ein Makemove funktioniert
		 */
		board.setTransposition(transposition);

		/*
		 * Den Zobrist-Hash bei jedem neuen Zug neu initialisieren!
		 */
		// board.setZobristHash(transposition.generateZobristHashForTable(board));

		/*
		 * das Transposition-Table neu aufsetzen, dass alte Memory-Einträge
		 * gelöscht werden!
		 */
		transposition.clearTable();

		// System.out.println("Hash des Boards == '" + board.hashCode() + "'.");

		super.bestMove = iterativeDeepening(board);

		/*
		 * Panic-Search --> Es wurde ein leerer Move ermittelt... Jetzt einen
		 * Zug ermitteln mit maximal 3 Rekursionen. Das sollte in 0-1 sek zu
		 * schaffen sein!
		 */
		if (super.bestMove.size() == 0) {
			super.isAllowedToRun = true;
			messageWriter.sendMessage("log PANIC SEARCH ACTIVATED!!!!!");
			super.bestMove = iterativeDeepening(board);
		}

		long estimatedTime = System.currentTimeMillis() - startTime;

		messageWriter.sendMessage("log finished calculating best move"
				+ ((super.isAllowedToRun == true) ? " all by himself :-)"
						: ", being interrupted by the timer..."));

		messageWriter.sendMessage("log time used: " + estimatedTime / 1000);

		messageWriter.sendMessage("log old score: "
				+ new SimpleEvaluation(generalAttributes).evaluateBoardState(
						board, false));

		messageWriter.sendMessage("log score: "
				+ ((MoveScore) super.bestMove.getLast()).getScore());

		messageWriter.sendMessage("log node count: " + super.leafNodesVisited);

		messageWriter.sendMessage("log used memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / (1024 * 1024) + "mb");

		/* Ausgabe des besten Moves! */
		if (super.printBestMove) {
			messageWriter.sendMessage(getBestMove());
		}

	}

	@Override
	public void newThread() {
		super.ownThread = new Thread(this);
		super.bestMove = new LinkedList<Move>();
		super.isAllowedToRun = true;

	}

}
