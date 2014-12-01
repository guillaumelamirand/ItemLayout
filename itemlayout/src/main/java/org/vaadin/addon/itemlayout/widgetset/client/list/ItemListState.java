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
package org.vaadin.addon.itemlayout.widgetset.client.list;

import org.vaadin.addon.itemlayout.widgetset.client.layout.ItemLayoutState;

/**
 * @author Guillaume Lamirand
 */
public class ItemListState extends ItemLayoutState
{

  /**
   * Serial version id
   */
  private static final long serialVersionUID = 2727334984288258608L;
  {
    primaryStyleName = "v-itemlayout-list";
  }
  /**
   * Scroll interval, default value is 1
   */
  public int                scrollInterval   = 1;
  /**
   * Scroller index, default value is 0
   */
  public int                scrollerIndex    = 0;
  public static String      SCROLLER_INDEX   = "scrollerIndex";
  public static String      PREV_ICON_KEY    = "itemPrevIcon";
  public static String      NEXT_ICON_KEY    = "itemNextIcon";

}
