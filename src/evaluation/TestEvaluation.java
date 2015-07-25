package evaluation;

import java.util.LinkedList;

import engine.EngineGeneralAttributes;

import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveGenerator;
import board.Piece;
import board.Position;

public class TestEvaluation extends Evaluation {

	/* Konstruktor */
	public TestEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);
	}

	
	
	/**
	 * Errechnet einen Wert (für Gold!) bezüglich der Materialaufteilung. Dabei
	 * werden einfach alle vorhandenen Figuren mit ihren Multiplieren
	 * multipliziert und zusammenaddiert.
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

		/* Bei weniger als 4 Hasen gibts ne Penalty! */
		int rabbitCount = board.getTypeCount(Constants.Color.GOLD,
				Constants.Type.RABBIT);
		if (rabbitCount < 4) {

			if (rabbitCount == 0) {
				return -10000000;
			}

			score -= (4 - rabbitCount) * Constants.Penalty.FEWRABBITS;
		}

		/* Wenn der Gegner weniger als 4 Hasen hat, gibts ne Belohnung! */
		rabbitCount = board.getTypeCount(Constants.Color.SILVER,
				Constants.Type.RABBIT);
		if (rabbitCount < 4) {
			if (rabbitCount == 0) {
				return 10000000;
			}

			score += (4 - rabbitCount) * Constants.Penalty.FEWRABBITS;
		}

		return score;
	}

	/**
	 * Es wird ein Wert errechnet, wieviele Figuren einer Farbe neben Traps
	 * stehen. Dabei wird nicht unterschieden, welche Art von Figur daneben
	 * steht.
	 * 
	 * @param board
	 *            das Board
	 * @param type
	 *            der Type
	 * @param color
	 *            die Farbe
	 * @return Wieviele Figuren des Typs type neben Traps stehen.
	 */
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
	 * Errechnet, wieviele Traps von der übergebenen Farbe kontrolliert werden.
	 * 
	 * @param board
	 *            das Board
	 * @param color
	 *            die Farbe
	 * @return Anzahl kontrollierter Traps.
	 */
	private int getCompleteTrapControlCount(Bitboard board, int color) {
		int score = 0;
		int run = 0, col = 0, row = 0;

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

			col = (run % 2 == 0) ? 2 : 5;
			row = (run < 2) ? 2 : 5;

			/**
			 * Wenn min. 2 gegnerische Figuren neben dem Trap stehen, gehört der
			 * nicht mir!
			 */
			if (board.getBitCount(trapControl
					& board.getColorBitmap(otherColor(color))) >= 2) {
				continue;
			}

			/*
			 * Wenn der eigene Elephant daneben steht.
			 */
			if ((trapControl & board.getTypeBitmap(color,
					Constants.Type.ELEPHANT)) != 0) {
				score++;
				continue;
			}

			/*
			 * 3 eigene Figuren stehen neben dem Trap
			 */
			if (board.getBitCount(trapControl & board.getColorBitmap(color)) >= 3) {
				score++;
				continue;
			}

			/* Schleife über alle Typen */
			for (int type = 0; type < 6; type++) {

				/* Die eigene Figur muss neben dem Trap stehen. */
				if ((trapControl & board.getTypeBitmap(color, type)) == 0) {
					continue;
				}

				/* Es darf keine fremde höhere Figur in der Nähe sein. */
				if ((board.area(col, row, 3) & board.getHigherTypeMap(type,
						otherColor(color))) != 0) {
					break;
				} else {
					/* Jetzt gehört der Trap uns! */
					score++;
					break;
				}

			}
			run++;
		}
		return score;
	}

	/**
	 * Bekommt einen long übergeben mit gesetzten Bits und guckt, ob die
	 * gesetzte Fläche der Farbe gehört, oder nicht. (Kontrolliert wird).
	 * 
	 * @param board
	 *            das Bitboard
	 * @param color
	 *            die Farbe
	 * @param color
	 *            die Farbe
	 * @return Ob die area durch die Farbe color kontrolliert wird.
	 */
	private boolean isControlledArea(Bitboard board, long area, int color) {
		int colorCnt = board.getBitCount(area & board.getColorBitmap(color));
		int otherColorCnt = board.getBitCount(area
				& board.getColorBitmap(otherColor(color)));

		/* Bei 3 Figuren gehört die Fläche mir! */
		if (colorCnt >= 3) {
			return true;
		}

		/* Bei einer Überzahl von min. 2 dann auch. */
		if (colorCnt - otherColorCnt >= 1) {
			return true;
		}

		return false;

	}

	/**
	 * Errechnet einen Wert (für Gold!) bezüglich der Trap-Kontrolle. Es gibt
	 * pro Figur, die neben einem Trap steht einen Punkt. Wobei höhere Figuren
	 * mehr Punkte bekommen, als nieder wertige.
	 * 
	 * @return Verhältnis von Material zwischen Gold und Silber.
	 */
	private int getTrapcontrolEvaluation(Bitboard board) {
		double score = 0;

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

				/***************************************
				 * BEACHTEN, dass ein Elephant neben nem Trap auch gefährlich
				 * ist, aber ein eigener Elephant neben nem fremden Trap nicht
				 * gefährdet!!!!!
				 *****************************************/

				/* Gehört der Trap mir? */
				if (isControlledArea(board, trapControl, Constants.Color.GOLD)) {
					/* Ein Gegner ist alleine daneben */
					if (board.getBitCount((trapControl & board
							.getColorBitmap(Constants.Color.SILVER))) == 1) {
						score += 1;
					}
				}

				/* Gehört der Trap dem Gegner? */
				if (isControlledArea(board, trapControl, Constants.Color.SILVER)) {
					/* Ein Gegner ist alleine daneben */
					if (board.getBitCount((trapControl & board
							.getColorBitmap(Constants.Color.GOLD))) == 1) {
						score -= 1;
					}
				}

			}

			/* Schleife über die Typen */
			for (int type = 0; type < 6; type++) {
				/* Schleife über den Long */
				for (int i = 0; i < 64; i++) {
					long maskG = (1L << i)
							& board.getTypeBitmap(Constants.Color.GOLD, type);
					long maskS = (1L << i)
							& board.getTypeBitmap(Constants.Color.SILVER, type);
					/* Ist dort eine Figur? */
					if ((trapControl & maskG) != 0) {
						score += getTypeImportance(type);
					}

					/* Ist dort eine Figur? */
					if ((trapControl & maskS) != 0) {
						score -= getTypeImportance(type);
					}

				}

			}

		}

		/* Bonuspunkte für komplett eigene Traps */
		score += getCompleteTrapControlCount(board, Constants.Color.GOLD)
				* Constants.TrapControl.CONTROL
				- getCompleteTrapControlCount(board, Constants.Color.SILVER)
				* Constants.TrapControl.CONTROL;

		/*
		 * Eine fremde Figur neben dem eigenen Trap gibt Bonuspunkte Wenn die
		 * gefreezed ist, noch welche. Wenn sie im Trap ist, noch welche :-)
		 */

		return (int) (score + 0.5);
	}

	/**
	 * Evaluiert die Bewegungsfreiheiten der eigenen Figuren. Je freier, desto
	 * besser. Je mehr Gegner gefreezed werden, desto besser.
	 * 
	 * @param board
	 *            das Board
	 * @return Evaluierung der Bewegungsfreiheit
	 */
	public int getMovementEvaluation(Bitboard board) {

		LinkedList<Move> moveListG = new MoveGenerator().generateAllColorMoves(
				board, Constants.Color.GOLD);
		LinkedList<Move> moveListS = new MoveGenerator().generateAllColorMoves(
				board, Constants.Color.SILVER);

		return moveListG.size() - moveListS.size();

	}

	/**
	 * Prüft, ob eine Partei mit diesem Zug gewinnt oder verliert.
	 * 
	 * @param board
	 *            das Board
	 * @return Win/Lose-Evaluation
	 */
	public int getWinLoseEvaluation(Bitboard board) {

		if ((board.getSetRowMap(7) & board.getTypeBitmap(Constants.Color.GOLD,
				Constants.Type.RABBIT)) != 0) {
			return 10000000;
		}

		if ((board.getSetRowMap(0) & board.getTypeBitmap(
				Constants.Color.SILVER, Constants.Type.RABBIT)) != 0) {
			return -10000000;
		}

		return 0;
	}

	/**
	 * Untersucht die Sicherheit der einzelnen Figuren - einzelstehende, ohne
	 * Unterstützung im eigenen Team, an unsicheren Positionen, in der Nähe von
	 * höheren Gegnern, zu weit bei Gegner. Gleichzeitig aber auch, dass die
	 * Gegner nicht allzu weit hinter die eigenen Linien kommen (ist das
	 * umzusetzen?).
	 * 
	 * @param board
	 *            das Board
	 * @return Evaluation der Sicherheit.
	 */
	public int getSavetyEvaluation(Bitboard board) {
		double score = 0;

		long[][] bboard = board.getBitboard();

		for (int i = 0; i < 6; i++) {
			long typeMap = bboard[i][Constants.Color.GOLD];

			for (int j = 0; j < 64; j++) {
				if (((1 << i) & typeMap) != 0) {
					int col = j % 8, row = j / 8;

					/* Mindestens 2-3 eigene in unmittelbarer Nähe */
					int bitCountG = board.getBitCount(board.area(col, row, 2)
							& board.getColorBitmap(Constants.Color.GOLD));
					if (bitCountG <= 2) {
						score -= 4 * bitCountG;
					} else {
						score += bitCountG;
					}

					/* Umgedreht für Silber */
					int bitCountS = board.getBitCount(board.area(col, row, 2)
							& board.getColorBitmap(Constants.Color.SILVER));
					if (bitCountS <= 2) {
						score += 4 * bitCountS;
					} else {
						score -= bitCountS;
					}

					/* Unsichere Position! */
					if ((col == 2 && row == 2) || (col == 2 && row == 5)
							|| (col == 5 && row == 2) || (col == 5 && row == 5)) {

						int directHelpG = board.getBitCount(board.area(col,
								row, 1)
								& board.getColorBitmap(Constants.Color.GOLD));
						int directHelpS = board.getBitCount(board.area(col,
								row, 1)
								& board.getColorBitmap(Constants.Color.SILVER));

						/* Unsicher für Gold! */
						if (directHelpG <= 1) {
							score -= 4 - bitCountG;
						}
						/* Unsicher für Silber */
						if (directHelpS <= 1) {
							score += 4 - bitCountS;
						}

						/* Allgemeine Penalty fürs Stehen auf einem Trap! */
						score -= 1;

					}

					/* Höhere gegnerische Figuren in unmittelbarer Nähe? */
					for (int type = i + 1; type < 6; type++) {
						int silverCnt = board.getBitCount(board.getTypeBitmap(
								Constants.Color.SILVER, type)
								& board.area(col, row, 2));
						int goldCnt = board.getBitCount(board.getTypeBitmap(
								Constants.Color.GOLD, type)
								& board.area(col, row, 2));

						score += goldCnt - silverCnt;
					}

				}
			}
		}

		return (int) score;
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
	public int evaluateBoardState(Bitboard board, boolean isQuiescence) {
		int score = 0;

//		score += (double) getWinLoseEvaluation(board);

		score += (double) getMaterialEvaluation(board)
				* Constants.EvalValue.MATERIAL_MY;

//		score += (double) getTrapcontrolEvaluation(board)
//				* Constants.EvalValue.TRAPCONTROL;
//
//		score += (double) getMovementEvaluation(board)
//				* Constants.EvalValue.MOVEMENT;
//
//		score += (double) getSavetyEvaluation(board)
//				* Constants.EvalValue.SAVETY;
//		
//		

		/* Wenn ich Silber spiele */
		if (generalAttributes.getColor() == Constants.Color.SILVER)
			score *= -1;

		return score;
	}

}
