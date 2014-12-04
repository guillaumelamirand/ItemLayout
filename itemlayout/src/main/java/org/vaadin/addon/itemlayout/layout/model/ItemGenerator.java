package org.vaadin.addon.itemlayout.layout.model;

import java.io.Serializable;

import org.vaadin.addon.itemlayout.layout.AbstractItemLayout;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;

/**
 * Used to create generated components according item property defined in the underlying {@link Container}.
 * Implement this interface and pass it to {@link AbstractItemLayout#setItemGenerator(ItemGenerator)} .
 * 
 * @author Guillaume Lamirand
 */
public interface ItemGenerator extends Serializable
{

  /**
   * Called by {@link AbstractItemLayout} when a item has been added or one of its property has changed
   * 
   * @param pSource
   *          the source layout
   * @param pItemId
   *          the itemId set up in associated {@link Container} for the item to be generated
   * @return A {@link Component} that should be rendered
   */
  Component generateItem(final AbstractItemLayout pSource, final Object pItemId);

  /**
   * Called by {@link AbstractItemLayout} to know if the Item need to be generated
   * 
   * @param pSource
   *          the source layout
   * @param pItemId
   *          the itemId set up in associated {@link Container} for the item to be generated
   * @param pPropertyChanged
   *          refer to property which has changed, it can be <code>null</code> if item is a new one
   * @return A {@link Component} that should be rendered
   * @param pSource
   * @param pItemId
   * @param pPropertyChanged
   */
  boolean canBeGenerated(final AbstractItemLayout pSource, final Object pItemId, final Object pPropertyChanged);
}
