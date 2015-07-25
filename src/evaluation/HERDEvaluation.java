package evaluation;

import board.Bitboard;
import board.Constants;
import engine.EngineGeneralAttributes;

public class HERDEvaluation extends Evaluation{

	public HERDEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);
		// TODO Auto-generated constructor stub
	}

	/*
	 * ein Array mit Werten, welche Figuren welcher Farbe vorhanden sind und
	 * wieviele
	 */
	private int[][] pieces = new int[2][20];

	/**
	 * Wieviele Major-Pieces einer Farbe getrapped wurden.
	 */
	private int getTrappedMajorPieces(Bitboard board, int color) {

		return (1 - board.getTypeCount(color, Constants.Type.ELEPHANT))
				+ (1 - board.getTypeCount(color, Constants.Type.CAMEL))
				+ (2 - board.getTypeCount(color, Constants.Type.HORSE))
				+ (2 - board.getTypeCount(color, Constants.Type.DOG))
				+ (2 - board.getTypeCount(color, Constants.Type.CAT));

	}

	/**
	 * Setzt das Array korrekt zur weiteren Berechnung.
	 */
	public void setPiecesValues(Bitboard board) {
		/* Schleife über die Farbe */
		for (int color = 0; color < 2; color++) {
			// Elephant
			pieces[color][0] = board.getTypeCount(color,
					Constants.Type.ELEPHANT);
			// Camel
			pieces[color][1] = board.getTypeCount(color, Constants.Type.CAMEL);
			// Last horse (the horse remaining on the board when the first one
			// has been trapped)
			pieces[color][2] = (board.getTypeCount(color, Constants.Type.HORSE) >= 1) ? 1
					: 0;
			// First horse
			pieces[color][3] = (board.getTypeCount(color, Constants.Type.HORSE) == 2) ? 1
					: 0;
			// Last dog (the dog remaining on the board when the first one has
			// been trapped)
			pieces[color][4] = (board.getTypeCount(color, Constants.Type.DOG) >= 1) ? 1
					: 0;
			// First dog
			pieces[color][5] = (board.getTypeCount(color, Constants.Type.DOG) == 2) ? 1
					: 0;
			// Last cat (the dog remaining on the board when the first one has
			// been trapped)
			pieces[color][6] = (board.getTypeCount(color, Constants.Type.CAT) >= 1) ? 1
					: 0;
			// First cat
			pieces[color][7] = (board.getTypeCount(color, Constants.Type.CAT) == 2) ? 1
					: 0;
			// First rabbit (note that the order has been inverted compared with
			// major pieces)
			pieces[color][8] = (board
					.getTypeCount(color, Constants.Type.RABBIT) == 8) ? 1 : 0;
			// Second rabbit
			pieces[color][9] = (board
					.getTypeCount(color, Constants.Type.RABBIT) >= 7) ? 1 : 0;
			// Third rabbit
			pieces[color][10] = (board.getTypeCount(color,
					Constants.Type.RABBIT) >= 6) ? 1 : 0;
			// Forth rabbit
			pieces[color][11] = (board.getTypeCount(color,
					Constants.Type.RABBIT) >= 5) ? 1 : 0;
			// Fifth rabbit
			pieces[color][12] = (board.getTypeCount(color,
					Constants.Type.RABBIT) >= 4) ? 1 : 0;
			// Sixth rabbit
			pieces[color][13] = (board.getTypeCount(color,
					Constants.Type.RABBIT) >= 3) ? 1 : 0;
			// Seventh rabbit
			pieces[color][14] = (board.getTypeCount(color,
					Constants.Type.RABBIT) >= 2) ? 1 : 0;
			// Last rabbit
			pieces[color][15] = (board.getTypeCount(color,
					Constants.Type.RABBIT) >= 1) ? 1 : 0;
			// Trapped major pieces
			pieces[color][16] = getTrappedMajorPieces(board, color);
			// Trapped rabbits
			pieces[color][17] = 8 - board.getTypeCount(color,
					Constants.Type.RABBIT);
			// Major pieces remaining on the board
			pieces[color][18] = 8 - getTrappedMajorPieces(board, color);
			// Rabbits remaining on the board
			pieces[color][19] = board
					.getTypeCount(color, Constants.Type.RABBIT);
		}

	}

	/**
	 * STRENGTH(G;s;i) is the number of free potential move of a piece n°“i”
	 * involved in a duel.
	 * 
	 * @param piece
	 *            Nummer einer Figur
	 * @param color
	 *            Spielfarbe
	 * @return die Anzahl potentieller freier Bewegungen in einem Duell.
	 */
	public int strength(int color, int piece) {
		int result = 0;
		
		for (int i = piece; i < 18; i++) {
			int tmp = pieces[(color == 0) ? 1 : 0][i];
			
			//print ("i: " + i + ", menge: " + tmp);
			
			result += tmp;
		}
		
		
		return pieces[color][piece] * result;
	}

	/**
	 * POWER(G;s;i) - The Power of a Level.
	 * 
	 * @param color
	 *            Spielfarbe
	 * @param piece
	 *            die Figur
	 * @return
	 */
	public int power(int color, int piece) {

		int result = 0;

		for (int i = 0; i <= piece; i++) {
			result += strength(color, i);
		}

		return result;
	}

	/**
	 * The balance of a level
	 * 
	 * @param color
	 *            Spielfarbe
	 * @param piece
	 *            die Figur
	 * @return die balance
	 */
	public double balance(int color, int piece) {
		double result = 0.0;
		
		if (power(color, piece) == 0) {
			return 0.0;
		}
		
		result = ((double)power(color, piece))
				/ ((double)power(color, piece) + (double)power((color == 0) ? 1 : 0, piece)); 
		
		return result;

	}

	/**
	 * Taking the goal of a match (depending on rabbits) into account.
	 * 
	 * @param color
	 *            Spielfarbe
	 * @return
	 */
	public double bias(int color) {
		double denominator = pieces[(color == 0) ? 1 : 0][18];
		// durch 0 teilen vorbeugen.
		if (denominator < 0.001 && denominator > -0.001) {
			denominator = 0.001;
		}

		return ((pieces[color][17] + pieces[(color == 0) ? 1 : 0][17])
				* pieces[color][19] / denominator);

	}

	/**
	 * Ab hier wird HERD nur noch für Gold ausgerechnet!
	 * 
	 * @return Der Materialwert für Gold.
	 */
	private double herd () {
		
		double denominator = 0;
		double numerator = 0;

		
		for (int i = 0; i < 16; i++) {	
			numerator += (pieces[0][i] + pieces[1][i]) * balance(Constants.Color.GOLD, i)
					+ bias(Constants.Color.GOLD);
			denominator += (pieces[0][i] + pieces[1][i]) + bias(Constants.Color.GOLD)
					+ bias(Constants.Color.SILVER);
		}
		
		if (Math.abs(numerator) < 0.01)
			return 0.0;
		
		return numerator / denominator;
		
	}
	
	/**
	 * The Holistic Evaluator of Remaining Duels
	 * 
	 * @param color
	 *            Spielfarbe
	 * @return Materialbewertung
	 */
	public double herd(int color) {

		double denominator = 0;
		double numerator = 0;

		
		for (int i = 0; i < 16; i++) {	
			numerator += (pieces[0][i] + pieces[1][i]) * balance(color, i)
					+ bias(color);
			denominator += (pieces[0][i] + pieces[1][i]) + bias(color)
					+ bias((color == 0) ? 1 : 0);
		}
		
		if (Math.abs(numerator) < 0.01)
			return 0.0;
		
		return numerator / denominator;
	}

	@Override
	public int evaluateBoardState(Bitboard board, boolean isQuiescence) {
		
		return (int)(herd(Constants.Color.GOLD)*100);
		
	}

}
