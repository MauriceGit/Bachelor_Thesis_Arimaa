package memory;

import board.Constants;

/**
 * Klasse, die für die Memory zuständig ist. Hier wird er Zobrist Hash verwaltet
 * und zwischengespeichert.
 * 
 * Die Klassenglobalen Variablen sind alle public um eine maximale Zugriffszeit
 * zu gewährleisten. Hier geht function vor configuration!
 * 
 * @author maurice
 */
public class ZobristHash {

	/* Der eigentliche Hash-Wert */
	public long hash = 0L;
	/* Rekursionstiefe an der gegebenen Stelle */
	public int depth = 0;
	/* Der Evaluierte Wert (finaler Wert, Alpha-bound oder Beta-bound) */
	public int score = 0;
	/* Welche Farbe die Boardposition ergeben hat */
	public boolean maximizingPlayer = false;
	/* Bei welcher MoveNumber der Cutoff oder die Position ermittelt wurde */
	public int moveNumber = -1;
	/* Der Typ des Knotens */
	public int nodeType = Constants.NodeType.NOTHING;
	
}
