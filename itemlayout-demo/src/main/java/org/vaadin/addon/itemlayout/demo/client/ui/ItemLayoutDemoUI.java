/**
 * NovaForge(TM) is a web-based forge offering a Collaborative Development and
 * Project Management Environment.
 *
 * Copyright (C) 2007-2012  BULL SAS
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
package org.vaadin.addon.itemlayout.demo.client.ui;

import org.vaadin.addon.itemlayout.event.ItemClickEvent;
import org.vaadin.addon.itemlayout.event.ItemClickListener;
import org.vaadin.addon.itemlayout.grid.ItemGrid;
import org.vaadin.addon.itemlayout.horizontal.ItemHorizontal;
import org.vaadin.addon.itemlayout.layout.AbstractItemLayout;
import org.vaadin.addon.itemlayout.vertical.ItemVertical;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * *
 * 
 * @author Guillaume Lamirand
 */
@Theme("itemlayoutdemo")
public class ItemLayoutDemoUI extends UI
{

  /**
   * Serial version id
   */
  private static final long      serialVersionUID = -7764280046700991233L;

  private final IndexedContainer container        = new IndexedContainer();

  /**
   * {@inheritDoc}
   */
  @Override
  protected void init(final VaadinRequest request)
  {
    // Demo datas
    buildDefaultContainer();
    // Main layout
    final VerticalLayout layout = new VerticalLayout();
    layout.setMargin(true);
    layout.addComponent(new Label("Demo for ItemLayout addon"));
    final Component demoButtons = initActionButtons();
    layout.addComponent(demoButtons);
    final Component itemGrid = initItemGridExamples();
    layout.addComponent(itemGrid);
    final Component itemHorizontal = initItemHorizontalExamples();
    layout.addComponent(itemHorizontal);
    final Component itemVertical = initItemVerticalExamples();
    layout.addComponent(itemVertical);

    setContent(layout);
  }

  private Component initActionButtons()
  {
    final HorizontalLayout buttonsLayout = new HorizontalLayout();
    final Button addItemButton = new Button("Add an item");
    final Button removeItemButton = new Button("Remove last item");
    addItemButton.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(final ClickEvent event)
      {
        final int currentIndex = container.size();
        container.addItem(currentIndex);
        container.getContainerProperty(currentIndex, "caption").setValue("Item " + currentIndex);
        container.getContainerProperty(currentIndex, "description").setValue("Item at index " + currentIndex);
      }
    });
    removeItemButton.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(final ClickEvent event)
      {
        final int lastIndex = container.size() - 1;
        container.removeItem(lastIndex);
      }
    });
    buttonsLayout.addComponent(addItemButton);
    buttonsLayout.addComponent(removeItemButton);
    return buttonsLayout;
  }

  private Component initItemGridExamples()
  {
    // Layout to show examples
    final HorizontalLayout example = new HorizontalLayout();
    example.addComponent(new Label("Demo for ItemGrid"));

    final HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.setMargin(true);
    example.addComponent(horizontalLayout);

    final ItemGrid itemGrid = buildDefaultItemGrid();
    itemGrid.addItemClickListener(buildClickListener());

    final OptionGroup sample = buildSelectableOption();
    sample.addValueChangeListener(buildValueChangeListener(itemGrid));
    horizontalLayout.addComponent(sample);
    horizontalLayout.addComponent(itemGrid);
    return example;
  }

  private Component initItemVerticalExamples()
  {
    // Layout to show examples
    final VerticalLayout example = new VerticalLayout();
    example.setHeight(100, Unit.PERCENTAGE);
    example.addComponent(new Label("Demo for ItemVertical"));

    final HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.setHeight(100, Unit.PERCENTAGE);
    horizontalLayout.setMargin(true);
    example.addComponent(horizontalLayout);

    final ItemVertical itemVertical = buildDefaultItemVertical();
    itemVertical.addItemClickListener(buildClickListener());

    final OptionGroup sample = buildSelectableOption();
    sample.addValueChangeListener(buildValueChangeListener(itemVertical));
    horizontalLayout.addComponent(sample);
    horizontalLayout.addComponent(itemVertical);
    return example;
  }

  private Component initItemHorizontalExamples()
  {
    // Layout to show examples
    final VerticalLayout example = new VerticalLayout();
    example.addComponent(new Label("Demo for ItemHorizontal"));

    final HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.setWidth(100, Unit.PERCENTAGE);
    horizontalLayout.setMargin(true);
    example.addComponent(horizontalLayout);

    final ItemHorizontal itemHorizontal = buildDefaultItemHorizontal();
    itemHorizontal.addItemClickListener(buildClickListener());

    final OptionGroup sample = buildSelectableOption();
    sample.addValueChangeListener(buildValueChangeListener(itemHorizontal));
    horizontalLayout.addComponent(sample);
    horizontalLayout.addComponent(itemHorizontal);
    horizontalLayout.setExpandRatio(itemHorizontal, 1f);
    return example;
  }

  private ItemGrid buildDefaultItemGrid()
  {
    final ItemGrid item = new ItemGrid();
    item.setColumns(5);
    item.setContainerDataSource(container);
    return item;
  }

  private ItemHorizontal buildDefaultItemHorizontal()
  {
    final ItemHorizontal item = new ItemHorizontal();
    item.setContainerDataSource(container);
    return item;
  }

  private ItemVertical buildDefaultItemVertical()
  {
    final ItemVertical item = new ItemVertical();
    item.setContainerDataSource(container);
    return item;
  }

  private IndexedContainer buildDefaultContainer()
  {
    container.addContainerProperty("caption", String.class, null);
    container.addContainerProperty("description", String.class, null);
    for (int i = 0; i < 25; i++)
    {
      container.addItem(i);
      container.getContainerProperty(i, "caption").setValue("Item " + i);
      container.getContainerProperty(i, "description").setValue("Item at index " + i);
    }
    return container;
  }

  private OptionGroup buildSelectableOption()
  {
    final OptionGroup sample = new OptionGroup("Select a selectable mode");
    sample.addItem(1);
    sample.setItemCaption(1, "Disable");
    sample.addItem(2);
    sample.setItemCaption(2, "Single selection");
    sample.addItem(3);
    sample.setItemCaption(3, "Multi selection");
    sample.select(1);
    sample.setNullSelectionAllowed(false);
    sample.setHtmlContentAllowed(true);
    sample.setImmediate(true);
    return sample;
  }

  private ItemClickListener buildClickListener()
  {
    return new ItemClickListener()
    {
      @Override
      public void onItemClick(final ItemClickEvent pEvent)
      {
        Notification.show("Item clicked:", "" + pEvent.getItemId(), Type.TRAY_NOTIFICATION);
      }
    };
  }

  private ValueChangeListener buildValueChangeListener(final AbstractItemLayout pLayout)
  {
    return new ValueChangeListener()
    {

      @Override
      public void valueChange(final ValueChangeEvent event)
      {
        final int valueString = Integer.valueOf(event.getProperty().getValue().toString());
        if (valueString == 1)
        {
          pLayout.setSelectable(false);
        }
        else if (valueString == 2)
        {
          pLayout.setSelectable(true);
          pLayout.setMultiSelect(false);
        }
        else if (valueString == 3)
        {
          pLayout.setSelectable(true);
          pLayout.setMultiSelect(true);
        }
      }
    };
  }

}