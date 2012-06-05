/*
 * Copyright 2012 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.jasoft.dragdroplayouts.client.ui;

import java.util.Iterator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.VFormLayout;
import com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VAcceptCallback;
import com.vaadin.terminal.gwt.client.ui.dd.VDragEvent;
import com.vaadin.terminal.gwt.client.ui.dd.VDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;

import fi.jasoft.dragdroplayouts.DDFormLayout;
import fi.jasoft.dragdroplayouts.client.ui.VLayoutDragDropMouseHandler.DragStartListener;
import fi.jasoft.dragdroplayouts.client.ui.interfaces.VHasDragFilter;
import fi.jasoft.dragdroplayouts.client.ui.interfaces.VHasDragMode;
import fi.jasoft.dragdroplayouts.client.ui.util.IframeCoverUtility;

/**
 * Client side implementation for {@link DDFormLayout}
 * 
 * @author John Ahlroos / www.jasoft.fi
 * @since 0.4.0
 */
public class VDDFormLayout extends VFormLayout implements VHasDragMode,
        VHasDropHandler, DragStartListener, VHasDragFilter {

    private Element currentlyEmphasised;

    private LayoutDragMode dragMode = LayoutDragMode.NONE;

    private float cellTopBottomDropRatio = DEFAULT_VERTICAL_DROP_RATIO;

    private static final int COLUMN_CAPTION = 0;
    private static final int COLUMN_ERRORFLAG = 1;
    private static final int COLUMN_WIDGET = 2;

    public static final String OVER = "v-ddformlayout-over";

    public static final String OVER_SPACED = OVER + "-spaced";

    public static final float DEFAULT_VERTICAL_DROP_RATIO = 0.3333f;

    private VAbstractDropHandler dropHandler;

    protected boolean iframeCoversEnabled = false;

    private final VDragFilter dragFilter = new VDragFilter();

    private final IframeCoverUtility iframeCoverUtility = new IframeCoverUtility();

    private final VFormLayoutTable table;

    protected ApplicationConnection client;

    public VDDFormLayout() {
        super();
        ddMouseHandler.addDragStartListener(this);
        table = (VFormLayoutTable) getWidget();
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        ddMouseHandler.detach();
        iframeCoverUtility.setIframeCoversEnabled(false, this.getElement());
    }

    // The drag mouse handler which handles the creation of the transferable
    private final VLayoutDragDropMouseHandler ddMouseHandler = new VLayoutDragDropMouseHandler(
            this, dragMode);

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext();) {
            final UIDL childUIDL = (UIDL) it.next();
            if (childUIDL.getTag().equals("-ac")) {
                updateDropHandler(childUIDL);
                break;
            }
        }

        this.client = client;

        UIDL modifiedUIDL = VDragDropUtil.removeDragDropCriteraFromUIDL(uidl);
        super.updateFromUIDL(modifiedUIDL, client);

        // Handles changes in dropHandler
        handleDragModeUpdate(modifiedUIDL);

        // Handle drop ratio settings
        handleCellDropRatioUpdate(modifiedUIDL);

        // Iframe cover check
        iframeCoverUtility.setIframeCoversEnabled(iframeCoversEnabled,
                this.getElement());

        dragFilter.update(modifiedUIDL, client);
    }

    /**
     * Handles drag mode changes recieved from the server
     * 
     * @param uidl
     *            The UIDL
     */
    private void handleDragModeUpdate(UIDL uidl) {
        if (uidl.hasAttribute(Constants.DRAGMODE_ATTRIBUTE)) {
            LayoutDragMode[] modes = LayoutDragMode.values();
            dragMode = modes[uidl.getIntAttribute(Constants.DRAGMODE_ATTRIBUTE)];
            ddMouseHandler.updateDragMode(dragMode);
            if (dragMode != LayoutDragMode.NONE) {
                if (dragMode != LayoutDragMode.NONE) {
                    // Cover iframes if necessery
                    iframeCoversEnabled = uidl
                            .getBooleanAttribute(IframeCoverUtility.SHIM_ATTRIBUTE);

                    // Listen to mouse down events
                    ddMouseHandler.attach();

                } else if (dragMode == LayoutDragMode.NONE) {
                    // Remove iframe covers
                    iframeCoversEnabled = false;

                    // Remove mouse down handler
                    ddMouseHandler.detach();
                }
            }
        }
    }

    /**
     * Handles updates the the hoover zones of the cell which specifies at which
     * position a component is dropped over a cell
     * 
     * @param uidl
     *            The UIDL
     */
    private void handleCellDropRatioUpdate(UIDL uidl) {
        if (uidl.hasAttribute(Constants.ATTRIBUTE_VERTICAL_DROP_RATIO)) {
            cellTopBottomDropRatio = uidl
                    .getFloatAttribute(Constants.ATTRIBUTE_VERTICAL_DROP_RATIO);
        }
    }

    /**
     * Removes any applies drag and drop style applied by emphasis()
     */
    protected void deEmphasis() {
        if (currentlyEmphasised != null) {
            // Universal over style
            UIObject.setStyleName(currentlyEmphasised, OVER, false);
            UIObject.setStyleName(currentlyEmphasised, OVER_SPACED, false);

            // Vertical styles
            UIObject.setStyleName(currentlyEmphasised, OVER + "-"
                    + VerticalDropLocation.TOP.toString().toLowerCase(), false);
            UIObject.setStyleName(currentlyEmphasised, OVER + "-"
                    + VerticalDropLocation.MIDDLE.toString().toLowerCase(),
                    false);
            UIObject.setStyleName(currentlyEmphasised, OVER + "-"
                    + VerticalDropLocation.BOTTOM.toString().toLowerCase(),
                    false);

            currentlyEmphasised = null;
        }
    }

    /**
     * Returns the horizontal location within the cell when hoovering over the
     * cell. By default the cell is devided into three parts: left,center,right
     * with the ratios 10%,80%,10%;
     * 
     * @param container
     *            The widget container
     * @param event
     *            The drag event
     * @return The horizontal drop location
     */
    protected VerticalDropLocation getVerticalDropLocation(Element rowElement,
            VDragEvent event) {
        return VDragDropUtil.getVerticalDropLocation(
                (com.google.gwt.user.client.Element) rowElement,
                Util.getTouchOrMouseClientY(event.getCurrentGwtEvent()),
                cellTopBottomDropRatio);
    }

    private static boolean elementIsRow(Element e) {
        String className = e.getClassName() == null ? "" : e.getClassName();
        if (className.contains("v-formlayout-row")) {
            return true;
        }
        return false;
    }

    static Element getRowFromChildElement(Element e, Element root) {
        while (!elementIsRow(e) && e != root && e.getParentElement() != null) {
            e = e.getParentElement().cast();
        }
        return e;
    }

    /**
     * Updates the drop details while dragging. This is needed to ensure client
     * side criterias can validate the drop location.
     * 
     * @param widget
     *            The container which we are hovering over
     * @param event
     *            The drag event
     */
    protected void updateDropDetails(Widget widget, VDragEvent event) {
        /*
         * The horizontal position within the cell
         */
        event.getDropDetails().put(
                Constants.DROP_DETAIL_VERTICAL_DROP_LOCATION,
                getVerticalDropLocation(
                        VDDFormLayout.getRowFromChildElement(
                                widget.getElement(),
                                VDDFormLayout.this.getElement()), event));

        /*
         * The index over which the drag is. Can be used by a client side
         * criteria to verify that a drag is over a certain index.
         */
        event.getDropDetails().put(Constants.DROP_DETAIL_TO, "-1");
        for (int i = 0; i < table.getRowCount(); i++) {
            Widget w = table.getWidget(i, COLUMN_WIDGET);
            if (widget.equals(w)) {
                event.getDropDetails().put(Constants.DROP_DETAIL_TO, i);
            }
        }

        /*
         * Add Classname of component over the drag. This can be used by a a
         * client side criteria to verify that a drag is over a specific class
         * of component.
         */
        String className = widget.getClass().getName();
        event.getDropDetails().put(Constants.DROP_DETAIL_OVER_CLASS, className);

        // Add mouse event details
        MouseEventDetails details = new MouseEventDetails(
                event.getCurrentGwtEvent(), getElement());
        event.getDropDetails().put(Constants.DROP_DETAIL_MOUSE_EVENT,
                details.serialize());
    }

    /**
     * Empasises the drop location of the component when hovering over a
     * ĆhildComponentContainer. Passing null as the container removes any
     * previous emphasis.
     * 
     * @param container
     *            The container which we are hovering over
     * @param event
     *            The drag event
     */
    protected void emphasis(Widget widget, VDragEvent event) {

        // Remove emphasis from previous hovers
        deEmphasis();

        // Null check..
        if (widget == null) {
            return;
        }

        /*
         * Get row for widget
         */
        Element rowElement = getRowFromChildElement(widget.getElement(),
                VDDFormLayout.this.getElement());

        currentlyEmphasised = rowElement;

        if (rowElement != this.getElement()) {
            VerticalDropLocation vl = getVerticalDropLocation(rowElement, event);
            UIObject.setStyleName(rowElement, OVER + "-"
                    + vl.toString().toLowerCase(), true);
        } else {
            UIObject.setStyleName(rowElement, OVER, true);
        }
    }

    /**
     * Returns the current drag mode which determines how the drag is visualized
     */
    public LayoutDragMode getDragMode() {
        return dragMode;
    }

    /**
     * A hook for extended components to post process the the drop before it is
     * sent to the server. Useful if you don't want to override the whole drop
     * handler.
     */
    protected boolean postDropHook(VDragEvent drag) {
        // Extended classes can add content here...
        return true;
    }

    /**
     * A hook for extended components to post process the the enter event.
     * Useful if you don't want to override the whole drophandler.
     */
    protected void postEnterHook(VDragEvent drag) {
        // Extended classes can add content here...
    }

    /**
     * A hook for extended components to post process the the leave event.
     * Useful if you don't want to override the whole drophandler.
     */
    protected void postLeaveHook(VDragEvent drag) {
        // Extended classes can add content here...
    }

    /**
     * A hook for extended components to post process the the over event. Useful
     * if you don't want to override the whole drophandler.
     */
    protected void postOverHook(VDragEvent drag) {
        // Extended classes can add content here...
    }

    /**
     * Can be used to listen to drag start events, must return true for the drag
     * to commence. Return false to interrupt the drag:
     */
    public boolean dragStart(Widget widget, LayoutDragMode mode) {
        return dragMode != LayoutDragMode.NONE
                && dragFilter.isDraggable(widget);
    }

    /**
     * Creates a drop handler if one does not already exist and updates it from
     * the details received from the server.
     * 
     * @param childUidl
     *            The UIDL
     */
    protected void updateDropHandler(UIDL childUidl) {
        if (dropHandler == null) {
            dropHandler = new VAbstractDropHandler() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see com.vaadin.terminal.gwt.client.ui.dd.VDropHandler#
                 * getApplicationConnection()
                 */
                public ApplicationConnection getApplicationConnection() {
                    return client;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #getPaintable()
                 */
                @Override
                public Paintable getPaintable() {
                    return VDDFormLayout.this;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #dragAccepted
                 * (com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                protected void dragAccepted(VDragEvent drag) {
                    dragOver(drag);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #drop(com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                public boolean drop(VDragEvent drag) {

                    // Un-emphasis any selections
                    emphasis(null, null);

                    // Update the details
                    updateDropDetails(getTableRowWidgetFromDragEvent(drag),
                            drag);
                    return postDropHook(drag) && super.drop(drag);
                };

                private Widget getTableRowWidgetFromDragEvent(VDragEvent event) {

                    /**
                     * Find the widget of the row
                     */
                    Element e = event.getElementOver();

                    if (table.getRowCount() == 0) {
                        /*
                         * Empty layout
                         */
                        return VDDFormLayout.this;
                    }

                    /**
                     * Check if element is inside one of the table widgets
                     */
                    for (int i = 0; i < table.getRowCount(); i++) {
                        Element caption = table.getWidget(i, COLUMN_CAPTION)
                                .getElement();
                        Element error = table.getWidget(i, COLUMN_ERRORFLAG)
                                .getElement();
                        Element widget = table.getWidget(i, COLUMN_WIDGET)
                                .getElement();
                        if (caption.isOrHasChild(e) || error.isOrHasChild(e)
                                || widget.isOrHasChild(e)) {
                            return table.getWidget(i, COLUMN_WIDGET);
                        }
                    }

                    /*
                     * Is the element a element outside the row structure but
                     * inside the layout
                     */
                    Element rowElement = getRowFromChildElement(e,
                            VDDFormLayout.this.getElement());
                    if (rowElement != null) {
                        Element tableElement = rowElement.getParentElement();
                        for (int i = 0; i < tableElement.getChildCount(); i++) {
                            Element r = tableElement.getChild(i).cast();
                            if (r.equals(rowElement)) {
                                return table.getWidget(i, COLUMN_WIDGET);
                            }
                        }
                    }

                    /*
                     * Element was not found in rows so defaulting to the form
                     * layout instead
                     */
                    return VDDFormLayout.this;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #dragOver(com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                public void dragOver(VDragEvent drag) {

                    // Remove any emphasis
                    emphasis(null, null);

                    // Update the drop details so we can validate the drop
                    Widget c = getTableRowWidgetFromDragEvent(drag);
                    if (c != null) {
                        updateDropDetails(c, drag);
                    } else {
                        updateDropDetails(VDDFormLayout.this, drag);
                    }

                    postOverHook(drag);

                    // Validate the drop
                    validate(new VAcceptCallback() {
                        public void accepted(VDragEvent event) {
                            Widget c = getTableRowWidgetFromDragEvent(event);
                            if (c != null) {
                                emphasis(c, event);
                            } else {
                                emphasis(VDDFormLayout.this, event);
                            }
                        }
                    }, drag);
                };

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #dragEnter(com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                public void dragEnter(VDragEvent drag) {
                    emphasis(null, null);

                    Widget c = getTableRowWidgetFromDragEvent(drag);
                    if (c != null) {
                        updateDropDetails(c, drag);
                    } else {
                        updateDropDetails(VDDFormLayout.this, drag);
                    }
                    super.dragEnter(drag);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #dragLeave(com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                public void dragLeave(VDragEvent drag) {
                    emphasis(null, drag);
                    postLeaveHook(drag);
                };
            };
        }

        // Update the rules
        dropHandler.updateAcceptRules(childUidl);
    }

    /**
     * Get the drop handler attached to the Layout
     */
    public VDropHandler getDropHandler() {
        return dropHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fi.jasoft.dragdroplayouts.client.ui.interfaces.VHasDragFilter#getDragFilter
     * ()
     */
    public VDragFilter getDragFilter() {
        return dragFilter;
    }
}
