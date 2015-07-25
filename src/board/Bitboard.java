package board;

import java.util.Arrays;
import java.util.LinkedList;

import engine.EngineGeneralAttributes;

import memory.Transposition;
import network.MessageOutputWriter;

/**
 * Implementierung der abstrakten Board-Klasse. Die abstrakte Teil ist
 * eigentlich nicht notwendig (zumindest bei der Benennung der Methoden und
 * Rückgabewerte... Aber kann man später auch noch ändern...)
 * 
 * @author maurice
 * 
 */
public class Bitboard {

	/************************************************************/
	/********************* __Variablen__ ************************/
	/************************************************************/

	/**
	 * Liste mit Masken für jede einzelne Position auf dem Feld (jew. ein
	 * gesetztes Bit).
	 */
	private long[] masks = null;

	/**
	 * Instanz mit dem Bitboards.
	 */
	private long[][] bitboard = null;

	/**
	 * Hash-Wert dieses Boards!
	 */
	private long hash = 0L;

	/**
	 * Eine Instanz des Transposition-Tables, damit der MakeMove funktioniert
	 * und den 'hash' korrekt updated.
	 */
	private Transposition transposition = null;

	/**
	 * Instanz der Attribute.
	 */
	private EngineGeneralAttributes generalAttributes;

	/**
	 * PERFORMANCE ENHANCING VARIABLES
	 */
	private long generalBitmapP = 0L;
	private long colorBitmapGoldP = 0L;
	private long colorBitmapSilverP = 0L;
	private long colorBitmapHigherGoldCamelP = 0L;
	private long colorBitmapHigherSilverCamelP = 0L;
	private long colorBitmapHigherGoldHorseP = 0L;
	private long colorBitmapHigherSilverHorseP = 0L;
	private long colorBitmapHigherGoldDogP = 0L;
	private long colorBitmapHigherSilverDogP = 0L;
	private long colorBitmapHigherGoldCatP = 0L;
	private long colorBitmapHigherSilverCatP = 0L;
	private long colorBitmapHigherGoldRabbitP = 0L;
	private long colorBitmapHigherSilverRabbitP = 0L;

	/****************************************************************/
	/********************* _Konstruktoren_ **************************/
	/****************************************************************/

	public Bitboard(EngineGeneralAttributes generalAttributes) {

		this.bitboard = new long[6][2];
		this.masks = new long[64];
		this.generalAttributes = generalAttributes;

		generateEmptyBitboard();
		generateMasks();
		initPerformanceEnhancingVariables();
	}

	public Bitboard(EngineGeneralAttributes generalAttributes, long[][] board,
			long[] masks) {
		this.bitboard = board;
		this.masks = masks;
		this.generalAttributes = generalAttributes;
		initPerformanceEnhancingVariables();
	}

	/****************************************************************/
	/********************* Initialisierung **************************/
	/****************************************************************/

	/**
	 * Erzeugt ein zweidimensionales Array für alle versch. Figuren und die
	 * Farben (Gold/Silber).
	 * 
	 * Für jede Figur sind in einem 'long' alle Positionen dieser Figurart
	 * verzeichnet. Z.B.: ...0001000000000111...
	 * 
	 * Damit sind für Gold- und Silber alle Figuren auf dem Feld festgelegt und
	 * zugreifbar.
	 * 
	 * Es ist also ein Array der Größe 'long[6][2]' (6 versch. Figuren, 2
	 * Farben).
	 * 
	 * @return Eine Definition des Spielfeldes unterteilt in Figuren und Farbe
	 *         als Long-Array.
	 */
	public void generateEmptyBitboard() {

		/* Bitboard initialisieren mit leerem 'long' */
		for (int i = 0; i < bitboard.length; i++) {
			this.bitboard[i][Constants.Color.SILVER] = Constants.Bitmap.EMPTY_BITMAP;
			this.bitboard[i][Constants.Color.GOLD] = Constants.Bitmap.EMPTY_BITMAP;
		}

	}

	/**
	 * Erzeugt ein Array mit Bitmaps, die jew. 1 Bit gesetzt haben, mit ihrer
	 * jew. Position als Maske.
	 */
	public void generateMasks() {

		/* Masken initialisieren mit ihrer jew. Position */
		for (int i = 0; i < masks.length; i++) {
			this.masks[i] = 1L << i;
		}

	}

	/**
	 * Initialisiert einmalig alle Variablen, die der Performance maßgeblich
	 * helfen können.
	 */
	public void initPerformanceEnhancingVariables() {

		for (int i = 0; i < 6; i++) {

			generalBitmapP = generalBitmapP
					| this.bitboard[i][Constants.Color.SILVER]
					| this.bitboard[i][Constants.Color.GOLD];

			colorBitmapGoldP = colorBitmapGoldP
					| this.bitboard[i][Constants.Color.GOLD];
			colorBitmapSilverP = colorBitmapSilverP
					| this.bitboard[i][Constants.Color.SILVER];

			switch (i) {
			case Constants.Type.ELEPHANT:
				colorBitmapHigherGoldCamelP = colorBitmapGoldP;
				colorBitmapHigherSilverCamelP = colorBitmapSilverP;
				break;
			case Constants.Type.CAMEL:
				colorBitmapHigherGoldHorseP = colorBitmapGoldP;
				colorBitmapHigherSilverHorseP = colorBitmapSilverP;
				break;
			case Constants.Type.HORSE:
				colorBitmapHigherGoldDogP = colorBitmapGoldP;
				colorBitmapHigherSilverDogP = colorBitmapSilverP;
				break;
			case Constants.Type.DOG:
				colorBitmapHigherGoldCatP = colorBitmapGoldP;
				colorBitmapHigherSilverCatP = colorBitmapSilverP;
				break;
			case Constants.Type.CAT:
				colorBitmapHigherGoldRabbitP = colorBitmapGoldP;
				colorBitmapHigherSilverRabbitP = colorBitmapSilverP;
				break;
			}

		}

	}

