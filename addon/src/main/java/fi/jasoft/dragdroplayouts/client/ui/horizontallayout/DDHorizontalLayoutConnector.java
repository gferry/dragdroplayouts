/*
 * Copyright 2014 John Ahlroos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package fi.jasoft.dragdroplayouts.client.ui.horizontallayout;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.orderedlayout.HorizontalLayoutConnector;
import com.vaadin.shared.ui.Connect;

import fi.jasoft.dragdroplayouts.DDHorizontalLayout;
import fi.jasoft.dragdroplayouts.client.VDragFilter;
import fi.jasoft.dragdroplayouts.client.ui.VDragDropUtil;
import fi.jasoft.dragdroplayouts.client.ui.interfaces.VHasDragFilter;

@Connect(DDHorizontalLayout.class)
public class DDHorizontalLayoutConnector extends HorizontalLayoutConnector implements Paintable,
    VHasDragFilter {

  @Override
  public VDDHorizontalLayout getWidget() {
    return (VDDHorizontalLayout) super.getWidget();
  }

  @Override
  public DDHorizontalLayoutState getState() {
    return (DDHorizontalLayoutState) super.getState();
  }

  @Override
  public void init() {
    super.init();
    VDragDropUtil.listenToStateChangeEvents(this, getWidget());
  }

  public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
    VDragDropUtil.updateDropHandlerFromUIDL(uidl, this, new VDDHorizontalLayoutDropHandler(this));
  }

  @Override
  public VDragFilter getDragFilter() {
    return getWidget().getDragFilter();
  }

  @Override
  public void setDragFilter(VDragFilter filter) {
    getWidget().setDragFilter(filter);
  }
}
