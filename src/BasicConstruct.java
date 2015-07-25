import network.MessageOutputWriter;
import network.ServerCommunication;
import engine.EngineGeneralAttributes;
import evaluation.SimpleEvaluation;

/**
 * Hauptprogramm von dem aus alles geregelt/gestartet/berechnet wird.
 * 
 * @author maurice
 * 
 */
public class BasicConstruct {

	public static void main(String[] args) {

		// Die Instanz für den Message-Writer erstellen.
		MessageOutputWriter messageWriter = new MessageOutputWriter();

		messageWriter.startNewLog();

		// Die Instanz mit Attributen erstellen
		EngineGeneralAttributes engineGeneralAttributes = new EngineGeneralAttributes(
				messageWriter);

		engineGeneralAttributes.loadEngineConfigFromFile();

		// Die Server-Communication-Klasse erstellen.
		ServerCommunication controller = new ServerCommunication(
				engineGeneralAttributes, messageWriter);

		// Programm starten :-)
		controller.inputControl();
		
		/* debug. */
//		SimpleEvaluation eval = new SimpleEvaluation(engineGeneralAttributes);
//		eval.test();

		messageWriter.sendMessage("log YetiBot wurde korrekt beendet.");

	}

}
