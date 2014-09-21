/**
 * NovaForge(TM) is a web-based forge offering a Collaborative Development and 
 * Project Management Environment.
 *
 * Copyright (C) 2007-2013  BULL SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.vaadin.addon.itemlayout.widgetset.client.horizontal;

import org.vaadin.addon.itemlayout.horizontal.ItemHorizontal;
import org.vaadin.addon.itemlayout.widgetset.client.list.AbstractListLayoutConnector;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.shared.ui.Connect;

/**
 * @author Guillaume Lamirand
 */
@Connect(ItemHorizontal.class)
public class ItemHorizontalConnector extends AbstractListLayoutConnector

{

  /**
   * Serial version id
   */
  private static final long serialVersionUID = 5919752655203388362L;

  /**
   * {@inheritDoc}
   */
  @Override
  public ItemHorizontalState getState()
  {
    return (ItemHorizontalState) super.getState();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ItemHorizontalWidget getWidget()
  {
    return (ItemHorizontalWidget) super.getWidget();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isClippedElems()
  {
    return getElemsListLayout().getElement().getAbsoluteRight() > getElemVisibleListLayout().getElement()
        .getAbsoluteRight();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isElementClipped(final Widget pWidget)
  {
    final int widgetLeft = pWidget.getElement().getAbsoluteLeft();
    final int visibleElementRight = getElemVisibleListLayout().getElement().getAbsoluteRight();
    return widgetLeft > visibleElementRight;
  }

}
