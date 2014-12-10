/**
 * NovaForge(TM) is a web-based forge offering a Collaborative Development and 
 * Project Management Environment.
 *
 * Copyright (C) 2007-2014  BULL SAS
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
package org.vaadin.addon.itemlayout.list;

import org.vaadin.addon.itemlayout.layout.AbstractItemLayout;
import org.vaadin.addon.itemlayout.widgetset.client.list.ItemListState;

import com.vaadin.server.Resource;

/**
 * @author Jeremy Casery
 */
public abstract class AbstractListLayout extends AbstractItemLayout
{

  /**
   * Serial version id
   */
  private static final long serialVersionUID = 7856191817150158454L;

  /**
   * Default constructor.
   */
  public AbstractListLayout()
  {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ItemListState getState()
  {
    return (ItemListState) super.getState();
  }

  /**
   * Get the scroll interval
   * 
   * @return the scrollInterval
   */
  public int getScrollInterval()
  {
    return getState().scrollInterval;
  }

  /**
   * Set the scroll interval
   * 
   * @param pSscrollInterval
   *          the scrollInterval to set
   */
  public void setScrollInterval(final int pSscrollInterval)
  {
    if (getState().scrollInterval != pSscrollInterval)
    {
      getState().scrollInterval = pSscrollInterval;
      markAsDirty();
    }
  }

  /**
   * Get the scroller index
   * 
   * @return the scrollerIndex
   */
  public int getScrollerIndex()
  {
    return getState().scrollerIndex;
  }

  /**
   * Set the scroller index
   * 
   * @param pScrollerIndex
   *          the scrollerIndex to set
   */
  public void setScrollerIndex(final int pScrollerIndex)
  {
    if (getState().scrollerIndex != pScrollerIndex)
    {
      getState().scrollerIndex = pScrollerIndex;
      markAsDirty();
    }
  }

  /**
   * Set custom icon to previous button
   * 
   * @param pPrevIcon
   *          The resource used for previous button
   */
  public void setPrevIcon(final Resource pPrevIcon)
  {
    setResource(ItemListState.PREV_ICON_KEY, pPrevIcon);
  }

  /**
   * Set custom icon to next button
   * 
   * @param pNextIcon
   *          The resource used for next button
   */
  public void setNextIcon(final Resource pNextIcon)
  {
    setResource(ItemListState.NEXT_ICON_KEY, pNextIcon);
  }

}
