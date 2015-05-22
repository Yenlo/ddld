package nl.yenlo.ddld.importing.narcis;

import nl.yenlo.ddld.importing.exceptions.ImportException;

import org.apache.axiom.om.OMElement;

/**
 * Runs a full Narcis import.
 * 
 * @author Philipp Gayret
 *
 */
public class NarcisIteratorLiveTest {

	// @Test
	@SuppressWarnings("deprecation")
	public void test() throws ImportException {
		NarcisIterator iterator = new NarcisIterator("u|c1385466237050104|moai_dc|s|f");
		NarcisProcessor processor = new NarcisProcessor();
		while (iterator.hasNext()) {
			OMElement element = iterator.next();
			processor.process(element);
			System.out.println("Current resumption token: " + iterator.getResumptionToken());
		}
		System.out.println("Done.");
	}

}
