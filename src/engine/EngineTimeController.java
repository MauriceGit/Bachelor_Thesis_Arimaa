package engine;

import board.Constants;
import network.MessageOutputWriter;

public class EngineTimeController implements Runnable {

	/************************************************************/
	/********************* Variablen ************************/
	/************************************************************/

	/* Instanzen auf die Engine, die kontrolliert werden soll */
	private Engine engine;
	private Thread engineThread;

	/* Attribute der Engine */
	private EngineGeneralAttributes engineGeneralAttributes;

	/* Instanz des Message-Writers */
	private MessageOutputWriter messageWriter;

	/* Instanz des eig. Threads */
	private Thread ownThread;

	private boolean isAllowedToRun;

	/****************************************************************/
	/********************* Konstruktor **************************/
	/****************************************************************/

	public EngineTimeController(Engine engine,
			EngineGeneralAttributes engineGeneralAttributes,
			MessageOutputWriter messageWriter) {
		this.engine = engine;
		this.engineGeneralAttributes = engineGeneralAttributes;
		this.messageWriter = messageWriter;
		this.ownThread = new Thread(this);
		this.isAllowedToRun = true;
	}

	@SuppressWarnings("static-access")
	public void run() {

		try {

			/* Zeit übrig? */
			int reserveTime = (engineGeneralAttributes.getColor() == Constants.Color.GOLD) ? engineGeneralAttributes
					.getGreserve() : engineGeneralAttributes.getSreserve();

			/*
			 * Wenn genug Reserve-Time da ist, dann bis ganz zum Ende rechnen
			 * lassen und einen Teil der Reserve-Zeit nutzen zum Beenden der
			 * Engine
			 */
			if (reserveTime > 10) {
				ownThread.sleep((engineGeneralAttributes.getTcmove() + (reserveTime / 4)) * 1000);
			} else {
				if (engineGeneralAttributes.getTcturntime() <= 2) {
					ownThread
							.sleep((engineGeneralAttributes.getTcmove()) * 1000);
				} else {
					ownThread
							.sleep((engineGeneralAttributes.getTcmove() - 2) * 1000);
				}
			}

			engine.setPrintBestMove(true);
			engine.stopThread();
			
			if (engineThread != null) {
				engineThread.join();
			}

		} catch (Exception e) {
			messageWriter
					.sendMessage("log TimeController had been interrupted and killed");
		}
	}

	public void stop() {
		this.isAllowedToRun = false;

	}

	public Thread getOwnThread() {
		return ownThread;
	}

	/**
	 * Da ein Thread niemals mehrfach gestartet werden kann, muss eine neue
	 * Instanz erzeugt werden. Alle Variablen, die zum Lauf relevant sind werden
	 * neu initialisiert.
	 */
	public void newThread() {
		this.ownThread = new Thread(this);
		this.isAllowedToRun = true;
	}

	/**
	 * Wenn ein neuer Thread erzeugt wird, muss die jew. aktuelle Instanz des
	 * Engine-Threads korrekt sein!
	 */
	public void setEngineThread(Thread engineThread) {
		this.engineThread = engineThread;
	}

}
