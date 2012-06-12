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
package org.eclipse.nebula.widgets.grid.internal.griditemkit;

import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rwt.lifecycle.WidgetLCAUtil.renderProperty;

import java.io.IOException;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.internal.IGridAdapter;
import org.eclipse.nebula.widgets.grid.internal.IGridItemAdapter;
import org.eclipse.rwt.internal.lifecycle.JSConst;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.ProcessActionRunner;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.IWidgetColorAdapter;
import org.eclipse.swt.internal.widgets.IWidgetFontAdapter;
import org.eclipse.swt.widgets.Widget;


@SuppressWarnings("restriction")
public class GridItemLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.TreeItem";

  private static final String PROP_ITEM_COUNT = "itemCount";
  private static final String PROP_TEXTS = "texts";
  private static final String PROP_IMAGES = "images";
  private static final String PROP_CELL_BACKGROUNDS = "cellBackgrounds";
  private static final String PROP_CELL_FOREGROUNDS = "cellForegrounds";
  private static final String PROP_CELL_FONTS = "cellFonts";
  private static final String PROP_EXPANDED = "expanded";
  private static final String PROP_CHECKED = "checked";
  private static final String PROP_GRAYED = "grayed";

  private static final int ZERO = 0;

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    GridItem item = ( GridItem )widget;
    IClientObject clientObject = ClientObjectFactory.getClientObject( item );
    clientObject.create( TYPE );
    clientObject.set( "parent", WidgetUtil.getId( getParent( item ) ) );
    clientObject.set( "index", getItemIndex( item ) );
  }

  public void readData( Widget widget ) {
    GridItem item = ( GridItem )widget;
    readChecked( item );
    processTreeEvent( item, JSConst.EVENT_TREE_EXPANDED );
    processTreeEvent( item, JSConst.EVENT_TREE_COLLAPSED );
  }

  @Override
  public void preserveValues( Widget widget ) {
    GridItem item = ( GridItem )widget;
    WidgetLCAUtil.preserveCustomVariant( item );
    preserveProperty( item, PROP_ITEM_COUNT, item.getItemCount() );
    preserveProperty( item, PROP_TEXTS, getTexts( item ) );
    preserveProperty( item, PROP_IMAGES, getImages( item ) );
    WidgetLCAUtil.preserveBackground( item, getUserBackground( item ) );
    WidgetLCAUtil.preserveForeground( item, getUserForeground( item ) );
    WidgetLCAUtil.preserveFont( item, getUserFont( item ) );
    preserveProperty( item, PROP_CELL_BACKGROUNDS, getCellBackgrounds( item ) );
    preserveProperty( item, PROP_CELL_FOREGROUNDS, getCellForegrounds( item ) );
    preserveProperty( item, PROP_CELL_FONTS, getCellFonts( item ) );
    preserveProperty( item, PROP_EXPANDED, item.isExpanded() );
    preserveProperty( item, PROP_CHECKED, item.getChecked() );
    preserveProperty( item, PROP_GRAYED, item.getGrayed() );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    GridItem item = ( GridItem )widget;
    WidgetLCAUtil.renderCustomVariant( item );
    renderProperty( item, PROP_ITEM_COUNT, item.getItemCount(), ZERO );
    renderProperty( item, PROP_TEXTS, getTexts( item ), getDefaultTexts( item ) );
    renderProperty( item, PROP_IMAGES, getImages( item ), new Image[ getColumnCount( item ) ] );
    WidgetLCAUtil.renderBackground( item, getUserBackground( item ) );
    WidgetLCAUtil.renderForeground( item, getUserForeground( item ) );
    WidgetLCAUtil.renderFont( item, getUserFont( item ) );
    renderProperty( item,
                    PROP_CELL_BACKGROUNDS,
                    getCellBackgrounds( item ),
                    new Color[ getColumnCount( item ) ] );
    renderProperty( item,
                    PROP_CELL_FOREGROUNDS,
                    getCellForegrounds( item ),
                    new Color[ getColumnCount( item ) ] );
    renderProperty( item,
                    PROP_CELL_FONTS,
                    getCellFonts( item ),
                    new Font[ getColumnCount( item ) ] );
    renderProperty( item, PROP_EXPANDED, item.isExpanded(), false );
    renderProperty( item, PROP_CHECKED, item.getChecked(), false );
    renderProperty( item, PROP_GRAYED, item.getGrayed(), false );
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    GridItem item = ( GridItem )widget;
    if( !isParentDisposed( item ) ) {
      // The tree disposes the items itself on the client (faster)
      ClientObjectFactory.getClientObject( widget ).destroy();
    }
  }

  ////////////////////////////////////////////
  // Helping methods to read client-side state

  private static void readChecked( GridItem item ) {
    String value = WidgetLCAUtil.readPropertyValue( item, "checked" );
    if( value != null ) {
      item.setChecked( Boolean.valueOf( value ).booleanValue() );
    }
  }

  private static void processTreeEvent( final GridItem item, String eventName ) {
    if( WidgetLCAUtil.wasEventSent( item, eventName ) ) {
      final boolean expanded = eventName.equals( JSConst.EVENT_TREE_EXPANDED );
      ProcessActionRunner.add( new Runnable() {
        public void run() {
          item.setExpanded( expanded );
        }
      } );
      if( expanded ) {
        item.fireEvent( SWT.Expand );
      } else {
        item.fireEvent( SWT.Collapse );
      }
    }
  }

  //////////////////
  // Helping methods

  private static Widget getParent( GridItem item ) {
    Widget result = item.getParent();
    GridItem parentItem = item.getParentItem();
    if( parentItem != null ) {
      result = item.getParentItem();
    }
    return result;
  }

  private static int getItemIndex( GridItem item ) {
    return getGridAdapter( item.getParent() ).getItemIndex( item );
  }

  private static boolean isParentDisposed( GridItem item ) {
    return getGridItemAdapter( item ).isParentDisposed();
  }

  private static String[] getTexts( GridItem item ) {
    int columnCount = getColumnCount( item );
    String[] texts = new String[ columnCount ];
    for( int i = 0; i < columnCount; i++ ) {
      texts[ i ] = item.getText( i );
    }
    return texts;
  }

  private static String[] getDefaultTexts( GridItem item ) {
    String[] result = new String[ getColumnCount( item ) ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = "";
    }
    return result;
  }

  private static Image[] getImages( GridItem item ) {
    int columnCount = getColumnCount( item );
    Image[] images = new Image[ columnCount ];
    for( int i = 0; i < columnCount; i++ ) {
      images[ i ] = item.getImage( i );
    }
    return images;
  }

  private static Color getUserBackground( GridItem item ) {
    return item.getAdapter( IWidgetColorAdapter.class ).getUserBackground();
  }

  private static Color getUserForeground( GridItem item ) {
    return item.getAdapter( IWidgetColorAdapter.class ).getUserForeground();
  }

  private static Font getUserFont( GridItem item ) {
    return item.getAdapter( IWidgetFontAdapter.class ).getUserFont();
  }

  private static Color[] getCellBackgrounds( GridItem item ) {
    return getGridItemAdapter( item ).getCellBackgrounds();
  }

  private static Color[] getCellForegrounds( GridItem item ) {
    return getGridItemAdapter( item ).getCellForegrounds();
  }

  private static Font[] getCellFonts( GridItem item ) {
    return getGridItemAdapter( item ).getCellFonts();
  }

  private static int getColumnCount( GridItem item ) {
    return Math.max( 1, item.getParent().getColumnCount() );
  }

  private static IGridAdapter getGridAdapter( Grid grid ) {
    return grid.getAdapter( IGridAdapter.class );
  }

  private static IGridItemAdapter getGridItemAdapter( GridItem item ) {
    return item.getAdapter( IGridItemAdapter.class );
  }
}