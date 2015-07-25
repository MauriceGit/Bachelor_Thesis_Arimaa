package engine;

import java.util.LinkedList;

import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveScore;
import evaluation.Evaluation;
import evaluation.SimpleEvaluation;
import evaluation.TestEvaluation;

public class Minimax extends Engine {

	/**
	 * Konstruktor
	 */
	public Minimax(MessageOutputWriter messageWriter,
			EngineGeneralAttributes generalAttributes) {
		super(messageWriter, generalAttributes);
	}

	/**
	 * Errechnet den Minimax-Wert für eine bestimmte Rekursionstiefe eines
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
	public LinkedList<Move> minimax(Bitboard board, int depth,
			boolean maximizingPlayer, int color, int moveNr) {

		/* Generiert Liste mit allen verfügbaren Moves! */
		LinkedList<Move> oneMoveList = moveGen.generateAllColorMoves(board,
				color);

		/* Rekursionsabbruch */
		if (depth == 0 || oneMoveList.size() == 0 || !super.isAllowedToRun) {

			 Evaluation eval = new SimpleEvaluation(generalAttributes);
			
			 Move move = new MoveScore(eval.evaluateBoardState(board, false));
			
			 LinkedList<Move> res = new LinkedList<Move>();
			 res.add(move);

//			LinkedList<Move> res = new LinkedList<Move>();
//			res.add(new MoveScore(5));
			super.leafNodesVisited++;
			return res;
		}

		if (maximizingPlayer) {
			LinkedList<Move> bestValue = new LinkedList<Move>();
			bestValue.add(new MoveScore(-1000000000));

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
				LinkedList<Move> value = minimax(newBoard, depth - 1,
						(newMoveNumber == 0) ? false : true,
						(newMoveNumber == 0) ? otherColor(color) : color,
						(newMoveNumber == 0) ? 4 : newMoveNumber);

				/* Ist besser, als vorher? */
				if (value.getLast().getMoveType() == Constants.MoveType.SCORE) {

					if (((MoveScore) value.getLast()).getScore() > ((MoveScore) bestValue
							.getLast()).getScore()) {

						bestValue = value;
						bestValue.addFirst(move);

					}

				} else {
					messageWriter
							.sendMessage("log ERROR - last node not a MoveScore type!");
				}

			}

			return bestValue;

		} else {
			LinkedList<Move> bestValue = new LinkedList<Move>();
			bestValue.add(new MoveScore(1000000000));

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
				LinkedList<Move> value = minimax(newBoard, depth - 1,
						(newMoveNumber == 0) ? true : false,
						(newMoveNumber == 0) ? otherColor(color) : color,
						(newMoveNumber == 0) ? 4 : newMoveNumber);

				/* Ist besser, als der vorher? */
				if (value.getLast().getMoveType() == Constants.MoveType.SCORE) {

					if (((MoveScore) value.getLast()).getScore() < ((MoveScore) bestValue
							.getLast()).getScore()) {

						bestValue = value;
						bestValue.addFirst(move);

					}

				} else {
					messageWriter
							.sendMessage("log ERROR - last node not a MoveScore type!");
				}

			}

			return bestValue;

		}

	}

	public void run() {

		super.leafNodesVisited = 0;

		long startTime = System.currentTimeMillis();
		
		super.bestMove = minimax(board, super.generalAttributes.getDepth(),
				true, super.generalAttributes.getColor(), 4);

		long estimatedTime = System.currentTimeMillis() - startTime;
		
		
		
		/*
		 * Panic-Search --> Es wurde ein leeres Board ermittelt... Jetzt einen
		 * Zug ermitteln mit maximal 2 Rekursionen. Das sollte in 0-1 sek zu
		 * schaffen sein!
		 */
		if (super.bestMove.size() == 0) {
			super.isAllowedToRun = true;
			messageWriter.sendMessage("log PANIC SEARCH ACTIVATED!!!!!");
			super.bestMove = minimax(board, 3, true,
					super.generalAttributes.getColor(), 4);
		}

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
