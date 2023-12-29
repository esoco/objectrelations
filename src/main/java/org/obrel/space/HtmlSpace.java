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
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.text.DateFormat;
import java.util.Date;

import static org.obrel.core.RelationTypes.newInitialValueType;
import static org.obrel.type.StandardTypes.NAME;

/**
 * An object space implementation that converts values from another object space
 * into HTML.
 *
 * @author eso
 */
public class HtmlSpace extends RelationSpace<String> {

	/**
	 * Template for a rendered HTML page. The template will be formatted with
	 * two string values: the first provides the page title, the second the
	 * body
	 * content.
	 */
	public static final RelationType<String> PAGE_TEMPLATE =
		newInitialValueType("<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" +
			"<title>%s</title>\n" + "</head>\n" + "<body>\n" + "%s</body>\n" +
			"</html>");

	/**
	 * Template for a space-internal link on a rendered HTML page. The template
	 * will be formatted with two string values: the first provides the link
	 * URL, the second the link text.
	 */
	public static final RelationType<String> INTERNAL_LINK_TEMPLATE =
		newInitialValueType("<a href=\"%s\">%s</a>");

	/**
	 * Template for the (read-only) display of text values.
	 */
	public static final RelationType<String> TEXT_DISPLAY_TEMPLATE =
		newInitialValueType("<b>%s</b>: %s");

	static {
		RelationTypes.init(HtmlSpace.class);
	}

	private final ObjectSpace<?> dataSpace;

	private final String baseUrl;

	/**
	 * Creates a new instance.
	 *
	 * @param dataSpace The object space that provides the data to be rendered
	 *                  as HTML
	 * @param baseUrl   The base URL to be prepended to all space-relative URLs
	 */
	public HtmlSpace(ObjectSpace<?> dataSpace, String baseUrl) {
		this.dataSpace = dataSpace;
		this.baseUrl = checkUrl(baseUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String get(String url) {
		Object value = dataSpace.get(url);

		if (value != null) {
			return renderAsHtml(url, value);
		} else {
			throw new IllegalArgumentException("Not found: " + url);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String url, String value) {
	}

	/**
	 * Overridden to return the HTML representation of this space.
	 *
	 * @return The HTML for this space
	 */
	@Override
	public String toString() {
		return renderAsHtml("", dataSpace);
	}

	/**
	 * A builder-style method to set a certain relation and then return this
	 * instance for concatenation.
	 *
	 * @param type  The type of the relation to set
	 * @param value The relation value
	 * @return This instance for method concatenation
	 */
	public <T> HtmlSpace with(RelationType<T> type, T value) {
		set(type, value);

		return this;
	}

	/**
	 * Checks a URL for correct termination with a forward slash '/'.
	 *
	 * @param url The URL to check
	 * @return The URL, modified if necessary
	 */
	protected String checkUrl(String url) {
		if (url.length() > 0 && !url.endsWith("/")) {
			url += "/";
		}

		return url;
	}

	/**
	 * Returns the title for a certain page.
	 *
	 * @param pageObject The relatable object from which the page is rendered
	 * @return The page title
	 */
	protected String getPageTitle(Relatable pageObject) {
		String title = pageObject.get(NAME);

		if (title == null) {
			title = pageObject.getClass().getSimpleName();
		}

		return title;
	}

	/**
	 * Renders a value with an HTML representation.
	 *
	 * @param url    The URL the value has been read from
	 * @param object The value to map
	 * @return The HTML to display for the value
	 */
	protected String renderAsHtml(String url, Object object) {
		String html;

		if (object instanceof HtmlSpace) {
			html = object.toString();
		} else {
			Object title;
			String body;

			if (object instanceof Relatable) {
				Relatable pageObject = (Relatable) object;

				title = getPageTitle(object == dataSpace ? this : pageObject);
				body = renderRelations(baseUrl + checkUrl(url), pageObject);
			} else {
				title = url.substring(url.lastIndexOf('/') + 1);
				body = renderDisplayValue(object);
			}

			html = String.format(get(PAGE_TEMPLATE), title, body);
		}

		return html;
	}

	/**
	 * Renders a single relation as HTML.
	 *
	 * @param url      The parent URL of the relation
	 * @param relation The relation to render
	 * @return The HTML representing the relation
	 */
	protected String renderRelation(String url, Relation<?> relation) {
		Object value = relation.getTarget();
		String html = null;

		if (value instanceof Relatable) {
			String type = relation.getType().getSimpleName().toLowerCase();

			html = String.format(get(INTERNAL_LINK_TEMPLATE), type, type);
		} else {
			String label = relation.getType().getSimpleName();
			String displayValue = renderDisplayValue(value);

			if (displayValue != null) {
				label = TextConvert.capitalize(label, " ");
				html = String.format(get(TEXT_DISPLAY_TEMPLATE), label,
					displayValue);
			}
		}

		return html;
	}

	/**
	 * Renders the relations of an object as HTML.
	 *
	 * @param url       The parent URL of the relations
	 * @param relatable The object to render the relations of
	 * @return An HTML string
	 */
	protected String renderRelations(String url, Relatable relatable) {
		StringBuilder html = new StringBuilder();

		relatable
			.getRelations(null)
			.stream()
			.filter(r -> r.getType() != NAME)
			.forEach(relation -> {
				String rendered = renderRelation(url, relation);

				if (rendered != null) {
					html.append(rendered).append("<br>");
				}
			});

		return html.toString();
	}

	/**
	 * Renders a value into a HTML representation.
	 *
	 * @param value The value to render
	 * @return The resulting HTML string
	 */
	private String renderDisplayValue(Object value) {
		String html = null;

		if (value instanceof Date) {
			html = DateFormat.getDateTimeInstance().format(value);
		} else if (value != null) {
			html = value.toString();
		}

		return html;
	}
}
