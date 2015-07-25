package board;

import java.util.LinkedList;

public class MoveSpecial extends Move {

	/*
	 * Muss aus genau 2 verschiedenen normalen Zügen bestehen. Z.B.: fremde
	 * Figur schieben und nachziehen.
	 */
	private MoveNormal firstMove;
	private MoveNormal secondMove;

	/* Konstruktor */
	public MoveSpecial(MoveNormal firstMove, MoveNormal secondMove) {
		super(Constants.MoveType.SPECIAL);
		this.firstMove = firstMove;
		this.secondMove = secondMove;
	}

	public LinkedList<MoveNormal> getAllMoves() {
		LinkedList<MoveNormal> res = new LinkedList<MoveNormal>();

		res.add(firstMove);
		res.add(secondMove);

		return res;
	}

	@Override
	public String toString() {
		return firstMove + "\n" + secondMove;
	}

}
