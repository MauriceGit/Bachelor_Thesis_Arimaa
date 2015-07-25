package engine;

import java.util.LinkedList;

import evaluation.SimpleEvaluation;

import memory.Transposition;
import memory.ZobristHash;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Move;
import board.MoveScore;

public class MoveOrdering {

	private EngineGeneralAttributes generalAttributes;

	private MessageOutputWriter messageWriter;

	/**
	 * Konstruktor
	 */
	public MoveOrdering(EngineGeneralAttributes generalAttributes,
			MessageOutputWriter messageWriter) {
		this.generalAttributes = generalAttributes;
		this.messageWriter = messageWriter;
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
	public LinkedList<Move> moveOrdering(Bitboard board,
			LinkedList<Move> moveList, int depth, int moveNr,
			boolean maximizingPlayer, int color, int alpha, int beta) {

		/*
		 * Nur die ersten beiden Ebenen sortieren, weil da die größten Teilbäume
		 * gepruned werden können und der Overhead nicht so hoch ist
		 */
		if (depth <= 3 || generalAttributes.getDepth() > depth + 2) {
			return moveList;
		}

		if (generalAttributes.getDepth() <= depth + 1) {
			messageWriter.sendMessage("log moveOrdering at depth = " + depth);
		}

		/*
		 * maximale Rekursion. Also bei d=8 --> 4 und bei d=4 --> 0, da die
		 * Evaluierung entsprechend gut und umfangreich ist! Vll bei 8 auch
		 * nicht 4 nehmen, sondern 0 oder 2 oder 3 oder so.
		 */
		int maxDepth = 0;

		Bitboard newBoard = board.cloneBitboard();
		AlphaBeta newAlphaBeta = new AlphaBeta(messageWriter, generalAttributes);
		LinkedList<MoveOrderItem> scoredMoveList = new LinkedList<MoveOrderItem>();

		/* Attribute der AlphaBeta-Instanz setzen! */
		newAlphaBeta.setEvaluation(new SimpleEvaluation(generalAttributes));

		/* Errechnen der Werte für jedes einzelne Kind. */
		for (Move move : moveList) {
			/* Zug anwenden */

			newBoard = board.softCloneBitboard(newBoard);
			newBoard.applyMoveAndRemovePieces(move);

			/* führt einen kleinen alphaBeta-Search aus für Zwischenergebnisse! */
			LinkedList<Move> tmpList = newAlphaBeta.alphaBeta(newBoard,
					maxDepth, maximizingPlayer, color, moveNr, alpha, beta,
					false, false, false, maxDepth, false, false, false);

			/* Move mit seinem Ergebnis speichern */
			scoredMoveList.add(new MoveOrderItem(move, ((MoveScore) tmpList
					.getLast()).getScore()));

		}

		LinkedList<MoveOrderItem> sortedMoveList = new LinkedList<MoveOrderItem>();

		/* Bubblesort! */
		for (MoveOrderItem moveItem : scoredMoveList) {
			/* Wurde das Element eingefügt? */
			boolean put = false;

			if (sortedMoveList.size() != 0) {
				for (int i = 0; i < sortedMoveList.size(); i++) {
					/* Absteigend sortieren. Wenn also das Element größer ist */
					if (moveItem.getScore() >= sortedMoveList.get(i).getScore()) {
						sortedMoveList.add(i, moveItem);
						put = true;
						break;
					}
				}

				/* ganz hinten einfügen, wenns vorher nicht gepasst hat. */
				if (!put) {
					sortedMoveList.addLast(moveItem);
				}

			} else {
				/* Das allererste Element immer normal einfügen. */
				sortedMoveList.add(moveItem);
			}
		}

		LinkedList<Move> resultList = new LinkedList<Move>();

		/* Jetzt eine normale Liste erzeugen! */
		for (MoveOrderItem moveItem : sortedMoveList) {
			resultList.add(moveItem.getMove());
		}

		return resultList;
	}

	/**
	 * Bezieht sich beim Moveordering ausschließlich auf Werte aus dem
	 * Transposition table!
	 * 
	 * @param board
	 *            Spielfeld
	 * @param moveList
	 *            nicht sortierte Liste mit Moves
	 * @param transposition
	 *            Memory-Boardpositionen
	 * @param depth
	 *            Rekursionstiefe
	 * @return
	 */
	public LinkedList<Move> moveOrderingFromMemory(Bitboard board,
			LinkedList<Move> moveList, Transposition transposition, int depth) {

		/* Allerletzte Rekursion und daher kein Moveordering möglich und nötig */
		if (depth == 0) {
			return moveList;
		}

		Bitboard newBoard = board.cloneBitboard();

		LinkedList<MoveOrderItem> sortedMoveList = new LinkedList<MoveOrderItem>();

		for (Move move : moveList) {

			newBoard = board.softCloneBitboard(newBoard);
			newBoard.applyMoveAndRemovePieces(move);
			ZobristHash zobrist = transposition.lookupHash(newBoard
					.getZobristHash());
			boolean put = false;
			if (zobrist != null) {

				if (!sortedMoveList.isEmpty()) {
					for (int i = 0; i < sortedMoveList.size(); i++) {
						MoveOrderItem resMove = sortedMoveList.get(i);

						if (zobrist.score > resMove.getScore()) {
							sortedMoveList.add(i, new MoveOrderItem(move,
									zobrist.score));
							put = true;
							break;
						}

					}

					if (!put) {
						sortedMoveList.addLast(new MoveOrderItem(move,
								zobrist.score));
					}
				} else {
					sortedMoveList.add(new MoveOrderItem(move, zobrist.score));
				}

			} else {
				/* darf eigentlich nicht passieren... */
				sortedMoveList.addLast(new MoveOrderItem(move, -1000000));
			}

		}

		LinkedList<Move> resultList = new LinkedList<Move>();

		/* Jetzt eine normale Liste erzeugen! */
		for (MoveOrderItem moveItem : sortedMoveList) {
			resultList.add(moveItem.getMove());
		}

		return resultList;

	}

}
