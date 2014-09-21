package org.vaadin.addon.itemlayout.widgetset.client.vertical;

import org.vaadin.addon.itemlayout.widgetset.client.list.ItemListWidget;

import com.google.gwt.dom.client.Style.Unit;

/**
 * @author Guillaume Lamirand
 */
public class ItemVerticalWidget extends ItemListWidget
{

  /**
   * Default constructor
   */
  public ItemVerticalWidget()
  {
    super(true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateSize()
  {
    super.updateSize();
    int elementHeight = getElement().getOffsetHeight();
    com.google.gwt.dom.client.Element parentElement = getElement().getParentElement();
    int height = getParentHeight(parentElement);
    while (((height == 0) || (((height == elementHeight)) && (parentElement != null))))
    {
      parentElement = parentElement.getParentElement();
      height = getParentHeight(parentElement);
    }
    getElement().getStyle().setHeight(height, Unit.PX);
  }

  private int getParentHeight(final com.google.gwt.dom.client.Element parentElement)
  {
    int height = 0;
    if (parentElement != null)
    {
      height = parentElement.getOffsetHeight();
      if (height > 0)
      {
        int padding = 0;
        String paddingBottom = parentElement.getStyle().getPaddingBottom();
        if ((paddingBottom != null) && ("".equals(paddingBottom) == false))
        {
          padding += Integer.valueOf(paddingBottom);
        }
        String paddingTop = parentElement.getStyle().getPaddingTop();
        if ((paddingTop != null) && ("".equals(paddingTop) == false))
        {
          padding += Integer.valueOf(paddingBottom);
        }
        if (height > padding)
        {
          height -= padding;
        }
      }
    }
    return height;
  }
}
