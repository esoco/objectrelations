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

import de.esoco.lib.text.TextConvert;

import java.text.DateFormat;

import java.util.Date;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * An object space implementation that converts values from another object space
 * into HTML.
 *
 * @author eso
 */
public class HtmlSpace extends RelationSpace<String>
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * Template for a rendered HTML page. The template will be formatted with
	 * two string values: the first provides the page title, the second the body
	 * content.
	 */
	public static final RelationType<String> PAGE_TEMPLATE =
		newInitialValueType("<!DOCTYPE html>\n" +
							"<html>\n" +
							"<head>\n" +
							"<title>%s</title>\n" +
							"</head>\n" +
							"<body>\n" +
							"%s</body>\n" +
							"</html>");

	/**
	 * Template for a space-internal link on a rendered HTML page. The template
	 * will be formatted with two string values: the first provides the link
	 * URL, the second the link text.
	 */
	public static final RelationType<String> INTERNAL_LINK_TEMPLATE =
		newInitialValueType("<a href=\"%s\">%s</a>");

	/** Template for the (read-only) display of text values. */
	public static final RelationType<String> TEXT_DISPLAY_TEMPLATE =
		newInitialValueType("<b>%s</b>: %s");

	static
	{
		RelationTypes.init(HtmlSpace.class);
	}

	//~ Instance fields --------------------------------------------------------

	private ObjectSpace<?> rDataSpace;

	private String sBaseUrl;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rDataSpace The object space that provides the data to be rendered
	 *                   as HTML
	 * @param sBaseUrl   The base URL to be prepended to all space-relative URLs
	 */
	public HtmlSpace(ObjectSpace<?> rDataSpace, String sBaseUrl)
	{
		this.rDataSpace = rDataSpace;
		this.sBaseUrl   = checkUrl(sBaseUrl);
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
	 * Overridden to return the HTML representation of this space.
	 *
	 * @return The HTML for this space
	 */
	@Override
	public String toString()
	{
		return renderAsHtml("", rDataSpace);
	}

	/***************************************
	 * A builder-style method to set a certain relation and then return this
	 * instance for concatenation.
	 *
	 * @param  rType  The type of the relation to set
	 * @param  rValue The relation value
	 *
	 * @return This instance for method concatenation
	 */
	public <T> HtmlSpace with(RelationType<T> rType, T rValue)
	{
		set(rType, rValue);

		return this;
	}

	/***************************************
	 * Checks a URL for correct termination with a forward slash '/'.
	 *
	 * @param  sUrl The URL to check
	 *
	 * @return The URL, modified if necessary
	 */
	protected String checkUrl(String sUrl)
	{
		if (sUrl.length() > 0 && !sUrl.endsWith("/"))
		{
			sUrl += "/";
		}

		return sUrl;
	}

	/***************************************
	 * Returns the title for a certain page.
	 *
	 * @param  rPageObject The relatable object from which the page is rendered
	 *
	 * @return The page title
	 */
	protected String getPageTitle(Relatable rPageObject)
	{
		String sTitle = rPageObject.get(NAME);

		if (sTitle == null)
		{
			sTitle = rPageObject.getClass().getSimpleName();
		}

		return sTitle;
	}

	/***************************************
	 * Renders a value with an HTML representation.
	 *
	 * @param  sUrl    The URL the value has been read from
	 * @param  rObject The value to map
	 *
	 * @return The HTML to display for the value
	 */
	protected String renderAsHtml(String sUrl, Object rObject)
	{
		String sHtml;

		if (rObject instanceof HtmlSpace)
		{
			sHtml = rObject.toString();
		}
		else
		{
			Object sTitle;
			String sBody;

			if (rObject instanceof Relatable)
			{
				Relatable rPageObject = (Relatable) rObject;

				sTitle =
					getPageTitle(rObject == rDataSpace ? this : rPageObject);
				sBody  =
					renderRelations(sBaseUrl + checkUrl(sUrl), rPageObject);
			}
			else
			{
				sTitle = sUrl.substring(sUrl.lastIndexOf('/') + 1);
				sBody  = renderDisplayValue(rObject);
			}

			sHtml = String.format(get(PAGE_TEMPLATE), sTitle, sBody);
		}

		return sHtml;
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
		String sHtml  = null;

		if (rValue instanceof Relatable)
		{
			String sType = rRelation.getType().getSimpleName().toLowerCase();

			sHtml = String.format(get(INTERNAL_LINK_TEMPLATE), sType, sType);
		}
		else
		{
			String sLabel = rRelation.getType().getSimpleName();
			String sValue = renderDisplayValue(rValue);

			if (sValue != null)
			{
				sLabel = TextConvert.capitalize(sLabel, " ");
				sHtml  =
					String.format(get(TEXT_DISPLAY_TEMPLATE), sLabel, sValue);
			}
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

		rRelatable.getRelations(null).stream().filter(r -> r.getType() != NAME)
				  .forEach(rRelation ->
			   			{
			   				String sRelation =
			   					renderRelation(sUrl, rRelation);

			   				if (sRelation != null)
			   				{
			   					aHtml.append(sRelation).append("<br>");
			   				}
						   });

		return aHtml.toString();
	}

	/***************************************
	 * Renders a value into a HTML representation.
	 *
	 * @param  rValue The value to render
	 *
	 * @return The resulting HTML string
	 */
	private String renderDisplayValue(Object rValue)
	{
		String sHtml = null;

		if (rValue instanceof Date)
		{
			sHtml = DateFormat.getDateTimeInstance().format(rValue);
		}
		else if (rValue != null)
		{
			sHtml = rValue.toString();
		}

		return sHtml;
	}
}
