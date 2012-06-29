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
package org.eclipse.nebula.widgets.grid.internal.gridkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridItems;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCATestUtil.jsonEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.nebula.widgets.grid.internal.gridkit.GridLCA.ItemMetrics;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.rwt.internal.lifecycle.JSConst;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;

import junit.framework.TestCase;


@SuppressWarnings("restriction")
public class GridLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    lca = ( GridLCA )WidgetUtil.getLCA( grid );
    Fixture.fakeNewRequest( display );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRenderCreate() throws IOException {
    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertEquals( "rwt.widgets.Grid", operation.getType() );
    assertEquals( "tree", operation.getProperty( "appearance" ) );
    assertEquals( Integer.valueOf( 16 ), operation.getProperty( "indentionWidth" ) );
    assertFalse( operation.getPropertyNames().contains( "checkBoxMetrics" ) );
    assertTrue( styles.contains( "FULL_SELECTION" ) );
  }

  public void testRenderParent() throws IOException {
    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertEquals( WidgetUtil.getId( grid.getParent() ), operation.getParent() );
  }

  public void testRenderCreateWithVirtualMulti() throws IOException {
    grid = new Grid( shell, SWT.VIRTUAL | SWT.MULTI );

    lca.renderInitialization( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    List<Object> styles = Arrays.asList( operation.getStyles() );
    assertTrue( styles.contains( "VIRTUAL" ) );
    assertTrue( styles.contains( "MULTI" ) );
  }

  public void testRenderDispose() throws IOException {
    lca.renderDispose( grid );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( grid ), operation.getTarget() );
  }

  public void testRenderInitialItemCount() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "itemCount" ) == -1 );
  }

  public void testRenderItemCount() throws IOException {
    createGridItems( grid, 10, 3 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findSetProperty( grid, "itemCount" ) );
  }

  public void testRenderItemCountUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setItemCount( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "itemCount" ) );
  }

  public void testRenderInitialItemHeight() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().contains( "itemHeight" ) );
  }

  public void testRenderItemHeight() throws IOException {
    grid.setItemHeight( 40 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 40 ), message.findSetProperty( grid, "itemHeight" ) );
  }

  public void testRenderItemHeightUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setItemHeight( 40 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "itemHeight" ) );
  }

  public void testRenderInitialItemMetrics() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().contains( "itemMetrics" ) );
  }

  public void testRenderItemMetrics() throws IOException, JSONException {
    GridColumn column = new GridColumn( grid, SWT.NONE );
    column.setWidth( 50 );
    GridItem[] items = createGridItems( grid, 3, 1 );
    items[ 0 ].setText( "foo" );

    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "itemMetrics" );
    assertTrue( jsonEquals( "[0,0,50,0,0,0,44,0,0]", ( JSONArray )actual.get( 0 ) ) );
  }

  public void testRenderItemMetrics_WithCheck() throws IOException, JSONException {
    createGridColumns( grid, 2, SWT.CHECK );
    GridItem[] items = createGridItems( grid, 3, 1 );
    items[ 0 ].setText( "foo" );

    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "itemMetrics" );
    assertTrue( jsonEquals( "[0,0,20,23,0,23,0,0,21]", ( JSONArray )actual.get( 0 ) ) );
    assertTrue( jsonEquals( "[1,20,40,49,0,49,5,26,21]", ( JSONArray )actual.get( 1 ) ) );
  }

  public void testRenderItemMetricsUnchanged() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setText( "foo" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "itemMetrics" ) );
  }

  public void testRenderInitialColumnCount() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "columnCount" ) == -1 );
  }

  public void testRenderColumnCount() throws IOException {
    new GridColumn( grid, SWT.NONE );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 1 ), message.findSetProperty( grid, "columnCount" ) );
  }

  public void testRenderColumnCountUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    new GridColumn( grid, SWT.NONE );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "columnCount" ) );
  }

  public void testRenderInitialTreeColumn() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "treeColumn" ) == -1 );
  }

  public void testRenderTreeColumn() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );

    grid.setColumnOrder( new int[]{ 1, 0 } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 1 ), message.findSetProperty( grid, "treeColumn" ) );
  }

  public void testRenderTreeColumnUnchanged() throws IOException {
    createGridColumns( grid, 2, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setColumnOrder( new int[]{ 1, 0 } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "treeColumn" ) );
  }

  public void testRenderInitialHeaderHeight() throws IOException {
    new GridColumn( grid, SWT.NONE );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "headerHeight" ) == -1 );
  }

  public void testRenderHeaderHeight() throws IOException {
    new GridColumn( grid, SWT.NONE );
    grid.setHeaderVisible( true );

    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 31 ), message.findSetProperty( grid, "headerHeight" ) );
  }

  public void testRenderHeaderHeightUnchanged() throws IOException {
    new GridColumn( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "headerHeight" ) );
  }

  public void testRenderInitialHeaderVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "headerVisible" ) == -1 );
  }

  public void testRenderHeaderVisible() throws IOException {
    grid.setHeaderVisible( true );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( grid, "headerVisible" ) );
  }

  public void testRenderHeaderVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setHeaderVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "headerVisible" ) );
  }

  public void testRenderInitialFooterHeight() throws IOException {
    new GridColumn( grid, SWT.NONE );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "footerHeight" ) == -1 );
  }

  public void testRenderFooterHeight() throws IOException {
    new GridColumn( grid, SWT.NONE );
    grid.setFooterVisible( true );

    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 31 ), message.findSetProperty( grid, "footerHeight" ) );
  }

  public void testRenderFooterHeightUnchanged() throws IOException {
    new GridColumn( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setFooterVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "footerHeight" ) );
  }

  public void testRenderInitialFooterVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "footerVisible" ) == -1 );
  }

  public void testRenderFooterVisible() throws IOException {
    grid.setFooterVisible( true );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( grid, "footerVisible" ) );
  }

  public void testRenderFooterVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setFooterVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "footerVisible" ) );
  }

  public void testRenderInitialLinesVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findCreateProperty( grid, "linesVisible" ) );
  }

  public void testRenderLinesVisible() throws IOException {
    Fixture.markInitialized( grid );
    grid.setLinesVisible( false );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findSetProperty( grid, "linesVisible" ) );
  }

  public void testRenderLinesVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setLinesVisible( true );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "linesVisible" ) );
  }

  public void testRenderInitialTopItemIndex() throws IOException {
    grid.setSize( 100, 100 );

    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "topItemIndex" ) == -1 );
  }

  public void testRenderTopItemIndex() throws IOException {
    grid.setSize( 100, 100 );
    createGridItems( grid, 10, 3 );
    grid.getItem( 4 ).setExpanded( true );

    grid.setTopIndex( 5 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 2 ), message.findSetProperty( grid, "topItemIndex" ) );
  }

  public void testRenderTopItemIndexUnchanged() throws IOException {
    grid.setSize( 100, 100 );
    createGridItems( grid, 10, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setTopIndex( 2 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "topItemIndex" ) );
  }

  public void testRenderInitialFocusItem() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "focusItem" ) == -1 );
  }

  public void testRenderFocusItem() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 0 );

    grid.setFocusItem( items[ 1 ] );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( WidgetUtil.getId( items[ 1 ] ), message.findSetProperty( grid, "focusItem" ) );
  }

  public void testRenderFocusItemUnchanged() throws IOException {
    GridItem[] items = createGridItems( grid, 3, 0 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setFocusItem( items[ 1 ] );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "focusItem" ) );
  }

  public void testRenderInitialScrollLeft() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "scrollLeft" ) == -1 );
  }

  public void testRenderScrollLeft() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    grid.getHorizontalBar().setSelection( 10 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 10 ), message.findSetProperty( grid, "scrollLeft" ) );
  }

  public void testRenderScrollLeftUnchanged() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.getHorizontalBar().setSelection( 10 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "scrollLeft" ) );
  }

  public void testRenderInitialSelection() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "selection" ) == -1 );
  }

  public void testRenderSelection() throws IOException, JSONException {
    GridItem[] items = createGridItems( grid, 3, 3 );

    grid.setSelection( new int[] { 0, 4 } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "selection" );
    StringBuilder expected = new StringBuilder();
    expected.append( "[" );
    expected.append( WidgetUtil.getId( items[ 0 ] ) );
    expected.append( "," );
    expected.append( WidgetUtil.getId( items[ 4 ] ) );
    expected.append( "]" );
    assertTrue( jsonEquals( expected.toString(), actual ) );
  }

  public void testRenderSelectionUnchanged() throws IOException {
    createGridItems( grid, 3, 3 );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    grid.setSelection( new int[] { 0, 4 } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "selection" ) );
  }

  public void testRenderInitialSortDirection() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "sortDirection" ) == -1 );
  }

  public void testRenderSortDirection() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    columns[ 1 ].setSort( SWT.UP );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "up", message.findSetProperty( grid, "sortDirection" ) );
  }

  public void testRenderSortDirectionUnchanged() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    columns[ 1 ].setSort( SWT.UP );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "sortDirection" ) );
  }

  public void testRenderInitialSortColumn() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "sortColumn" ) == -1 );
  }

  public void testRenderSortColumn() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    columns[ 1 ].setSort( SWT.UP );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( WidgetUtil.getId( columns[ 1 ] ), message.findSetProperty( grid, "sortColumn" ) );
  }

  public void testRenderSortColumnUnchanged() throws IOException {
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    columns[ 1 ].setSort( SWT.UP );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "sortColumn" ) );
  }

  public void testRenderInitialScrollBarsVisible() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "scrollBarsVisible" ) == -1 );
  }

  public void testRenderScrollBarsVisible_Horizontal() throws IOException, JSONException {
    grid.setSize( 200, 200 );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );

    columns[ 0 ].setWidth( 150 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "scrollBarsVisible" );
    assertTrue( jsonEquals( "[ true, false ]", actual ) );
  }

  public void testRenderScrollBarsVisible_Vertical() throws IOException, JSONException {
    grid.setSize( 200, 200 );
    createGridColumns( grid, 3, SWT.NONE );

    createGridItems( grid, 20, 0 );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    JSONArray actual = ( JSONArray )message.findSetProperty( grid, "scrollBarsVisible" );
    assertTrue( jsonEquals( "[ false, true ]", actual ) );
  }

  public void testRenderScrollBarsVisibleUnchanged() throws IOException {
    grid.setSize( 200, 200 );
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    columns[ 0 ].setWidth( 150 );
    createGridItems( grid, 20, 0 );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "scrollBarsVisible" ) );
  }

  public void testRenderAddScrollBarsSelectionListener_Horizontal() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getHorizontalBar().addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderRemoveScrollBarsSelectionListener_Horizontal() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    grid.getHorizontalBar().addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getHorizontalBar().removeSelectionListener( listener );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderScrollBarsSelectionListenerUnchanged_Horizontal() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getHorizontalBar().addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( grid, "scrollBarsSelection" ) );
  }

  public void testRenderAddScrollBarsSelectionListener_Vertical() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getVerticalBar().addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderRemoveScrollBarsSelectionListener_Vertical() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    grid.getVerticalBar().addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getVerticalBar().removeSelectionListener( listener );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( grid, "scrollBarsSelection" ) );
  }

  public void testRenderScrollBarsSelectionListenerUnchanged_Vertical() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.getVerticalBar().addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( grid, "scrollBarsSelection" ) );
  }

  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( grid, "selection" ) );
  }

  public void testRenderRemoveSelectionListener() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    grid.addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.removeSelectionListener( listener );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( grid, "selection" ) );
  }

  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );
    Fixture.preserveWidgets();

    grid.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( grid, "selection" ) );
  }

  public void testRenderInitialEnableCellToolTip() throws IOException {
    lca.render( grid );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( grid );
    assertTrue( operation.getPropertyNames().indexOf( "enableCellToolTip" ) == -1 );
  }

  public void testRenderEnableCellToolTip() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );

    item.setToolTipText( 1, "foo" );
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( grid, "enableCellToolTip" ) );
  }

  public void testRenderEnableCellToolTipUnchanged() throws IOException {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    item.setToolTipText( 1, "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( grid );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( grid, "enableCellToolTip" ) );
  }

  public void testRenderCellToolTipText() {
    createGridColumns( grid, 3, SWT.NONE );
    GridItem item = new GridItem( grid, SWT.NONE );
    item.setToolTipText( 1, "foo" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( grid );

    String itemId = WidgetUtil.getId( item );
    processCellToolTipRequest( grid, itemId, 1 );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( grid, "cellToolTipText" ) );
  }

  public void testReadSelection() {
    String gridId = WidgetUtil.getId( grid );
    GridItem[] items = createGridItems( grid, 3, 0 );
    String item0Id = WidgetUtil.getId( items[ 0 ] );
    String item2Id = WidgetUtil.getId( items[ 2 ] );

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( gridId + ".selection", item0Id + "," + item2Id );
    Fixture.readDataAndProcessAction( grid );

    GridItem[] selectedItems = grid.getSelection();
    assertEquals( 2, selectedItems.length );
    assertSame( items[ 0 ], selectedItems[ 0 ] );
    assertSame( items[ 2 ], selectedItems[ 1 ] );
  }

  public void testReadSelectionDisposedItem() {
    String gridId = WidgetUtil.getId( grid );
    GridItem[] items = createGridItems( grid, 3, 0 );
    String item0Id = WidgetUtil.getId( items[ 0 ] );
    String item2Id = WidgetUtil.getId( items[ 2 ] );
    items[ 0 ].dispose();

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( gridId + ".selection", item0Id + "," + item2Id );
    Fixture.readDataAndProcessAction( grid );

    GridItem[] selectedItems = grid.getSelection();
    assertEquals( 1, selectedItems.length );
    assertSame( items[ 2 ], selectedItems[ 0 ] );
  }

  public void testReadScrollLeft() {
    grid.setSize( 100, 100 );
    String gridId = WidgetUtil.getId( grid );
    createGridColumns( grid, 5, SWT.NONE );
    createGridItems( grid, 10, 0 );

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( gridId + ".scrollLeft", "30" );
    Fixture.readDataAndProcessAction( grid );

    assertEquals( 30, grid.getHorizontalBar().getSelection() );
  }

  public void testReadTopIndex() {
    grid.setSize( 100, 100 );
    String gridId = WidgetUtil.getId( grid );
    createGridColumns( grid, 5, SWT.NONE );
    GridItem[] items = createGridItems( grid, 10, 3 );
    items[ 4 ].setExpanded( true );

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( gridId + ".topItemIndex", "3" );
    Fixture.readDataAndProcessAction( grid );

    assertEquals( 3, grid.getVerticalBar().getSelection() );
    assertEquals( 6, grid.getTopIndex() );
  }

  public void testProcessSelectionEvent() {
    List<SelectionEvent> events = new LinkedList<SelectionEvent>();
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addSelectionListener( new LoggingSelectionListener( events ) );
    String gridId = WidgetUtil.getId( grid );
    String itemId = WidgetUtil.getId( item );

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_SELECTED, gridId );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_SELECTED + ".item", itemId );
    Fixture.readDataAndProcessAction( display );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.Selection, event.getID() );
    assertEquals( grid, event.getSource() );
    assertEquals( item, event.item );
    assertEquals( SWT.NONE, event.detail );
  }

  public void testProcessSelectionEvent_Check() {
    List<SelectionEvent> events = new LinkedList<SelectionEvent>();
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addSelectionListener( new LoggingSelectionListener( events ) );
    String gridId = WidgetUtil.getId( grid );
    String itemId = WidgetUtil.getId( item );

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_SELECTED, gridId );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_SELECTED + ".item", itemId );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_SELECTED + ".detail", "check" );
    Fixture.readDataAndProcessAction( display );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.Selection, event.getID() );
    assertEquals( grid, event.getSource() );
    assertEquals( item, event.item );
    assertEquals( SWT.CHECK, event.detail );
  }

  public void testProcessDefaultSelectionEvent() {
    List<SelectionEvent> events = new LinkedList<SelectionEvent>();
    GridItem item = new GridItem( grid, SWT.NONE );
    grid.addSelectionListener( new LoggingSelectionListener( events ) );
    String gridId = WidgetUtil.getId( grid );
    String itemId = WidgetUtil.getId( item );

    Fixture.fakeNewRequest( display );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_DEFAULT_SELECTED, gridId );
    Fixture.fakeRequestParam( JSConst.EVENT_WIDGET_DEFAULT_SELECTED + ".item", itemId );
    Fixture.readDataAndProcessAction( display );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertEquals( SWT.DefaultSelection, event.getID() );
    assertEquals( grid, event.getSource() );
    assertEquals( item, event.item );
    assertEquals( SWT.NONE, event.detail );
  }

  public void testGetItemMetrics_CellLeft() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 150 );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );

    assertEquals( 0, metrics[ 0 ].left );
    assertEquals( 100, metrics[ 1 ].left );
  }

  public void testGetItemMetrics_CellWidth() {
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 150 );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );

    assertEquals( 100, metrics[ 0 ].width );
    assertEquals( 150, metrics[ 1 ].width );
  }

  public void testGetItemMetrics_ImageLeft() {
    Image image1 = loadImage( display, Fixture.IMAGE_100x50 );
    Image image2 = loadImage( display, Fixture.IMAGE_50x100 );
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 150 );
    GridItem[] items = createGridItems( grid, 3, 1 );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 0, metrics[ 0 ].imageLeft );
    assertEquals( 106, metrics[ 1 ].imageLeft );

    items[ 1 ].setImage( image2 );
    items[ 0 ].setImage( 1, image1 );

    metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 0, metrics[ 0 ].imageLeft );
    assertEquals( 106, metrics[ 1 ].imageLeft );
  }

  public void testGetItemMetrics_ImageWidth() {
    Image image1 = loadImage( display, Fixture.IMAGE_100x50 );
    Image image2 = loadImage( display, Fixture.IMAGE_50x100 );
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 150 );
    GridItem[] items = createGridItems( grid, 3, 1 );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 0, metrics[ 0 ].imageWidth );

    items[ 1 ].setImage( image2 );
    items[ 0 ].setImage( image1 );

    metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 50, metrics[ 0 ].imageWidth );

    items[ 1 ].setImage( null );
    items[ 0 ].setImage( null );

    metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 0, metrics[ 0 ].imageWidth );
  }

  public void testGetItemMetrics_TextLeftWithImage() {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    columns[ 0 ].setWidth( 100 );
    columns[ 1 ].setWidth( 150 );
    GridItem[] items = createGridItems( grid, 3, 1 );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 106, metrics[ 1 ].textLeft );

    items[ 0 ].setImage( 1, image );

    metrics = GridLCA.getItemMetrics( grid );
    assertEquals( 206, metrics[ 1 ].textLeft );
  }

  public void testGetItemMetrics_TextLeftWithCheckbox() {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    grid = new Grid( shell, SWT.CHECK );
    GridColumn[] columns = createGridColumns( grid, 1, SWT.NONE );
    columns[ 0 ].setWidth( 200 );
    GridItem[] items = createGridItems( grid, 3, 1 );
    items[ 0 ].setText( "item" );
    items[ 0 ].setImage( image );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );

    assertEquals( 123, metrics[ 0 ].textLeft );
  }

  public void testGetItemMetrics_TextWidthWithCheckbox() {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    grid = new Grid( shell, SWT.CHECK );
    GridColumn[] columns = createGridColumns( grid, 1, SWT.NONE );
    columns[ 0 ].setWidth( 200 );
    GridItem[] items = createGridItems( grid, 3, 1 );
    items[ 0 ].setText( "item" );
    items[ 0 ].setImage( image );

    ItemMetrics[] metrics = GridLCA.getItemMetrics( grid );

    assertEquals( 71, metrics[ 0 ].textWidth );
  }

  //////////////////
  // Helping methods

  private static void processCellToolTipRequest( Grid grid, String itemId, int column ) {
    Fixture.fakeNewRequest( grid.getDisplay() );
    String gridId = WidgetUtil.getId( grid );
    Fixture.fakeRequestParam( JSConst.EVENT_CELL_TOOLTIP_REQUESTED, gridId );
    String cellString = itemId + "," + column;
    Fixture.fakeRequestParam( JSConst.EVENT_CELL_TOOLTIP_DETAILS, cellString );
    Fixture.executeLifeCycleFromServerThread();
  }

  //////////////////
  // Helping classes

  private static class LoggingSelectionListener extends SelectionAdapter {
    private final List<SelectionEvent> events;
    private LoggingSelectionListener( List<SelectionEvent> events ) {
      this.events = events;
    }
    @Override
    public void widgetSelected( SelectionEvent event ) {
      events.add( event );
    }
    @Override
    public void widgetDefaultSelected( SelectionEvent event ) {
      events.add( event );
    }
  }

}
