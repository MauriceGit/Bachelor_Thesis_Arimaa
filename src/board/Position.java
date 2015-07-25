package board;

/**
 * Klasse, die schlicht und einfach eine Position definiert.
 * 
 * @author maurice
 * 
 */
public class Position {

	/*********************   Variablen   ************************/
	
	/**
	 * Collumn und Row auf dem Spielfeld.
	 * 
	 * col: links -> rechts row: unte -> oben
	 * 
	 * 0/0 ist also links unte, während 7/7 rechts oben ist.
	 */
	private int col, row;

	/*********************   Konstruktoren   ************************/
	
	public Position() {
		this.col = -1;
		this.row = -1;
	}

	public Position(int col, int row) {
		this.col = col;
		this.row = row;
	}
	
	public Position(char col, int row) {
		
		this.col = col - 97;
		this.row = row;
	}

	/*********************   Getter / Setter   ************************/
	
	public int getCol() {
		return col;
	}
	
	public char getCharCol () {
		return (char) (this.col + 97);
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}
	
	/*********************   Allgemeine Methoden   ************************/

	public String toString() {
		String res = "";

		switch (this.col) {
		case 0:
			res = res + "a";
			break;
		case 1:
			res = res + "b";
			break;
		case 2:
			res = res + "c";
			break;
		case 3:
			res = res + "d";
			break;
		case 4:
			res = res + "e";
			break;
		case 5:
			res = res + "f";
			break;
		case 6:
			res = res + "g";
			break;
		case 7:
			res = res + "h";
			break;
		default:
			break;
		}

		res = res + (row + 1);

		return res;
	}
}
