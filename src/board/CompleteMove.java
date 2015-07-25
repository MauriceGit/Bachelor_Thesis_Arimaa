package board;

import java.util.LinkedList;

/**
 * Klasse, die einen kompletten Move anzeigt. Also 4 Züge.
 * 
 * @author maurice
 * 
 */
public class CompleteMove {

	LinkedList<Move> completeMove;

	public CompleteMove() {
		completeMove = new LinkedList<Move>();
	}

	/**
	 * Setzt einen Move in die Liste. Wenn der nicht ganz hinten ist, werden
	 * alle folgenden Moves gelöscht!
	 * 
	 * @param at
	 *            Position zum einfügen. Fängt bei 0 an!
	 * @param move
	 *            Der Move
	 */
	public void setMoveAt(int at, Move move) {

		if (at == 0)
			completeMove.removeAll(completeMove);
		else {
			while (completeMove.size() > at)
				completeMove.removeLast();
			
			completeMove.add(move);
		}
	}
	
	/**
	 * Löscht den allerletzten Eintrag!
	 */
	public void deleteLastMove () {
		if (completeMove.size() > 0)
			completeMove.removeLast();
	}
	
	/**
	 * Gibt die komplette Liste zurück!
	 * 
	 * @return die komplette Liste.
	 */
	public LinkedList<Move> getCompleteMove () {
		return completeMove;
	}

}
