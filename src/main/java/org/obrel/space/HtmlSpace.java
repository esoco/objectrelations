//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package org.obrel.space;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;


/********************************************************************
 * An object space implementation that converts values from another object space
 * into HTML.
 *
 * @author eso
 */
public class HtmlSpace extends RelatedObject implements ObjectSpace<String>
{
	//~ Instance fields --------------------------------------------------------

	private ObjectSpace<Object> rDataSpace;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rDataSpace The object space that provides the data to be rendered
	 *                   as HTML
	 */
	public HtmlSpace(ObjectSpace<Object> rDataSpace)
	{
		this.rDataSpace = rDataSpace;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String get(String sUrl)
	{
		Object rValue = rDataSpace.get(sUrl);

		if (rValue != null)
		{
			return renderValue(rValue);
		}
		else
		{
			throw new IllegalArgumentException("Not found: " + sUrl);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void put(String sUrl, String sValue)
	{
	}

	/***************************************
	 * Renders the relations of an object as HTML.
	 *
	 * @param  rRelatable The object to render the relations of
	 *
	 * @return An HTML string
	 */
	protected String renderRelations(Relatable rRelatable)
	{
		StringBuilder aHtml = new StringBuilder();

		rRelatable.stream()
				  .forEach(r -> aHtml.append(renderSimpleValue(r.getTarget())));

		return aHtml.toString();
	}

	/***************************************
	 * Renders a value with an HTML representation.
	 *
	 * @param  rValue The value to map
	 *
	 * @return The HTML to display for the value
	 */
	protected String renderValue(Object rValue)
	{
		String sHtml =
			"<html>\n" +
			"  <head>\n" +
			"    <title>Title</title>\n" +
			"  </head>\n" +
			"  <body>\n%s" +
			"  </body>\n" +
			"</html>";

		String sBody;

		if (rValue instanceof Relatable)
		{
			sBody = renderRelations((RelatedObject) rValue);
		}
		else
		{
			sBody = renderSimpleValue(rValue);
		}

		return String.format(sHtml, sBody);
	}

	/***************************************
	 * Renders a simple value into HTML.
	 *
	 * @param  rValue The value to render
	 *
	 * @return The resulting HTML string
	 */
	private String renderSimpleValue(Object rValue)
	{
		return "";
	}
}
