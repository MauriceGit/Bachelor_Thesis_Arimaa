package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import board.Constants;

import engine.EngineController;
import engine.EngineGeneralAttributes;

public class ServerCommunication {

	/* Instanz des Engine-Controllers (Thread-Control) */
	private EngineController engineController;
	/* Instanz der Klasse mit für die Engine globalen Attributen (vom Server) */
	private EngineGeneralAttributes engineGeneralAttributes;
	/* Instanz des Message-Writers */
	private MessageOutputWriter messageWriter;

	// #################################################################################################################

	public ServerCommunication(EngineGeneralAttributes engineGeneralAttributes,
			MessageOutputWriter messageWriter) {

		this.messageWriter = messageWriter;
		this.engineGeneralAttributes = engineGeneralAttributes;

		this.engineController = new EngineController(engineGeneralAttributes,
				messageWriter);
	}

	// #################################################################################################################

	/**
	 * Regelt die komplette Kommunikation über die vorgeschriebenen Protokolle
	 * mit der AEI.
	 */
	public void inputControl() {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		boolean isFirstMove = true;

		/* Herausfinden, welche Farbe wir überhaupt sind... */
		int yetisColor = Constants.Color.NONE;

		/*
		 * Schleife, die ununterbrochen versucht Befehle des Arimaa-Servers
		 * einzulesen und zu verarbeiten.
		 */
		while (true) {
			try {
				String[] messageArray;
				String initialMessage = reader.readLine();
				String firstMessage = "";
				String secondMessage = "";

				if (initialMessage == null) {
					messageWriter
							.sendMessage("log ERROR: Input Stream broken. Exit engine.");
					return;
				}

				/*
				 * Message kann in eine Sub-Message unterteilt sein im Falle
				 * 'go' 'ponder'
				 */
				messageArray = initialMessage.split(" ");

				// Log.
				messageWriter.logIncomingMessage(initialMessage);

				if (messageArray.length > 1) {
					firstMessage = messageArray[0];
					secondMessage = messageArray[1];
				}

				switch (messageArray.length) {
				case 0:
					messageWriter
							.sendMessage("log Message mit 0 Zeichen erhalten.");
					return;
				default:
					secondMessage = messageArray[1];
				case 1:
					firstMessage = messageArray[0];
				}

				/* ========= Einlesen von Befehlen ========= */

				if (firstMessage.equals("aei")) {

					messageWriter.sendMessage("protocol-version 1");
					messageWriter.sendMessage("id name BotYeti");
					messageWriter.sendMessage("id author Maurice Tollmien");
					messageWriter.sendMessage("id version 1.0");
					messageWriter.sendMessage("aeiok");

				} else if (firstMessage.equals("isready")) {

					messageWriter.sendMessage("readyok");

				} else if (firstMessage.equals("newgame")) {

					isFirstMove = true;
					yetisColor = Constants.Color.NONE;
					engineController.newGame();

				} else if (firstMessage.equals("setposition")) {

				} else if (firstMessage.equals("print")) {

					engineController.printBoard();

				} else if (firstMessage.equals("allcolormoves")) {

					engineController.printColorMoves(Integer
							.parseInt(secondMessage));

				} else if (firstMessage.equals("specialmoves")) {

					engineController.printSpecialMoveMap(
							Integer.parseInt(secondMessage),
							Integer.parseInt(messageArray[2]));

				} else if (firstMessage.equals("setoption")) {

					String name = messageArray[2];
					String value = (messageArray.length < 5) ? ""
							: messageArray[4];
					engineGeneralAttributes.setOption(name, value);

				} else if (firstMessage.equals("makemove")) {

					/* Wir sind Silber! */
					if (yetisColor == Constants.Color.NONE) {
						yetisColor = Constants.Color.SILVER;
						engineGeneralAttributes.setColorToPlay(yetisColor);
					}

					/* Alle Moves durchgehen und setzen. */
					for (int i = 1; i < messageArray.length; i++) {
						engineController.makeMove(messageArray[i]);
					}
					
					/* Registrierung der Board-Situation */
					engineController.registerBoardSituation();

					engineController.logBitboard();

				} else if (firstMessage.equals("go")) {

					if (secondMessage.equals("ponder")) {
						messageWriter
								.sendMessage("log pondering... ... ... naah, joking. Just chillin'");
					} else {
						/* Startaufstellung oder Zug? */
						if (isFirstMove) {

							/* Wir sind Gold! */
							if (yetisColor == Constants.Color.NONE) {
								yetisColor = Constants.Color.GOLD;
								engineGeneralAttributes
										.setColorToPlay(yetisColor);
							}

							if (yetisColor == Constants.Color.SILVER) {
								messageWriter
										.sendMessage("bestmove ra8 rb8 rc8 rd8 re8 rf8 rg8 rh8 da7 mb7 cc7 hd7 ee7 cf7 hg7 dh7");
							} else {
								messageWriter
										.sendMessage("bestmove Ra1 Rb1 Rc1 Rd1 Re1 Rf1 Rg1 Rh1 Da2 Hb2 Cc2 Ed2 He2 Cf2 Mg2 Dh2");
							}
							isFirstMove = false;

						} else {
							messageWriter.sendMessage("log started engine");
							engineController.go();
						}
					}

				} else if (firstMessage.equals("stop")) {

					// Stoppt die Engine und printet den BestMove direkt aus.
					engineController.stop();

				} else if (firstMessage.equals("quit")) {

					// Stoppt die Engine und printet keinen BestMove aus.
					// engineController.kill();
					engineController.interrupt();
					return;

				} else {

					messageWriter.sendMessage("log Message not recognized.");
					// return;

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

		}

	}

}
