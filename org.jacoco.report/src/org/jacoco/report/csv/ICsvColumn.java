package org.jacoco.report.csv;

import java.io.IOException;

/**
 * Column in a CSV report
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public interface ICsvColumn {

	/**
	 * Writes the contents of the column
	 * 
	 * @param writer
	 *            Writer to write column data though
	 * @throws IOException
	 *             Thrown if there is any error writing the column data
	 */
	public void writeContents(final DelimitedWriter writer) throws IOException;
}
