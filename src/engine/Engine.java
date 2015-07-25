package engine;

import java.util.LinkedList;
import java.util.Random;

import memory.RepetitionCounter;
import network.MessageOutputWriter;
import board.Bitboard;
import board.Constants;
import board.Move;
import board.MoveGenerator;
import board.MoveNormal;
import board.MoveSpecial;
import board.Piece;
import board.Position;

/**
 * Die eigentliche Engine, die als Thread gestartet wird und die Berechnung des
 * besten Zuges, Evaluation etc. regelt/ausführt.
 * 
 * @author maurice
 * 
 */
public abstract class Engine implements Runnable {

	/************************************************************/
	/********************* Variablen ************************/
	/************************************************************/

	/**
	 * Verweis auf den eigenen Thread (der ausschließlich hier verwaltet wird!).
	 */
	protected Thread ownThread;

	/**
	 * Der String mit dem aktuellen besten Move für den Server.
	 */
	protected LinkedList<Move> bestMove;

	/**
	 * Ob der beste Move beim Beenden des Threads überhaupt ausgegeben werden
	 * soll oder nicht.
	 */
	protected boolean printBestMove;

	/**
	 * Wieviele Endknoten analysiert und besucht wurden bei der Rekursion.l
	 */
	protected int leafNodesVisited = 0;

	/**
	 * Boolean über den der Thread/Engine kontrolliert beendet werden kann.
	 */
	protected boolean isAllowedToRun;

	/**
	 * Verweis auf die Writer-Instanz zur Kommunikation mit dem Server (Senden
	 * v. Nachrichten).
	 */
	protected MessageOutputWriter messageWriter;

	/**
	 * Instanz der Root-Position des Boards. Also des aktuellen Zustands!
	 */
	protected Bitboard board;

	/**
	 * Instanz auf die Klasse mit Attributen.
	 */
	protected EngineGeneralAttributes generalAttributes;
	
	/**
	 * Kann eine Liste mit allen möglichen Zügen generieren.
	 */
	protected MoveGenerator moveGen = new MoveGenerator();
	
	/**
	 * Registrierung aller gespielten Situationen, zur Überprüfung einer Wiederholung
	 */
	protected RepetitionCounter repetitionCounter;

	/****************************************************************/
	/********************* Konstruktor **************************/
	/****************************************************************/

	public Engine(MessageOutputWriter messageWriter,
			EngineGeneralAttributes generalAttributes) {
		this.ownThread = new Thread(this);
		this.bestMove = new LinkedList<Move>();
		this.isAllowedToRun = true;
		this.printBestMove = false;
		this.messageWriter = messageWriter;
		this.generalAttributes = generalAttributes;
	}

	/**
	 * gibt eine Instanz des eigenen Threads zurück.
	 * 
	 * @return Instanz des eig. Threads.
	 */
	public Thread getOwnThread() {
		return ownThread;
	}

	/**
	 * Die komplette eigentliche Berechnung des besten Moves!
	 */
	public abstract void run();

	/**
	 * Gibt den Präfix, also den Typ der Figur für einen Move von einer Position
	 * zurück.
	 * 
	 * @param tmpBoard
	 * @param pos
	 * @return
	 */
	private char getMovePrefix(Bitboard tmpBoard, Position pos) {
		char typePrefix = 'X';
		/* Bitmap der Position */
		long posMap = tmpBoard.getBitmapAtPosition(pos);

		/* Schleife über die Farben */
		for (int color = 0; color < 2; color++) {
			/* Schleife über alle Figurtypen */
			for (int type = 0; type < 6; type++) {

				/* Alle Vorkommen der Figur */
				long typeMap = tmpBoard.getTypeBitmap(color, type);

				/* Figur gefunden */
				if ((typeMap & posMap) != 0) {
					switch (type) {
					case Constants.Type.ELEPHANT:
						typePrefix = (color == Constants.Color.GOLD) ? 'E'
								: 'e';
						break;
					case Constants.Type.CAMEL:
						typePrefix = (color == Constants.Color.GOLD) ? 'M'
								: 'm';
						break;
					case Constants.Type.HORSE:
						typePrefix = (color == Constants.Color.GOLD) ? 'H'
								: 'h';
						break;
					case Constants.Type.DOG:
						typePrefix = (color == Constants.Color.GOLD) ? 'D'
								: 'd';
						break;
					case Constants.Type.CAT:
						typePrefix = (color == Constants.Color.GOLD) ? 'C'
								: 'c';
						break;
					case Constants.Type.RABBIT:
						typePrefix = (color == Constants.Color.GOLD) ? 'R'
								: 'r';
						break;
					}
				}
			}
		}

		return typePrefix;
	}

