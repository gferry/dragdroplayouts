/*
 * Copyright 2011 John Ahlroos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.incubator.dragdroplayouts.drophandlers;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.incubator.dragdroplayouts.DDAbsoluteLayout;
import com.vaadin.incubator.dragdroplayouts.DDGridLayout;
import com.vaadin.incubator.dragdroplayouts.DDGridLayout.GridLayoutTargetDetails;
import com.vaadin.incubator.dragdroplayouts.events.LayoutBoundTransferable;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.ui.AbsoluteLayout.ComponentPosition;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

/**
 * A default drop handler for GridLayout
 */
@SuppressWarnings("serial")
public class DefaultGridLayoutDropHandler implements DropHandler {

    private Alignment dropAlignment;

    /**
     * Constructor
     */
    public DefaultGridLayoutDropHandler() {
        // Default
    }

    /**
     * Constructor
     * 
     * @param dropCellAlignment
     *            The cell alignment of the component after it has been dropped
     */
    public DefaultGridLayoutDropHandler(Alignment dropCellAlignment) {
        this.dropAlignment = dropCellAlignment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.DragAndDropEvent
     * )
     */
    public void drop(DragAndDropEvent event) {
        GridLayoutTargetDetails details = (GridLayoutTargetDetails) event
                .getTargetDetails();

        DDGridLayout layout = (DDGridLayout) details.getTarget();
        Component source = event.getTransferable().getSourceComponent();

        int row = details.getOverRow();
        int column = details.getOverColumn();

        Component comp = null;
        if (layout == source) {
            // Component re-ordering
            LayoutBoundTransferable transferable = (LayoutBoundTransferable) event
                    .getTransferable();
            comp = transferable.getComponent();
            layout.removeComponent(comp);

        } else if (event.getTransferable() instanceof LayoutBoundTransferable) {
            // Dragged from another layout
            LayoutBoundTransferable transferable = (LayoutBoundTransferable) event
                    .getTransferable();

            comp = transferable.getComponent();

            if (comp == layout) {
                // Dropping myself on myself, if parent is absolute layout then
                // move
                if (comp.getParent() instanceof DDAbsoluteLayout) {
                    MouseEventDetails mouseDown = transferable
                            .getMouseDownEvent();
                    MouseEventDetails mouseUp = details.getMouseEvent();
                    int movex = mouseUp.getClientX() - mouseDown.getClientX();
                    int movey = mouseUp.getClientY() - mouseDown.getClientY();

                    DDAbsoluteLayout parent = (DDAbsoluteLayout) comp
                            .getParent();
                    ComponentPosition position = parent.getPosition(comp);

                    float x = position.getLeftValue() + movex;
                    float y = position.getTopValue() + movey;
                    position.setLeft(x, Sizeable.UNITS_PIXELS);
                    position.setTop(y, Sizeable.UNITS_PIXELS);

                    return;
                }

            } else {

                // Check that we are not dragging an outer layout into an inner
                // layout
                Component parent = layout.getParent();
                while (parent != null) {
                    if (parent == comp) {
                        return;
                    }
                    parent = parent.getParent();
                }

                // Remove component from its source
                if (source instanceof ComponentContainer) {
                    ComponentContainer sourceLayout = (ComponentContainer) source;
                    sourceLayout.removeComponent(comp);
                }
            }

        } else {
            // Cannot add component, aborting..
            return;
        }

        // If no components exist in the grid, then just add the
        // component
        if (!layout.getComponentIterator().hasNext()) {
            layout.addComponent(comp, column, row);
            return;
        }

        // If component was dropped on top of another component, abort
        if (layout.getComponent(column, row) != null) {
            return;
        }

        // Add the component
        layout.addComponent(comp, column, row);

        // Add component alignment if given
        if (dropAlignment != null) {
            layout.setComponentAlignment(comp, dropAlignment);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
     */
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }

}
