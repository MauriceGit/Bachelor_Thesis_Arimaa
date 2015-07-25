package engine;

import java.util.Iterator;
import java.util.LinkedList;

import memory.RepetitionCounter;
import memory.Transposition;
import memory.ZobristHash;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveScore;
import evaluation.Evaluation;
import evaluation.SimpleEvaluation;

public class AlphaBeta extends Engine {

	/**
	 * Private Instanz der Memory-Haltung (Transposition-Table)
	 */
	private Transposition transposition;

	private long oldBoardHash;

	private int oldScore = 0;

	private int nodesVisited = 0;

	private Evaluation evaluation;

	private MoveOrdering moveOrder;

	private RepetitionCounter repetitionCounter;

	/**
	 * Konstruktor
	 */
	public AlphaBeta(MessageOutputWriter messageWriter,
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
	public LinkedList<Move> alphaBeta(Bitboard board, int depth,
			boolean maximizingPlayer, int color, int moveNr, int alpha,
			int beta, boolean useMemory, boolean moveOrdering,
			boolean normalSearch, int maxSearchDepth, boolean isQuiescence,
			boolean isPVNode, boolean isExtendedSearch) {

		/* Generiert Liste mit allen verfügbaren Moves! */
		LinkedList<Move> oneMoveList = moveGen.generateAllColorMoves(board,
				color);
		int newAlpha = alpha;
		int newBeta = beta;
		int newMoveNumber;
		int newDepth = depth;

		/* Rekursionsabbruch */
		if (depth <= 0 || oneMoveList.size() == 0 || !super.isAllowedToRun) {
			Move move;

			/* Kein Move-Ordering */
			if (normalSearch) {

				long hash = this.repetitionCounter.getBoardHash(board);

				if (this.repetitionCounter.getBoardCount(board, hash) >= 2
						|| oldBoardHash == hash) {

					move = new MoveScore(-100000000);
				} else {

					move = new MoveScore(evaluation.evaluateBoardState(board,
							isQuiescence));
					nodesVisited++;

				}

			} else {
				move = new MoveScore(
						evaluation.evaluateBoardState(board, false));
			}

			LinkedList<Move> res = new LinkedList<Move>();
			res.add(move);
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
			if (zobrist != null
					// && (depth % 4) == 0
					// && zobrist.depth >= depth

					&& zobrist.depth >= depth && zobrist.moveNumber == moveNr
					&& zobrist.maximizingPlayer == maximizingPlayer

			) {
				switch (zobrist.nodeType) {
				case Constants.NodeType.EXACT:

					/*
					 * Ist der perfekte Wert - kann direkt returned werden :-)
					 * Scheiß auf die Kinder^^
					 */
					if ((depth % 4) == 0) {
						LinkedList<Move> bestValue = new LinkedList<Move>();
						bestValue.add(new MoveScore(zobrist.score));
						return bestValue;
					}

					break;

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
			// oneMoveList = moveOrder.moveOrdering(board, oneMoveList, depth,
			// moveNr, maximizingPlayer, color, newAlpha, newBeta);
			oneMoveList = moveOrder.moveOrderingFromMemory(board, oneMoveList,
					transposition, depth);

			/* Altes Move-Ordering */
			// oneMoveList = moveOrder.moveOrdering(board, oneMoveList,
			// newDepth, moveNr, maximizingPlayer, color, newAlpha, newBeta);
		}

		/* Normales Alpha-Beta! */
		LinkedList<Move> bestValue = new LinkedList<Move>();
		ZobristHash zobristValue = null;

		if (useMemory) {
			zobristValue = new ZobristHash();
		}

		/*
		 * Performance-Optimierung - Nur noch eine eigene Instanz des Bitboards
		 * pro Rekursionsebene! (statt ca. 11.)
		 */
		Bitboard newBoard = board.cloneBitboard();
		boolean isFirst = true;
		// LinkedList<Move> value = new LinkedList<Move>();

		/* Liste mit Threads! */
		LinkedList<ParallelAlphaBetaWrapper> threadList = new LinkedList<ParallelAlphaBetaWrapper>();

		/* Schleife über alle Kinder */
		for (int i = 0; i < oneMoveList.size(); i++) {
			Move move = oneMoveList.get(i);

			Move moveApplied = move;

			LinkedList<Move> value = new LinkedList<Move>();

			if (isPVNode) {

				if (isFirst) {

					Iterator<Move> moveIt = oneMoveList.iterator();

					newBoard = board.softCloneBitboard(newBoard);

					newMoveNumber = moveNr - 1;
					newDepth = depth - 1;

					/* Zug anwenden */
					newBoard.applyMoveAndRemovePieces(move);

					if (move.getMoveType() == Constants.MoveType.SPECIAL) {

						if (newMoveNumber < 1)
							continue;

						newMoveNumber--;
						newDepth--;
					}

					boolean extended = isExtendedSearch;

					if (false && depth <= 1
							&& maxSearchDepth == generalAttributes.getDepth()
							&& !isExtendedSearch) {
						newDepth += 4;
						extended = true;
						messageWriter
								.sendMessage("log First Node Search extended.");
					}

					/* Alles normal! */
					value = alphaBeta(newBoard, newDepth,
							(newMoveNumber == 0) ? !maximizingPlayer
									: maximizingPlayer,
							(newMoveNumber == 0) ? otherColor(color) : color,
							(newMoveNumber == 0) ? 4 : newMoveNumber, newAlpha,
							newBeta, useMemory, moveOrdering, normalSearch,
							maxSearchDepth, isQuiescence, isPVNode, extended);

					if (moveIt.hasNext()) {
						moveIt.next();
					}

					int count = 0;

					/* Liste mit Threads erzeugen und starten -> Multithreading */
					while (moveIt.hasNext()) {
						Move tmpMove = moveIt.next();

						ParallelAlphaBetaWrapper parallelAlphaBeta = new ParallelAlphaBetaWrapper();

						newBoard = board.cloneBitboard();

						newMoveNumber = moveNr - 1;
						newDepth = depth - 1;

						/* Zug anwenden */
						newBoard.applyMoveAndRemovePieces(tmpMove);

						if (tmpMove.getMoveType() == Constants.MoveType.SPECIAL) {
							if (newMoveNumber < 1) {
								continue;
							}

							newMoveNumber--;
							newDepth--;
						}

						boolean isaPVNode = false;
						extended = isExtendedSearch;

						/*
						 * Wenn wir fast unten sind, und die ersten 3 Knoten
						 * betrachten und die Suche noch nicht extended wurde!
						 */
						if (false
								&& depth <= 1
								&& count <= 3
								&& maxSearchDepth == generalAttributes
										.getDepth() && !isExtendedSearch) {
							isaPVNode = false;
							newDepth += 4;
							extended = true;
							messageWriter.sendMessage("log Search extended.");
						}

						parallelAlphaBeta.setAlphaBetaRunParams(this, tmpMove,
								newBoard, newDepth,
								(newMoveNumber == 0) ? !maximizingPlayer
										: maximizingPlayer,
								(newMoveNumber == 0) ? otherColor(color)
										: color, (newMoveNumber == 0) ? 4
										: newMoveNumber, newAlpha, newBeta,
								useMemory, moveOrdering, normalSearch,
								maxSearchDepth, isQuiescence, isaPVNode,
								extended);

						parallelAlphaBeta.newThread();
						threadList.add(parallelAlphaBeta);
						parallelAlphaBeta.getThread().start();
						count++;
					}

				} else { /* isFirst */

					if (i < threadList.size()) {
						/* Darf eigentlich niemals einen Überlauf geben hier... */
						ParallelAlphaBetaWrapper pAB = threadList.get(i - 1);
						try {
							pAB.getThread().join();
						} catch (InterruptedException e) {
						}

						value = pAB.getResultList();
						moveApplied = pAB.getMove();

						if (value == null) {
							value = new LinkedList<Move>();
						}
					} else {
						/* break könnte auch gehen... */
						continue;
					}
				}

			} else { /* isPV */

				newBoard = board.softCloneBitboard(newBoard);

				newMoveNumber = moveNr - 1;
				newDepth = depth - 1;

				/* Zug anwenden */
				newBoard.applyMoveAndRemovePieces(move);

				if (move.getMoveType() == Constants.MoveType.SPECIAL) {
					if (newMoveNumber < 1)
						continue;

					newMoveNumber--;
					newDepth--;
				}

				/* Alles normal! */
				value = alphaBeta(newBoard, newDepth,
						(newMoveNumber == 0) ? !maximizingPlayer
								: maximizingPlayer,
						(newMoveNumber == 0) ? otherColor(color) : color,
						(newMoveNumber == 0) ? 4 : newMoveNumber, newAlpha,
						newBeta, useMemory, moveOrdering, normalSearch,
						maxSearchDepth, isQuiescence, false, isExtendedSearch);

			}

			if (maximizingPlayer) {

				/*
				 * Den wichtigen, besten Move merken und als bestValue
				 * verzeichnen.
				 */
				if (((MoveScore) value.getLast()).getScore() > newAlpha) {

					bestValue = value;
					bestValue.addFirst(moveApplied);
					newAlpha = ((MoveScore) value.getLast()).getScore();

				}

				/* Pruning! -> Beta cut-off */
				if (newBeta <= newAlpha) {
					if (useMemory) {
						zobristValue.nodeType = Constants.NodeType.ALPHA;
					}

					/* Mögliche aktive Threads platt machen!!! */
					Iterator<ParallelAlphaBetaWrapper> it = threadList
							.iterator();
					while (it.hasNext()) {
						it.next().getThread().interrupt();
					}

					break;
				}

			} else {

				/*
				 * Den wichtigen, besten Move merken und als bestValue
				 * verzeichnen.
				 */
				if (((MoveScore) value.getLast()).getScore() < newBeta) {

					bestValue = value;
					bestValue.addFirst(moveApplied);
					newBeta = ((MoveScore) value.getLast()).getScore();

				}

				/* Pruning -> Alpha cut-off */
				if (newBeta <= newAlpha) {
					if (useMemory) {
						zobristValue.nodeType = Constants.NodeType.BETA;
					}

					/* Mögliche aktive Threads platt machen!!! */
					Iterator<ParallelAlphaBetaWrapper> it = threadList
							.iterator();
					while (it.hasNext()) {
						it.next().getThread().interrupt();
					}

					break;
				}

			}

			isFirst = false;
		}

		if (bestValue.size() == 0) {
			if (maximizingPlayer) {
				bestValue.add(new MoveScore(newAlpha));
			} else {
				bestValue.add(new MoveScore(newBeta));
			}
		}

		if (useMemory) {
			zobristValue.nodeType = (zobristValue.nodeType == Constants.NodeType.BETA) ? Constants.NodeType.BETA
					: (zobristValue.nodeType == Constants.NodeType.ALPHA ? Constants.NodeType.ALPHA
							: Constants.NodeType.EXACT);

			/* Füge die aktuelle Position in das Table ein! */
			zobristValue.depth = depth;
			zobristValue.hash = board.getZobristHash();
			zobristValue.score = ((MoveScore) bestValue.getLast()).getScore();
			zobristValue.maximizingPlayer = maximizingPlayer;
			zobristValue.moveNumber = moveNr;

			synchronized (transposition.getClass()) {
				transposition.addTableEntry(zobristValue);
			}
		}

		return bestValue;
	}

	public void run() {

		/* Minus infinity */
		int alpha = -1000000000;
		/* Infinity */
		int beta = 1000000000;

		super.leafNodesVisited = 0;
		boolean moveOrderingSet = false;

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

		oldBoardHash = this.repetitionCounter.getBoardHash(board);

		oldScore = new SimpleEvaluation(generalAttributes).evaluateBoardState(
				board, false);

		evaluation = new SimpleEvaluation(generalAttributes);

		if (generalAttributes.getMoveOrdering()
				&& super.generalAttributes.getDepth() >= 6) {
			moveOrder = new MoveOrdering(generalAttributes, messageWriter);
			moveOrderingSet = true;
		}

		// System.out.println("Hash des Boards == '" + board.hashCode() + "'.");

		super.bestMove = alphaBeta(board, super.generalAttributes.getDepth(),
				true, super.generalAttributes.getColor(), 4, alpha, beta,
				super.generalAttributes.getUseMemory(), moveOrderingSet, true,
				super.generalAttributes.getDepth(), false,
				super.generalAttributes.getUseParallelization(), false);

		System.out.println(bestMove);

		/*
		 * Panic-Search --> Es wurde ein leerer Move ermittelt... Jetzt einen
		 * Zug ermitteln mit maximal 3 Rekursionen. Das sollte in 0-1 sek zu
		 * schaffen sein!
		 */
		if (super.bestMove.size() == 0) {
			super.isAllowedToRun = true;
			messageWriter.sendMessage("log PANIC SEARCH ACTIVATED!!!!!");
			super.bestMove = alphaBeta(board, 3, true,
					super.generalAttributes.getColor(), 4, alpha, beta,
					super.generalAttributes.getUseMemory(),
					super.generalAttributes.getMoveOrdering(), true, 3, false,
					super.generalAttributes.getUseParallelization(), false);
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

		messageWriter.sendMessage("log node count: " + nodesVisited);

		messageWriter.sendMessage("log used memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / (1024 * 1024) + "mb");

		/* Ausgabe des besten Moves! */
		if (super.printBestMove) {
			messageWriter.sendMessage(getBestMove());
		}
	}

	public void setOldBoardHash(long oldBoardHash) {
		this.oldBoardHash = oldBoardHash;
	}

	public void setRepetitionCounter(RepetitionCounter repetitionCounter) {
		this.repetitionCounter = repetitionCounter;
	}

	public int getNodesVisited() {
		return this.nodesVisited;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	public void setMoveOrdering(MoveOrdering moveOrder) {
		this.moveOrder = moveOrder;
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