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
import org.obrel.core.Relation;


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
	private String			    sBaseUrl;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sBaseUrl   The base URL of this space
	 * @param rDataSpace The object space that provides the data to be rendered
	 *                   as HTML
	 */
	public HtmlSpace(String sBaseUrl, ObjectSpace<Object> rDataSpace)
	{
		if (!sBaseUrl.endsWith("/"))
		{
			sBaseUrl += "/";
		}

		this.sBaseUrl   = sBaseUrl;
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
			return renderAsHtml(sUrl, rValue);
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
	 * Overridden to return the HTML representation of this complete space.
	 *
	 * @return The HTML for this space
	 */
	@Override
	public String toString()
	{
		return renderAsHtml("", rDataSpace);
	}

	/***************************************
	 * Renders a value with an HTML representation.
	 *
	 * @param  sUrl   The URL the value has been read from
	 * @param  rValue The value to map
	 *
	 * @return The HTML to display for the value
	 */
	protected String renderAsHtml(String sUrl, Object rValue)
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
			if (!sUrl.endsWith("/"))
			{
				sUrl += "/";
			}

			sBody = renderRelations(sBaseUrl + sUrl, (RelatedObject) rValue);
		}
		else
		{
			sBody = renderValue(rValue);
		}

		return String.format(sHtml, sBody);
	}

	/***************************************
	 * Renders a single relation as HTML.
	 *
	 * @param  sUrl      The parent URL of the relation
	 * @param  rRelation The relation to render
	 *
	 * @return The HTML representing the relation
	 */
	protected String renderRelation(String sUrl, Relation<?> rRelation)
	{
		Object rValue = rRelation.getTarget();
		String sHtml  = "";

		if (rValue instanceof Relatable)
		{
			String sType = rRelation.getType().getSimpleName().toLowerCase();

			sHtml =
				String.format("<a href=\"%s%s\">%s</a>", sUrl, sType, sType);
		}
		else
		{
			sHtml = renderValue(rValue);
		}

		return sHtml;
	}

	/***************************************
	 * Renders the relations of an object as HTML.
	 *
	 * @param  sUrl       The parent URL of the relations
	 * @param  rRelatable The object to render the relations of
	 *
	 * @return An HTML string
	 */
	protected String renderRelations(String sUrl, Relatable rRelatable)
	{
		StringBuilder aHtml = new StringBuilder();

		rRelatable.stream()
				  .forEach(r ->
						   aHtml.append(renderRelation(sUrl, r))
						   .append('\n'));

		return aHtml.toString();
	}

	/***************************************
	 * Renders a value into a HTML representation.
	 *
	 * @param  rValue The value to render
	 *
	 * @return The resulting HTML string
	 */
	private String renderValue(Object rValue)
	{
		return rValue.toString();
	}
}
