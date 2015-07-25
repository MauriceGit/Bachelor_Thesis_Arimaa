package evaluation;

import board.Bitboard;
import engine.EngineGeneralAttributes;

/**
 * Das Prinzip der Evaluierung basiert auf dem abstrakten Grundkonzept von
 * bot_badger, aus folgendem Thread:
 * http://arimaa.com/arimaa/forum/cgi/YaBB.cgi?
 * board=devTalk;action=display;num=1213482791.
 * 
 * Dabei ist das Ziel jeder Evaluierung das goal mit dem Hasen. Der Weg dorthin
 * ist kategorisiert in die Entfernung vom Ziel in Etappen.
 * 
 * 1. Primary goal: Force a rabbit through to the end row.
 * 
 * 2. Secondary goal: Thin out the opponent force.
 * 
 * 3. Tertiary goal: Create a position that makes it likely that we will be able
 * to capture his pieces.
 * 
 * 4. Quarternary goal: Find strategic values that let us eventually fulfill the
 * tertiary goal.
 * 
 * Im Prinzip arbeiten alle Schichten mit dem lokalen Ziel, die überliegende
 * Etappe zu realisieren. Das Endresultat ist dann ein forced goal. Die
 * Bearbeitung und Umsetzung des Konzeptes ist eigenständig realisierung und
 * ausschließlich die Idee wurde übernommen.
 * 
 * @author maurice
 * 
 */
public class GoalHierarchyEvaluation extends Evaluation {

	/**
	 * globale Variablen zum Check auf die verschiedenen Etappen.
	 */
	private boolean primaryGoal = false;
	private boolean secondaryGoal = false;
	private boolean tertiaryGoal = false;
	private boolean quarternaryGoal = false;
	
	/* Konstruktor */
	public GoalHierarchyEvaluation(EngineGeneralAttributes generalAttributes) {
		super(generalAttributes);
	}
	
	

	public int evaluateBoardState(Bitboard board, boolean isQiescence) {

		return 0;
	}
}