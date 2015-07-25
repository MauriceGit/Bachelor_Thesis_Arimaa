package evaluation;

import java.util.LinkedList;

import network.MessageOutputWriter;

import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveScore;
import board.Piece;
import board.Position;
import engine.AlphaBeta;
import engine.EngineGeneralAttributes;

public class SimpleEvaluation extends Evaluation {

	/* Konstruktor */
	public SimpleEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);
	}

	/**
	 * Gibt den Wert einer Figur zurück.
	 * 
	 * @param type
	 *            Figur
	 * @return Wert
	 */
	protected double getRabbitValue(int count) {

		switch (count) {
		case 8:
			return 1;
		case 7:
			return 1.5;
		case 6:
			return 2.0;
		case 5:
			return 2.5;
		case 4:
			return 3.0;
		case 3:
			return 4.0;
		case 2:
			return 5.0;
		case 1:
			return 12.0;
		case 0:
			return 1000.0;
		}

		return 0;
	}

	/**
	 * Prüft, ob eine Partei mit diesem Zug gewinnt oder verliert.
	 * 
	 * @param board
	 *            das Board
	 * @return Win/Lose-Evaluation
	 */
	private double getWinLoseEvaluation(Bitboard board) {
		double score = 0.0;

		/* Gold gewinnt */
		if ((board.getSetRowMap(7) & board.getTypeBitmap(Constants.Color.GOLD,
				Constants.Type.RABBIT)) != 0
				|| board.getTypeCount(Constants.Color.SILVER,
						Constants.Type.RABBIT) == 0) {
			if (super.generalAttributes.getColor() == Constants.Color.GOLD) {
				score = 10000000.0;
				score *= Constants.EvalValue.DEPTH_DEPENDING_RATING;
			} else {
				score = -10000000.0;
				score /= Constants.EvalValue.DEPTH_DEPENDING_RATING;
			}
		}

		/* Silber gewinnt */
		if ((board.getSetRowMap(0) & board.getTypeBitmap(
				Constants.Color.SILVER, Constants.Type.RABBIT)) != 0
				|| board.getTypeCount(Constants.Color.GOLD,
						Constants.Type.RABBIT) == 0) {
			if (super.generalAttributes.getColor() == Constants.Color.GOLD) {
				score = -10000000.0;
				score /= Constants.EvalValue.DEPTH_DEPENDING_RATING;
			} else {
				score = 10000000.0;
				score *= Constants.EvalValue.DEPTH_DEPENDING_RATING;
			}
		}

		return score;
	}

	/**
	 * Evaluierung des übrigen Materials auf dem Spielfeld. Sehr einfach
	 * gehalten.
	 * 
	 * @param board
	 *            das Spielfeld
	 * @return Der Wert der Materialevaluation
	 */
	private double materialEvaluation(Bitboard board) {
		/* Der Maximalwert, wenn alle Figuren (Außer Hasen) noch vorhanden sind! */
		double goldScore = 0;

		/* Schleife über die Typen, außer Hasen */
		for (int type = 0; type < 5; type++) {

			goldScore += board.getTypeCount(Constants.Color.GOLD, type)
					* getTypeValue(type);
			goldScore -= board.getTypeCount(Constants.Color.SILVER, type)
					* getTypeValue(type);
		}

		/*
		 * Penalty für die Hasen. Gibt immer penalty, aber halt mehr, wenn
		 * weniger da sind ;-)
		 */
		goldScore -= getRabbitValue(board.getTypeCount(Constants.Color.GOLD,
				Constants.Type.RABBIT));
		goldScore += getRabbitValue(board.getTypeCount(Constants.Color.SILVER,
				Constants.Type.RABBIT));

		if (super.generalAttributes.getColor() != Constants.Color.GOLD) {
			goldScore *= -1;
		}

		return goldScore;

	}

	/**
	 * Errechnet die Anzahl aller Figuren einer Farbe.
	 * 
	 * @param board
	 *            Spielfeld
	 * @return Anzahl Figuren.
	 */
	private int getPiecesCount(Bitboard board, int color) {

		int count = 0;

		for (int type = 0; type < 6; type++) {
			count += board.getTypeCount(color, type);
		}

		return count;
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
			// score += board.getTypeCount(Constants.Color.GOLD, type)
			// * getTypeImportance(type)
			// - board.getTypeCount(Constants.Color.SILVER, type)
			// * getTypeImportance(type);

		}

		/* Bei weniger als 4 Hasen gibts ne Penalty! */
		int rabbitCount = board.getTypeCount(Constants.Color.GOLD,
				Constants.Type.RABBIT);
		if (rabbitCount < 4) {
			score -= (4 - rabbitCount) * Constants.Penalty.FEWRABBITS;
		}

		/* Wenn der Gegner weniger als 4 Hasen hat, gibts ne Belohnung! */
		rabbitCount = board.getTypeCount(Constants.Color.SILVER,
				Constants.Type.RABBIT);
		if (rabbitCount < 4) {
			score += (4 - rabbitCount) * Constants.Penalty.FEWRABBITS;
		}

		if (super.generalAttributes.getColor() != Constants.Color.GOLD) {
			score *= -1;
		}

		return score;
	}

	/**
	 * Implementierung der Materialevaluation.
	 * 
	 * @param board
	 * @return
	 */
	private double harLogMaterialEvaluation(Bitboard board) {
		double score = 0.0;
		double q = 1.447530126;
		double g = 0.6314442034;
		int typeCountG = 0;
		int typeCountS = 0;

		for (int type = 0; type < 5; type++) {
			typeCountG = 0;
			typeCountS = 0;
			/* nur höhere gegnerische Figuren angucken! */
			for (int typeEnemy = 0; typeEnemy < type; typeEnemy++) {
				typeCountG += board.getTypeCount(Constants.Color.GOLD,
						typeEnemy);
				typeCountS += board.getTypeCount(Constants.Color.SILVER,
						typeEnemy);
			}

			/* Minus, da für Silber berechnet */
			if (typeCountG == 0) {
				score -= 2 / q;
			} else {
				score -= 1 / (q + typeCountG);
			}

			/* Für Gold berechnet */
			if (typeCountS == 0) {
				score += 2 / q;
			} else {
				score += 1 / (q + typeCountS);
			}
		}

		score += g
				* (Math.log(board.getTypeCount(Constants.Color.GOLD,
						Constants.Type.RABBIT)) + Math.log(getPiecesCount(
						board, Constants.Color.GOLD)));

		score -= g
				* (Math.log(board.getTypeCount(Constants.Color.SILVER,
						Constants.Type.RABBIT)) + Math.log(getPiecesCount(
						board, Constants.Color.SILVER)));

		if (super.generalAttributes.getColor() != Constants.Color.GOLD) {
			score *= -1;
		}

		return score * 100.0;
	}

	/**
	 * Berechnet die Stärke einer Figur anhand der Anzahl höherer Figuren in
	 * seiner Nähe.
	 * 
	 * @param count
	 *            Anzahl höherer Figuren.
	 * @return Stärke
	 */
	private int getFigureStrength(int count) {

		switch (count) {
		case 0:
			return 32;
		case 1:
			return 22;
		case 2:
			return 17;
		case 3:
			return 13;
		case 4:
			return 11;
		case 5:
			return 10;
		case 6:
			return 9;
		case 7:
			return 9;
		default:
			return 9;
		}

	}

	/**
	 * Errechnet einen Abstandswert, abhängig von einer Position und seiner
	 * Entfernung zu ihr.
	 * 
	 * @param col
	 * @param row
	 * @return
	 */
	private int getDistanceValue(int trapCol, int trapRow, int col, int row) {

		int distance = Math.abs(trapCol - col) + Math.abs(trapRow - row);

		switch (distance) {
		case 0:
			return 18;
		case 1:
			return 20;
		case 2:
			return 13;
		case 3:
			return 5;
		case 4:
			return 2;
		}

		return 0;
	}

	/**
	 * Gibt einen Wert aus den Constanten zurück, der der Figur entspricht.
	 * 
	 * @param type
	 *            der Typ
	 * @return
	 */
	private double getMovementTypeValue(int type) {
		switch (type) {
		case Constants.Type.ELEPHANT:
			return Constants.EvalValue.MOVEMENT_ELEPHANT;
		case Constants.Type.CAMEL:
			return Constants.EvalValue.MOVEMENT_CAMEL;
		case Constants.Type.HORSE:
			return Constants.EvalValue.MOVEMENT_HORSE;
		case Constants.Type.DOG:
			return Constants.EvalValue.MOVEMENT_DOG;
		case Constants.Type.CAT:
			return Constants.EvalValue.MOVEMENT_CAT;
		case Constants.Type.RABBIT:
			return Constants.EvalValue.MOVEMENT_RABBIT;
		default:
			return 0.0;
		}
	}

	/**
	 * Überprüft, ob die beiden Figuren tatsächlich sicher sind, oder ob eine
	 * Form der falseProtection vorliegt.
	 * 
	 * Wenn eine Figur auf dem Trap steht (zusätzlich), wird überprüft, ob die
	 * beiden sichernden Figuren unter false protection stehen.
	 * 
	 * @param board
	 *            Das Spielfeld
	 * @param firstPiece
	 *            Erste Figur
	 * @param secondPiece
	 *            Zweite Figur
	 * @param pieceOnTrap
	 *            Ob eine Figur direkt auf dem Trap steht.
	 * @return Ob eine Form des false protection vorliegt.
	 */
	private boolean isFalseProtection(Bitboard board, int col, int row,
			long colorMap, boolean pieceOnTrap) {

		Piece firstPiece = null;
		Piece secondPiece = null;

		if ((board.getMaskAt(col - 1, row) & colorMap) != 0) {
			firstPiece = board.findPieceAt(new Position(col - 1, row));
		}
		if ((board.getMaskAt(col + 1, row) & colorMap) != 0) {
			if (firstPiece == null) {
				firstPiece = board.findPieceAt(new Position(col + 1, row));
			} else {
				secondPiece = board.findPieceAt(new Position(col + 1, row));
			}
		}
		if ((board.getMaskAt(col, row - 1) & colorMap) != 0) {
			if (firstPiece == null) {
				firstPiece = board.findPieceAt(new Position(col, row - 1));
			} else {
				secondPiece = board.findPieceAt(new Position(col, row - 1));
			}
		}
		if ((board.getMaskAt(col, row + 1) & colorMap) != 0) {
			secondPiece = board.findPieceAt(new Position(col, row + 1));
		}

		/* Darf nicht passieren! */
		if (firstPiece == null || secondPiece == null) {
			return true;
		}

		long firstHigherEnemyMap = board.getHigherTypeMap(firstPiece.getType(),
				firstPiece.getColor())
				& board.area(firstPiece.getPosition().getCol(), firstPiece
						.getPosition().getRow(), 1);
		long secondHigherEnemyMap = board.getHigherTypeMap(
				secondPiece.getType(), secondPiece.getColor())
				& board.area(secondPiece.getPosition().getCol(), secondPiece
						.getPosition().getRow(), 1);

		/* Mindestens eine der beiden hat keinen höheren Gegner neben sich! */
		if (firstHigherEnemyMap == 0 || secondHigherEnemyMap == 0) {
			return false;
		}

		/*
		 * Wenn insgesamt mindestens zwei höhere Figuren da sind, die beide
		 * Figuren bedrohen!
		 */
		if (board.getBitCount(firstHigherEnemyMap | secondHigherEnemyMap) >= 2) {
			return true;
		}

		return false;
	}

	/**
	 * Überprüft, wieviele eigene Figuren direkt neben der Figur stehen.
	 * 
	 * WICHTIG: Darf nur für Felder aufgerufen werden, die nicht am Rand
	 * liegen!!!
	 * 
	 * @param board
	 * @param piece
	 * @return Anzahl Unterstützer
	 */
	private int directPosSupportCount(Bitboard board, int col, int row,
			long colorMap) {
		int count = 0;

		if ((board.getMaskAt(col - 1, row) & colorMap) != 0) {
			count++;
		}
		if ((board.getMaskAt(col + 1, row) & colorMap) != 0) {
			count++;
		}
		if ((board.getMaskAt(col, row - 1) & colorMap) != 0) {
			count++;
		}
		if ((board.getMaskAt(col, row + 1) & colorMap) != 0) {
			count++;
		}

		return count;
	}

	/**
	 * Guckt, ob ein Piece direkt von einem Gegner gefangen genommen (gepushed)
	 * werden kann.
	 * 
	 * Dabei müssen alle Fälle abgedeckt werden, bei denen der Gegner 1 und 2
	 * Steps von der eigenen Figur entfernt ist.
	 * 
	 * @param board
	 * @param piece
	 * @return
	 */
	private boolean checkCapture(Bitboard board, Piece piece,
			long higherEnemyMap, long area, int col, int row) {
		boolean capture = false;

		/*
		 * Eine Figur ist auch bedroht, wenn ein Gegner 3 Schritte weg ist,
		 * solange der Weg dazwischen frei ist!
		 */
		long pieceMap = board.area(piece.getPosition().getCol(), piece
				.getPosition().getRow(), 3);

		higherEnemyMap = higherEnemyMap & pieceMap;

		long generalMap = board.getGeneralBitmap();

		for (int trapCol = 0; trapCol < 8; trapCol++) {
			for (int trapRow = 0; trapRow < 8; trapRow++) {
				long mask = board.getMaskAt(trapCol, trapRow);
				/* In dem anzuguckendem Bereich ist ein höherer Gegner! */
				if ((mask & higherEnemyMap) != 0) {

					boolean trapSupportedByEnemy = (area & higherEnemyMap) != 0;

					/* Spalten checken! */
					if (Math.abs(piece.getPosition().getCol() - trapCol) <= 1) {

						long pieceColMap = board.getColMap(piece.getPosition()
								.getCol());
						long pieceCol = pieceColMap
								& generalMap
								& ~mask
								& ~board.getMaskAt(
										piece.getPosition().getCol(), piece
												.getPosition().getRow());

						long enemyColMap = board.getColMap(trapCol)
								& generalMap;
						long enemyCol = enemyColMap
								& ~mask
								& ~board.getMaskAt(
										piece.getPosition().getCol(), piece
												.getPosition().getRow());

						for (int i = 0; i < 8; i++) {
							if ((i < piece.getPosition().getRow() && i < trapRow)
									|| (i > piece.getPosition().getRow() && i > trapRow)) {
								pieceCol = pieceCol & ~board.getRowMap(i);
								enemyCol = enemyCol & ~board.getRowMap(i);
							}
						}

						/*
						 * Eine Spalte ist leer, oder
						 * 
						 * Andere Spalte ist leer, oder
						 * 
						 * Die zwei Felder dazwischen sind frei, wenn die
						 * Figuren 2 auseinander stehen
						 */

						/*
						 * Ein Trap befindet sich auf der gleichen Spalte, wie
						 * die Figur --> Ist (soweit ich das sehe) immer auch
						 * zwischen dem Gegner und der Figur!
						 */
						boolean trapOnPieceCol = piece.getPosition().getCol() == col;
						boolean trapOnEnemyCol = trapCol == col;
						/* Der Gegner ist zwei Steps weg */
						boolean twoStepsBetween = Math.abs(trapRow
								- piece.getPosition().getRow()) == 2;
						boolean isSameCol = piece.getPosition().getCol() == trapCol;
						/* Die Reihe zwischen Gegner und Figur */
						int rowBetween = (int) Math.abs((trapRow
								+ piece.getPosition().getRow() + 0.5) / 2);
						/* Ob der Trap auf dem Feld zwischen den Figuren ist */
						boolean trapOnRowBetween = (board.getRowMap(rowBetween) & board
								.getMaskAt(col, row)) != 0;
						/*
						 * Ob das Feld zwischen den Figuren (quer - beide) leer
						 * ist
						 */
						boolean pieceColMiddleEmpty = (board.getMaskAt(piece
								.getPosition().getCol(), rowBetween) & generalMap) == 0;
						boolean enemyColMiddleEmpty = (board.getMaskAt(trapCol,
								rowBetween) & generalMap) == 0;

						if (enemyCol == 0) {
							if (twoStepsBetween || isSameCol) {
								if (!trapOnEnemyCol
										|| trapSupportedByEnemy
										|| (!trapOnRowBetween
												&& pieceColMiddleEmpty && enemyColMiddleEmpty)) {

								}
							} else {
								return true;
							}
						}

						if (pieceCol == 0) {
							if (twoStepsBetween || isSameCol) {
								if ((!trapOnPieceCol || trapSupportedByEnemy || (!trapOnRowBetween
										&& pieceColMiddleEmpty && enemyColMiddleEmpty))) {
									return true;
								}
							} else {
								return true;
								// /* Ein Step zwischen Figur und Gegner */
								//
								// long enemyColorMap = board
								// .getColorBitmap(otherColor(piece
								// .getColor()));
								// Piece bigEnemy = board
								// .findPieceAt(new Position(trapCol,
								// trapRow));
								// long smallerOwnPiecesMap = board
								// .getLowerEqualTypeMap(
								// bigEnemy.getType() + 1,
								// piece.getColor());
								// int col1 = trapCol;
								// int row1 = piece.getPosition().getRow();
								// /* Ob Feld 1 leer ist */
								// boolean isEmptyField1 =
								// (board.getMaskAt(col1,
								// row1) & generalMap) == 0;
								// int directionRow1 = (trapRow > piece
								// .getPosition().getRow()) ? -1 : 1;
								// int directionCol1 = (trapCol > piece
								// .getPosition().getCol()) ? -1 : 1;
								// /* Ob Feld 1 ein Trap ist */
								// boolean isTrap1 = (board.getMaskAt(trapCol,
								// piece.getPosition().getRow()) & board
								// .getMaskAt(col, row)) != 0;
								// /*
								// * Wenn das Feld ein Trap ist, aber das Feld
								// * kann vom Gegner unterstützt werden oder
								// wird
								// * schon unterstützt. Und das Feld ist leer,
								// * kann also vom Gegner begangen werden.
								// */
								// boolean indirectDoubleSupported1 =
								// !isEmptyField1 ? false
								// : (!isTrap1
								// || (board.area(col, row
								// + directionRow1, 1) & enemyColorMap) != 0 ||
								// (board
								// .area(col + directionCol1, row,
								// 1) & enemyColorMap) != 0);
								// if (indirectDoubleSupported1) {
								// return true;
								// }
								//
								// /*
								// * Wenn da ein Gegner steht, aber dieser in
								// * einem Schritt weggehen kann
								// */
								// if ((board.getMaskAt(col1, row1) &
								// enemyColorMap) != 0
								// && board.getPieceMovementBitmap(board
								// .findPieceAt(new Position(col1,
								// row1))) != 0) {
								// return true;
								// }
								//
								// /**
								// * isSmallerPiece && kannWeggeschobenWerden &&
								// * (IsNotATrap || TrapIsDirectDoubleSupported)
								// */

							}
						}

					}

					/* Reihen checken! */
					if (Math.abs(piece.getPosition().getRow() - trapRow) <= 1) {

						long pieceRowMap = board.getRowMap(piece.getPosition()
								.getRow());
						long pieceRow = pieceRowMap
								& generalMap
								& ~mask
								& ~board.getMaskAt(
										piece.getPosition().getCol(), piece
												.getPosition().getRow());
						long enemyRowMap = board.getColMap(trapRow);
						long enemyRow = enemyRowMap
								& generalMap
								& ~mask
								& ~board.getMaskAt(
										piece.getPosition().getCol(), piece
												.getPosition().getRow());

						for (int i = 0; i < 8; i++) {
							if ((i < piece.getPosition().getCol() && i < trapCol)
									|| (i > piece.getPosition().getCol() && i > trapCol)) {
								pieceRow = pieceRow & ~board.getColMap(i);
								enemyRow = enemyRow & ~board.getColMap(i);
							}
						}

						/*
						 * Eine Spalte ist leer, oder
						 * 
						 * Andere Spalte ist leer, oder
						 * 
						 * Die zwei Felder dazwischen sind frei, wenn die
						 * Figuren 2 auseinander stehen
						 */

						/*
						 * Ein Trap befindet sich auf der gleichen Spalte, wie
						 * die Figur --> Ist (soweit ich das sehe) immer auch
						 * zwischen dem Gegner und der Figur!
						 */
						boolean trapOnPieceRow = piece.getPosition().getRow() == row;
						boolean trapOnEnemyRow = trapRow == row;
						/* Der Gegner ist zwei Steps weg */
						boolean twoStepsBetween = Math.abs(trapCol
								- piece.getPosition().getCol()) == 2;
						boolean isSameRow = piece.getPosition().getRow() == trapRow;
						/* Die Reihe zwischen Gegner und Figur */
						int colBetween = (int) Math.abs((trapCol
								+ piece.getPosition().getCol() + 0.5) / 2);
						/* Ob der Trap auf dem Feld zwischen den Figuren ist */
						boolean trapOnColBetween = (board.getColMap(colBetween) & board
								.getMaskAt(col, row)) != 0;
						/*
						 * Ob das Feld zwischen den Figuren (quer - beide) leer
						 * ist
						 */
						boolean pieceRowMiddleEmpty = (board.getMaskAt(
								colBetween, piece.getPosition().getRow()) & generalMap) == 0;
						boolean enemyRowMiddleEmpty = (board.getMaskAt(
								colBetween, trapRow) & generalMap) == 0;

						if (enemyRow == 0) {
							if (twoStepsBetween || isSameRow) {
								if (!trapOnEnemyRow
										|| trapSupportedByEnemy
										|| (!trapOnColBetween
												&& pieceRowMiddleEmpty && enemyRowMiddleEmpty)) {

								}
							} else {
								return true;
							}
						}

						if (pieceRow == 0) {
							if (twoStepsBetween || isSameRow) {
								if ((!trapOnPieceRow || trapSupportedByEnemy || (!trapOnColBetween
										&& pieceRowMiddleEmpty && enemyRowMiddleEmpty))) {
									return true;
								}
							} else {
								return true;
							}
						}

					}

				}
			}
		}

		return capture;
	}

	/**
	 * Guckt, ob die übergebene Figur in ca. 2 Zügen - also einem Push/Pull
	 * gefangen werden kann.
	 * 
	 * @param board
	 * @param piece
	 * @param distance
	 *            Distanz der Figur zum Trap in Manhattan-Distanz
	 * @param col
	 *            Col des Traps
	 * @param row
	 *            Row des Traps
	 * @return Ob die Figur in Gefahr ist.
	 */
	private boolean pieceInDanger6(Bitboard board, Piece piece, int distance,
			int col, int row, long generalMap, long colorMap) {

		long higherEnemyMap = board.getColorBitmapHigher(
				otherColor(piece.getColor()), piece.getType());

		int directTrapSupport = directPosSupportCount(board, col, row, colorMap);

		/* Wird nicht super einfach gefangen zu nehmen. */
		if (distance > 1) {
			return false;
		}

		/* Wenn kein Gegner in der Nähe ist besteht auch keine Gefahr! */
		if ((board.area(piece.getPosition().getCol(), piece.getPosition()
				.getRow(), 2) & higherEnemyMap) == 0) {
			return false;
		}

		// long area = board.area(col, row, 1) & ~board.getMaskAt(col, row);

		switch (distance) {
		case 0:

			/* Wieviele Supporter vorhanden sind */
			if (directTrapSupport == 1) {

				if ((board.getMaskAt(col - 1, row) & colorMap) != 0) {
					return pieceInDanger6(board,
							board.findPieceAt(new Position(col - 1, row)), 1,
							col, row, generalMap, colorMap);
				}
				if ((board.getMaskAt(col + 1, row) & colorMap) != 0) {
					return pieceInDanger6(board,
							board.findPieceAt(new Position(col + 1, row)), 1,
							col, row, generalMap, colorMap);
				}
				if ((board.getMaskAt(col, row - 1) & colorMap) != 0) {
					return pieceInDanger6(board,
							board.findPieceAt(new Position(col, row - 1)), 1,
							col, row, generalMap, colorMap);
				}
				if ((board.getMaskAt(col, row + 1) & colorMap) != 0) {
					return pieceInDanger6(board,
							board.findPieceAt(new Position(col, row + 1)), 1,
							col, row, generalMap, colorMap);
				}

			} else {
				return false;
			}

		case 1:

			if (directTrapSupport == 1) {

				int directEnemyContactCount = directPosSupportCount(board,
						piece.getPosition().getCol(), piece.getPosition()
								.getRow(), higherEnemyMap);

				/* Gegner direkt neben mir! */
				if (directEnemyContactCount != 0) {

					long pieceMap = generalMap & ~colorMap & ~higherEnemyMap;

					/* Eine fremde kleinere Figur blockt den Trap! */
					if ((board.getMaskAt(col, row) & pieceMap) != 0) {
						return false;
					}

					/* Eine eigene Figur steht auf dem Trap */
					if ((generalMap & colorMap & board.getMaskAt(col, row)) != 0) {
						return true;
					}

					/*
					 * Die höhere Figur steht auf dem Trap. Ob es mehrere höhere
					 * Figuren gibt, spielt im Moment keine Rolle, da nur die im
					 * Trap stehende das Piece in 2 Zügen gefangen nehmen kann.
					 */
					if ((higherEnemyMap & board.getMaskAt(col, row)) != 0) {
						if (directPosSupportCount(board, col, row, generalMap) != 4) {
							return true;
						} else {
							return false;
						}
					}

					/* Es gibt mehr als eine höhere Figur neben dem Piece */
					if (directEnemyContactCount > 1) {
						return true;
					}

					int pieceCol = piece.getPosition().getCol();
					int pieceRow = piece.getPosition().getRow();
					long enemies = generalMap & ~colorMap;
					int enemyCol = pieceCol;
					int enemyRow = pieceRow;

					/* Rausfinden, wo genau dieser eine Gegner steht */
					if ((board.getMaskAt(pieceCol - 1, pieceRow) & higherEnemyMap) != 0) {
						enemyCol = pieceCol - 1;
					}
					if ((board.getMaskAt(pieceCol + 1, pieceRow) & higherEnemyMap) != 0) {
						enemyCol = pieceCol + 1;
					}
					if ((board.getMaskAt(pieceCol, pieceRow - 1) & higherEnemyMap) != 0) {
						enemyRow = pieceRow - 1;
					}
					if ((board.getMaskAt(pieceCol, pieceRow + 1) & higherEnemyMap) != 0) {
						enemyRow = pieceRow + 1;
					}

					long area = board.area(enemyCol, enemyRow, 1)
							& ~board.getMaskAt(enemyCol, enemyRow);

					/* Der Gegner ist nicht gefreezed und wird unterstützt */
					if ((area & enemies) != 0) {
						return true;
					} else {

						Piece enemy = board.findPieceAt(new Position(enemyCol,
								enemyRow));
						if (board.pieceIsFrozen(enemy)) {
							return false;
						} else {
							return true;
						}
					}

				} else {
					return false;
				}

			} else {
				return false;
			}
		}

		return false;
	}

	/**
	 * Guckt, ob die übergebene Figur in ca. 2 Zügen - also einem Push/Pull
	 * gefangen werden kann.
	 * 
	 * @param board
	 * @param piece
	 * @param distance
	 *            Distanz der Figur zum Trap in Manhattan-Distanz
	 * @param col
	 *            Col des Traps
	 * @param row
	 *            Row des Traps
	 * @return Ob die Figur in Gefahr ist.
	 */
	private boolean pieceInDanger4(Bitboard board, Piece piece, int distance,
			int col, int row) {

		long higherEnemyMap = board.getColorBitmapHigher(
				otherColor(piece.getColor()), piece.getType());
		long colorMap = board.getColorBitmap(piece.getColor());

		int directTrapSupport = directPosSupportCount(board, col, row, colorMap);

		/* Wird nicht super einfach gefangen zu nehmen. */
		if (distance >= 3) {
			return false;
		}

		/* Wenn kein Gegner in der Nähe ist besteht auch keine Gefahr! */
		if ((board.area(piece.getPosition().getCol(), piece.getPosition()
				.getRow(), 4) & higherEnemyMap) == 0) {
			return false;
		}

		long area = board.area(col, row, 1) & ~board.getMaskAt(col, row);

		switch (distance) {
		case 0:

			/* Wieviele Supporter vorhanden sind */
			switch (directTrapSupport) {
			case 1:

				if ((board.getMaskAt(col - 1, row) & colorMap) != 0) {
					return pieceInDanger4(board,
							board.findPieceAt(new Position(col - 1, row)), 1,
							col, row);
				}
				if ((board.getMaskAt(col + 1, row) & colorMap) != 0) {
					return pieceInDanger4(board,
							board.findPieceAt(new Position(col + 1, row)), 1,
							col, row);
				}
				if ((board.getMaskAt(col, row - 1) & colorMap) != 0) {
					return pieceInDanger4(board,
							board.findPieceAt(new Position(col, row - 1)), 1,
							col, row);
				}
				if ((board.getMaskAt(col, row + 1) & colorMap) != 0) {
					return pieceInDanger4(board,
							board.findPieceAt(new Position(col, row + 1)), 1,
							col, row);
				}

			case 2:

				return isFalseProtection(board, col, row, colorMap, true);

			default:
				/*
				 * drei oder mehr Supporter vorhanden neben dem Trap und der
				 * Figur
				 */
				return false;
			}

		case 1:

			switch (directTrapSupport) {
			case 1:
				/* kein weiterer Support! */
				if ((higherEnemyMap & board.area(piece.getPosition().getCol(),
						piece.getPosition().getRow(), 1)) != 0) {
					/* Gegner direkt neben mir! */
					return true;
				}

				/* Für alle anderen Fälle */
				return checkCapture(board, piece, higherEnemyMap, area, col,
						row);
				// return true;

			case 2:

				return isFalseProtection(board, col, row, colorMap, true);

			default:
				/* drei oder mehr supporter */
				return false;
			}

		case 2:

			/*
			 * Neben dem Trap steht min. noch eine eigene Figur, die das Feld
			 * bewachen kann (Zumindest vor einem Capture der übergebenen Figur!
			 * Ob die bewachende Figur in Gefahr ist, wird später ermittelt.)
			 */
			if ((area & board.getColorBitmap(piece.getColor())) != 0) {
				return false;
			}

			long generalMap = board.getGeneralBitmap();

			/*
			 * Direkt neben mir steht ein höherer Gegner und der Trap ist nicht
			 * belegt
			 */
			if ((board.getBitCount(board.area(piece.getPosition().getCol(),
					piece.getPosition().getRow(), 1) & higherEnemyMap) != 0)
					&& ((generalMap & board.getMaskAt(col, row)) == 0)) {

				long blockingMap = generalMap & ~higherEnemyMap;

				/*
				 * Das Feld links neben dem Trap muss zum Schieben/Ziehen muss
				 * frei sein.
				 */

				/**
				 * !!!!!!!!!!!!!!!!!!! Hier kann/muss vll genauer evaluiert
				 * werden, ob der Gegner vll zwischen der Figur und dem Trap
				 * steht und sich garnicht bewegen kann...
				 */
				if (piece.getPosition().getCol() < col
						&& (blockingMap & board.getMaskAt(col - 1, row)) == 0) {
					return true;
				}
				/* Rechts neben dem Trap ist frei. */
				if (piece.getPosition().getCol() > col
						&& (blockingMap & board.getMaskAt(col + 1, row)) == 0) {
					return true;
				}
				/* Über dem Trap ist frei. */
				if (piece.getPosition().getRow() > row
						&& (blockingMap & board.getMaskAt(col, row + 1)) == 0) {
					return true;
				}
				/* Unter dem Trap ist frei. */
				if (piece.getPosition().getRow() < row
						&& (blockingMap & board.getMaskAt(col, row - 1)) == 0) {
					return true;
				}

			}

			break;
		}

		return false;
	}

	/**
	 * Evaluiert die 4 Traps
	 * 
	 * @param board
	 * @return
	 */
	private double trapControlEvaluation(Bitboard board, Piece piece,
			long generalMap, long colorMap) {
		double score = 0.0;

		int typeCountG = 0;
		int typeCountS = 0;
		int col = piece.getPosition().getCol();
		int row = piece.getPosition().getRow();

		LinkedList<Position> traps = new LinkedList<Position>();

		traps.add(new Position(2, 2));
		traps.add(new Position(2, 5));
		traps.add(new Position(5, 2));
		traps.add(new Position(5, 5));

		for (Position trap : traps) {
			/* Manhattan-Distanz = 4 */
			long trapAreaMap = generalMap
					& board.area(trap.getCol(), trap.getRow(), 4);

			double tmpScore = 0.0;

			/* Wenn die übergebene Figur überhaupt in dem relevanten Bereich ist */
			if ((trapAreaMap & board.getMaskAt(col, row)) == 0) {
				continue;
			}

			/* Wieviel höhere Figuren existieren denn noch? */
			typeCountG = board.getBitCount(board.getColorBitmapHigher(
					Constants.Color.GOLD, piece.getType()) & trapAreaMap);
			typeCountS = board.getBitCount(board.getColorBitmapHigher(
					Constants.Color.SILVER, piece.getType()) & trapAreaMap);

			/* Für Gold erstmal addieren */
			tmpScore += getFigureStrength(typeCountS)
					* getDistanceValue(trap.getCol(), trap.getRow(), col, row)
					* Constants.EvalValue.TRAPCONTROL_PIECE_STRENGTH;

			tmpScore -= getFigureStrength(typeCountG)
					* getDistanceValue(trap.getCol(), trap.getRow(), col, row)
					* Constants.EvalValue.TRAPCONTROL_PIECE_STRENGTH;

			if (super.generalAttributes.getColor() != Constants.Color.GOLD) {
				tmpScore *= -1;
			}

			/*
			 * Zusatzpunkte oder Bestrafungen:
			 * =====================================================
			 */

			int distance = Math.abs(trap.getCol() - col)
					+ Math.abs(trap.getRow() - row);

			/*
			 * Es besteht die Akkute Gefahr, dass die besagte Figur direkt
			 * gecaptured wird!
			 */
			if (pieceInDanger6(board, piece, distance, trap.getCol(),
					trap.getRow(), generalMap, colorMap)) {

				if (piece.getColor() == super.generalAttributes.getColor()) {
					tmpScore -= Constants.EvalValue.TRAPCONTROL_DANGER
							* getTypeImportance(piece.getType())
							* Constants.EvalValue.DEPTH_DEPENDING_RATING;
				} else {
					tmpScore += Constants.EvalValue.TRAPCONTROL_DANGER
							* getTypeImportance(piece.getType())
							/ Constants.EvalValue.DEPTH_DEPENDING_RATING;
				}

			} else {

			}

			/*
			 * Haben wir eine Form von False Protection? Gibt Punkte oder
			 * Bestrafung.
			 */
			if (distance == 1
					&& directPosSupportCount(board, col, row, colorMap) == 2
					&& isFalseProtection(board, col, row, colorMap, false)) {

				if (piece.getColor() == super.generalAttributes.getColor()) {
					tmpScore -= Constants.EvalValue.TRAPCONTROL_FALSE_PROTECTION
							* Constants.EvalValue.DEPTH_DEPENDING_RATING;
				} else {
					tmpScore += Constants.EvalValue.TRAPCONTROL_FALSE_PROTECTION
							/ Constants.EvalValue.DEPTH_DEPENDING_RATING;
				}

			}

			score += tmpScore;

		}

		return score;

	}

	/**
	 * Checkt nur ob die Figur gefreezed ist.
	 * 
	 * @param board
	 * @param piece
	 * @return
	 */
	private double frozenEvaluation(Bitboard board, Piece piece) {
		double score = 0.0;

		/* Ist die Figur gefreezed? */
		if (board.pieceIsFrozen(piece)) {
			if (piece.getColor() == super.generalAttributes.getColor()) {
				score -= getTypeImportance(piece.getType());

			} else {
				score += getTypeImportance(piece.getType());

			}
		}

		return score;
	}

	/**
	 * Evaluation der Positionierung auf dem Spielfeld.
	 * 
	 * @param board
	 *            Spielfeld
	 * @return Wert der Evaluation.
	 */
	private double elephantPositioning(Bitboard board, Piece piece,
			long colorMap) {
		double score = 0.0;

		if (piece.getType() == Constants.Type.ELEPHANT) {

			/* Belohnung, in der Nähe des feindlichen Camels zu sein! */
			long enemyCamelMap = board.getTypeBitmap(
					otherColor(piece.getColor()), Constants.Type.CAMEL)
					& board.area(piece.getPosition().getCol(), piece
							.getPosition().getRow(), 4);

			score = enemyCamelMap != 0 ? 1 : -1;
		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	private double camelPositioning(Bitboard board, Piece piece) {
		double score = 0.0;

		if (piece.getType() == Constants.Type.CAMEL) {

			/* Penalty für die Nähe zum gegnerischen Elephanten! */
			long enemyElephantMap = board.getTypeBitmap(
					otherColor(piece.getColor()), Constants.Type.ELEPHANT)
					& board.area(piece.getPosition().getCol(), piece
							.getPosition().getRow(), 4);

			score += enemyElephantMap == 0 ? Constants.EvalValue.POSITIONING_CAMEL_ELEPHANT
					: -Constants.EvalValue.POSITIONING_CAMEL_ELEPHANT;

			/*
			 * Bonuspunkte, wenn das Camel auf der anderen Spielfeldhälfte ist,
			 * als der gegnerische Elephant
			 */
			long rightSide = board.getColMap(4) | board.getColMap(5)
					| board.getColMap(6) | board.getColMap(7);

			boolean elephantRight = (rightSide & board.getTypeBitmap(
					otherColor(piece.getColor()), Constants.Type.ELEPHANT)) != 0;
			boolean camelRight = (rightSide & board.getTypeBitmap(
					piece.getColor(), Constants.Type.CAMEL)) != 0;

			/* Elephant und Camel sind auf der gleichen Spielfeldseite */
			if (elephantRight == camelRight) {
				score -= Constants.EvalValue.POSITIONING_CAMEL_SIDE;

			}

			/*
			 * Bonuspunkte, wenn der eigene Elefant max 4 Schritte entfernt ist.
			 * oder das Kamel auf der eigenen Spielfeldhälfte ist.
			 */
			if ((piece.getColor() == Constants.Color.GOLD && piece
					.getPosition().getRow() <= 3)
					|| (piece.getColor() == Constants.Color.SILVER && piece
							.getPosition().getRow() >= 4)
					|| ((board.area(piece.getPosition().getCol(), piece
							.getPosition().getRow(), 4) & board.getTypeBitmap(
							piece.getColor(), Constants.Type.ELEPHANT)) != 0)) {
				score += Constants.EvalValue.POSITIONING_CAMEL_OWN_ELEPHANT;
			}
		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	private double pieceSquareTableSpeedLookup(Bitboard board, Piece piece) {

		if (piece.getColor() == super.generalAttributes.getColor()) {

			int newRow = piece.getPosition().getRow();
			int col = piece.getPosition().getCol();

			/* Tables invertieren, wenn für Silber evaluiert wird. */
			if (super.generalAttributes.getColor() != Constants.Color.GOLD) {
				newRow = 7 - newRow;
			}

			switch (piece.getType()) {
			case Constants.Type.ELEPHANT:
				return Constants.PieceSquareTable.MORE_SPEED[col][newRow];
			case Constants.Type.CAMEL:
				return Constants.PieceSquareTable.MORE_SPEED[col][newRow];
			case Constants.Type.HORSE:
				return Constants.PieceSquareTable.MORE_SPEED[col][newRow];
			case Constants.Type.DOG:
				return Constants.PieceSquareTable.MORE_SPEED[col][newRow];
			case Constants.Type.CAT:
				return Constants.PieceSquareTable.MORE_SPEED[col][newRow];
			case Constants.Type.RABBIT:
				return Constants.PieceSquareTable.MORE_SPEED[col][newRow];
			}

		}

		return 0.0;

	}

	private double pieceSquareTablePositionLookup(Bitboard board, Piece piece) {

		if (piece.getColor() == super.generalAttributes.getColor()) {

			int newRow = piece.getPosition().getRow();
			int col = piece.getPosition().getCol();

			/* Tables invertieren, wenn für Silber evaluiert wird. */
			if (super.generalAttributes.getColor() != Constants.Color.GOLD) {
				newRow = 7 - newRow;
			}

			switch (piece.getType()) {
			case Constants.Type.ELEPHANT:
				return Constants.PieceSquareTable.HIGH[col][newRow]
						* Constants.EvalValue.POSITIONING_ELEPHANT;
			case Constants.Type.CAMEL:

				return Constants.PieceSquareTable.HIGH[col][newRow]
						* Constants.EvalValue.POSITIONING_CAMEL;
			case Constants.Type.HORSE:
				return Constants.PieceSquareTable.HIGH[col][newRow]
						* Constants.EvalValue.POSITIONING_HORSE;
			case Constants.Type.DOG:
				return Constants.PieceSquareTable.MIDDLE[col][newRow]
						* Constants.EvalValue.POSITIONING_DOG;
			case Constants.Type.CAT:
				return Constants.PieceSquareTable.MIDDLE[col][newRow]
						* Constants.EvalValue.POSITIONING_CAT;
			case Constants.Type.RABBIT:
				return Constants.PieceSquareTable.LOW[col][newRow]
						* Constants.EvalValue.POSITIONING_RABBIT;
			}

		}

		return 0.0;

	}

	/**
	 * Sorgt dafür, dass keine Figuren Alleingänge machen und auf Einmal
	 * irgendwohin laufen!
	 * 
	 * @param board
	 * @param piece
	 * @param colorMap
	 * @return
	 */
	private double positioningLonely(Bitboard board, Piece piece, long colorMap) {
		double score = 0.0;

		if (piece.getType() != Constants.Type.ELEPHANT) {

			/* Keine Unterstützung in 2 Schritten --> kleine Bestrafung */
			if ((board.area(piece.getPosition().getCol(), piece.getPosition()
					.getRow(), 2) & colorMap) == 0) {
				score += 1.0;
			}

			/* Keine Unterstützung in 3 Schritten --> große Bestrafung */
			if ((board.area(piece.getPosition().getCol(), piece.getPosition()
					.getRow(), 3) & colorMap) == 0) {
				score += 1.0;
			}

		}

		if (piece.getColor() == super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	private double lonelyCamelOrElephant(Bitboard board, Piece piece,
			long colorMap, int col) {
		double score = 0.0;

		if (piece.getType() == Constants.Type.ELEPHANT
				|| piece.getType() == Constants.Type.CAMEL) {
			/* Penalty, wenn der Elephant alleine ist! */
			long surrounding = board.area(col, piece.getPosition().getRow(), 2)
					& ~board.getMaskAt(col, piece.getPosition().getRow());

			if ((surrounding & colorMap) == 0) {
				score = -1;
			}

		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	private double passiveElephant(Bitboard board, Piece piece, int col) {

		double score = 0.0;

		if (piece.getType() == Constants.Type.ELEPHANT) {
			/* Penalty, wenn der Elephant keinen Gegner neben sich hat! */
			long surrounding = board.area(col, piece.getPosition().getRow(), 1)
					& board.getColorBitmap(otherColor(piece.getColor()))
					& ~board.getTypeBitmap(otherColor(piece.getColor()),
							Constants.Type.ELEPHANT);
			if (surrounding == 0) {
				score = -1;
			}
		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	/**
	 * Guckt, wieviel die übergebene Figur an Geh-Möglichkeiten hat.
	 * 
	 * @return
	 */
	private int getMoveCount(Bitboard board, Piece piece) {
		// int movement = 0;

		if (board.pieceIsFrozen(piece)) {
			return 0;
		}

		long movementBits = board.getPieceMovementBitmap(piece);

		// for (int i = 0; i < 64; i++) {
		// if ((1L << i & movementBits) != 0)
		// movement++;
		// }

		return board.getBitCount(movementBits);

		// return movement;
	}

	/**
	 * Evaluiert die Bewegungsfreiheit der Figuren und das Freezing.
	 * 
	 * @param board
	 *            Spielfeld
	 * @return Evaluierter Wert.
	 */
	private double mobilityEvaluation(Bitboard board, Piece piece,
			long generalMap, long myColorMap) {

		double score = 0.0;
		int moveCount = getMoveCount(board, piece);

		double tmpScore = 0.0;

		if (moveCount == 0) {
			moveCount = -1;

			tmpScore -= Constants.EvalValue.MOVEMENT_FREEZING
					* getMovementTypeValue(piece.getType());

		}

		tmpScore += moveCount * getMovementTypeValue(piece.getType());

		if (piece.getColor() != super.generalAttributes.getColor()) {
			tmpScore *= -1;
		}

		score += tmpScore;

		return score;

	}

	/**
	 * Gibt eine generelle Bestrafung, wenn die Figur neben höheren Gegner
	 * steht! Fällt relativ gering aus.
	 * 
	 * @param board
	 * @param piece
	 * @param higherEnemy
	 * @param col
	 * @param row
	 * @return
	 */
	private double generalEnemyContact(Bitboard board, Piece piece,
			long higherEnemy, int col, int row) {

		long oneAroundPiece = board
				.getBitmapAroundPosition(piece.getPosition())
				& ~board.getMaskAt(col, row);

		if ((higherEnemy & oneAroundPiece) != 0) {
			return -getTypeImportance(piece.getType());
		}

		return 0.0;
	}

	/**
	 * Wenn links vorne ein höherer Gegner steht und das Feld neben mir noch
	 * frei ist und das Feld vor mir frei ist und das Feld vor mir auf der
	 * anderen Seite nicht von meinem Team belegt ist.
	 * 
	 * @param board
	 * @param piece
	 * @param generalMap
	 * @param myColorMap
	 * @param higherEnemy
	 * @param col
	 * @param row
	 * @param direction
	 * @return
	 */
	private double enemyFrontIndirect(Bitboard board, Piece piece,
			long generalMap, long myColorMap, long higherEnemy, int col,
			int row, int direction) {
		double score = 0.0;

		if (col >= 1
				&& (board.getMaskAt(col - 1, row + direction) & higherEnemy) != 0
				&& (board.getMaskAt(col - 1, row) & generalMap) == 0
				&& (board.getMaskAt(col, row + direction) & generalMap) == 0
				&& (col == 7 || !((board.getMaskAt(col + 1, row + direction) & myColorMap) == 0))) {
			score -= getTypeImportance(piece.getType());
		}

		/* Und das Gleiche nochmal für das rechte Feld... */
		if (col <= 6
				&& (board.getMaskAt(col + 1, row + direction) & higherEnemy) != 0
				&& (board.getMaskAt(col + 1, row) & generalMap) == 0
				&& (board.getMaskAt(col, row + direction) & generalMap) == 0
				&& (col == 0 || !((board.getMaskAt(col - 1, row + direction) & myColorMap) == 0)))

		{
			score -= getTypeImportance(piece.getType());
		}

		return score;
	}

	/**
	 * Hohe Bestrafung, wenn die Figur vor dem Gegner steht und dieser direkt
	 * vor einem oder neben einem! (Kann dann die Figur hinter sich ziehen +
	 * schieben.)
	 * 
	 * @param board
	 * @param piece
	 * @param higherEnemy
	 * @param col
	 * @param row
	 * @param direction
	 * @param enemyContact
	 * @return
	 */
	private double enemyFrontDirect(Bitboard board, Piece piece,
			long higherEnemy, int col, int row, int direction,
			boolean enemyContact) {

		if (enemyContact
				&& (board.getMaskAt(col, row + direction) & higherEnemy) != 0) {
			return -getTypeImportance(piece.getType());
		}
		return 0.0;
	}

	/**
	 * Gegner steht zwischen den eigenen Reihen und der Figur.
	 * 
	 * @param board
	 * @param piece
	 * @param higherEnemy
	 * @param col
	 * @param row
	 * @param direction
	 * @param enemyContact
	 * @return
	 */
	private double enemyBackDirect(Bitboard board, Piece piece,
			long higherEnemy, int col, int row, int direction,
			boolean enemyContact) {

		if (enemyContact && row > 0 && row < 7
				&& (board.getMaskAt(col, row - direction) & higherEnemy) != 0) {
			return -getTypeImportance(piece.getType());
		}

		return 0.0;
	}

	private double enemySide(Bitboard board, Piece piece, long higherEnemy,
			int col, int row, boolean enemyContact) {
		double score = 0.0;

		/* Links neben der Figur ist der Gegner */
		if (enemyContact && col > 0
				&& (higherEnemy & board.getMaskAt(col - 1, row)) != 0) {
			score -= getTypeImportance(piece.getType());
		}

		/* Rechts neben der Figur ist der Gegner */
		if (enemyContact && col < 7
				&& (higherEnemy & board.getMaskAt(col + 1, row)) != 0) {
			score -= getTypeImportance(piece.getType());
		}

		return score;
	}

	/**
	 * Wenn ein Gegner 1-2 Schritte entfernt steht gibts nochmal kleinere
	 * Bonuspunkte oder Bestrafungen
	 * 
	 * @param board
	 * @param higherEnemy
	 * @param col
	 * @param row
	 * @param direction
	 * @return
	 */
	private double enemyFrontFar(Bitboard board, long higherEnemy, int col,
			int row, int direction) {

		if ((board.getMaskAt(col, row + direction) & higherEnemy) != 0
				|| (board.getMaskAt(col, row + 2 * direction) & higherEnemy) != 0) {
			return -1;
		}
		return 0.0;
	}

	/**
	 * Wenn hinter mir ein höherer Gegner steht und eine Area mit Radius von 1-2
	 * einen Schritt vor mir keine einzige Figur der eigenen Farbe beinhaltet.
	 * 
	 * @param board
	 * @param piece
	 * @param myColorMap
	 * @param higherEnemy
	 * @param col
	 * @param row
	 * @param enemyContact
	 * @return
	 */
	private double pieceSeparated(Bitboard board, Piece piece, long myColorMap,
			long higherEnemy, int col, int row, boolean enemyContact,
			int direction) {
		double score = 0.0;

		// if (enemyContact && row > 0
		// && row < 7
		// // && (board.getMaskAt(col, row - direction) & higherEnemy) != 0
		// && (board.area(col, row + direction, 2)
		// & ~board.getMaskAt(col, row) & myColorMap) == 0) {
		// return -getTypeImportance(piece.getType());
		// }

		/* Keine Verstärkung einen Schritt entfernt */
		if (enemyContact && (board.area(col, row, 1) & myColorMap) == 0) {
			score -= Constants.EvalValue.PIECE_SEPARATED_1_STEP;
		} else {
			return score;
		}

		/* Keine Verstärkung zwei Schritte entfernt */
		if (enemyContact && (board.area(col, row, 2) & myColorMap) == 0) {
			score -= Constants.EvalValue.PIECE_SEPARATED_2_STEP;
		} else {
			return score;
		}

		return score;
	}

	private double teamSpirit(Bitboard board, Piece piece, long myColorMap,
			int col, int row) {

		if ((board.area(col, row, 2) & myColorMap) == 0) {
			return -getTypeImportance(piece.getType());
		}
		return 0.0;
	}

	/**
	 * Evaluiert alle Sachen, die mit Feind-Kontakt zu tun haben.
	 * 
	 * @param board
	 *            Spielfeld
	 * @param piece
	 *            Die Figur
	 * @param generalMap
	 *            Spielfeldrepräsentation
	 * @param myColorMap
	 *            Figurfarbe
	 * @return Evaluierter Wert
	 */
	private double enemyContactEvaluation(Bitboard board, Piece piece,
			long generalMap, long myColorMap) {
		double score = 0.0;

		int col = piece.getPosition().getCol();
		int row = piece.getPosition().getRow();

		if (piece.getType() != Constants.Type.ELEPHANT) {

			/* Enemy contact */
			long higherEnemy = board.getColorBitmapHigher(
					otherColor(piece.getColor()), piece.getType());

			/*
			 * Allgemeine Penalty, für das Stehen neben höheren gegnerischen
			 * Figuren.
			 */
			score += generalEnemyContact(board, piece, higherEnemy, col, row)
					* Constants.EvalValue.ENEMY_CONTACT;

			boolean enemyContact = score > 0.1 || score < -0.1;

			/*
			 * Nur zu Spielanfang wirklich relevant. Sobald in der Mitte
			 * gekämpft wird nicht mehr sosehr...
			 */
			if ((piece.getColor() == Constants.Color.GOLD && row <= 4)
					|| (piece.getColor() == Constants.Color.SILVER && row >= 3)) {

				int direction = piece.getColor() == Constants.Color.GOLD ? 1
						: -1;

				// score += enemyFrontIndirect(board, piece, generalMap,
				// myColorMap, higherEnemy, col, row, direction)
				// * Constants.EvalValue.ENEMY_CONTACT_FRONT_INDIRECT;

				score += pieceSeparated(board, piece, myColorMap, higherEnemy,
						col, row, enemyContact, direction)
						* Constants.EvalValue.PIECE_SEPARATED;

				score += enemyBackDirect(board, piece, higherEnemy, col, row,
						direction, enemyContact)
						* Constants.EvalValue.ENEMY_CONTACT_BACK_DIRECT;

				score += enemySide(board, piece, higherEnemy, col, row,
						enemyContact) * Constants.EvalValue.ENEMY_CONTACT_SIDE;

				score += enemyFrontDirect(board, piece, higherEnemy, col, row,
						direction, enemyContact)
						* Constants.EvalValue.ENEMY_CONTACT_FRONT_DIRECT;

				// score += enemyFrontFar(board, higherEnemy, col, row,
				// direction)
				// * Constants.EvalValue.ENEMY_CONTACT_FRONT_FAR;
			}

			/*
			 * Generelle Bestrafung, wenn die Figur alleine gelassen wurde :-(
			 * (die Arme...)
			 */
			/*
			 * Wenn von seiner Position aus im Umkreis von 2 Schritten keine
			 * eigene Figur ist. Das sollte dazu führen, dass alle etwas mehr
			 * zusammenrücken und nicht alleine loslaufen... Das gilt nicht für
			 * den Elephanten.
			 */
			// score += teamSpirit(board, piece, myColorMap, col, row)
			// * Constants.EvalValue.TEAM_SPIRIT;

		}

		if (piece.getColor() == super.generalAttributes.getColor()) {
			score *= Constants.EvalValue.DEPTH_DEPENDING_RATING;
		} else {
			score /= Constants.EvalValue.DEPTH_DEPENDING_RATING;
		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;

	}

	/**
	 * Sorgt dafür, dass Hasen und andere Figuren sich so anordnen, dass ein
	 * Durchbruch schwerer wird.
	 * 
	 * @param board
	 *            Spielfeld
	 * @return blubb...
	 */
	private double rabbitWallEvaluation(Bitboard board, Piece piece) {
		double score = 0.0;

		if (piece.getType() != Constants.Type.RABBIT) {
			return score;
		}
		int col = piece.getPosition().getCol(), row = piece.getPosition()
				.getRow();

		long colorMap = board.getColorBitmap(piece.getColor());

		/* oben */
		if (row < 7 && (board.getMaskAt(col, row + 1) & colorMap) != 0) {
			score += 0.1;
		}
		/* unten */
		if (row > 0 && (board.getMaskAt(col, row - 1) & colorMap) != 0) {
			score += 0.1;
		}
		/* links */
		if (col > 0 && (board.getMaskAt(col - 1, row) & colorMap) != 0) {
			score += 0.1;
		}
		/* rechts */
		if (col < 7 && (board.getMaskAt(col + 1, row) & colorMap) != 0) {
			score += 0.1;
		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	private double advancedRabbitCrowding(Bitboard board, Piece piece,
			long generalBitmap, long colorMap) {
		double score = 0.0;

		if (piece.getType() == Constants.Type.RABBIT) {

			/* ist advanced */
			if ((piece.getColor() == Constants.Color.GOLD && piece
					.getPosition().getRow() >= 5)
					|| (piece.getColor() == Constants.Color.SILVER && piece
							.getPosition().getRow() <= 2)) {
				int col = piece.getPosition().getCol();
				int row = piece.getPosition().getRow();
				int direction = piece.getColor() == Constants.Color.GOLD ? -1
						: 1;

				/* Zwei eigene Figuren neben dem Hasen */
				if ((board.area(col, row, 1) & colorMap) < 2) {
					score += 0.5;
				}

				/* Figuren vor dem Hasen */
				if ((board.area(col, row + direction, 2) & colorMap) < 3) {
					score += 1.5;
				}
			}

		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -1;
		}

		return score;
	}

	/**
	 * Checkt, ob ein gegnerischer Hase in 2 Schritten ins Ziel kommen kann.
	 * 
	 * @param board
	 * @param piece
	 * @param generalBitmap
	 * @param colorMap
	 * @return
	 */
	private double Rabbit2StepGoal(Bitboard board, Piece piece,
			long generalBitmap, long colorMap) {

		/* gegnerischer Hase */
		if (piece.getType() == Constants.Type.RABBIT
				&& piece.getColor() != super.generalAttributes.getColor()) {
			/* is advanced Rabbit */
			if ((piece.getColor() == Constants.Color.GOLD
					&& piece.getPosition().getRow() >= 5 && piece.getPosition()
					.getRow() != 7)
					|| (piece.getColor() == Constants.Color.SILVER
							&& piece.getPosition().getRow() <= 2 && piece
							.getPosition().getRow() != 0)) {
				int col = piece.getPosition().getCol();
				int row = piece.getPosition().getRow();
				int direction = piece.getColor() == Constants.Color.GOLD ? 1
						: -1;
				int goalDistance = (row == 5 || row == 2) ? 2 : 1;

				switch (goalDistance) {
				case 1:

					boolean frozen = board.pieceIsFrozen(piece);
					long front = board.getMaskAt(col, row + direction);

					if (frozen) {

						/* wenn vorne dicht ist --> kein goal. */
						if ((front & generalBitmap) != 0) {
							return 0.0;
						}

						/* Unterstützung von links tendentiell möglich */
						if (col > 0
								&& (board.getMaskAt(col - 1, row) & generalBitmap) == 0) {

							/*
							 * VORLÄUFIG!!! Kein Check, ob der Unterstützer
							 * selber gefreezed ist! Sollte das Programm
							 * aufgrund der falschen Berechung vorsichtiger
							 * machen...
							 */
							if ((board.area(col - 1, row, 1)
									& ~board.getMaskAt(col, row) & colorMap) != 0) {
								return -1.0;
							}

						}

						/* Unterstützung von rechts tendentiell möglich */
						if (col < 7
								&& (board.getMaskAt(col + 1, row) & generalBitmap) == 0) {

							/*
							 * VORLÄUFIG!!! Kein Check, ob der Unterstützer
							 * selber gefreezed ist! Sollte das Programm
							 * aufgrund der falschen Berechung vorsichtiger
							 * machen...
							 */
							if ((board.area(col + 1, row, 1)
									& ~board.getMaskAt(col, row) & colorMap) != 0) {
								return -1.0;
							}
						}

					} else {

						/* Einfachster Fall -> directer goal. */
						if ((front & generalBitmap) == 0) {
							return -1.0;
						}

						/* Eigene Figur vorne */
						if ((front & colorMap) != 0) {

							/* Figur kann zur Seite weggehen */
							if ((board.area(col, row + direction, 1) & ~front & ~board
									.getMaskAt(col, row)) == 0) {
								return -1.0;
							} else {
								/*
								 * Wenn der nicht weggehen kann, sind alle drei
								 * Felder vorne belegt und ein goal nicht
								 * möglich.
								 */
								return 0.0;
							}
						} else {
							/* Gegner vorne */

							/* links rum tendenziell möglich */
							if (col > 0
									&& (board.getMaskAt(col - 1, row) & generalBitmap) == 0
									&& (board.getMaskAt(col - 1, row
											+ direction) & generalBitmap) == 0) {

								/* linkes Feld wird unterstützt --> goal. */
								if ((board.area(col - 1, row, 1)
										& ~board.getMaskAt(col, row) & colorMap) != 0) {
									return -1.0;
								}

								/* Goal, wenn kein höherer Gegner */
								if (!((board.area(col - 1, row, 1) & board
										.getColorBitmapHigher(
												otherColor(piece.getColor()),
												piece.getType())) != 0)) {
									return -1.0;
								}

							}
							/* rechts rum tendenziell möglich */
							if (col < 7
									&& (board.getMaskAt(col + 1, row) & generalBitmap) == 0
									&& (board.getMaskAt(col + 1, row
											+ direction) & generalBitmap) == 0) {

								/* rechtes Feld wird unterstützt --> goal */
								if ((board.area(col + 1, row, 1)
										& ~board.getMaskAt(col, row) & colorMap) != 0) {
									return -1.0;
								}

								/* Höherer Gegner --> kein goal. */
								if (!((board.area(col + 1, row, 1) & board
										.getColorBitmapHigher(
												otherColor(piece.getColor()),
												piece.getType())) != 0)) {
									return -1.0;
								}

							}

						}

					}

					break;
				case 2:
					/* Kann nicht bewegt werden */
					if (board.pieceIsFrozen(piece)) {
						return 0.0;
					}

					/* Eines der Felder zur Endlinie ist belegt */
					if ((board.getMaskAt(col, row + direction) & generalBitmap) != 0
							|| (board.getMaskAt(col, row + 2 * direction) & generalBitmap) != 0) {
						return 0.0;
					}

					/*
					 * Eines der seitlichen Felder der ersten Reihe wird
					 * unterstützt --> goal.
					 */
					if ((board.area(col, row + direction, 1)
							& ~board.getMaskAt(col, row) & colorMap) != 0) {
						return -1.0;
					}

					/* Gegner zum freezen auf der ersten Reihe */
					if ((board.getColorBitmapHigher(
							otherColor(piece.getColor()), piece.getType()) & board
							.area(col, row + direction, 1)) != 0) {
						return 0.0;
					}

					/* Alle anderen Fälle führen zum goal. */
					return -1.0;

				}

			}
		}

		return 0.0;
	}

	/**
	 * Checkt, ob ein gegnerischer Hase in 3 Schritten ins Ziel kommen kann.
	 * 
	 * @param board
	 * @param piece
	 * @param generalBitmap
	 * @param colorMap
	 * @return
	 */
	private double Rabbit3StepGoal(Bitboard board, Piece piece,
			long generalBitmap, long colorMap) {
		double score = 0.0;

		return score;
	}

	/**
	 * Guckt, ob die Hasen einen freien Weg zum Ziel haben.
	 * 
	 * @param board
	 *            Spielfeld
	 * @return @return
	 */
	private double rabbitFreewayEvaluation(Bitboard board, Piece piece,
			long generalBitmap) {
		double score = 0.0;

		if (piece.getType() != Constants.Type.RABBIT) {
			return score;
		}

		int col = piece.getPosition().getCol(), row = piece.getPosition()
				.getRow();
		boolean found = false;

		if (super.generalAttributes.getColor() == Constants.Color.GOLD) {
			for (int newRow = row + 1; newRow < 8; newRow++) {
				if ((board.getMaskAt(col, newRow) & generalBitmap) != 0) {
					row = newRow;
					found = true;
					break;
				}
			}
		} else {
			for (int newRow = row - 1; newRow >= 0; newRow--) {
				if ((board.getMaskAt(col, newRow) & generalBitmap) != 0) {
					row = 7 - newRow;
					found = true;
					break;
				}
			}
		}

		if (!found) {
			row = 7;
		}

		switch (row) {
		case 0:
			score += Constants.EvalValue.RABBIT_FREEWAY_0;
			break;
		case 1:
			score += Constants.EvalValue.RABBIT_FREEWAY_1;
			break;
		case 2:
			score += Constants.EvalValue.RABBIT_FREEWAY_2;
			break;
		case 3:
			score += Constants.EvalValue.RABBIT_FREEWAY_3;
			break;
		case 4:
			score += Constants.EvalValue.RABBIT_FREEWAY_4;
			break;
		case 5:
			score += Constants.EvalValue.RABBIT_FREEWAY_5;
			break;
		case 6:
			score += Constants.EvalValue.RABBIT_FREEWAY_6;
			break;
		case 7:
			score += Constants.EvalValue.RABBIT_FREEWAY_7;
			break;
		}

		if (piece.getColor() != super.generalAttributes.getColor()) {
			score *= -2;
		}

		return score;
	}

	/**
	 * Überprüft, ob eine Frame-Situation vorhanden ist und bewertet diese
	 * 
	 * @param board
	 *            Spielfeld
	 * @param piece
	 *            Die Figur
	 * @param generalMap
	 *            Map
	 * @return Hostagebewertung
	 */
	private double hostageSituation(Bitboard board, Piece piece, long generalMap) {

		long trap11 = board.area(2, 2, 1);
		long trap21 = board.area(5, 2, 1);
		long trap31 = board.area(2, 5, 1);
		long trap41 = board.area(5, 5, 1);

		long trap1 = board.area(2, 2, 2) & ~trap11;
		long trap2 = board.area(5, 2, 2) & ~trap21;
		long trap3 = board.area(2, 5, 2) & ~trap31;
		long trap4 = board.area(5, 5, 2) & ~trap41;
		long piecePos = board.getMaskAt(piece.getPosition().getCol(), piece
				.getPosition().getRow());

		int col = piece.getPosition().getCol();
		int row = piece.getPosition().getRow();

		/*
		 * Figur muss genau zwei Schritte neben einem Trap stehen! (Gefahr in
		 * einem Zug reingezogen zu werden!)
		 */
		if ((trap1 & piecePos) != 0 || (trap2 & piecePos) != 0
				|| (trap3 & piecePos) != 0 || (trap4 & piecePos) != 0) {

			/* Darf nicht weg können... Meistens zumindest. Erstmal. */
			if (board.pieceIsFrozen(piece)) {
				if (piece.getColor() == generalAttributes.getColor()) {
					return -1.5;
				} else {
					return 1.5;
				}
			}
		}

		/* Bestrafung für eine Vorbereitung auf eine Hostage-Situation! */
		if ((trap11 & piecePos) != 0 || (trap21 & piecePos) != 0
				|| (trap31 & piecePos) != 0 || (trap41 & piecePos) != 0) {

			long higherEnemyMap = board.getColorBitmapHigher(
					otherColor(piece.getColor()), piece.getType());

			/* Hoher Gegner neben der Figur */
			if ((higherEnemyMap & board.area(col, row, 1)) != 0) {

				int direction = piece.getColor() == Constants.Color.GOLD ? 1
						: -1;

				if (row < 7
						&& row > 0
						&& (board.getMaskAt(col, row + direction) & generalMap) == 0) {

					if (piece.getColor() == generalAttributes.getColor()) {
						return -1.0;
					} else {
						return 1.0;
					}

				}

			}

		}

		return 0.0;
	}

	/**
	 * Überprüft, ob eine Hostage-Situation vorhanden ist und bewertet diese
	 * 
	 * @param board
	 *            Spielfeld
	 * @param piece
	 *            Die Figur
	 * @param generalMap
	 *            Map
	 * @return Hostagebewertung
	 */
	private double frameSituation(Bitboard board, Piece piece, long generalMap) {

		int col = piece.getPosition().getCol();
		int row = piece.getPosition().getRow();

		/* Figur steht auf einem Trap */
		if ((col == 2 && row == 2) || (col == 2 && row == 5)
				|| (col == 5 && row == 2) || (col == 5 && row == 5)) {

			/* Alle 4 Bits drumrum müssen gesetzt sein! */
			if (directPosSupportCount(board, col, row, generalMap) == 4) {

				/* Kein Push/Pull-Move kann gemacht werden! */
				if (board.getPieceSpecialMoveList(piece).isEmpty()) {

					if (piece.getColor() == generalAttributes.getColor()) {
						return -1.0;
					} else {
						return 1.0;
					}
				}
			}
		}

		return 0.0;
	}

	/**
	 * Gefahr, in zwei Traps gezogen zu werden.
	 * 
	 * @param board
	 * @param piece
	 * @param generalMap
	 * @return
	 */
	private double forkSituation(Bitboard board, Piece piece, long generalMap) {

		int col = piece.getPosition().getCol();
		int row = piece.getPosition().getRow();

		/* steht zwischen den Traps */
		if ((row == 2 || row == 5) && (col == 3 || col == 4)
				&& board.pieceIsFrozen(piece)) {
			if (piece.getColor() == generalAttributes.getColor()) {
				return -1.5;
			} else {
				return 1.5;
			}
		}

		long area = 0L;

		/* Je nach Farbe der Figur die andere kritische Seite */
		if (piece.getColor() == Constants.Color.GOLD) {
			area = board.area(3, 5, 1) | board.area(4, 5, 1);
		} else {
			area = board.area(3, 2, 1) | board.area(4, 2, 1);
		}

		/* Hinführung zu einer Fork */
		if ((board.getMaskAt(col, row) & area) != 0) {
			long higherEnemyMap = board.getColorBitmapHigher(
					otherColor(piece.getColor()), piece.getType());

			/* Gegner neben der Figur */
			if ((board.area(col, row, 1) & higherEnemyMap) != 0) {
				double score = 0.0;

				if (col <= 3) {
					if ((board.getMaskAt(3, 5) & generalMap) == 0) {
						score = 1.0;
					}
				} else {
					if ((board.getMaskAt(4, 5) & generalMap) == 0) {
						score = 1.0;
					}
				}

				if (piece.getColor() == generalAttributes.getColor()) {
					return -score;
				} else {
					return score;
				}

			}
		}

		return 0.0;
	}

	/**
	 * Evaluiert u.U. weniger, als die tatsächliche Evaluierungsfunktion, um das
	 * Moveordering schnell zu halten.
	 * 
	 * @param board
	 *            Spielfeld
	 * @return Evaluierter Wert
	 */
	public int evaluateBoardStateMoveOrdering(Bitboard board) {
		int score = 0;
		long generalMap = board.getGeneralBitmap();

		score += harLogMaterialEvaluation(board)
				* Constants.EvalValue.MATERIAL_HARLOG;

		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {

				if ((board.getMaskAt(col, row) & generalMap) != 0) {
					Piece piece = board.findPieceAt(new Position(col, row));
					long colorMap = board.getColorBitmap(piece.getColor());

					score += trapControlEvaluation(board, piece, generalMap,
							colorMap) * Constants.EvalValue.TRAPCONTROL;

				}
			}
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
	public int evaluateBoardState(Bitboard board, boolean isQuiescence) {
		int score = 0;

		long generalMap = board.getGeneralBitmap();

		score += getWinLoseEvaluation(board);

		/* Damit wird nur ausgewertet, ob gewonnen oder verloren ist! */
		if (isQuiescence) {
			return score;
		}

		score += harLogMaterialEvaluation(board)
				* Constants.EvalValue.MATERIAL_HARLOG;

		// score += getMaterialEvaluation(board) *
		// Constants.EvalValue.MATERIAL_MY;

		/*
		 * Alle Evaluierungen, bei denen inkrementell alle Figuren an allen
		 * Positionen evaluiert werden!
		 */
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {

				if ((board.getMaskAt(col, row) & generalMap) != 0) {
					Piece piece = board.findPieceAt(new Position(col, row));
					long colorMap = board.getColorBitmap(piece.getColor());

					score += trapControlEvaluation(board, piece, generalMap,
							colorMap) * Constants.EvalValue.TRAPCONTROL;

					// score += frozenEvaluation(board, piece)
					// * Constants.EvalValue.TRAPCONTROL_FREEZING;

					// score += mobilityEvaluation(board, piece, generalMap,
					// colorMap) * Constants.EvalValue.MOVEMENT;

					score += enemyContactEvaluation(board, piece, generalMap,
							colorMap)
							* Constants.EvalValue.ENEMY_GENERAL_CONFLICT;

					score += frameSituation(board, piece, generalMap)
							* Constants.EvalValue.FRAME_SITUATION;

					score += hostageSituation(board, piece, generalMap)
							* Constants.EvalValue.HOSTAGE_SITUATION;

					score += forkSituation(board, piece, generalMap)
							* Constants.EvalValue.FORK_SITUATION;

					// score += elephantPositioning(board, piece, colorMap)
					// * Constants.EvalValue.POSITIONING_ELEPHANT_CAMEL;

					score += pieceSquareTablePositionLookup(board, piece)
							* Constants.EvalValue.POSITIONING_PIECE_SQUARE;

					score += positioningLonely(board, piece, colorMap)
							* Constants.EvalValue.POSITIONING_LONELY;

					score += pieceSquareTableSpeedLookup(board, piece)
							* Constants.EvalValue.SPEED;

					score += passiveElephant(board, piece, col)
							* Constants.EvalValue.PASSIVE_ELEPHANT;

					// score += lonelyCamelOrElephant(board, piece, colorMap,
					// col)
					// * Constants.EvalValue.LONELY_ELEPHANT;

					score += camelPositioning(board, piece)
							* Constants.EvalValue.POSITIONING_CAMEL_GENERAL;

					score += rabbitWallEvaluation(board, piece)
							* Constants.EvalValue.RABBIT_WALL;

					score += rabbitFreewayEvaluation(board, piece, generalMap)
							* Constants.EvalValue.RABBIT_FREEWAY;

					score += advancedRabbitCrowding(board, piece, generalMap,
							colorMap) * Constants.EvalValue.RABBIT_CROWDING;

					score += Rabbit2StepGoal(board, piece, generalMap, colorMap)
							* Constants.EvalValue.RABBIT_2_STEP_GOAL;

				}
			}
		}

		return score;
	}

	public void test() {
		System.out.println(Constants.PieceSquareTable.LOW[5][0]);
	}

}
