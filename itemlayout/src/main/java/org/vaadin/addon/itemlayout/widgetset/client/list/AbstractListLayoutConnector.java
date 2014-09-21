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
package org.vaadin.addon.itemlayout.widgetset.client.list;

import org.vaadin.addon.itemlayout.widgetset.client.layout.AbstractItemLayoutConnector;
import org.vaadin.addon.itemlayout.widgetset.client.layout.ItemLayoutConstant;
import org.vaadin.addon.itemlayout.widgetset.client.model.ItemSlot;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;

/**
 * @author Jeremy Casery
 */
public abstract class AbstractListLayoutConnector extends AbstractItemLayoutConnector
{

  /**
   * Serial version Id
   */
  private static final long           serialVersionUID = 527577753200374050L;
  /**
   * Handler for next button click listener
   */
  private HandlerRegistration         nextButtonClickHandler;
  /**
   * Handler for previous button click listener
   */
  private HandlerRegistration         prevButtonClickHandler;

  private final ElementResizeListener resizeListener   = new ElementResizeListener()
                                                       {
                                                         @Override
                                                         public void onElementResize(
                                                             final ElementResizeEvent e)
                                                         {
                                                           getWidget().updateSize();
                                                           updateClippedElement();
                                                           updateScrollButtonsVisibility();
                                                         }

                                                       };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void init()
  {
    super.init();
    getLayoutManager().addElementResizeListener(getWidget().getElement(), resizeListener);
    showPreviousButton(false);
    showNextButton(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUnregister()
  {
    super.onUnregister();
    getLayoutManager().removeElementResizeListener(getWidget().getElement(), resizeListener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initLayout()
  {
    getWidget().removeAll();
  }

  @Override
  protected void addItemSlot(final ComponentConnector pConnector, final ItemSlot pSlot)
  {
    getWidget().add(pSlot);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onStateChanged(final StateChangeEvent pStateChangeEvent)
  {
    super.onStateChanged(pStateChangeEvent);
    getWidget().updateSize();
    final boolean hasScrollIndexChanged = pStateChangeEvent.hasPropertyChanged(ItemListState.SCROLLER_INDEX);
    if (hasScrollIndexChanged)
    {
      hideDefaultScrolledElems();
      updateClippedElement();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postItemsRendered()
  {
    addMouseWheelListener();
    updateScrollButtonsVisibility();
  }

  /**
   * Scroll to getState().scrollerIndex, i.e. hide elements with index minor to getState().scrollerIndex
   */
  protected void hideDefaultScrolledElems()
  {
    if (getState().scrollerIndex > 0)
    {
      for (int i = 0; i < getState().scrollerIndex; i++)
      {
        showWidget(getWidget(i), false);
      }
    }
  }

  /**
   * Add mousewheel listener to visible elements list layout
   */
  private void addMouseWheelListener()
  {
    getElemVisibleListLayout().addMouseWheelHandler(new MouseWheelHandler()
    {
      @Override
      public void onMouseWheel(final MouseWheelEvent event)
      {
        event.preventDefault();
        // Wheel UP
        if (event.isNorth())
        {
          scrollPrevEvent();
        }
        // Wheel DOWN
        else if (event.isSouth())
        {
          scrollNextEvent();
        }
      }
    });
  }

  /**
   * Update the prev/next button visiblity
   */
  protected void updateScrollButtonsVisibility()
  {
    showPreviousButton(isScrolledElems());
    showNextButton(isClippedElems());
  }

  private void updateClippedElement()
  {
    boolean firstClippedFound = false;
    for (int i = getState().scrollerIndex; i < getWidgetCount(); i++)
    {
      final Widget currentWidget = getWidget(i);
      if (currentWidget != null)
      {
        if (firstClippedFound)
        {
          showWidget(currentWidget, false);
        }
        else
        {
          showWidget(currentWidget, true);
          final boolean isClipped = isElementClipped(currentWidget);
          if (isClipped)
          {
            firstClippedFound = true;
            showWidget(currentWidget, false);
          }
        }

      }
    }
  }

  /**
   * Show or hide the next button
   * 
   * @param {@link boolean} pVisible, true to show, false otherwise
   */
  private void showNextButton(final boolean pVisible)
  {
    if (pVisible)
    {
      addScrollNextButtonClickListener(getNextLayout());
      getNextLayout().removeStyleName(ItemLayoutConstant.CLASS_SCROLL_HIDDEN);
    }
    else
    {
      getNextLayout().addStyleName(ItemLayoutConstant.CLASS_SCROLL_HIDDEN);
      removeScrollNextButtonClickListener();
    }
  }

  /**
   * Show or hide the previous button
   * 
   * @param {@link boolean} pVisible, true to show, false otherwise
   */
  private void showPreviousButton(final boolean pVisible)
  {
    if (pVisible)
    {
      addScrollPrevButtonClickListener(getPrevLayout());
      getPrevLayout().removeStyleName(ItemLayoutConstant.CLASS_SCROLL_HIDDEN);
    }
    else
    {
      getPrevLayout().addStyleName(ItemLayoutConstant.CLASS_SCROLL_HIDDEN);
      removeScrollPrevButtonClickListener();
    }
  }

  /**
   * Add click listener to scroll next button
   * 
   * @param {@link FocusPanel} pNextLayout, the Layout to associate the listener
   */
  private void addScrollNextButtonClickListener(final FocusPanel pNextLayout)
  {
    if (nextButtonClickHandler == null)
    {
      nextButtonClickHandler = pNextLayout.addClickHandler(new ClickHandler()
      {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(final ClickEvent event)
        {
          scrollNextEvent();
        }

      });
    }
  }

  /**
   * Remove the next button click listener
   */
  private void removeScrollNextButtonClickListener()
  {
    if (nextButtonClickHandler != null)
    {
      nextButtonClickHandler.removeHandler();
      nextButtonClickHandler = null;
    }
  }

  /**
   * Add click listener to scroll previous button
   * 
   * @param {@link FocusPanel} pPrevLayout, the Layout to associate the listener
   */
  private void addScrollPrevButtonClickListener(final FocusPanel pPrevLayout)
  {
    if (prevButtonClickHandler == null)
    {
      prevButtonClickHandler = pPrevLayout.addClickHandler(new ClickHandler()
      {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(final ClickEvent event)
        {
          scrollPrevEvent();
        }

      });
    }
  }

  /**
   * Remove the previous button click listener
   */
  private void removeScrollPrevButtonClickListener()
  {
    if (prevButtonClickHandler != null)
    {
      prevButtonClickHandler.removeHandler();
      prevButtonClickHandler = null;
    }
  }

  /**
   * Scroll <getState().scrollInterval> times to next element.
   * Called next button click or MouseWheel south
   */
  public void scrollNextEvent()
  {
    for (int i = 0; i < getState().scrollInterval; i++)
    {
      if (isClippedElems())
      {
        scrollNext();
        updateClippedElement();
        updateScrollButtonsVisibility();
      }
    }
  }

  /**
   * Scroll <getState().scrollInterval> times to previous element.
   * Called previous button click or MouseWheel north
   */
  public void scrollPrevEvent()
  {
    for (int i = 0; i < getState().scrollInterval; i++)
    {
      if (isScrolledElems())
      {
        scrollPrev();
        updateClippedElement();
        updateScrollButtonsVisibility();
      }
    }
  }

  /**
   * Scroll once to previous element
   */
  private void scrollPrev()
  {
    final int prevVisible = getPreviousVisibleElem(getState().scrollerIndex);
    if (prevVisible != -1)
    {
      final Widget newFirst = getWidget(prevVisible);
      showWidget(newFirst, true);
      getState().scrollerIndex--;
    }
  }

  /**
   * Scroll once to next element
   */
  private void scrollNext()
  {
    final int nextVisible = getNextVisibleElem(getState().scrollerIndex);
    if (nextVisible != -1)
    {
      final Widget currentFirst = getWidget(getState().scrollerIndex);
      showWidget(currentFirst, false);
      getState().scrollerIndex++;
    }
  }

  /**
   * Find the previous visible element. Returns -1 if none is found.
   * 
   * @param i
   *          the index from which you want to find the previous element
   * @return the previous element index if exist, -1 otherwise
   */
  private int getPreviousVisibleElem(final int i)
  {
    final int indexPrev = i - 1;
    final Widget prevWidget = getWidget(indexPrev);
    if (prevWidget != null)
    {
      return indexPrev;
    }
    else
    {
      return -1;
    }

  }

  /**
   * Show or hide an element
   * 
   * @param {@link Widget} pWidget, the element to show or hide
   * @param {@link boolean} pVisible, true to show, false to hide
   */
  private void showWidget(final Widget pWidget, final boolean pVisible)
  {
    if (pVisible)
    {
      pWidget.removeStyleName(ItemLayoutConstant.CLASS_SLOT_REMOVED);
    }
    else
    {
      pWidget.addStyleName(ItemLayoutConstant.CLASS_SLOT_REMOVED);
    }
  }

  /**
   * Check if there is previous elements already scrolled
   * 
   * @return true if there is elements already scrolled, false otherwise
   */
  private boolean isScrolledElems()
  {
    return getState().scrollerIndex > getFirstVisibleElem();
  }

  /**
   * Returns the index of the first visible tab
   * 
   * @return {@link int} the index of the first element
   */
  private int getFirstVisibleElem()
  {
    final int firstElemIndex = getNextVisibleElem(-1);
    if (firstElemIndex > 0)
    {
      return firstElemIndex;
    }
    else
    {
      return 0;
    }
  }

  /**
   * Find the next visible element. Returns -1 if none is found.
   * 
   * @param the
   *          index from which you want to find the next element
   * @return the next element index if exist, -1 otherwise
   */
  private int getNextVisibleElem(final int i)
  {
    final int indexNext = i + 1;
    final Widget nextWidget = getWidget(indexNext);
    if (nextWidget != null)
    {
      return indexNext;
    }
    else
    {
      return -1;
    }
  }

  /**
   * Determines if there are clipped elements or not
   * 
   * @return {@link boolean} true if there is clipped elements, false otherwise
   */
  protected abstract boolean isClippedElems();

  /**
   * Determines if given widget is clipped or not
   * 
   * @return {@link boolean} true if widget is clipped , false otherwise
   */
  protected abstract boolean isElementClipped(final Widget pWidget);

  /**
   * Get the scroll next layout
   * 
   * @return {@link FocusPanel} the scroll next layout
   */
  protected FocusPanel getNextLayout()
  {
    return getWidget().getNextLayout();
  }

  /**
   * Get the scroll previous layout
   * 
   * @return {@link FocusPanel} the scroll previous layout
   */
  protected FocusPanel getPrevLayout()
  {
    return getWidget().getPrevLayout();
  }

  /**
   * Get the elements list layout
   * 
   * @return {@link Widget} the elements list layout
   */
  protected Widget getElemsListLayout()
  {
    return getWidget().getElemsListLayout();
  }

  /**
   * Get the visible elements list layout
   * 
   * @return {@link Panel} the visible elements list layout
   */
  protected FocusPanel getElemVisibleListLayout()
  {
    return getWidget().getElemVisibleListLayout();
  }

  /**
   * Get the element at index
   * 
   * @param {@link int} pIndex, the index of element to get
   * @return {@link Widget} the widget at index pIndex if exist, null otherwise
   */
  protected Widget getWidget(final int pIndex)
  {
    if ((pIndex < 0) || (pIndex >= getWidgetCount()) || (getWidgetCount() < 1))
    {
      return null;
    }
    else
    {
      return getWidget().getElemsListLayout().getWidget(pIndex);
    }
  }

  /**
   * Get the elements count
   * 
   * @return {@link int} the number of element
   */
  protected int getWidgetCount()
  {
    return getWidget().getElemsListLayout().getWidgetCount();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ItemListWidget getWidget()
  {
    return (ItemListWidget) super.getWidget();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ItemListState getState()
  {
    return (ItemListState) super.getState();
  }
}
