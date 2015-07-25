package engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import memory.Transposition;
import memory.ZobristHash;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveScore;
import evaluation.Evaluation;
import evaluation.TestEvaluation;

public class NegaScout extends Engine {

	/**
	 * Private Instanz der Memory-Haltung (Transposition-Table)
	 */
	private Transposition transposition;

	/**
	 * Konstruktor
	 */
	public NegaScout(MessageOutputWriter messageWriter,
			EngineGeneralAttributes generalAttributes) {
		super(messageWriter, generalAttributes);
		this.transposition = new Transposition(generalAttributes.getHash());
		this.transposition.initTranspsition();
	}

	/**
	 * Führt einen Iterative-Deepening-Depth-First-Search aus mit geringere
	 * Rekursionstiefe, um ein Move-Ordering zu erzeugen, die den alpha-beta
	 * verkürzt.
	 * 
	 * @param board
	 *            die aktuelle Spielsituation
	 * @param moveList
	 *            die ursprünglich generierte Move-List.
	 * @param depth
	 *            die Tiefe der Rekursion, in der sortiert werden soll.
	 * @param moveNr
	 *            MoveNr zur Unterscheidung, ob es für den maximizing oder
	 *            minimizing Player ist
	 * @return eine absteigend sortierte Liste mit Moves.
	 */
	private LinkedList<Move> moveOrdering(Bitboard board,
			LinkedList<Move> moveList, int depth, int moveNr,
			boolean maximizingPlayer, int color) {

		/*
		 * Nur die ersten beiden Ebenen sortieren, weil da die größten Teilbäume
		 * gepruned werden können und der Overhead nicht so hoch ist
		 */
		if (depth <= 3 || generalAttributes.getDepth() > depth + 1) {
			return moveList;
		}

		messageWriter.sendMessage("log moveOrdering at depth = " + depth);

		/* maximale Rekursion */
		int maxDepth = 2;
		/* Map mit allen Moves und Werten. */
		HashMap<Integer, Move> moveHashMap = new HashMap<Integer, Move>();

		LinkedList<Move> resultList = new LinkedList<Move>();

		/* Errechnen der Werte für jedes einzelne Kind. */
		for (Move move : moveList) {
			/* Zug anwenden */
			Bitboard newBoard = board.cloneBitboard();
			newBoard.applyMoveAndRemovePieces(move);

			AlphaBeta newAlphaBeta = new AlphaBeta(messageWriter,
					generalAttributes);

			/* führt einen kleinen alphaBeta-Search aus für Zwischenergebnisse! */
			LinkedList<Move> tmpList = newAlphaBeta.alphaBeta(newBoard,
					maxDepth, maximizingPlayer, color, moveNr, -1000000000,
					1000000000, false, false, false, maxDepth, false, false, false);

			/* Move mit seinem Ergebnis speichern */
			moveHashMap.put(((MoveScore) tmpList.getLast()).getScore(), move);

			tmpList = null;
		}

		/* sortieren */
		LinkedList<Integer> sortedList = new LinkedList<Integer>(
				moveHashMap.keySet());
		Iterator<Integer> it = sortedList.descendingIterator();

		/* sortiert in Liste einfügen */
		while (it.hasNext()) {
			resultList.add(moveHashMap.get(it.next()));
		}

		return resultList;
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
	public LinkedList<Move> negaScout(Bitboard board, int depth,
			boolean maximizingPlayer, int color, int moveNr, int alpha,
			int beta, boolean useMemory, boolean moveOrdering) {

		/* Generiert Liste mit allen verfügbaren Moves! */
		LinkedList<Move> oneMoveList = moveGen.generateAllColorMoves(board,
				color);
		int newAlpha = alpha;
		int newBeta = beta;
		boolean isPrincipalVariation = true;
		int scoreValue = -100000000;

		/* Rekursionsabbruch */
		if (depth <= 0 || oneMoveList.size() == 0 || !super.isAllowedToRun) {

			Evaluation eval = new TestEvaluation(generalAttributes);

			Move move = new MoveScore(eval.evaluateBoardState(board, false));

			LinkedList<Move> res = new LinkedList<Move>();
			res.add(move);
			super.leafNodesVisited++;
			return res;
		}

		/* Memory-Nutzung! */
		if (useMemory) {

			ZobristHash zobrist = transposition.lookupHash(board
					.getZobristHash());

			/*
			 * einen Eintrag im Cache gefunden, der entweder auf der gleichen
			 * Rekursionstiefe erzeugt wurde, oder vorher. Wenn vorher heißt
			 * das, dass mit hoher Wahrscheinlichkeit im gleichen
			 * Auswertungsbaum schon mal die gleiche Boardkonstellation
			 * augetaucht ist - mit weniger Moves.
			 */
			if (zobrist != null && zobrist.depth >= depth) {
				switch (zobrist.nodeType) {
				case Constants.NodeType.EXACT:

					/*
					 * Ist der perfekte Wert - kann direkt returned werden :-)
					 * Scheiß auf die Kinder^^
					 */
					LinkedList<Move> bestValue = new LinkedList<Move>();
					bestValue.add(new MoveScore(zobrist.score));

					return bestValue;

				case Constants.NodeType.ALPHA:

					/* neuer lower-bound -> Eingrenzen */
					if (zobrist.score > newAlpha) {
						newAlpha = zobrist.score;
					}

					break;
				case Constants.NodeType.BETA:

					/* neuer upper-bound -> Eingrenzen */
					if (zobrist.score < newBeta) {
						newBeta = zobrist.score;
					}

					break;
				}

				/*
				 * Vll haben wir durch die neuen Bounds ja grade einen perfekten
				 * Score geschaffen? Können wir prunen?
				 */
				if (newAlpha >= newBeta) {
					LinkedList<Move> bestValue = new LinkedList<Move>();
					bestValue.add(new MoveScore(zobrist.score));

					return bestValue;
				}
			}

		}

		/* Move-Ordering! */
		if (moveOrdering) {
			oneMoveList = moveOrdering(board, oneMoveList, depth, moveNr,
					maximizingPlayer, color);
		}

		/* Normales Alpha-Beta! */
		LinkedList<Move> bestValue = new LinkedList<Move>();
		ZobristHash zobristValue = new ZobristHash();

		/* Schleife über alle Kinder */
		for (Move move : oneMoveList) {
			Bitboard newBoard = board.cloneBitboard();
			int newMoveNumber = moveNr - 1;

			/* Zug anwenden */
			newBoard.applyMoveAndRemovePieces(move);

			if (move.getMoveType() == Constants.MoveType.SPECIAL) {
				if (newMoveNumber < 1)
					continue;

				newMoveNumber--;
			}

			/* Rekursion! */
			LinkedList<Move> t = negaScout(newBoard, depth - 1,
					(newMoveNumber == 0) ? false : true,
					(newMoveNumber == 0) ? otherColor(color) : color,
					(newMoveNumber == 0) ? 4 : newMoveNumber, -newBeta,
					-newAlpha, useMemory, moveOrdering);
			scoreValue = (((MoveScore) t.getLast()).getScore());

			/* Negieren des Wertes - typisch für NegaScout/Negamax! */
			if (!maximizingPlayer) {
				((MoveScore) t.getLast()).setScore(-scoreValue);
			}

			/*
			 * Muss ein Research gemacht werden, weil das Move-Ordering schlecht
			 * war?
			 */
			if ((scoreValue > newAlpha) && (scoreValue < beta)
					&& !isPrincipalVariation) {
				/* Neues, tiefes suchen! */
				t = negaScout(newBoard, depth - 1, (newMoveNumber == 0) ? false
						: true, (newMoveNumber == 0) ? otherColor(color)
						: color, (newMoveNumber == 0) ? 4 : newMoveNumber,
						-beta, -newAlpha, useMemory, moveOrdering);
				scoreValue = (((MoveScore) t.getLast()).getScore());

				/* Negieren des Wertes - typisch für NegaScout/Negamax! */
				if (!maximizingPlayer) {
					((MoveScore) t.getLast()).setScore(-scoreValue);
				}
			}

			/*
			 * Den wichtigen, besten Move merken und als bestValue verzeichnen.
			 */
			if (scoreValue > newAlpha) {

				bestValue = t;
				bestValue.addFirst(move);

			}

			newAlpha = max(newAlpha, scoreValue);

			/* Pruning! -> Beta cut-off */
			if (newBeta <= newAlpha) {
				if (useMemory) {
					zobristValue.nodeType = Constants.NodeType.EXACT;
				}
				break;
			}

			/* Neues Null-Window setzen */
			newBeta = newAlpha + 1;

			isPrincipalVariation = false;
		}

		/* TODO: Hier muss das Alpha/Beta angepasst werden!!! */
		if (useMemory) {
			zobristValue.nodeType = (zobristValue.nodeType == Constants.NodeType.EXACT) ? Constants.NodeType.EXACT
					: Constants.NodeType.ALPHA;
		}
		/* Hier war vorher das return bestValue drin! */

		if (useMemory) {
			/* Füge die aktuelle Position in das Table ein! */
			zobristValue.depth = depth;
			zobristValue.hash = board.getZobristHash();
			zobristValue.score = scoreValue;
			zobristValue.maximizingPlayer = maximizingPlayer;
			zobristValue.moveNumber = moveNr;

			transposition.addTableEntry(zobristValue);
		}

		return bestValue;
	}

	public void run() {

		/* Minus infinity */
		int alpha = -1000000000;
		/* Infinity */
		int beta = 1000000000;

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
		board.setZobristHash(transposition.generateZobristHashForTable(board));

		/*
		 * das Transposition-Table neu aufsetzen, dass alte Memory-Einträge
		 * gelöscht werden!
		 */
		transposition.clearTable();

		super.bestMove = negaScout(board, super.generalAttributes.getDepth(),
				true, super.generalAttributes.getColor(), 4, alpha, beta,
				super.generalAttributes.getUseMemory(),
				super.generalAttributes.getMoveOrdering());

		/*
		 * Panic-Search --> Es wurde ein leerer Move ermittelt... Jetzt einen
		 * Zug ermitteln mit maximal 3 Rekursionen. Das sollte in 0-1 sek zu
		 * schaffen sein!
		 */
		if (super.bestMove.size() == 0) {
			super.isAllowedToRun = true;
			messageWriter.sendMessage("log PANIC SEARCH ACTIVATED!!!!!");
			super.bestMove = negaScout(board, 3, true,
					super.generalAttributes.getColor(), 4, alpha, beta,
					super.generalAttributes.getUseMemory(),
					super.generalAttributes.getMoveOrdering());
		}

		long estimatedTime = System.currentTimeMillis() - startTime;

		messageWriter.sendMessage("log finished calculating best move"
				+ ((super.isAllowedToRun == true) ? " all by himself :-)"
						: ", being interrupted by the timer..."));

		messageWriter.sendMessage("log time used: " + estimatedTime / 1000);

		messageWriter.sendMessage("log old score: "
				+ new TestEvaluation(generalAttributes).evaluateBoardState(
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

	/**
	 * Da ein Thread niemals mehrfach gestartet werden kann, muss eine neue
	 * Instanz erzeugt werden. Alle Variablen, die zum Lauf relevant sind werden
	 * neu initialisiert.
	 */
	public void newThread() {
		super.ownThread = new Thread(this);
		super.bestMove = new LinkedList<Move>();
		super.isAllowedToRun = true;
	}

}
