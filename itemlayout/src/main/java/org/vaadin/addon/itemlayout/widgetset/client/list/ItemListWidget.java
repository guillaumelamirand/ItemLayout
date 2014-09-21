package org.vaadin.addon.itemlayout.widgetset.client.list;

import org.vaadin.addon.itemlayout.widgetset.client.horizontal.ItemHorizontalConstant;
import org.vaadin.addon.itemlayout.widgetset.client.layout.ItemLayoutConstant;
import org.vaadin.addon.itemlayout.widgetset.client.model.ResourceBundle;
import org.vaadin.addon.itemlayout.widgetset.client.vertical.ItemVerticalConstant;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Guillaume Lamirand
 */
public class ItemListWidget extends ComplexPanel
{

  /**
   * The scroll previous button image
   */
  private final Image        scrollPrevButtonElement;
  /**
   * The scroll next button image
   */
  private final Image        scrollNextButtonElement;
  /**
   * The scroll previous panel layout
   */
  private final FocusPanel   prevLayout;
  /**
   * The scroll next panel layout
   */
  private final FocusPanel   nextLayout;
  /**
   * The visible elements list layout
   */
  private final FocusPanel   elemVisibleListLayout;
  /**
   * The elements list layout
   */
  private final ComplexPanel elemsListLayout;
  /**
   * True if the list is shown verticaly
   */
  private final boolean      vertical;

  /**
   * Default constructor
   */
  public ItemListWidget(final boolean pVertical)
  {
    super();
    vertical = pVertical;
    final Element div = DOM.createDiv();
    setElement(div);
    if (pVertical)
    {
      setStyleName(ItemVerticalConstant.CLASSNAME);
    }
    else
    {
      setStyleName(ItemHorizontalConstant.CLASSNAME);

    }
    // Scroll Previous widget
    prevLayout = new FocusPanel();
    prevLayout.setStyleName(ItemLayoutConstant.CLASS_SCROLL_LAYOUT);
    prevLayout.addStyleName(ItemLayoutConstant.CLASS_SCROLL_PREV);
    ImageResource imageResourcePrev = ResourceBundle.INSTANCE.horizontalPrev();
    if (pVertical)
    {
      imageResourcePrev = ResourceBundle.INSTANCE.verticalPrev();
    }
    scrollPrevButtonElement = new Image(imageResourcePrev);
    prevLayout.add(scrollPrevButtonElement);
    // Scroll Next widget
    nextLayout = new FocusPanel();
    nextLayout.setStyleName(ItemLayoutConstant.CLASS_SCROLL_LAYOUT);
    nextLayout.addStyleName(ItemLayoutConstant.CLASS_SCROLL_NEXT);
    ImageResource imageResourceNext = ResourceBundle.INSTANCE.horizontalNext();
    if (pVertical)
    {
      imageResourceNext = ResourceBundle.INSTANCE.verticalNext();
    }
    scrollNextButtonElement = new Image(imageResourceNext);
    nextLayout.add(scrollNextButtonElement);
    // Elements list layout
    elemVisibleListLayout = new FocusPanel();
    elemVisibleListLayout.setStyleName(ItemLayoutConstant.CLASS_LIST_VISIBLE);
    if (pVertical)
    {
      elemsListLayout = new VerticalPanel();
    }
    else
    {
      elemsListLayout = new HorizontalPanel();

    }
    elemVisibleListLayout.add(elemsListLayout);

    add(prevLayout, div);
    if (pVertical)
    {
      add(elemVisibleListLayout, div);
      add(nextLayout, div);
    }
    else
    {
      add(nextLayout, div);
      add(elemVisibleListLayout, div);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onLoad()
  {
    // The following code handles vertical alignement for prev and next button depending on item displayed.
    // We have to use deferred command otherwise getOffsetWidth return invalid value
    Scheduler.get().scheduleDeferred(new ScheduledCommand()
    {
      @Override
      public void execute()
      {
        if (vertical)
        {
          final int offsetWidth = elemsListLayout.getOffsetWidth();
          prevLayout.getElement().getStyle().setWidth(offsetWidth, Unit.PX);
          nextLayout.getElement().getStyle().setWidth(offsetWidth, Unit.PX);

          final int prevHeight = prevLayout.getOffsetHeight();
          elemVisibleListLayout.getElement().getStyle().setTop(prevHeight, Unit.PX);
          final int nextHeight = nextLayout.getOffsetHeight();
          elemVisibleListLayout.getElement().getStyle().setBottom(nextHeight, Unit.PX);

        }
        else
        {
          final int offsetHeight = elemsListLayout.getOffsetHeight();
          prevLayout.getElement().getStyle().setHeight(offsetHeight, Unit.PX);
          prevLayout.getElement().getStyle().setLineHeight(offsetHeight, Unit.PX);
          nextLayout.getElement().getStyle().setHeight(offsetHeight, Unit.PX);
          nextLayout.getElement().getStyle().setLineHeight(offsetHeight, Unit.PX);
        }
      }
    });
  }

  /**
   * Update the width or height of the widget
   */
  public void updateSize()
  {
    // Nothing to do by default
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(final Widget w)
  {
    elemsListLayout.add(w);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean remove(final Widget w)
  {
    return elemsListLayout.remove(w);
  }

  /**
   * Remove all widget from the container
   */
  public void removeAll()
  {
    if (elemsListLayout != null)
    {
      elemsListLayout.clear();
    }
  }

  /**
   * @return the prevLayout
   */
  public FocusPanel getPrevLayout()
  {
    return prevLayout;
  }

  /**
   * @return the nextLayout
   */
  public FocusPanel getNextLayout()
  {
    return nextLayout;
  }

  /**
   * @return the elemVisibleListLayout
   */
  public FocusPanel getElemVisibleListLayout()
  {
    return elemVisibleListLayout;
  }

  /**
   * @return the elemsListLayout
   */
  public ComplexPanel getElemsListLayout()
  {
    return elemsListLayout;
  }

}
