package evaluation;

import engine.EngineGeneralAttributes;
import board.Bitboard;
import board.Constants;

public class APEEvaluation extends Evaluation {

	/* Konstruktor */
	public APEEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);

	}

	/**
	 * Erweiterte herd-Evaluation für nur einen Teil eines Spielfeldes.
	 * 
	 * @param color
	 *            Spielfarbe
	 * @param col
	 *            Spalte
	 * @param row
	 *            Reihe
	 * @param distance
	 *            Anzahl an Steps
	 * @return Materialevaluation
	 */
	public double herdExtended(Bitboard board, int color, int col, int row,
			int distance) {
		HERDEvaluation herdEval = new HERDEvaluation(generalAttributes);
		/* Clone des Bitboards, auf die die Materialevaluation ausgewertet werden soll */
		Bitboard tmpBoard = board.cloneBitboard();
		/*  */
		long[][] tmpBitboard = tmpBoard.getBitboard();
		long stepMask = tmpBoard.area(col, row, distance);

		/*
		 * herd nur auf die *area des Boards anwenden. Also Board auf area
		 * verkleinern.
		 */
		for (int c = 0; c < 2; c++) {
			for (int t = 0; t < 6; t++) {
				tmpBitboard[t][c] = tmpBitboard[t][c] & stepMask;
			}
		}

		tmpBoard.setBitboard(tmpBitboard);
		
		//tmpBoard.printBitboard();

		herdEval.setPiecesValues(tmpBoard);

		return herdEval.herd(color);

	}

	/**
	 * Definition, ob der Spieler der Farbe color die Kontrolle einer Boardarea
	 * innehat.
	 * 
	 * @param color
	 *            Spielfarbe
	 * @param col
	 *            Spalte
	 * @param row
	 *            Reihe
	 * @param distance
	 *            Anzahl Schritte
	 * @return Wenn die Farbe die Kontrolle hat 1, sonst 0.
	 */
	private int control(Bitboard board, int color, int col, int row,
			int distance) {

		//System.out.println("       herdExt: " + herdExtended(board, color, col, row, distance));
		return (herdExtended(board, color, col, row, distance) > 0.5) ? 1 : 0;

	}

	/**
	 * Tuning-Funktion für die Wichtigkeit der Lokallität.
	 * 
	 * @param distance
	 *            Anzahl an Schritten
	 * @return Wert
	 */
	private int localfactor(int distance) {
		return (int) Math.pow(5, distance);
	}

	/**
	 * Tuning-Funktion für die Unterscheidung zwischen leeren und belegten
	 * Feldern (Und u.U. auch der verschiedenen Typen).
	 * 
	 * @param col
	 *            Spalte
	 * @param row
	 *            Reihe
	 * @param type
	 *            Typ der Figur
	 * @return Wert
	 */
	private double piecefactor(Bitboard board, int col, int row, int type) {
		if (board.isEmptyField(col, row))
			return 1.0;
		else
			return 2.5;
	}

	/**
	 * Errechnet eine globale Boardkontrolle für eine Farbe.
	 * 
	 * @param board
	 *            das Board
	 * @param color
	 *            Spielfarbe
	 * @return globale Kontrolle
	 */
	public double globalcontrol(Bitboard board, int color) {

		double result = 0;
		double tmpResult = 0;

		/* all board squares */
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {

				tmpResult = 0.0;
				/* all distances (rated) */
				for (int distance = 0; distance <= board.distancemax(col, row); distance++) {	
					
					//System.out.println("       control: " + control(board, color, col, row, distance));
					
					tmpResult += (double)control(board, color, col, row, distance)
							/ (double)localfactor(distance);
				}

				//System.out.println("Control at: " + col + "/" + row + " is: " + tmpResult);
				
				result += tmpResult
						* (double)piecefactor(board, col, row, Constants.Type.NONE);
			}
		}

		return result;
	}

	/**
	 * Die eigentliche Evaluierung APE für eine Farbe.
	 * 
	 * @param board
	 *            Bitboard
	 * @param color
	 *            Spielfarbe
	 */
	private double ape(Bitboard board, int color) {
		
		return (globalcontrol(board, color))
				/ (globalcontrol(board, color) + globalcontrol(board,
						(color == 0) ? 1 : 0));

	}

	/* Evaluierung */
	public int evaluateBoardState(Bitboard board, boolean isQuiescence) {
		
		return (int)(ape(board, Constants.Color.GOLD) * 100);
	}

}