	/****************************************************************/
	/********************* ____Bitmaps____ **************************/
	/****************************************************************/

	/**
	 * Erzeugt eine Sicht auf das komplette Spielfeld, wo 0 ein leeres Feld und
	 * 1 ein belegtes Feld anzeigt. Unabhängig von Farbe oder Figurtyp.
	 * 
	 * @param bitboard
	 *            Das Bitboard, mit dem die Map generiert werden soll.
	 * @return eine Map, in der alle (den obigen Einschränkungen unterliegenden)
	 *         Felder mit 1 belegt sind und der Rest mit 0.
	 */
	public long getGeneralBitmap() {

		long generalBitmap = 0L;

		for (int i = 0; i < this.bitboard.length; i++) {
			generalBitmap = generalBitmap
					| this.bitboard[i][Constants.Color.SILVER]
					| this.bitboard[i][Constants.Color.GOLD];
		}

		return generalBitmap;

		// return generalBitmapP;

	}

	/**
	 * Erzeugt eine Bitmap für eine ganz bestimmte Figur.
	 * 
	 * @param bitboard
	 *            Das Bitboard, mit dem die Map generiert werden soll.
	 * @return eine Map, in der alle (den obigen Einschränkungen unterliegenden)
	 *         Felder mit 1 belegt sind und der Rest mit 0.
	 */
	public long getPieceBitmap(Piece piece) {

		return 1L << (piece.getPosition().getRow() * 8 + piece.getPosition()
				.getCol());

	}

	/**
	 * Erzeugt eine Bitmap für eine Farbe.
	 * 
	 * @param bitboard
	 *            Das Bitboard, mit dem die Map generiert werden soll.
	 * @return eine Map, in der alle (den obigen Einschränkungen unterliegenden)
	 *         Felder mit 1 belegt sind und der Rest mit 0.
	 */
	public long getColorBitmap(int color) {

		long colorBitmap = 0L;

		for (int i = 0; i < this.bitboard.length; i++) {
			colorBitmap = colorBitmap | this.bitboard[i][color];
		}

		return colorBitmap;

		// switch (color) {
		// case Constants.Color.GOLD:
		// return colorBitmapGoldP;
		// case Constants.Color.SILVER:
		// return colorBitmapSilverP;
		// default:
		// return 0L;
		// }

	}

	/**
	 * Erzeugt eine Bitmap einer Farbe mit allen Figuren, die höher sind, als
	 * die Übergebene.
	 * 
	 * @param color
	 * @param type
	 * @return
	 */
	public long getColorBitmapHigher(int color, int type) {
		long colorMap = 0L;

		for (int i = 0; i < type; i++) {
			colorMap = colorMap | this.bitboard[i][color];
		}

		return colorMap;

		// switch (type) {
		// case Constants.Type.CAMEL:
		// return color == Constants.Color.GOLD ? colorBitmapHigherGoldCamelP
		// : colorBitmapHigherSilverCamelP;
		// case Constants.Type.HORSE:
		// return color == Constants.Color.GOLD ? colorBitmapHigherGoldHorseP
		// : colorBitmapHigherSilverHorseP;
		// case Constants.Type.DOG:
		// return color == Constants.Color.GOLD ? colorBitmapHigherGoldDogP
		// : colorBitmapHigherSilverDogP;
		// case Constants.Type.CAT:
		// return color == Constants.Color.GOLD ? colorBitmapHigherGoldCatP
		// : colorBitmapHigherSilverCatP;
		// case Constants.Type.RABBIT:
		// return color == Constants.Color.GOLD ? colorBitmapHigherGoldRabbitP
		// : colorBitmapHigherSilverRabbitP;
		// default:
		// return 0L;
		// }
	}

	/**
	 * Erzeugt eine Bitmap für eine Figur und Farbe (Alle Vorkommen der Figur).
	 * 
	 * @param bitboard
	 *            Das Bitboard, mit dem die Map generiert werden soll.
	 * @return eine Map, in der alle (den obigen Einschränkungen unterliegenden)
	 *         Felder mit 1 belegt sind und der Rest mit 0.
	 */
	public long getTypeBitmap(int color, int type) {

		return this.bitboard[type][color];

	}

	/**
	 * Erzeugt eine Bitmap für eine bestimmte Position auf dem Feld. Also
	 * entweder eine 1 oder eine 0 an der Position, je nachdem ob sie belegt ist
	 * oder nicht.
	 * 
	 * @param bitboard
	 *            Das Bitboard, mit dem die Map generiert werden soll.
	 * @param position
	 *            Die Position, für die die Map erzeugt werden soll.
	 * @return
	 */
	public long getBitmapAtPosition(Position position) {

		return getGeneralBitmap() & getMaskAtPosition(position);

	}

