package board;

public class MoveNormal extends Move {

	/* Von wo der Move geht */
	private Position from;
	/* In welche Richtung der Move geht: -1..1 für beide Richtungen */
	private int dirCol, dirRow;

	/* Konstruktor */
	public MoveNormal(Position from, int dirCol, int dirRow) {
		super (Constants.MoveType.NORMAL);
		this.from = from;
		this.dirCol = dirCol;
		this.dirRow = dirRow;
	}

	public Position getFrom() {
		return from;
	}

	public void setFrom(Position from) {
		this.from = from;
	}

	public int getDirCol() {
		return dirCol;
	}

	public void setDirCol(int dirCol) {
		this.dirCol = dirCol;
	}

	public int getDirRow() {
		return dirRow;
	}

	public void setDirRow(int dirRow) {
		this.dirRow = dirRow;
	}

	@Override
	public String toString() {
		String res;
		res = "Move ["
				+ from
				+ " --> "
				+ ((dirCol == -1) ? "left " : (dirCol == 1) ? "right"
						: (dirRow == -1) ? "down " : (dirRow == 1) ? "up   "
								: "-.-  ") + "]";

		return res;
	}

}
