package engine;

import java.util.LinkedList;

import board.Bitboard;
import board.Constants;
import board.Piece;
import board.Position;

public class Evaluation {

	// Instanz der Attribute.
	private EngineGeneralAttributes generalAttributes;

	/**
	 * Variablen, die einen Teil der Evaluierung eines Endknotens ausmacht! Sie
	 * werden vor dem rekursivem Abstieg gesetzt und im Leafknoten ausgewertet.
	 */
	// ....... ... .... .....

	// Konstruktor
	public Evaluation(EngineGeneralAttributes generalAttributes) {
		this.generalAttributes = generalAttributes;
	}

	/**
	 * Gibt den Wert einer Figur zurück.
	 * 
	 * @param type
	 *            Figur
	 * @return Wert
	 */
	private int getTypeValue(int type) {
		switch (type) {
		case Constants.Type.ELEPHANT:
			return Constants.TypeValue.ELEPHANT;
		case Constants.Type.CAMEL:
			return Constants.TypeValue.CAMEL;
		case Constants.Type.HORSE:
			return Constants.TypeValue.HORSE;
		case Constants.Type.DOG:
			return Constants.TypeValue.DOG;
		case Constants.Type.CAT:
			return Constants.TypeValue.CAT;
		case Constants.Type.RABBIT:
			return Constants.TypeValue.RABBIT;
		}
		return 0;
	}

	/**
	 * Errechnet einen Wert (für Gold!) bezüglich der Materialaufteilung.
	 * 
	 * @return Verhältnis von Material zwischen Gold und Silber.
	 */
	private int getMaterialEvaluation(Bitboard board) {
		int score = 0;

		/* Schleife über die Typen */
		for (int type = 0; type < 6; type++) {

			score += board.getTypeCount(Constants.Color.GOLD, type)
					* getTypeValue(type)
					- board.getTypeCount(Constants.Color.SILVER, type)
					* getTypeValue(type);

		}

		return score;
	}

	private int getTypeTrapControlEvaluation(Bitboard board, int type, int color) {
		int score = 0;
		long typeMask = board.getTypeBitmap(color, type);

		LinkedList<Long> trapList = new LinkedList<Long>();

		trapList.add(board.getBitmapAroundPosition(2, 2)
				& ~board.getMaskAt(2, 2));
		trapList.add(board.getBitmapAroundPosition(5, 2)
				& ~board.getMaskAt(5, 2));
		trapList.add(board.getBitmapAroundPosition(2, 5)
				& ~board.getMaskAt(2, 5));
		trapList.add(board.getBitmapAroundPosition(5, 5)
				& ~board.getMaskAt(5, 5));

		/* Schleife über die 4 Traps */
		for (long trapControl : trapList) {			
			/* Schleife über den Long */
			for (int i = 0; i < 64; i++) {
				long mask = (1L << i) & typeMask;
				/* Ist dort eine Figur? */
				if ((trapControl & mask) != 0) {
					score++;
				}
			}
		}
		return score;
	}

	/**
	 * Errechnet einen Wert (für Gold!) bezüglich der Trap-Kontrolle.
	 * 
	 * --> Hier seeeehr einfach. Punkte für neben dem Trap stehen^^
	 * 
	 * @return Verhältnis von Material zwischen Gold und Silber.
	 */
	private int getTrapcontrolEvaluation(Bitboard board) {
		int score = 0;

		/* Schleife über die Typen */
		for (int type = 0; type < 6; type++) {

			/**
			 * Wir ziehen die Kontrolle von Silber die der von Gold ab, um ein
			 * Verhältnis zu bekommen.
			 */
			score += getTypeTrapControlEvaluation(board, type,
					Constants.Color.GOLD)
					- getTypeTrapControlEvaluation(board, type,
							Constants.Color.SILVER);

		}

		return score;
	}

	/**
	 * Evaluiert die übergebene Boardsituation und gibt eine Zahl zwischen 0 und
	 * 100 zurück, oder so...
	 * 
	 * Sie wird immer für Gold berechnet!!! Bei Bedarf wird das Ergebnis dann
	 * negiert.
	 * 
	 * @param board
	 *            die zu evaluierende Boardsituation.
	 * @return Der errechnete Wert
	 */
	protected int evaluateBoardState(Bitboard board) {
		int score = 0;

		score += getMaterialEvaluation(board) * Constants.EvalValue.MATERIAL_MY;

		score += getTrapcontrolEvaluation(board)
				* Constants.EvalValue.TRAPCONTROL;

		/* Wenn ich Silber spiele */
		if (generalAttributes.getColor() == Constants.Color.SILVER)
			score *= -1;

		return score;
	}
}
