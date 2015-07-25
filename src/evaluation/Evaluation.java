package evaluation;

import board.Bitboard;
import board.Constants;
import engine.EngineGeneralAttributes;

public abstract class Evaluation {

	// Instanz der Attribute.
	protected EngineGeneralAttributes generalAttributes;

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
	protected int getTypeValue(int type) {
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
	 * Gibt einen Multiplier eines Typs zurück.
	 * 
	 * @param type
	 *            der Typ der Figur
	 * @return den Multiplier
	 */
	protected double getTypeImportance(int type) {
		switch (type) {
		case Constants.Type.ELEPHANT:
			return Constants.TypeImportance.ELEPHANT;
		case Constants.Type.CAMEL:
			return Constants.TypeImportance.CAMEL;
		case Constants.Type.HORSE:
			return Constants.TypeImportance.HORSE;
		case Constants.Type.DOG:
			return Constants.TypeImportance.DOG;
		case Constants.Type.CAT:
			return Constants.TypeImportance.CAT;
		case Constants.Type.RABBIT:
			return Constants.TypeImportance.RABBIT;
		}
		return 1.0;
	}

	/**
	 * Gibt die jeweils andere Farbe zurück.
	 * 
	 * @param color
	 *            Farbe
	 * @return andere Farbe
	 */
	protected int otherColor(int color) {
		return (color == 0) ? 1 : 0;
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
	public abstract int evaluateBoardState(Bitboard board, boolean isQiescence);

	public int evaluateBoardStateMoveOrdering(Bitboard board) {
		return evaluateBoardState(board, false);
	}
}