	/**
	 * Erzeugt eine Bitmap für eine bestimmte Position und Farbe auf dem Feld.
	 * Also entweder eine 1 oder eine 0 an der Position, je nachdem ob sie mit
	 * der bestimmten Farbe belegt ist oder nicht.
	 * 
	 * @param bitboard
	 *            Das Bitboard, mit dem die Map generiert werden soll.
	 * @param position
	 *            Die Position, für die die Map erzeugt werden soll.
	 * @return
	 */
	public long getColorBitmapAtPosition(int color, Position position) {

		return getColorBitmap(color) & getMaskAtPosition(position);

	}

	/****************************************************************/
	/********************* _____Masks_____ **************************/
	/****************************************************************/

	/**
	 * Gibt die Maske einer bestimmten Position zurück. (1 Bit gesetzt.)
	 * 
	 * @param position
	 *            Die Position, für die die Maske.
	 * @return
	 */
	public long getMaskAtPosition(Position pos) {

		return getMaskAt(pos.getCol(), pos.getRow());

	}

	/**
	 * Gibt die Maske einer bestimmten Position zurück. (1 Bit gesetzt.)
	 * 
	 * @param position
	 *            Die Position, für die die Maske.
	 * @return
	 */
	public long getMaskAt(int col, int row) {
		return masks[(row << 3) + col];
	}

	/****************************************************************/
	/********************* ____Set/Unset____ **************************/
	/****************************************************************/

	/**
	 * Setzt eine ganz spezifische Figur auf eine Position in dem Bitboard.
	 * 
	 * @param piece
	 *            Die zu setzende Figur.
	 */
	public void setPieceAtPosition(Piece piece) {
		bitboard[piece.getType()][piece.getColor()] |= getMaskAtPosition(piece
				.getPosition());
		initPerformanceEnhancingVariables();
	}

	/**
	 * Entfernt eine ganz spezifische Figur von seiner Position auf dem Bitboard
	 * 
	 * @param piece
	 *            Die zu entfernende Figur.
	 */
	public void unsetPieceAtPosition(Piece piece) {
		bitboard[piece.getType()][piece.getColor()] &= ~getMaskAtPosition(piece
				.getPosition());
		initPerformanceEnhancingVariables();
	}

	/****************************************************************/
	/********************* ____ACTION_____ **************************/
	/****************************************************************/