	/**
	 * Ermittelt eine Stringrekonstruktion aus dem Bestmove, die der Controller
	 * verstehen kann.
	 * 
	 * @return Stringrepresentation der Moves.
	 */
	public String getBestMove() {
		String allMoves = "bestmove ";

		Bitboard tmpBoard = board.cloneBitboard();

		LinkedList<MoveNormal> moveList = new LinkedList<MoveNormal>();

		int moveCnt = 0;

		for (Move move : bestMove) {
			switch (move.getMoveType()) {
			case Constants.MoveType.NORMAL:
				moveList.add((MoveNormal) move);
				break;
			case Constants.MoveType.SPECIAL:
				moveList.addAll(((MoveSpecial) move).getAllMoves());
				break;
			}
		}

		/* Schleife über wirklich alle Moves. */
		for (MoveNormal move : moveList) {

			if (moveCnt >= 4)
				break;

			Position pos = move.getFrom();
			int colDir = move.getDirCol();
			int rowDir = move.getDirRow();

			char prefix = getMovePrefix(tmpBoard, pos);

			tmpBoard.applyMove(new MoveNormal(pos, colDir, rowDir));

			allMoves += String.valueOf(prefix)
					+ String.valueOf(pos.getCharCol())
					+ (pos.getRow() + 1)
					+ ((colDir == -1) ? "w" : (colDir == 1) ? "e"
							: (rowDir == -1) ? "s" : "n") + " ";

			/*
			 * Wenn eine Figur entfernt wurde, ein 'x' einbauen für den
			 * Controller --> 5. Move.
			 */
			Piece removedPiece = tmpBoard.removePiecesFromTraps();
			if (removedPiece != null) {
				allMoves += String.valueOf(removedPiece.getPrefix())
						+ String.valueOf(removedPiece.getPosition()
								.getCharCol())
						+ (removedPiece.getPosition().getRow() + 1) + "x" + " ";
			}

			moveCnt++;
		}

		tmpBoard = null;

		return allMoves;
	}

	public void stopThread() {
		this.isAllowedToRun = false;
	}

	public void setPrintBestMove(boolean flag) {
		this.printBestMove = flag;
	}

	/**
	 * Da ein Thread niemals mehrfach gestartet werden kann, muss eine neue
	 * Instanz erzeugt werden. Alle Variablen, die zum Lauf relevant sind werden
	 * neu initialisiert.
	 */
	public abstract void newThread();

	/**
	 * Setzt die private Instanz des Boards vor jeder neuen Berechnung auf die
	 * aktuelle Position!
	 * 
	 * @param board
	 *            die aktuelle Boardkonfiguration des Spielfeldes
	 */
	public void setBoard(Bitboard board) {
		this.board = board;
	}
	
	/**
	 * Setzt die Instanz zum Lookup für gespielte Boardsituationen.
	 * 
	 * @param boardRegistration Die entsprechende Instanz.
	 */
	public void setRepetitionCounter (RepetitionCounter repetitionCounter) {
		this.repetitionCounter = repetitionCounter;
	}

	/**
	 * Errechnet den maximalen Wert und gibt diesen zurück.
	 * 
	 * @param x
	 *            ein Wert
	 * @param y
	 *            nochn Wert
	 * @return Der Höchste.
	 */
	public int max(int x, int y) {
		return (x >= y) ? x : y;
	}

	/**
	 * Errechnet den minimalen Wert und gibt diesen zurück.
	 * 
	 * @param x
	 *            ein Wert
	 * @param y
	 *            nochn Wert
	 * @return Der Niedrigste.
	 */
	public int min(int x, int y) {
		return (x <= y) ? x : y;
	}

	/**
	 * Gibt die jeweils andere Farbe zurück.
	 * 
	 * @param color
	 *            Farbe
	 * @return andere Farbe
	 */
	public int otherColor(int color) {
		return (color == Constants.Color.SILVER) ? Constants.Color.GOLD
				: Constants.Color.SILVER;
	}

}
