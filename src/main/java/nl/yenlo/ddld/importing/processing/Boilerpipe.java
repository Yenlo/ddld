package nl.yenlo.ddld.importing.processing;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.CanolaExtractor;
import de.l3s.boilerpipe.extractors.ExtractorBase;

/**
 * Removes boilerplate content from HTML, takes HTML and returns a String.
 * 
 * @author Philipp Gayret
 *
 */
public class Boilerpipe {

	private final ExtractorBase extractor;

	public Boilerpipe() {
		this.extractor = CanolaExtractor.getInstance();
	}

	public String getText(String string) throws BoilerpipeProcessingException {
		return this.extractor.getText(string);
	}

}