	/**
	 * Findet eine Figur an einer übergebenen Position.
	 * 
	 * @param pos
	 *            Position
	 * @return Die Figur
	 */
	public Piece findPieceAt(Position pos) {

		long mask = getMaskAtPosition(pos);

		for (int color = 0; color < 2; color++) {
			if ((mask & getColorBitmap(color)) != 0) {
				for (int type = 0; type < 6; type++) {
					if ((mask & getTypeBitmap(color, type)) != 0) {
						return new Piece(pos, color, type);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gibt eine Bitmap zurück, die alle 4 Felder um die übergebende Position
	 * und diese belegt.
	 * 
	 * @param col
	 *            Column
	 * @param row
	 *            Row
	 * @return
	 */
	public long getBitmapAroundPosition(int col, int row) {

		long bitmap = getMaskAt(col, row);

		/* nicht am rechten Rand */
		if (col < 7) {
			bitmap |= getMaskAt(col + 1, row);
		}

		/* nicht am linken Rand */
		if (col > 0) {
			bitmap |= getMaskAt(col - 1, row);
		}

		/* nicht am oberen Rand */
		if (row < 7) {
			bitmap |= getMaskAt(col, row + 1);
		}

		/* nicht am oberen Rand */
		if (row > 0) {
			bitmap |= getMaskAt(col, row - 1);
		}

		return bitmap;
	}

	/**
	 * Gibt eine Bitmap zurück, die alle 4 Felder um die übergebende Position
	 * und diese belegt.
	 * 
	 * @param pos
	 *            Die Position
	 * @return
	 */
	public long getBitmapAroundPosition(Position pos) {
		return getBitmapAroundPosition(pos.getCol(), pos.getRow());
	}

	/**
	 * Prüft, ob ein Feld leer ist.
	 * 
	 * @param col
	 *            col
	 * @param row
	 *            row
	 * @return obs leer ist.
	 */
	public boolean isEmptyField(int col, int row) {
		return (getMaskAt(col, row) & getGeneralBitmap()) == 0;
	}

	/**
	 * Gibt zurück, ob eine bestimmte übergebene Figur auf dem Spielfeld durch
	 * eine höhere gegnerische Figur blockiert wird und keine Figur der eigenen
	 * Farbe dies verhindert.
	 * 
	 * @return ob die Figur blockiert ist.
	 */
	public boolean pieceIsFrozen(Piece piece) {

		long tmpBitmap = getBitmapAroundPosition(piece.getPosition().getCol(),
				piece.getPosition().getRow());
		int enemyColor = (piece.getColor() == 0) ? 1 : 0;

		/* Die eigene Figur aus der Auswahl entfernen! */
		tmpBitmap &= ~getMaskAt(piece.getPosition().getCol(), piece
				.getPosition().getRow());

		/* Eine gleichfarbige Figur steht neben der Figur! */
		if ((tmpBitmap & getColorBitmap(piece.getColor())) != 0) {
			return false;
		}

		/*
		 * Nur höherwertige Constanten checken --> Geht als Schleife, da Figuren
		 * absteigend sortiert wurden bei ihrer Definition!
		 */
		for (int i = 0; i < piece.getType(); i++) {
			if ((tmpBitmap & getTypeBitmap(enemyColor, i)) != 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Alle Attackmöglichkeiten der Figur. Wobei Hasen nicht zurück dürfen und
	 * die Bewegung durch andere Figuren eingegrenzt sein kann
	 * 
	 * @param piece
	 *            die attackierende Figur
	 * @return
	 */
	public long getPieceMovementBitmap(Piece piece) {

		Position pos = piece.getPosition();
		int col = pos.getCol(), row = pos.getRow();
		long bitmap = 0L;

		/* Ob eine Figur gefreezed wurde, muss übergeordnet abgefragt werden!!! */

		long generalBitmap = getGeneralBitmap();

		bitmap = getBitmapAroundPosition(col, row);

		/* Hasen dürfen nicht zurück gehen */
		if (piece.getType() == Constants.Type.RABBIT) {
			switch (piece.getColor()) {
			case Constants.Color.SILVER:
				if (row < 7)
					bitmap &= ~getMaskAt(col, row + 1);
				break;
			case Constants.Color.GOLD:
				if (row > 0)
					bitmap &= ~getMaskAt(col, row - 1);
				break;
			}
		}

		/*
		 * Es kann sein, dass bestimmte Positionen durch andere Figuren belegt
		 * sind. Die Dürfen auch nicht begangen werden
		 */
		bitmap &= ~generalBitmap;

		return bitmap;

	}

	/**
	 * Geht stumpf für eine Position alle Push-Möglichkeiten durch und gibt die
	 * List mit validen Pushs zurück. Es wird davon ausgegangen, dass bei der
	 * attackMap schon alle Typüberprüfungen gemacht wurde.
	 * 
	 * @param attackMap
	 *            eine Map mit möglichen Pushes (Richtungen)
	 * @param col
	 *            col
	 * @param row
	 *            row
	 * @return Liste mit allen Push-Möglichkeiten
	 */
	private LinkedList<MoveSpecial> getPushListAtPosition(long attackMap,
			int col, int row) {
		LinkedList<MoveSpecial> resultList = new LinkedList<MoveSpecial>();

		/* nach oben pushen ist möglich ? */
		if ((row < 7) && (attackMap & getMaskAt(col, row + 1)) != 0) {
			/* Figur nach oben pushen */
			if (row < 6 && isEmptyField(col, row + 2)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row + 1), 0, 1), new MoveNormal(new Position(col, row),
						0, 1)));
			}
			/* Figur nach links pushen */
			if (col > 0 && isEmptyField(col - 1, row + 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row + 1), -1, 0), new MoveNormal(
						new Position(col, row), 0, 1)));
			}
			/* Figur nach rechts pushen */
			if (col < 7 && isEmptyField(col + 1, row + 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row + 1), 1, 0), new MoveNormal(new Position(col, row),
						0, 1)));
			}

		}

		/* nach links pushen möglich ? */
		if ((col > 0) && (attackMap & getMaskAt(col - 1, row)) != 0) {
			/* Figur nach links pushen */
			if (col > 1 && isEmptyField(col - 2, row)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(
						col - 1, row), -1, 0), new MoveNormal(new Position(col,
						row), -1, 0)));
			}
			/* Figur nach oben pushen */
			if (row < 7 && isEmptyField(col - 1, row + 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(
						col - 1, row), 0, 1), new MoveNormal(new Position(col,
						row), -1, 0)));
			}
			/* Figur nach unten pushen */
			if (row > 0 && isEmptyField(col - 1, row - 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(
						col - 1, row), 0, -1), new MoveNormal(new Position(col,
						row), -1, 0)));
			}

		}

		/* nach unten pushen möglich ? */
		if ((row > 0) && (attackMap & getMaskAt(col, row - 1)) != 0) {
			/* Figur nach unten pushen */
			if (row > 1 && isEmptyField(col, row - 2)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row - 1), 0, -1), new MoveNormal(
						new Position(col, row), 0, -1)));
			}
			/* Figur nach links pushen */
			if (col > 0 && isEmptyField(col - 1, row - 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row - 1), -1, 0), new MoveNormal(
						new Position(col, row), 0, -1)));
			}
			/* Figur nach rechts pushen */
			if (col < 7 && isEmptyField(col + 1, row - 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row - 1), 1, 0), new MoveNormal(new Position(col, row),
						0, -1)));
			}
		}

		/* nach rechts pushen möglich ? */
		if ((col < 7) && (attackMap & getMaskAt(col + 1, row)) != 0) {
			/* Figur nach rechts pushen */
			if (col < 6 && isEmptyField(col + 2, row)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(
						col + 1, row), 1, 0), new MoveNormal(new Position(col,
						row), 1, 0)));
			}
			/* Figur nach unten pushen */
			if (row > 0 && isEmptyField(col + 1, row - 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(
						col + 1, row), 0, -1), new MoveNormal(new Position(col,
						row), 1, 0)));
			}
			/* Figur nach oben pushen */
			if (row < 7 && isEmptyField(col + 1, row + 1)) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(
						col + 1, row), 0, 1), new MoveNormal(new Position(col,
						row), 1, 0)));
			}
		}
		return resultList;
	}

	/**
	 * Geht stumpf für eine Position alle Pull-Möglichkeiten durch und gibt die
	 * List mit validen Pulls zurück. Es wird davon ausgegangen, dass bei der
	 * attackMap schon alle Typüberprüfungen gemacht wurde.
	 * 
	 * @param attackMap
	 *            eine Map mit möglichen Pushes (Richtungen)
	 * @param col
	 *            col
	 * @param row
	 *            row
	 * @return Liste mit allen Pull-Möglichkeiten
	 */
	private LinkedList<MoveSpecial> getPullListAtPosition(long attackMap,
			int col, int row) {
		LinkedList<MoveSpecial> resultList = new LinkedList<MoveSpecial>();

		/* nach rechts gehen */
		if (col < 7 && isEmptyField(col + 1, row)) {
			/* von oben reinziehen */
			if (row < 7 && (attackMap & getMaskAt(col, row + 1)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 1, 0), new MoveNormal(new Position(col, row + 1),
						0, -1)));
			}
			/* von links reinziehen */
			if (col > 0 && (attackMap & getMaskAt(col - 1, row)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 1, 0), new MoveNormal(new Position(col - 1, row),
						1, 0)));
			}
			/* von unten reinziehen */
			if (row > 0 && (attackMap & getMaskAt(col, row - 1)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 1, 0), new MoveNormal(new Position(col, row - 1),
						0, 1)));
			}
		}
		/* nach links gehen */
		if (col > 0 && isEmptyField(col - 1, row)) {
			/* von oben reinziehen */
			if (row < 7 && (attackMap & getMaskAt(col, row + 1)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), -1, 0), new MoveNormal(
						new Position(col, row + 1), 0, -1)));
			}
			/* von rechts reinziehen */
			if (col < 7 && (attackMap & getMaskAt(col + 1, row)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), -1, 0), new MoveNormal(
						new Position(col + 1, row), -1, 0)));
			}
			/* von unten reinziehen */
			if (row > 0 && (attackMap & getMaskAt(col, row - 1)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), -1, 0), new MoveNormal(
						new Position(col, row - 1), 0, 1)));
			}
		}
		/* nach unten gehen */
		if (row > 0 && isEmptyField(col, row - 1)) {
			/* von links reinziehen */
			if (col > 0 && (attackMap & getMaskAt(col - 1, row)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 0, -1), new MoveNormal(
						new Position(col - 1, row), 1, 0)));
			}
			/* von oben reinziehen */
			if (row < 7 && (attackMap & getMaskAt(col, row + 1)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 0, -1), new MoveNormal(
						new Position(col, row + 1), 0, -1)));
			}
			/* von rechts reinziehen */
			if (col < 7 && (attackMap & getMaskAt(col + 1, row)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 0, -1), new MoveNormal(
						new Position(col + 1, row), -1, 0)));
			}
		}
		/* nach oben gehen */
		if (row < 7 && isEmptyField(col, row + 1)) {
			/* von links reinziehen */
			if (col > 0 && (attackMap & getMaskAt(col - 1, row)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 0, 1), new MoveNormal(new Position(col - 1, row),
						1, 0)));
			}
			/* von unten reinziehen */
			if (row > 0 && (attackMap & getMaskAt(col, row - 1)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 0, 1), new MoveNormal(new Position(col, row - 1),
						0, 1)));
			}
			/* von rechts reinziehen */
			if (col < 7 && (attackMap & getMaskAt(col + 1, row)) != 0) {
				resultList.add(new MoveSpecial(new MoveNormal(new Position(col,
						row), 0, 1), new MoveNormal(new Position(col + 1, row),
						-1, 0)));
			}
		}

		return resultList;
	}

	/**
	 * Alle Attackmöglichkeiten der übergebenen Figur, bei denen eine andere
	 * Figur weggeschoben werden kann! Beinhaltet die Überprüfung, ob der Push
	 * erfolgreich sein kann (oder blockiert wird). Markiert die Position, in
	 * die gepushed werden kann.
	 * 
	 * @param piece
	 *            Die Figur, für die die special-MoveList berechnet wird.
	 * @return Alle Züge, bei denen gezogen/geschoben werden kann von der
	 *         übergebenen Figur.
	 */
	public LinkedList<MoveSpecial> getPieceSpecialMoveList(Piece piece) {

		Position pos = piece.getPosition();
		int col = pos.getCol(), row = pos.getRow();
		int enemyColor = (piece.getColor() == Constants.Color.GOLD) ? Constants.Color.SILVER
				: Constants.Color.GOLD;
		long bitmap = 0L;

		LinkedList<MoveSpecial> resultList = new LinkedList<MoveSpecial>();

		/* Sonderfall: Hase */
		if (piece.getType() == Constants.Type.RABBIT)
			return resultList;

		/* Ob die Figur gefreezed ist, muss übergeordnet abgefragt werden!!! */

		bitmap = getBitmapAroundPosition(col, row);

		/* Überprüfen niedriger Figuren */
		for (int type = piece.getType() + 1; type < 6; type++) {

			long attackMap = bitmap & getTypeBitmap(enemyColor, type);
			/* Position belegt? */
			if (attackMap != 0) {

				/* Das stumpfe Durchgehen aller Möglichkeiten... */
				resultList.addAll(getPushListAtPosition(attackMap, col, row));

				/* Das stumpfe Durchgehen aller Möglichkeiten... */
				resultList.addAll(getPullListAtPosition(attackMap, col, row));

			}
		}

		return resultList;
	}

	/**
	 * printet eine Bitmap aus in Form eines fertigen Spielfeldes.
	 * 
	 * @param bitmap
	 *            die zu printende Bitmap
	 */
	public void printBitmap(long bitmap, char sign) {

		String output = "\n +-----------------+\n   a b c d e f g h";
		String tmpString = "";
		int colnum = 1;
		MessageOutputWriter messageWriter = new MessageOutputWriter();

		// Alle Zeichen durchgehen
		for (int i = 0; i < 64; i++) {
			// Vorne anhängen
			if ((i % 8 == 0)) {
				if (i == 0) {

				} else {
					output = "\n" + colnum++ + "| " + tmpString + "|" + output;
					tmpString = "";
				}
			}

			if (((bitmap >>> i) & 1L) != 0) {
				tmpString += sign + " ";
			} else {
				tmpString += "  ";
			}
		}

		output = " +-----------------+" + "\n" + colnum++ + "| " + tmpString
				+ "|" + output + "\n";

		messageWriter.sendMessage(output);

	}

	/**
	 * Printet das komplette Spielfeld mit allen Figuren.
	 */
	public void printBitboard() {
		MessageOutputWriter messageWriter = new MessageOutputWriter();

		String bitBoardString = toString();

		messageWriter.sendMessage(bitBoardString);
	}

	/**
	 * Überprüft alle 4 Traps, ob dort ungesicherte Figuren stehen und entfernt
	 * diese unter Umständen.
	 * 
	 * @return eine Instanz auf den Typ und Position der Figur, die entfernt
	 *         wurde.
	 */
	public Piece removePiecesFromTraps() {
		Piece removedPiece = null;

		LinkedList<Position> traps = new LinkedList<Position>();
		traps.add(new Position(2, 2));
		traps.add(new Position(5, 2));
		traps.add(new Position(2, 5));
		traps.add(new Position(5, 5));

		for (Position pos : traps) {

			if ((getMaskAt(pos.getCol(), pos.getRow()) & getGeneralBitmap()) != 0) {

				Piece piece = findPieceAt(pos);
				if ((piece != null)
						&& ((getColorBitmap(piece.getColor()) & (getBitmapAroundPosition(pos) & ~getBitmapAtPosition(pos))) == 0)) {
					unsetPieceAtPosition(piece);
					removedPiece = piece;

					if (generalAttributes.getUseMemory() && transposition != null) {
						/* den Zobrist-Hash updaten! */
						synchronized (transposition.getClass()) {
							transposition.setTypeAt(this, piece.getColor(),
									piece.getType(), piece.getPosition().getCol(),
									piece.getPosition().getRow());
						}
							
					}

				}
			}
		}

		return removedPiece;
	}

	/**
	 * Erzeugt eine exakte Kopie dieses Bitboards als neue Instanz und gibt
	 * diese zurück.
	 * 
	 * @return Neue Instanz als Kopie dieses Boards.
	 */
	public Bitboard cloneBitboard() {

		Bitboard clone = new Bitboard(this.generalAttributes);

		long[][] res = new long[6][2];

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 2; j++) {
				res[i][j] = this.bitboard[i][j];
			}
		}

		clone.setBitboard(res);
		clone.setZobristHash(this.hash);
		clone.setTransposition(transposition);

		clone.initPerformanceEnhancingVariables();

		// clone.colorBitmapGoldP = this.colorBitmapGoldP;
		// clone.colorBitmapHigherGoldCamelP = this.colorBitmapHigherGoldCamelP;
		// clone.colorBitmapHigherGoldCatP = this.colorBitmapHigherGoldCatP;
		// clone.colorBitmapHigherGoldDogP = this.colorBitmapHigherGoldDogP;
		// clone.colorBitmapHigherGoldHorseP = this.colorBitmapHigherGoldHorseP;
		// clone.colorBitmapHigherGoldRabbitP =
		// this.colorBitmapHigherGoldRabbitP;
		// clone.colorBitmapHigherSilverCamelP =
		// this.colorBitmapHigherSilverCamelP;
		// clone.colorBitmapHigherSilverCatP = this.colorBitmapHigherSilverCatP;
		// clone.colorBitmapHigherSilverDogP = this.colorBitmapHigherSilverDogP;
		// clone.colorBitmapHigherSilverHorseP =
		// this.colorBitmapHigherSilverHorseP;
		// clone.colorBitmapHigherSilverRabbitP =
		// this.colorBitmapHigherSilverRabbitP;
		// clone.colorBitmapSilverP = this.colorBitmapSilverP;
		// clone.generalBitmapP = this.generalBitmapP;

		return clone;
	}

	/**
	 * Erstellt keine neuen Instanzen, sondern überschreibt in der übergebenen
	 * Instanz einfach die Werte.
	 * 
	 * @param board
	 * @return
	 */
	public Bitboard softCloneBitboard(Bitboard board) {

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 2; j++) {
				board.bitboard[i][j] = this.bitboard[i][j];
			}
		}

		board.setZobristHash(this.hash);
		board.setTransposition(transposition);
		board.initPerformanceEnhancingVariables();

		// board.colorBitmapGoldP = this.colorBitmapGoldP;
		// board.colorBitmapHigherGoldCamelP = this.colorBitmapHigherGoldCamelP;
		// board.colorBitmapHigherGoldCatP = this.colorBitmapHigherGoldCatP;
		// board.colorBitmapHigherGoldDogP = this.colorBitmapHigherGoldDogP;
		// board.colorBitmapHigherGoldHorseP = this.colorBitmapHigherGoldHorseP;
		// board.colorBitmapHigherGoldRabbitP =
		// this.colorBitmapHigherGoldRabbitP;
		// board.colorBitmapHigherSilverCamelP =
		// this.colorBitmapHigherSilverCamelP;
		// board.colorBitmapHigherSilverCatP = this.colorBitmapHigherSilverCatP;
		// board.colorBitmapHigherSilverDogP = this.colorBitmapHigherSilverDogP;
		// board.colorBitmapHigherSilverHorseP =
		// this.colorBitmapHigherSilverHorseP;
		// board.colorBitmapHigherSilverRabbitP =
		// this.colorBitmapHigherSilverRabbitP;
		// board.colorBitmapSilverP = this.colorBitmapSilverP;
		// board.generalBitmapP = this.generalBitmapP;

		return board;
	}

	public long[] getMasks() {
		return this.masks;
	}

	public long[][] getBitboard() {
		return this.bitboard;
	}

	public void setMasks(long[] masks) {
		this.masks = masks;
	}

	public void setBitboard(long[][] board) {
		this.bitboard = board;
	}

	public void setZobristHash(long hash) {
		this.hash = hash;
	}

	public long getZobristHash() {
		return this.hash;
	}

	public void setTransposition(Transposition transposition) {
		this.transposition = transposition;
	}

	/**
	 * Errechnet eine Fläche, die mit *distance Schritten begangen werden
	 * könnte. Also die Fläche abgedeckt von *distance Schritten.
	 * 
	 * @param col
	 *            col der Position
	 * @param row
	 *            row der Position
	 * @param distance
	 *            Anzahl möglicher Schritte
	 * @return mit distance Schritten abgedeckte Fläche
	 */
	public long area(int col, int row, int distance) {

		long result = 0L;

		/* col-Richtung */
		for (int i = 0; i <= distance; i++) {
			/* row-Richtung */
			for (int j = 0; j <= distance; j++) {

				/*
				 * nur, wenn schritte in X und Y-Richtung zusammen <= distance
				 * ergeben!
				 */
				if ((i + j) <= distance) {

					if ((col - i) >= 0 && (row - j) >= 0)
						result |= getMaskAt(col - i, row - j);

					if ((col + i) <= 7 && (row - j) >= 0)
						result |= getMaskAt(col + i, row - j);

					if ((col - i) >= 0 && (row + j) <= 7)
						result |= getMaskAt(col - i, row + j);

					if ((col + i) <= 7 && (row + j) <= 7)
						result |= getMaskAt(col + i, row + j);

				}

			}

		}

		return result;
	}

	/**
	 * Gibt die maximale Anzahl an Schritten an, die man braucht um alle Felder
	 * des Boards erreichen zu können.
	 * 
	 * @param col
	 *            die Col
	 * @param row
	 *            die Row
	 * @return Anzahl an maximalen Schritten
	 */
	public int distancemax(int col, int row) {

		int maxCol = (col <= 3) ? (7 - col) : col;
		int maxRow = (row <= 3) ? (7 - row) : row;

		return maxCol + maxRow;

	}

	/**
	 * Errechnet, wie oft eine Figur einer Farbe auf dem Spielfeld vorhanden
	 * ist.
	 * 
	 * @param color
	 *            die Farbe
	 * @param type
	 *            der Typ
	 * @return Anzahl der Vorkommen
	 */
	public int getTypeCount(int color, int type) {
		int count = 0;
		long typeMask = getTypeBitmap(color, type);

		/* Vorkommen suchen */
		for (int i = 0; i < 64; i++) {
			long mask = 1L << i;
			/* eine gefunden */
			if ((typeMask & mask) != 0) {
				count++;
			}
		}
		return count;

		// return getBitCount(typeMask);
	}

	/**
	 * Gibt zurück, wieviele Bits im übergebenen long gesetzt sind.
	 * 
	 * @param map
	 *            die Map mit Bits.
	 * @return wieviele Bits gesetzt sind.
	 */
	public int getBitCount(long map) {
		int count = 0;

		for (int i = 0; i < 64; i++) {
			if (((1L << i) & map) != 0)
				count++;
		}

		return count;

		// while (map != 0) {
		// count += map & 1;
		// map = map >> 1;
		// }
		//
		// return count;
	}

	/**
	 * Gibt eine Map zurück, in der für alle höheren Typen mit der Color color
	 * die Bits gesetzt sind.
	 * 
	 * @param type
	 *            Figurtyp
	 * @param color
	 *            Farbe
	 * @return gesetzte Bits mit höheren Figuren.
	 */
	public long getHigherTypeMap(int type, int color) {

		// long res = 0L;
		// for (int i = 0; i < type; i++) {
		// res |= getTypeBitmap(color, i);
		// }
		// return res;

		return getColorBitmapHigher(color, type);
	}

	/**
	 * Gibt eine Map zurück, in der für alle niederen Typen mit der Color color
	 * die Bits gesetzt sind.
	 * 
	 * @param type
	 *            Figurtyp
	 * @param color
	 *            Farbe
	 * @return gesetzte Bits mit niederen Figuren.
	 */
	public long getLowerEqualTypeMap(int type, int color) {

		// long res = 0L;
		// for (int i = type; i < 6; i++) {
		// res |= getTypeBitmap(color, i);
		// }
		// return res;

		return getColorBitmap(color) & ~getColorBitmapHigher(color, type);

	}

	/**
	 * Gibt eine Map zurück, die eine komplette Reihe mit 1en belegt.
	 * 
	 * @param row
	 *            die zu belegende Reihe
	 * @return die Map
	 */
	public long getSetRowMap(int row) {
		long res = 0L;

		for (int i = 0; i < 8; i++)
			res |= getMaskAt(i, row);

		return res;
	}

	public long getRowMap(int row) {
		return getSetRowMap(row);
	}

	/**
	 * Gibt eine Map zurück, die eine komplette Spalte mit 1en belegt.
	 * 
	 * @param col
	 *            die zu belegende Spalte
	 * @return die Map
	 */
	public long getColMap(int col) {
		long res = 0L;

		for (int i = 0; i < 8; i++)
			res |= getMaskAt(col, i);

		return res;
	}

	/****************************************************************/
	/********************* _____MOVE______ **************************/
	/****************************************************************/

	/**
	 * Führt einen bestimmten MOve auf dem Feld tatsäch aus und verändert die
	 * Boardsituation.
	 * 
	 * @param move
	 *            der Move.
	 */
	public void applyMove(MoveNormal move) {
		Piece piece = findPieceAt(move.getFrom());

		if (generalAttributes.getUseMemory()) {
			/* Updaten des Zobrist-Hashes!!!! */
			/* Entfernen der Figur an seiner alten Position: */
			synchronized (transposition.getClass()) {
				transposition.setTypeAt(this, piece.getColor(), piece.getType(),
						piece.getPosition().getCol(), piece.getPosition().getRow());
			}
			
		}

		unsetPieceAtPosition(piece);

		piece.setPosition(new Position(piece.getPosition().getCol()
				+ move.getDirCol(), piece.getPosition().getRow()
				+ move.getDirRow()));
		setPieceAtPosition(piece);

		if (generalAttributes.getUseMemory()) {
			/* Updaten des Zobrist-Hashes!!!! */
			/* Entfernen der Figur an seiner alten Position: */
			synchronized (transposition.getClass()) {
				transposition.setTypeAt(this, piece.getColor(), piece.getType(),
						piece.getPosition().getCol(), piece.getPosition().getRow());
			}
			
		}

	}

	/**
	 * Wendet einen konkreten Move auf ein Board an!
	 * 
	 * @param move
	 *            Der anzuwendene Move
	 */
	public void applyMove(Move move) {

		LinkedList<MoveNormal> moveList = new LinkedList<MoveNormal>();

		/*
		 * Unter Umständen ist der Special Move nicht einer, sondern gleich
		 * mehrere.
		 */
		switch (move.getMoveType()) {
		case Constants.MoveType.NORMAL:
			moveList.add((MoveNormal) move);
			break;
		case Constants.MoveType.SPECIAL:
			moveList.addAll(((MoveSpecial) move).getAllMoves());
			break;
		}

		/* Wendet jeden einzelnen der Züge nacheinander an. */
		for (MoveNormal normalMove : moveList) {
			applyMove(normalMove);
		}

	}

	/**
	 * Lässt den Move ausführen und entfernt u.U. Figuren von Traps, die
	 * gefangen genommen wurde.
	 * 
	 * @param move
	 */
	public void applyMoveAndRemovePieces(Move move) {

		applyMove(move);
		/*
		 * löscht Figuren, die auf Traps gesetzt werden und keine eigenen
		 * Figuren neben sich haben. Updaten des Zobrist-Hash!
		 */
		removePiecesFromTraps();

	}

	/**
	 * Erzeugt einen String mit der grafischen Darstellung des Spielfeldes. Alle
	 * Figuren beider Parteien werden abgebildet.
	 */
	public String toString() {
		String output = "\n +-----------------+\n   a b c d e f g h";
		String tmpString = "";
		int colnum = 1;
		char sign = ' ';
		boolean found = false;

		/* Alle Zeichen durch... */
		for (int i = 0; i < 64; i++) {

			found = false;

			/* Vorne anhängen */
			if ((i % 8 == 0)) {
				if (i == 0) {

				} else {
					output = "\n" + colnum++ + "| " + tmpString + "|" + output;
					tmpString = "";
				}
			}

			/* Schleife über alle Figuren... */
			for (int j = 0; j < this.bitboard.length; j++) {

				if (found)
					break;

				/* Schleife über alle Farben... */
				for (int k = 0; k < 2; k++) {
					long tmp = getTypeBitmap(k, j);

					if (found)
						break;

					/* Wenn das Feld belegt ist */
					if ((getMaskAt(i % 8, i / 8) & tmp) != 0) {
						switch (j) {
						case Constants.Type.ELEPHANT:
							sign = (k == Constants.Color.SILVER) ? 'e' : 'E';
							break;
						case Constants.Type.CAMEL:
							sign = (k == Constants.Color.SILVER) ? 'm' : 'M';
							break;
						case Constants.Type.HORSE:
							sign = (k == Constants.Color.SILVER) ? 'h' : 'H';
							break;
						case Constants.Type.DOG:
							sign = (k == Constants.Color.SILVER) ? 'd' : 'D';
							break;
						case Constants.Type.CAT:
							sign = (k == Constants.Color.SILVER) ? 'c' : 'C';
							break;
						case Constants.Type.RABBIT:
							sign = (k == Constants.Color.SILVER) ? 'r' : 'R';
							break;
						default:
							sign = 'x';
						}

						found = true;

					} else {
						sign = ' ';
					}
				}
			}

			tmpString += sign + " ";

		}

		output = " +-----------------+" + "\n" + colnum++ + "| " + tmpString
				+ "|" + output + "\n";

		return output;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bitboard);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bitboard other = (Bitboard) obj;
		if (!Arrays.deepEquals(bitboard, other.bitboard))
			return false;
		
		if (other.hashCode() == this.hashCode()) {
			return true;
		} else {
			return false;
		}
		
	}

}
