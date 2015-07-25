package memory;

import java.util.Random;

import board.Bitboard;
import board.MoveNormal;
import board.Piece;
import board.Position;

public class Transposition {

	/*
	 * großes Array für konstanten Zugriff mit Hilfe des Indizes, welches aus
	 * dem Zobrist-Hash ermittelt wird.
	 */
	private ZobristHash[] transpositionTable = null;
	/* Größe des Hash-Tables */
	private int size = 30000000;
	/*
	 * die Random-Bit-Werte für die Berechnung des Hashes später.
	 */
	private long typePosTable[][][] = new long[2][6][64];

	/**
	 * Konstruktor
	 * 
	 * @param size
	 *            Größe der Map in MB
	 */
	public Transposition(int size) {

		/**
		 * Hier muss die Berechnung rein, wieviele Instanzen tatsächlich
		 * abgebildet werden dürfen.
		 */


		initZobristHash();

	}

	/**
	 * Erst hier wird der endgültige Speicher reserviert, und nicht wie vorher
	 * bei Erstellung der Klasseninstanz.
	 */
	public void initTranspsition() {

		this.transpositionTable = new ZobristHash[size];

	}

	/**
	 * Löscht das gesamte Transposition-Table und setzt es leer neu auf.
	 */
	public void clearTable() {
		transpositionTable = null;
		this.transpositionTable = new ZobristHash[size];
	}

	/**
	 * Macht einen Lookup auf das Table und gibt uU den Zobrist-Zeugs zurück.
	 * 
	 * @param hash
	 *            Der Hash
	 * @return Zobrist-Knoten
	 */
	public ZobristHash lookupHash(long hash) {

		if (transpositionTable.length > Math.abs(hash) % (long) size) {
			return transpositionTable[(int) (Math.abs(hash) % (long) size)];
		}

		return null;
	}

	/**
	 * Fügt dem Table einen neuen Eintrag hinzu!
	 * 
	 * @param zobrist
	 *            Zobrist als Value.
	 */
	public void addTableEntry(ZobristHash zobrist) {
		if (transpositionTable.length > Math.abs(zobrist.hash) % (long) size) {
			long index = Math.abs(zobrist.hash) % (long) size;
			transpositionTable[(int) index] = zobrist;
		}
	}

	/**
	 * Generiert aus einem Table einen kompletten Hash (Für den eigentlichen
	 * Start-Wert!) und setzt ihn für das übergebene Bitboard ein!
	 */
	public long generateZobristHashForTable(Bitboard board) {
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

	/**
	 * Funktion, um den Zobrist-Hash des übergebenen Boards zu updaten bezüglich
	 * einer Position. Das kann z.B. das Setzen oder Entfernen einer Figur an
	 * einer Position sein. Ein Zug bedingt daher mehrere Aufrufe dieser
	 * Funktion (min. 1x setzen und 1x entfernen von der vorherigen Position)!
	 * 
	 * @param board
	 *            das Bitboard, für den der Hash geupdated wird
	 * @param color
	 *            Farbe der zu setzenden Figur
	 * @param type
	 *            Typ der zu setzenden Figur
	 * @param pos
	 *            Position der zu setzenden Figur
	 */
	public void setTypeAt(Bitboard board, int color, int type, int pos) {

		board.setZobristHash(board.getZobristHash()
				^ typePosTable[color][type][pos]);

	}

	public void setTypeAt(Bitboard board, int color, int type, int col, int row) {
		setTypeAt(board, color, type, 8 * col + row);
	}

}
