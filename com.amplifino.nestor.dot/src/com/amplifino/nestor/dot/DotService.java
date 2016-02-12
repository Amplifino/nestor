package com.amplifino.nestor.dot;


/**
 * converts dot sources to output
 * requires that the dot program is available on the system PATH
 *
 */
public interface DotService {

	 /**
	 * converts the source to a svg
	 * @param source
	 * @return graphical representation of source as svg
	 */
	byte[] toSvg(String source);
			
	 /**
	 * converts the source to png
	 * @param source
	 * @return graphical representation of source as png
	 */
	byte[] toPng(String source);
	 
	 /**
	 * reduces the source by removing transitive dependencies
	 * @param source
	 * @return the reduced source
	 */
	String tred(String source);
}
