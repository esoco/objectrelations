package org.obrel.tool;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import org.obrel.core.ObjectSpace;
import org.obrel.core.Relation;

import static org.obrel.core.StandardRelationFilters.*;
import static org.obrel.type.PropertyTypes.ID;
import static org.obrel.type.PropertyTypes.INFO;
import static org.obrel.type.PropertyTypes.NAME;


/********************************************************************
 * Tool to visualize object relations.
 *
 * @author eso
 */
public class RelationDisplay
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * TODO: `Description`
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		Object aObject = "TEST";

		ObjectSpace.setRelation(ID, aObject, "1");
		ObjectSpace.setRelation(NAME, aObject, "Test 1");
		ObjectSpace.setRelation(INFO, aObject, "First test");

		new RelationDisplay().show(aObject);
	}

	/***************************************
	 * TODO: DOCUMENT ME!
	 *
	 * @param  rObject TODO: DOCUMENT ME!
	 * @param  x       TODO: DOCUMENT ME!
	 * @param  y       TODO: DOCUMENT ME!
	 * @param  w       TODO: DOCUMENT ME!
	 * @param  h       TODO: DOCUMENT ME!
	 * @param  bg      TODO: DOCUMENT ME!
	 *
	 * @return TODO: DOCUMENT ME!
	 */
	public DefaultGraphCell createCell(Object rObject,
									   double x,
									   double y,
									   double w,
									   double h,
									   Color  bg)
	{
		DefaultGraphCell aCell			 = new DefaultGraphCell(
											   createObjectString(rObject));
		AttributeMap     rCellAttributes = aCell.getAttributes();

		GraphConstants.setBounds(rCellAttributes,
								 new Rectangle2D.Double(x, y, w, h));
		GraphConstants.setAutoSize(rCellAttributes, true);
		GraphConstants.setInset(rCellAttributes, 3);
		GraphConstants.setResize(rCellAttributes, false);
		GraphConstants.setFont(rCellAttributes,
							   new Font("FrobisherCondensed", Font.PLAIN, 13));
		GraphConstants.setBackground(rCellAttributes, bg);
		GraphConstants.setOpaque(rCellAttributes, true);
		GraphConstants.setBorder(rCellAttributes,
								 BorderFactory.createRaisedBevelBorder());

		aCell.addPort();

		return aCell;
	}

	/***************************************
	 * TODO: DOCUMENT ME!
	 *
	 * @param rObject
	 */
	void show(Object rObject)
	{
		// Construct Model and Graph
		GraphModel			   aModel = new DefaultGraphModel();
		JGraph				   aGraph = new JGraph(aModel);
		List<DefaultGraphCell> aCells = new ArrayList<DefaultGraphCell>();
		int					   x	  = 20;
		int					   y	  = 20;
		int					   w	  = 60;
		int					   h	  = 20;

		aGraph.setEditable(false);
		aGraph.setJumpToDefaultPort(true);

		DefaultGraphCell aParent = createCell(rObject, x, y, w, h, Color.GREEN);

		aCells.add(aParent);
		x += w * 3;

		for (Relation<?, ?> rRelation :
			 ObjectSpace.getRelations(ALL_RELATIONS, rObject))
		{
			Object			 rTarget = rRelation.resolve();
			DefaultGraphCell rCell   = createCell(String.format(
													  "<html>%s</html>",
													  rTarget),
												  x,
												  y,
												  w,
												  h,
												  Color.ORANGE);

			aCells.add(rCell);
			y += h * 2;

			DefaultEdge aEdge = new DefaultEdge(rRelation.getType());

			aEdge.setSource(aParent.getChildAt(0));
			aEdge.setTarget(rCell.getChildAt(0));
			aCells.add(aEdge);

			GraphConstants.setLineEnd(aEdge.getAttributes(),
									  GraphConstants.ARROW_CLASSIC);
			GraphConstants.setEndFill(aEdge.getAttributes(), true);
		}

		aGraph.getGraphLayoutCache().insert(aCells.toArray());
		aGraph.getSelectionModel().clearSelection();

		JFrame    aFrame	  = new JFrame();
		Dimension aScreenSize = GraphicsEnvironment
								.getLocalGraphicsEnvironment()
								.getDefaultScreenDevice()
								.getDefaultConfiguration().getBounds()
								.getSize();

		aFrame.getContentPane().add(new JScrollPane(aGraph));
		aFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		aFrame.pack();
		aFrame.setSize(aFrame.getWidth() + 20, aFrame.getHeight() + 20);
		aFrame.setLocation((aScreenSize.width - aFrame.getWidth()) / 2,
						   (aScreenSize.height - aFrame.getHeight()) / 2);
		aFrame.setVisible(true);
	}

	/***************************************
	 * TODO: DOCUMENT ME!
	 *
	 * @param  rObject TODO: DOCUMENT ME!
	 *
	 * @return TODO: DOCUMENT ME!
	 */
	private String createObjectString(Object rObject)
	{
		String								 sText;
		Collection<Relation<Object, Object>> rRelations = ObjectSpace
														  .getRelations(
															  SINGLE_RELATIONS,
															  rObject);

		if (rRelations.size() > 0)
		{
			StringBuilder aText = new StringBuilder();

			aText.append(
				"<html><table width=\"100%\"><tr><th colspan=\"2\"><b>");
			aText.append(rObject.toString());
			aText.append("</b></th></tr>");
			aText.append("<tr><td colspan=\"2\"><hr></td></tr>");

			for (Relation<?, ?> rRelation : rRelations)
			{
				aText.append("<tr><td><b>");
				aText.append(rRelation.getType());
				aText.append(":</b></td><td>");
				aText.append(rRelation.resolve());
				aText.append("</td>");
			}
			aText.append("</table></html>");

			sText = aText.toString();
		}
		else
		{
			sText = rObject.toString();
		}

		return sText;
	}
}
