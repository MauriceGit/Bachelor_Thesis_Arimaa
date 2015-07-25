package memory;

import java.util.LinkedList;
import java.util.Random;

import board.Bitboard;
import board.Piece;
import board.Position;

public class RepetitionCounter {

	/* Liste mit Boardzuständen... */
	private LinkedList<Repetition> repetitionList = new LinkedList<Repetition>();

	/*
	 * die Random-Bit-Werte für die Berechnung des Hashes später.
	 */
	private long typePosTable[][][] = new long[2][6][64];

	/* Konstruktor */
	public RepetitionCounter() {
		initZobristHash();
	}

	/**
	 * Berechnung der Anzahl an Spielfeldsituationen.
	 * 
	 * @param board
	 * @param hash
	 * @return
	 */
	public int getBoardCount(Bitboard board, long hash) {

		for (Repetition rep : repetitionList) {
			if (rep.hash == hash) {
				return rep.counter;
			}
		}

		return 0;

	}

	/**
	 * Gibt zurück, wie oft die Spielfeldsituation schon aufgetreten ist.
	 * 
	 * @param board
	 * @return
	 */
	public int getBoardCount(Bitboard board) {
		long hash = generateZobristHashForTable(board);

		return getBoardCount(board, hash);

	}

	/**
	 * Speichert die übergebene Spielfeldsituation in der Liste ab und erhöht
	 * u.U. den Zähler.
	 * 
	 * @param board
	 *            Spielfeld
	 */
	public void saveBoard(Bitboard board) {

		long hash = generateZobristHashForTable(board);

		for (Repetition rep : repetitionList) {
			if (rep.hash == hash) {
				rep.counter++;
				return;
			}
		}

		repetitionList.addFirst(new Repetition(hash, 1));

	}

	/**
	 * Gibt den Zobrist-Hash des Boards zurück;
	 * 
	 * @param board
	 * @return
	 */
	public long getBoardHash(Bitboard board) {
		return generateZobristHashForTable(board);
	}

	/**
	 * Generiert aus einem Table einen kompletten Hash (Für den eigentlichen
	 * Start-Wert!) und setzt ihn für das übergebene Bitboard ein!
	 */
	private long generateZobristHashForTable(Bitboard board) {
		long hash = 0L;

		for (int at = 0; at < 64; at++) {
			/* Wenn das Feld nicht leer ist */
			Position pos = new Position(at % 8, at / 8);
			if (board.getBitmapAtPosition(pos) != 0) {
				Piece piece = board.findPieceAt(pos);
				hash ^= typePosTable[piece.getColor()][piece.getType()][at];
			}
		}

		return hash;

	}

	/**
	 * Initialisiert das Table mit den random-Bitstrings, mit denen der spätere
	 * Zobrist-Hash generiert und erzeugt werden kann.
	 */
	private void initZobristHash() {

		Random rand = new Random();

		for (int color = 0; color < 2; color++) {
			for (int type = 0; type < 6; type++) {
				for (int at = 0; at < 64; at++) {
					typePosTable[color][type][at] = rand.nextLong();
				}
			}
		}
	}

}
