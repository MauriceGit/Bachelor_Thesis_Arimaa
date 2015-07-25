package board;

public class MoveScore extends Move {

	/*
	 * Bewertung / Resultat dieses Zuges --> Beinhaltet damit auch alle weiteren
	 * Züge, die zu dem Ergebnis geführt haben schlussendlich
	 */
	private int score;


	public MoveScore(int score) {
		super(Constants.MoveType.SCORE);
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "Move score --> " + score;
	}

}
