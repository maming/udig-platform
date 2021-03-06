/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Axios BSD
 * License v1.0 (http://udig.refractions.net/files/asd3-v10.html).
 */
package eu.udig.tools.parallel.internal.behaviour;

//import es.axios.udig.ui.editingtools.precisionparallels.internal.ParallelContext;
import eu.udig.tools.parallel.internal.ParallelContext;
import eu.udig.tools.parallel.internal.PrecisionToolsUtil;
import eu.udig.tools.parallel.internal.command.SetReferenceFeatureCommand;
//import es.axios.udig.ui.editingtools.precisionparallels.internal.command.SetReferenceFeatureCommand;
//import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsUtil;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;

/**
 * The user select the reference line.
 * <p>
 * Requirements:
 * <ul>
 * <li>state==MODIFYING or NONE</li>
 * <li>event type == RELEASED</li>
 * <li>button1 must be the button that was released</li>
 * <li>Mouse over a geometry</li>
 * </ul>
 * </p>
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class SetReferenceFeatureBehaviour implements EventBehaviour {

	ParallelContext	parallelContext	= null;

	public SetReferenceFeatureBehaviour(ParallelContext parallelContext) {

		this.parallelContext = parallelContext;
	}

	public UndoableMapCommand getCommand(EditToolHandler handler, MapMouseEvent e, EventType eventType) {

		if (!isValid(handler, e, eventType)) {
			throw new IllegalArgumentException("Behaviour is not valid for the current state"); //$NON-NLS-1$
		}

		SetReferenceFeatureCommand cmd = new SetReferenceFeatureCommand(parallelContext, handler, e);
		return cmd;
	}

	public void handleError(EditToolHandler handler, Throwable error, UndoableMapCommand command) {

		EditPlugin.log("", error); //$NON-NLS-1$
	}

	public boolean isValid(EditToolHandler handler, MapMouseEvent e, EventType eventType) {

		boolean legalState = handler.getCurrentState() == EditState.NONE
					|| handler.getCurrentState() == EditState.MODIFYING;
		boolean releaseButtonState = eventType == EventType.RELEASED;
		boolean legalButton = e.button == MapMouseEvent.BUTTON1;

		if (!(legalState && releaseButtonState && legalButton)) {
			return false;
		}

		if (PrecisionToolsUtil.isFeatureUnderCursor(handler, e) && parallelContext.getReferenceFeature() == null) {
			return true;
		}

		return false;
	}

}
