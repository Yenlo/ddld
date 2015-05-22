package nl.yenlo.ddld.engines;

import java.util.List;

/**
 * The mimimum properties an article should have to be renderable.
 * 
 * @author Philipp Gayret
 *
 */
public class Article {

	private String title;
	private List<String> paragraphs;

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getParagraphs() {
		return this.paragraphs;
	}

	public void setParagraphs(List<String> paragraphs) {
		this.paragraphs = paragraphs;
	}

}
