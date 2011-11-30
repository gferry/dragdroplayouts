/*
 * Copyright 2011 John Ahlroos
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
package com.vaadin.incubator.dragdroplayouts.client.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.incubator.dragdroplayouts.client.ui.VLayoutDragDropMouseHandler.DragStartListener;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VCaption;
import com.vaadin.terminal.gwt.client.ui.VTabsheet;
import com.vaadin.terminal.gwt.client.ui.VTabsheetPanel;
import com.vaadin.terminal.gwt.client.ui.dd.HorizontalDropLocation;
import com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VAcceptCallback;
import com.vaadin.terminal.gwt.client.ui.dd.VDragEvent;
import com.vaadin.terminal.gwt.client.ui.dd.VDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler;

public class VDDTabSheet extends VTabsheet implements VHasDragMode,
VHasDropHandler, DragStartListener {

    public static final String CLASSNAME_NEW_TAB = "new-tab";

    public static final String CLASSNAME_NEW_TAB_LEFT = "new-tab-left";

    public static final String CLASSNAME_NEW_TAB_RIGHT = "new-tab-right";

    public static final String CLASSNAME_NEW_TAB_CENTER = "new-tab-center";

    public static final float DEFAULT_HORIZONTAL_DROP_RATIO = 0.2f;

    private LayoutDragMode dragMode = LayoutDragMode.NONE;

    private VAbstractDropHandler dropHandler;

    private float tabLeftRightDropRatio = DEFAULT_HORIZONTAL_DROP_RATIO;

    private ApplicationConnection client;

    private HandlerRegistration reg;

    private final ComplexPanel tabBar;
    private final VTabsheetPanel tabPanel;

    private final Element spacer;

    private Element currentlyEmphasised;

    private Element newTab = DOM.createDiv();

    protected boolean iframeCoversEnabled = false;

    private VDragFilter dragFilter = new VDragFilter();

    public VDDTabSheet() {
        super();

        newTab.setClassName(CLASSNAME_NEW_TAB);

        // Get the tabBar
        tabBar = (ComplexPanel)getChildren().get(0);

        // Get the content
        tabPanel = (VTabsheetPanel)getChildren().get(1);

        // Get the spacer
        Element tBody = tabBar.getElement();
        spacer = tBody.getChild(tBody.getChildCount() - 1).getChild(0)
                .getChild(0).cast();

        ddMouseHandler.addDragStartListener(this);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (reg != null) {
            reg.removeHandler();
            reg = null;
        }
        setIframeCoversEnabled(false);
    }

    // The drag mouse handler which handles the creation of the transferable
    private VLayoutDragDropMouseHandler ddMouseHandler = new VLayoutDragDropMouseHandler(
            this, dragMode);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler#getDropHandler()
     */
    public VDropHandler getDropHandler() {
        return dropHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.incubator.dragdroplayouts.client.ui.VHasDragMode#getDragMode()
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
        Widget w = tabPanel.getWidget(getTabPosition(widget));
        return dragMode != LayoutDragMode.NONE && dragFilter.isDraggable(w);
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
                    return VDDTabSheet.this;
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

                    deEmphasis();

                    // Update the details
                    updateDropDetails(drag);
                    return postDropHook(drag) && super.drop(drag);
                };

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #dragOver(com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                public void dragOver(VDragEvent drag) {

                    if (drag.getElementOver() == newTab) {
                        return;
                    }

                    deEmphasis();

                    updateDropDetails(drag);

                    postOverHook(drag);

                    // Check if we are dropping on our self
                    if (VDDTabSheet.this.equals(drag.getTransferable().getData(
                            "component"))) {
                        return;
                    }

                    // Validate the drop
                    validate(new VAcceptCallback() {
                        public void accepted(VDragEvent event) {
                            emphasis(event.getElementOver(), event);
                        }
                    }, drag);
                };

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler
                 * #dragLeave(com.vaadin.terminal.gwt.client.ui.dd.VDragEvent)
                 */
                @Override
                public void dragLeave(VDragEvent drag) {
                    deEmphasis();
                    updateDropDetails(drag);
                    postLeaveHook(drag);
                };
            };
        }

        // Update the rules
        dropHandler.updateAcceptRules(childUidl);
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
    protected void updateDropDetails(VDragEvent event) {
        Element element = event.getElementOver();

        if (tabBar.getElement().isOrHasChild(element)) {
            Widget w = Util.findWidget(element, null);

            if (w == tabBar) {
                // Ove3r the spacer

                // Add index
                event.getDropDetails().put("to", tabBar.getWidgetCount() - 1);

                // Add drop location
                event.getDropDetails().put("hdetail",
                        HorizontalDropLocation.RIGHT);

            } else {

                // Add index
                event.getDropDetails().put("to", getTabPosition(w));

                // Add drop location
                HorizontalDropLocation location = VDragDropUtil
                        .getHorizontalDropLocation(element, event
                                .getCurrentGwtEvent().getClientX(),
                                tabLeftRightDropRatio);
                event.getDropDetails().put("hdetail", location);
            }

            // Add mouse event details
            MouseEventDetails details = new MouseEventDetails(
                    event.getCurrentGwtEvent(), getElement());
            event.getDropDetails().put("mouseEvent", details.serialize());
        }
    }

    /**
     * Handles drag mode changes recieved from the server
     * 
     * @param uidl
     *            The UIDL
     */
    private void handleDragModeUpdate(UIDL uidl) {
        if (uidl.hasAttribute("dragMode")) {
            LayoutDragMode[] modes = LayoutDragMode.values();
            dragMode = modes[uidl.getIntAttribute("dragMode")];
            ddMouseHandler.updateDragMode(dragMode);
            if (reg == null && dragMode != LayoutDragMode.NONE) {
                // Cover iframes if necessery
                iframeCoversEnabled = uidl.getBooleanAttribute("shims");

                // Listen to mouse down events
                reg = tabBar.addDomHandler(ddMouseHandler,
                        MouseDownEvent.getType());
            } else if (dragMode == LayoutDragMode.NONE && reg != null) {
                // Remove iframe covers
                iframeCoversEnabled = false;

                // Remove mouse down handler
                reg.removeHandler();
                reg = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.terminal.gwt.client.ui.VTabsheet#updateFromUIDL(com.vaadin
     * .terminal.gwt.client.UIDL,
     * com.vaadin.terminal.gwt.client.ApplicationConnection)
     */
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;

        for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext();) {
            final UIDL childUIDL = (UIDL) it.next();
            if (childUIDL.getTag().equals("-ac")) {
                updateDropHandler(childUIDL);
                break;
            }
        }

        UIDL modifiedUIDL = VDragDropUtil.removeDragDropCriteraFromUIDL(uidl);
        super.updateFromUIDL(modifiedUIDL, client);

        // Handles changes in dropHandler
        handleDragModeUpdate(modifiedUIDL);

        // Handle drop ratio settings
        handleCellDropRatioUpdate(modifiedUIDL);

        // Handle iframe covering
        setIframeCoversEnabled(iframeCoversEnabled);

        dragFilter.update(modifiedUIDL, client);
    }

    /**
     * Emphasisizes a container element
     * 
     * @param element
     */
    protected void emphasis(Element element, VDragEvent event) {

        boolean internalDrag = event.getTransferable().getDragSource() == this;

        if (tabBar.getElement().isOrHasChild(element)) {
            Widget w = Util.findWidget(element, null);

            if (w == tabBar && !internalDrag) {
                // Over spacer
                Element spacerContent = spacer.getChild(0).cast();
                spacerContent.appendChild(newTab);
                currentlyEmphasised = element;

            } else if (w instanceof VCaption) {
                // Over a tab
                VCaption tab = (VCaption) w;
                HorizontalDropLocation location = VDragDropUtil
                        .getHorizontalDropLocation(element, event
                                .getCurrentGwtEvent().getClientX(),
                                tabLeftRightDropRatio);

                if (location == HorizontalDropLocation.LEFT) {

                    int index = getTabPosition(w);

                    if (index == 0) {
                        currentlyEmphasised = tab.getElement();
                        currentlyEmphasised
                        .addClassName(CLASSNAME_NEW_TAB_LEFT);
                    } else {
                        Widget prevTab = tabBar.getWidget(index - 1);
                        currentlyEmphasised = prevTab.getElement();
                        currentlyEmphasised
                        .addClassName(CLASSNAME_NEW_TAB_RIGHT);
                    }

                } else if (location == HorizontalDropLocation.RIGHT) {
                    tab.getElement().addClassName(CLASSNAME_NEW_TAB_RIGHT);
                    currentlyEmphasised = tab.getElement();
                } else {
                    tab.getElement().addClassName(CLASSNAME_NEW_TAB_CENTER);
                    currentlyEmphasised = tab.getElement();
                }


            }
        }
    }

    /**
     * Removes any previous emphasis made by drag&drop
     */
    protected void deEmphasis() {
        if (currentlyEmphasised != null
                && tabBar.getElement().isOrHasChild(currentlyEmphasised)) {
            Widget w = Util.findWidget(currentlyEmphasised, null);

            currentlyEmphasised.removeClassName(CLASSNAME_NEW_TAB_LEFT);
            currentlyEmphasised.removeClassName(CLASSNAME_NEW_TAB_RIGHT);
            currentlyEmphasised.removeClassName(CLASSNAME_NEW_TAB_CENTER);

            if (w == tabBar) {
                // Over spacer
                Element spacerContent = spacer.getChild(0).cast();
                spacerContent.removeChild(newTab);
            }

            currentlyEmphasised = null;
        }
    }

    /**
     * Handles updates the the hoover zones of the tab which specifies at which
     * position a component is dropped over a tab
     * 
     * @param uidl
     *            The UIDL
     */
    private void handleCellDropRatioUpdate(UIDL uidl) {
        if (uidl.hasAttribute("hDropRatio")) {
            tabLeftRightDropRatio = uidl.getFloatAttribute("hDropRatio");
        }
    }

    /**
     * Returns the position of a tab
     * 
     * @param tab
     *            The tab in the tabbar
     * @return
     */
    public int getTabPosition(Widget tab) {
        int idx = -1;
        for (int i = 0; i < tabBar.getWidgetCount(); i++) {
            Widget w = tabBar.getWidget(i);
            if (w.getElement().isOrHasChild(tab.getElement())) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    private Set<Element> coveredIframes = new HashSet<Element>();
    private void setIframeCoversEnabled(boolean enabled) {
        if (enabled) {
            coveredIframes = VDragDropUtil.addIframeCovers(getElement());
        } else if (coveredIframes != null) {
            VDragDropUtil.removeIframeCovers(coveredIframes);
            coveredIframes = null;
        }
    }
}
