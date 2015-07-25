package engine;

import java.util.LinkedList;

import board.Bitboard;
import board.Move;

public class ParallelAlphaBetaWrapper implements Runnable {

	/* Instanz einer Alpha-Beta-Klasse */
	private AlphaBeta alphaBeta = null;

	/* Argumente für Alpha Beta */
	private Bitboard board;
	private int depth;
	private boolean maximizingPlayer;
	private int color;
	private int moveNr;
	private int alpha;
	private int beta;
	private boolean useMemory;
	private boolean moveOrdering;
	private boolean normalSearch;
	private int maxSearchDepth;
	private boolean isQuiescence;
	private boolean isPVNode;
	private boolean isExtended;

	/* Ergebnis der Rekursion */
	private LinkedList<Move> resultList = null;
	
	private Thread ownThread;
	
	private Move playedMove;

	/**
	 * Setzen aller Parameter, die der Alpha-Beta-Instanz später übergeben
	 * werden sollen.
	 */
	public void setAlphaBetaRunParams(AlphaBeta alphaBeta, Move move, Bitboard board,
			int depth, boolean maximizingPlayer, int color, int moveNr,
			int alpha, int beta, boolean useMemory, boolean moveOrdering,
			boolean normalSearch, int maxSearchDepth, boolean isQuiescence,
			boolean isPVNode, boolean isExtended) {
		this.alphaBeta = alphaBeta;
		this.playedMove = move;
		this.board = board;
		this.depth = depth;
		this.maximizingPlayer = maximizingPlayer;
		this.color = color;
		this.moveNr = moveNr;
		this.alpha = alpha;
		this.beta = beta;
		this.useMemory = useMemory;
		this.moveOrdering = moveOrdering;
		this.normalSearch = normalSearch;
		this.maxSearchDepth = maxSearchDepth;
		this.isQuiescence = isQuiescence;
		this.isPVNode = isPVNode;
		this.isExtended = isExtended;
	}
	
	/**
	 * Gibt das Ergebnis der parallelen Berechnung zurück.
	 */
	public LinkedList<Move> getResultList () {
		return resultList;
	}
	
	public void newThread () {
		this.ownThread = new Thread(this);
	}
	
	public Thread getThread () {
		return this.ownThread;
	}
	
	public Move getMove () {
		return this.playedMove;
	}
	
	public LinkedList<Move> runAlphaBetaNotParallel () {
		
		return resultList = alphaBeta.alphaBeta(board, depth, maximizingPlayer, color,
				moveNr, alpha, beta, useMemory, moveOrdering, normalSearch,
				maxSearchDepth, isQuiescence, isPVNode, isExtended);
		
	}

	public void run() {
		
		/* Parallele Suche */
		resultList = alphaBeta.alphaBeta(board, depth, maximizingPlayer, color,
				moveNr, alpha, beta, useMemory, moveOrdering, normalSearch,
				maxSearchDepth, isQuiescence, false, isExtended);

	}

}
