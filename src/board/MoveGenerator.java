package board;

import java.util.LinkedList;

public class MoveGenerator {

	/**
	 * Generiert eine Liste mit allen direkten resultierenden möglichen Zügen
	 * aller Figuren der übergebenen Farbe für eine bestimmte Konstellation des
	 * übergebenen Boards.
	 * 
	 * @param board
	 *            Das Board, für das die Züge generierte werden sollen.
	 * @param color
	 *            Die übergebene Farbe, für die die Züge generiert werden
	 *            sollen.
	 * @return Eine Liste mit allen möglichen Zügen für die übergebene
	 *         Spielsituation.
	 */
	public LinkedList<Move> generateAllColorMoves(Bitboard board, int color) {

		LinkedList<Move> possibleMoves = new LinkedList<Move>();

		/* Schleife über alle Figurtypen */
		for (int type = 0; type < 6; type++) {

			/* Alle Vorkommen der Figur */
			long typeMap = board.getTypeBitmap(color, type);

			Piece piece = new Piece();
			Position pos = new Position();

			for (int i = 0; i < 64; i++) {
				long mask = 1L << i;

				/* Wenn an der Position eine Figur ist */
				if ((typeMap & mask) != 0) {

					pos.setCol(i % 8);
					pos.setRow(i / 8);
					piece.setColor(color);
					piece.setPosition(pos);
					piece.setType(type);

					if (board.pieceIsFrozen(piece)) {
						continue;
					}
					
					/* Alle möglichen Bewegungsrichtungen der Figur */
					long moveMask = board.getPieceMovementBitmap(piece);

					/* Alle Push/Pull-Möglichkeiten dazuadden */
					possibleMoves.addAll(board.getPieceSpecialMoveList(piece));
					
					/* Check aller 4 Möglichkeiten */
					
					/* oben */
					if (((i / 8) <= 6)
							&& (moveMask & (board.getMaskAt(i % 8, (i + 8) / 8))) != 0) {
						possibleMoves.add(new MoveNormal(new Position(i % 8,
								i / 8), 0, 1));
					}
					
//					if ((i <= 54)
//							&& (moveMask & (board.getMaskAt(i % 8, (i + 8) / 8))) != 0) {
//						possibleMoves.add(new MoveNormal(new Position(i % 8,
//								i / 8), 0, 1));
//					}

					/* unten */
					if (((i / 8) >= 1)
							&& (moveMask & (board.getMaskAt(i % 8, (i - 8) / 8))) != 0) {
						possibleMoves.add(new MoveNormal(new Position(i % 8,
								i / 8), 0, -1));
					}
					
//					if ((i >= 8)
//							&& (moveMask & (board.getMaskAt(i % 8, (i - 8) / 8))) != 0) {
//						possibleMoves.add(new MoveNormal(new Position(i % 8,
//								i / 8), 0, -1));
//					}

					/* links */
					if (((i % 8) >= 1)
							&& (moveMask & (board.getMaskAt((i - 1) % 8, i / 8))) != 0) {
						possibleMoves.add(new MoveNormal(new Position(i % 8,
								i / 8), -1, 0));
					}

					/* rechts */
					if (((i % 8) <= 6)
							&& (moveMask & (board.getMaskAt((i + 1) % 8, i / 8))) != 0) {
						possibleMoves.add(new MoveNormal(new Position(i % 8,
								i / 8), 1, 0));
					}

					

				}
			}
		}

		return possibleMoves;
	}
}
