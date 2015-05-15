package org.vaadin.addon.itemlayout.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.addon.itemlayout.event.ItemClickEvent;
import org.vaadin.addon.itemlayout.event.ItemClickListener;
import org.vaadin.addon.itemlayout.layout.model.DefaultItemGenerator;
import org.vaadin.addon.itemlayout.layout.model.DefaultItemSetChangeEvent;
import org.vaadin.addon.itemlayout.layout.model.DefaultPropertySetChangeEvent;
import org.vaadin.addon.itemlayout.layout.model.ItemGenerator;
import org.vaadin.addon.itemlayout.widgetset.client.layout.ItemLayoutServerRpc;
import org.vaadin.addon.itemlayout.widgetset.client.layout.ItemLayoutState;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;

/**
 * Defines a layout which generate {@link Component} based on {@link Container} and its {@link Item}
 * <p>
 * Components in a {@link AbstractItemLayout} will not have their caption nor icon rendered.
 * </p>
 * 
 * @author Guillaume Lamirand
 */
public abstract class AbstractItemLayout extends AbstractLayout implements Container, Container.Viewer,
    Container.PropertySetChangeListener, Container.PropertySetChangeNotifier,
    Container.ItemSetChangeNotifier, Container.ItemSetChangeListener, Property.ValueChangeListener
{

  /**
   * Serial version id
   */
  private static final long                        serialVersionUID          = -5457410734160515489L;

  /**
   * List of property set change event listeners.
   */
  private Set<Container.PropertySetChangeListener> propertySetEventListeners = null;

  /**
   * List of item set change event listeners.
   */
  private Set<Container.ItemSetChangeListener>     itemSetEventListeners     = null;
  /**
   * Contains the registered listened properties, so we can do proper cleanup to free
   * memory.
   */
  private final Set<Property<?>>                   listenedProperties        = new HashSet<Property<?>>();
  /**
   * Contains the {@link ItemGenerator} used to handle item rendered
   */
  private ItemGenerator                            generator;

  /**
   * Select options.
   */
  protected Container                              items;
  /**
   * Contains list of listener for {@link org.vaadin.addon.itemlayout.event.ItemClickEvent}
   */
  private final List<ItemClickListener>            clickListeners            = new ArrayList<ItemClickListener>();
  /**
   * Custom layout slots containing the components.
   */
  protected List<Component>                        components                = new LinkedList<Component>();

  /**
   * Default constructor.
   * <p>
   * It will used defautl generator
   * </p>
   */
  public AbstractItemLayout()
  {
    super();
    setContainerDataSource(new IndexedContainer());
    generator = new DefaultItemGenerator();
    this.registerRpc(new ItemLayoutServerRpc()
    {
      /**
       * Serial version id
       */
      private static final long serialVersionUID = 7140970124276573377L;

      /**
       * {@inheritDoc}
       */
      @Override
      public void onSelectItem(final String pItemId)
      {
        handleClickEvent(pItemId);
      }

    });
  }

  /**
   * Notifies the component that it is connected to an application.
   * 
   * @see com.vaadin.ui.Component#attach()
   */

  @Override
  public void attach()
  {
    super.attach();

    updateItemComponents();
  }

  /**
   * Notifies the component that it is detached from the application
   * 
   * @see com.vaadin.ui.Component#detach()
   */

  @Override
  public void detach()
  {
    super.detach();
  }

  /**
   * Notifies this listener that the Property's value has changed.
   * Also listens changes in rendered items to refresh content area.
   * 
   * @see com.vaadin.data.Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
   */
  @Override
  public void valueChange(final Property.ValueChangeEvent event)
  {
    // Hack to retrieve current property id which has changed
    Object itemIdChange = null;
    Object propertyIdChange = null;
    for (final Object itemId : getItemIds())
    {
      for (final Object propId : getContainerPropertyIds())
      {
        final Property<?> p = getContainerProperty(itemId, propId);
        final Property<?> o = event.getProperty();
        if (p.equals(o))
        {
          itemIdChange = itemId;
          propertyIdChange = propId;
          break;
        }
      }
    }
    if ((itemIdChange != null) && (propertyIdChange != null))
    {
      updateItemComponent(itemIdChange, propertyIdChange);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Deprecated
  @Override
  public void requestRepaint()
  {
    markAsDirty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void markAsDirtyRecursive()
  {
    super.markAsDirtyRecursive();

    // Avoid sending a partial repaint (#8714)
    updateItemComponents();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void beforeClientResponse(final boolean initial)
  {
    super.beforeClientResponse(initial);
    getState().hasItemsClick = !clickListeners.isEmpty();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getComponentCount()
  {
    return components.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Component> iterator()
  {
    return components.iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void containerItemSetChange(final Container.ItemSetChangeEvent event)
  {
    // Notify all listeners
    fireItemSetChange();

    // The following allows for an IndexedContainer to draw only new item element
    boolean generateAll = true;
    if (event instanceof IndexedContainer.ItemSetChangeEvent)
    {
      final IndexedContainer.ItemSetChangeEvent iEvent = (IndexedContainer.ItemSetChangeEvent) event;
      final int addedItemIndex = iEvent.getAddedItemIndex();
      if (addedItemIndex != -1)
      {
        generateAll = false;
        final IndexedContainer iContentainer = (IndexedContainer) event.getContainer();
        updateItemComponent(iContentainer.getIdByIndex(addedItemIndex), null);
      }
    }
    if (generateAll)
    {
      updateItemComponents();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void containerPropertySetChange(final Container.PropertySetChangeEvent event)
  {
    firePropertySetChange();
    updateItemComponents();

  }

  protected void updateItemComponent(final Object pItemId, final Object pPropertyIdChanged)
  {
    if (!isAttached())
    {
      return;
    }

    final boolean canBeGenerated = generator.canBeGenerated(this, pItemId, pPropertyIdChanged);
    if (canBeGenerated)
    {

      // Keep old reference
      final Property<?> oldProp = getContainerProperty(pItemId, pPropertyIdChanged);
      Component oldComponent = null;
      final Set<Entry<Connector, String>> entrySet = getState().items.entrySet();
      for (final Entry<Connector, String> item : entrySet)
      {
        final Connector key = item.getKey();
        final String value = item.getValue();
        if ((item.getValue() != null) && (pItemId != null) && (value.equals(pItemId.toString())))
        {
          oldComponent = (Component) key;
          getState().items.remove(oldComponent);
          components.remove(oldComponent);
          break;
        }
      }

      // Generate new component for the current item id
      generateItemComponent(pItemId);
      listenProperty(pItemId, pPropertyIdChanged);

      // Detach old component
      if ((oldComponent != null) && (components.contains(oldComponent)))
      {
        super.removeComponent(oldComponent);
      }
      // Unregister listenner
      if ((oldProp instanceof Property.ValueChangeNotifier)
          && (listenedProperties.contains(oldProp) == false))
      {
        final Property.ValueChangeNotifier property = (ValueChangeNotifier) oldProp;
        property.removeValueChangeListener(this);
      }

      markAsDirty();
    }
  }

  protected void updateItemComponents()
  {
    if (!isAttached())
    {
      return;
    }

    // Keep old reference
    final Set<Property<?>> oldListenedProperties = listenedProperties;
    final Set<Component> oldVisibleComponents = new HashSet<Component>(components);
    getState().items.clear();
    components.clear();

    // Atatch the new ones
    for (final Object itemId : getItemIds())
    {
      generateItemComponent(itemId);
      for (final Object propId : getContainerPropertyIds())
      {
        listenProperty(itemId, propId);
      }
    }

    // Detach existing component
    unregisterPropertiesAndComponents(oldListenedProperties, oldVisibleComponents);

    markAsDirty();
  }

  protected void generateItemComponent(final Object pItemId)
  {
    final Component c = generator.generateItem(this, pItemId);
    if (c != null)
    {
      addComponent(c);
      components.add(c);
      getState().items.put(c, pItemId.toString());
    }
  }

  private void listenProperty(final Object pItemId, final Object pPropertyId)
  {
    final Property<?> prop = getContainerProperty(pItemId, pPropertyId);
    if ((prop instanceof Property.ValueChangeNotifier) && !listenedProperties.contains(prop))
    {
      ((Property.ValueChangeNotifier) prop).addValueChangeListener(this);
      listenedProperties.add(prop);
    }
  }

  /**
   * Helper method to remove listeners and maintain correct component
   * hierarchy. Detaches properties and components if those are no more
   * rendered in client.
   * 
   * @param pOldListenedProperties
   *          set of properties that where listened in last render
   * @param pOldVisibleComponents
   *          set of components that where attached in last render
   */
  private void unregisterPropertiesAndComponents(final Set<Property<?>> pOldListenedProperties,
      final Set<Component> pOldVisibleComponents)
  {
    if (pOldVisibleComponents != null)
    {
      for (final Component c : pOldVisibleComponents)
      {
        if (components.contains(c) == false)
        {
          super.removeComponent(c);
        }
      }
    }

    if (pOldListenedProperties != null)
    {
      for (final Property<?> property : pOldListenedProperties)
      {
        final Property.ValueChangeNotifier o = (ValueChangeNotifier) property;
        if (listenedProperties.contains(o) == false)
        {
          o.removeValueChangeListener(this);
        }
      }
    }
  }

  /**
   * Adds an Item click listener for the object.
   * 
   * @param pListener
   *          the listener to add
   */
  public void addItemClickListener(final ItemClickListener pListener)
  {
    clickListeners.add(pListener);
  }

  /**
   * Removes an Item click listener for the object.
   * 
   * @param pListener
   *          the listener to remove
   */
  public void removeItemClickListener(final ItemClickListener pListener)
  {
    if (clickListeners.contains(pListener))
    {
      clickListeners.remove(pListener);
    }
  }

  /**
   * Called when a item has been click
   * 
   * @param pEvent
   */
  protected void fireItemClick(final ItemClickEvent pEvent)
  {
    for (final ItemClickListener listener : clickListeners)
    {
      listener.onItemClick(pEvent);
    }
    markAsDirty();
  }

  /**
   * Getter for property selectable.
   * <p>
   * The item layout is not selectable by default.
   * </p>
   * 
   * @return the Value of property selectable.
   */
  public boolean isSelectable()
  {
    return getState().selectable;
  }

  /**
   * Setter for property selectable. Disable selectable mode will clear current selection.
   * <p>
   * The item layout is not selectable by default.
   * </p>
   * 
   * @param pSelectable
   *          the New value of property selectable.
   */
  public void setSelectable(final boolean pSelectable)
  {
    if (getState().selectable != pSelectable)
    {
      getState().selectable = pSelectable;
      sanitizeSelectedItems();
      markAsDirty();
    }
  }

  /**
   * Is the select in multiselect mode? In multiselect mode
   * 
   * @return the Value of property multiSelect.
   */
  public boolean isMultiSelectable()
  {
    return getState().multiSelectable;
  }

  /**
   * Sets the multiselect mode. Setting multiselect mode false will clean current selection.
   * 
   * @param pMultiSelect
   *          the new value of property multiSelect.
   */
  public void setMultiSelect(final boolean pMultiSelect)
  {
    if (getState().multiSelectable != pMultiSelect)
    {
      // Selection before mode change
      getState().multiSelectable = pMultiSelect;
      sanitizeSelectedItems();
      markAsDirty();
    }
  }

  /**
   * Tests if an item is selected.
   * 
   * @param pItemId
   *          the Id the of the item to be tested.
   * @return <code>true</code> if item is selected
   */
  public boolean isSelected(final Object pItemId)
  {
    boolean isSelected = false;
    if ((isSelectable()) && (pItemId != null) && (getState().selectedItems.contains(pItemId)))
    {
      isSelected = true;
    }
    return isSelected;
  }

  protected void sanitizeSelectedItems()
  {
    if ((getState().selectable == false) || (getState().multiSelectable == false))
    {
      clearSelectedItems();
    }

  }

  /**
   * Allow or disallow empty selection by the user. If the select is in
   * single-select mode, you can make an item represent the empty selection by
   * calling <code>setNullSelectionItemId()</code>. This way you can for
   * instance set an icon and caption for the null selection item.
   * 
   * @param pNullSelectionAllowed
   *          whether or not to allow empty selection
   */
  public void setNullSelectionAllowed(final boolean pNullSelectionAllowed)
  {
    if (getState().nullSelectionAllowed != pNullSelectionAllowed)
    {
      getState().nullSelectionAllowed = pNullSelectionAllowed;
      markAsDirty();
    }
  }

  /**
   * Checks if null empty selection is allowed by the user.
   * 
   * @return whether or not empty selection is allowed
   * @see #setNullSelectionAllowed(boolean)
   */
  public boolean isNullSelectionAllowed()
  {
    return getState().nullSelectionAllowed;
  }

  public void clearSelectedItems()
  {
    getState().selectedItems = new HashSet<String>();
    markAsDirty();
  }

  /**
   * Selects an item, by default it will notify all listener
   * 
   * @param pItemId
   *          the identifier of the Item to be selected.
   */
  public void selectItem(final String pItemId)
  {
    selectItem(pItemId, true);
  }

  public void selectItem(final String pItemId, final boolean pFireEvent)
  {
    if ((pItemId != null) && (isSelected(pItemId) == false))
    {
      if (isMultiSelectable() == false)
      {
        getState().selectedItems.clear();
      }
      getState().selectedItems.add(pItemId.toString());
      if (pFireEvent)
      {
        fireItemClick(new ItemClickEvent(pItemId, true));
      }
      markAsDirty();
    }
  }

  /**
   * Unselects an item, by default it will notify all listener
   * 
   * @param pItemId
   *          the identifier of the Item to be unselected.
   * @see #getNullSelectionItemId()
   * @see #setNullSelectionItemId(Object)
   */
  public void unselectItem(final String pItemId)
  {
    unselectItem(pItemId, true);
  }

  /**
   * Unselects an item.
   * 
   * @param pItemId
   *          the identifier of the Item to be unselected.
   * @param pFireEvent
   *          true to fire a {@link ItemClickEvent}
   */
  public void unselectItem(final String pItemId, final boolean pFireEvent)
  {
    if ((pItemId != null)
        && (isSelected(pItemId))
        && (((getState().selectedItems.size() == 1) && (isNullSelectionAllowed())) || (getState().selectedItems
            .size() != 1)))
    {
      getState().selectedItems.remove(pItemId);
      if (pFireEvent)
      {
        fireItemClick(new ItemClickEvent(pItemId, false));
      }
      markAsDirty();
    }
  }

  /**
   * Handles click event
   * 
   * @param item
   *          id
   *          item id selected
   */
  private void handleClickEvent(final String pItemId)
  {
    if ((isSelectable()) && (pItemId != null))
    {
      final boolean isAlreadySelected = isSelected(pItemId);
      if (isAlreadySelected)
      {
        unselectItem(pItemId);
      }
      else
      {
        selectItem(pItemId);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ItemLayoutState getState()
  {
    return (ItemLayoutState) super.getState();
  }

  /**
   * Get list of selected items
   * 
   * @return {@link Set} of items
   */
  public Set<String> getSelectedItems()
  {
    return Collections.unmodifiableSet(getState().selectedItems);
  }

  /**
   * Returns item identifiers of the items which are currently rendered on the
   * client.
   * 
   * @return items rendered
   * @see com.vaadin.ui.Select#getVisibleItemIds()
   */
  public List<Object> getVisibleItemIds()
  {
    final List<Object> visible = new LinkedList<Object>();

    final Collection<?> itemIds = getItemIds();
    final Map<Connector, String> item = getState().items;
    for (final Object itemId : itemIds)
    {
      final Set<Entry<Connector, String>> entrySet = item.entrySet();
      for (final Entry<Connector, String> entry : entrySet)
      {
        final Connector key = entry.getKey();
        if (key instanceof Component)
        {
          final Component component = (Component) key;
          if ((entry.getValue() != null) && (entry.getValue().equals(itemId)) && (component.isVisible()))
          {
            visible.add(itemId);
          }
        }

      }
    }
    return visible;
  }

  /**
   * Set the item component generator to the {@link AbstractItemLayout}.
   * 
   * @param pItemGenerator
   *          the {@link ItemGenerator} to use
   */
  public void setItemGenerator(final ItemGenerator pItemGenerator)
  {
    if (pItemGenerator == null)
    {
      throw new IllegalArgumentException("Can not add null as a ItemGenerator");
    }
    if (generator.equals(pItemGenerator) == false)
    {
      generator = pItemGenerator;
      updateItemComponents();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void replaceComponent(final Component pOldComponent, final Component pNewComponent)
  {
    throw new IllegalAccessError("Unsupported method");

  }

  /**
   * Fires the item set change event.
   */
  protected void fireItemSetChange()
  {
    if ((itemSetEventListeners != null) && !itemSetEventListeners.isEmpty())
    {
      final Container.ItemSetChangeEvent event = new DefaultItemSetChangeEvent(this);
      final Object[] listeners = itemSetEventListeners.toArray();
      for (final Object listener : listeners)
      {
        ((Container.ItemSetChangeListener) listener).containerItemSetChange(event);
      }
    }
    markAsDirty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public void addListener(final ItemSetChangeListener pListener)
  {
    addItemSetChangeListener(pListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addItemSetChangeListener(final ItemSetChangeListener pListener)
  {
    if (itemSetEventListeners == null)
    {
      itemSetEventListeners = new LinkedHashSet<Container.ItemSetChangeListener>();
    }
    itemSetEventListeners.add(pListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public void removeListener(final ItemSetChangeListener pListener)
  {
    removeItemSetChangeListener(pListener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeItemSetChangeListener(final ItemSetChangeListener pListener)
  {
    if (itemSetEventListeners != null)
    {
      itemSetEventListeners.remove(pListener);
      if (itemSetEventListeners.isEmpty())
      {
        itemSetEventListeners = null;
      }
    }
  }

  /**
   * Fires the property set change event.
   */
  protected void firePropertySetChange()
  {
    if ((propertySetEventListeners != null) && !propertySetEventListeners.isEmpty())
    {
      final Container.PropertySetChangeEvent event = new DefaultPropertySetChangeEvent(this);
      final Object[] listeners = propertySetEventListeners.toArray();
      for (final Object listener : listeners)
      {
        ((Container.PropertySetChangeListener) listener).containerPropertySetChange(event);
      }
    }
    markAsDirty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public void addListener(final PropertySetChangeListener pListener)
  {
    addPropertySetChangeListener(pListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addPropertySetChangeListener(final PropertySetChangeListener pListener)
  {
    if (propertySetEventListeners == null)
    {
      propertySetEventListeners = new LinkedHashSet<Container.PropertySetChangeListener>();
    }
    propertySetEventListeners.add(pListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public void removeListener(final PropertySetChangeListener pListener)
  {
    removePropertySetChangeListener(pListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removePropertySetChangeListener(final PropertySetChangeListener pListener)
  {
    if (propertySetEventListeners != null)
    {
      propertySetEventListeners.remove(pListener);
      if (propertySetEventListeners.isEmpty())
      {
        propertySetEventListeners = null;
      }
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContainerDataSource(final Container pNewDataSource)
  {
    Container newDataSource = pNewDataSource;
    if (newDataSource == null)
    {
      newDataSource = new IndexedContainer();
    }

    if (items != newDataSource)
    {
      // Removes listeners from the old datasource
      if (items != null)
      {
        if (items instanceof Container.ItemSetChangeNotifier)
        {
          ((Container.ItemSetChangeNotifier) items).removeItemSetChangeListener(this);
        }
        if (items instanceof Container.PropertySetChangeNotifier)
        {
          ((Container.PropertySetChangeNotifier) items).removePropertySetChangeListener(this);
        }
      }

      // Assigns new data source
      items = newDataSource;

      // Adds listeners
      if (items != null)
      {
        if (items instanceof Container.ItemSetChangeNotifier)
        {
          ((Container.ItemSetChangeNotifier) items).addItemSetChangeListener(this);
        }
        if (items instanceof Container.PropertySetChangeNotifier)
        {
          ((Container.PropertySetChangeNotifier) items).addPropertySetChangeListener(this);
        }
      }

      clearSelectedItems();

      markAsDirty();

    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Container getContainerDataSource()
  {
    return items;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Item getItem(final Object pItemId)
  {
    return items.getItem(pItemId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<?> getContainerPropertyIds()
  {
    return items.getContainerPropertyIds();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<?> getItemIds()
  {
    return items.getItemIds();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Property<?> getContainerProperty(final Object pItemId, final Object pPropertyId)
  {
    return items.getContainerProperty(pItemId, pPropertyId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<?> getType(final Object pPropertyId)
  {
    return items.getType(pPropertyId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size()
  {
    return items.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsId(final Object pItemId)
  {
    boolean contains = false;
    if (pItemId != null)
    {
      contains = items.containsId(pItemId);
    }
    return contains;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Item addItem(final Object pItemId) throws UnsupportedOperationException
  {
    final Item retval = items.addItem(pItemId);
    if ((retval != null) && !(items instanceof Container.ItemSetChangeNotifier))
    {
      fireItemSetChange();
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object addItem() throws UnsupportedOperationException
  {
    final Object retval = items.addItem();
    if ((retval != null) && !(items instanceof Container.ItemSetChangeNotifier))
    {
      fireItemSetChange();
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeItem(final Object pItemId) throws UnsupportedOperationException
  {
    unselectItem(pItemId.toString(), false);
    final boolean retval = items.removeItem(pItemId);
    if (retval && !(items instanceof Container.ItemSetChangeNotifier))
    {
      fireItemSetChange();
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addContainerProperty(final Object pPropertyId, final Class<?> pType,
      final Object pDefaultValue) throws UnsupportedOperationException
  {

    final boolean retval = items.addContainerProperty(pPropertyId, pType, pDefaultValue);
    if (retval && !(items instanceof Container.PropertySetChangeNotifier))
    {
      firePropertySetChange();
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeContainerProperty(final Object pPropertyId) throws UnsupportedOperationException
  {
    final boolean retval = items.removeContainerProperty(pPropertyId);
    if (retval && !(items instanceof Container.PropertySetChangeNotifier))
    {
      firePropertySetChange();
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeAllItems() throws UnsupportedOperationException
  {
    final boolean retval = items.removeAllItems();
    if (retval)
    {
      clearSelectedItems();
      if (!(items instanceof Container.ItemSetChangeNotifier))
      {
        fireItemSetChange();
      }
    }
    return retval;
  }

}
