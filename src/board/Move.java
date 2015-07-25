package board;

/**
 * Definiert einen konkreten Zug für ein Spielfeld (festgelegt, aber nicht hier
 * enthalten. Ist in der aufrufenden Klasse definiert!) Dabei kann es sich um
 * einen normalen Zug handeln, ein gepushed oder gepulled-ten Zug oder ein Score
 * des Teilbaums.
 * 
 * @author maurice
 * 
 */
public abstract class Move {

	/**
	 * Welche Art des Moves vorliegt.
	 */
	private int moveType;

	/**
	 * Konstruktor.
	 */
	public Move(int moveType) {
		this.moveType = moveType;
	}

	public int getMoveType() {
		return moveType;
	}

	public void setMoveType(int moveType) {
		this.moveType = moveType;
	}

}
