package engine;

import board.Move;

/**
 * 
 * Klasse, die zum Sortieren im MoveOrdering genutzt wird, um ein Tupel von Move
 * und Score zu speichern und auszuwerten ohne Hashmaps nutzen zu müssen.
 * 
 * @author maurice
 * 
 */
public class MoveOrderItem {

	private Move move;

	private int score;

	public MoveOrderItem(Move move, int score) {
		this.move = move;
		this.score = score;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}
