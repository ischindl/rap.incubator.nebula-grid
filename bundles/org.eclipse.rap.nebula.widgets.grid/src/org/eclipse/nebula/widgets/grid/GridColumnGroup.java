/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Item;


/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p>
 * Instances of this class represent a column group in a grid widget.  A column group header is
 * displayed above grouped columns.  The column group can optionally be configured to expand and
 * collapse.  A column group in the expanded state shows {@code GridColumn}s whose detail property
 * is true.  A column group in the collapsed state shows {@code GridColumn}s whose summary property
 * is true.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SWT.TOGGLE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Expand, Collapse</dd>
 * </dl>
 */
public class GridColumnGroup extends Item {

  private Grid parent;
  private List<GridColumn> columns = new ArrayList<GridColumn>();
  private boolean expanded = true;

  /**
   * Constructs a new instance of this class given its parent (which must be a Grid) and a style
   * value describing its behavior and appearance.
   *
   * @param parent the parent table
   * @param style the style of the group
   * @throws IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the parent</li>
   * <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  public GridColumnGroup( Grid parent, int style ) {
    super( parent, style );
    this.parent = parent;
    parent.newColumnGroup( this );
  }

  @Override
  public void dispose() {
    super.dispose();
    if( !parent.isDisposing() ) {
      GridColumn[] oldColumns = columns.toArray( new GridColumn[ columns.size() ] );
      columns.clear();
      for( int i = 0; i < oldColumns.length; i++ ) {
        oldColumns[ i ].dispose();
      }
      parent.removeColumnGroup( this );
    }
  }

  /**
   * Returns the parent grid.
   *
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public Grid getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when an item in the receiver is expanded or collapsed
   * by sending it one of the messages defined in the <code>TreeListener</code>
   * interface.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception org.eclipse.swt.SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see TreeListener
   * @see #removeTreeListener
   */
  public void addTreeListener( TreeListener listener ) {
    checkWidget();
    TreeEvent.addListener( this, listener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when items in the receiver are expanded or collapsed.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception org.eclipse.swt.SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see TreeListener
   * @see #addTreeListener
   */
  public void removeTreeListener( TreeListener listener ) {
    checkWidget();
    TreeEvent.removeListener( this, listener );
  }

  /**
   * Returns the columns within this group.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain
   * its list of items, so modifying the array will not affect the receiver.
   * </p>
   * @return the columns
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public GridColumn[] getColumns() {
    checkWidget();
    return columns.toArray( new GridColumn[ columns.size() ] );
  }

  /**
   * Sets the expanded state of the receiver.
   *
   * @param expanded the expanded to set
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setExpanded( boolean expanded ) {
    checkWidget();
    if( this.expanded != expanded ) {
      this.expanded = expanded;
      parent.updateScrollBars();
      parent.redraw();
    }
  }

  /**
   * Returns true if the receiver is expanded, false otherwise.
   *
   * @return the expanded attribute
   * @throws org.eclipse.swt.SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public boolean getExpanded() {
    checkWidget();
    return expanded;
  }

  void newColumn( GridColumn column ) {
    columns.add( column );
  }

  void removeColumn( GridColumn column ) {
    columns.remove( column );
  }

  int getNewColumnIndex() {
    int result = -1;
    if( columns.size() != 0 ) {
      GridColumn lastCol = columns.get( columns.size() - 1 );
      result = parent.indexOf( lastCol ) + 1;
    }
    return result;
  }
}
