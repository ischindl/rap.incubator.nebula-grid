/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.grid.internal.gridcolumnkit;

import static org.eclipse.nebula.widgets.grid.GridTestUtil.createGridColumns;
import static org.eclipse.nebula.widgets.grid.GridTestUtil.loadImage;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.Message.DestroyOperation;
import org.eclipse.rap.rwt.testfixture.Message.Operation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


@SuppressWarnings("restriction")
public class GridColumnLCA_Test extends TestCase {

  private Display display;
  private Shell shell;
  private Grid grid;
  private GridColumn column;
  private GridColumnLCA lca;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    grid = new Grid( shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    column = new GridColumn( grid, SWT.NONE );
    lca = ( GridColumnLCA )WidgetUtil.getLCA( column );
    Fixture.fakeNewRequest();
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testRenderCreate() throws IOException {
    lca.renderInitialization( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertEquals( "rwt.widgets.GridColumn", operation.getType() );
    assertFalse( operation.getPropertyNames().contains( "group" ) );
  }

  public void testRenderCreateWithAligment() throws IOException {
    column = new GridColumn( grid, SWT.RIGHT );

    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "style" ) == -1 );
    assertEquals( "right", message.findCreateProperty( column, "alignment" ).asString() );
  }

  public void testRenderParent() throws IOException {
    lca.renderInitialization( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertEquals( WidgetUtil.getId( column.getParent() ), operation.getParent() );
  }

  public void testRenderGroup() throws IOException {
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    column = new GridColumn( group, SWT.NONE );

    lca.renderInitialization( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertEquals( getId( group ), operation.getProperty( "group" ).asString() );
  }

  public void testRenderDispose() throws IOException {
    lca.renderDispose( column );

    Message message = Fixture.getProtocolMessage();
    Operation operation = message.getOperation( 0 );
    assertTrue( operation instanceof DestroyOperation );
    assertEquals( WidgetUtil.getId( column ), operation.getTarget() );
  }

  public void testRenderInitialToolTip() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "toolTip" ) == -1 );
  }

  public void testRenderToolTip() throws IOException {
    column.setHeaderTooltip( "foo" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( column, "toolTip" ).asString() );
  }

  public void testRenderToolTipUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setHeaderTooltip( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "toolTip" ) );
  }

  public void testRenderInitialCustomVariant() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "customVariant" ) == -1 );
  }

  public void testRenderCustomVariant() throws IOException {
    column.setData( RWT.CUSTOM_VARIANT, "blue" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "variant_blue", message.findSetProperty( column, "customVariant" ).asString() );
  }

  public void testRenderCustomVariantUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setData( RWT.CUSTOM_VARIANT, "blue" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "customVariant" ) );
  }

  public void testRenderInitialText() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "text" ) == -1 );
  }

  public void testRenderText() throws IOException {
    column.setText( "foo" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( column, "text" ).asString() );
  }

  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "text" ) );
  }

  public void testRenderInitialImage() throws IOException {
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "image" ) );
  }

  public void testRenderImage() throws IOException {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    column.setImage( image );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    String expected = "[\"" + imageLocation + "\", 100, 50 ]";
    JsonValue actual = message.findSetProperty( column, "image" );
    assertEquals( JsonArray.readFrom( expected ), actual );
  }

  public void testRenderImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    column.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "image" ) );
  }

  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    column.setImage( image );

    Fixture.preserveWidgets();
    column.setImage( null );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( column, "image" ) );
  }

  public void testRenderInitialIndex() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 0, message.findCreateProperty( column, "index" ).asInt() );
  }

  public void testRenderIndex() throws IOException {
    new GridColumn( grid, SWT.NONE, 0 );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 1, message.findSetProperty( column, "index" ).asInt() );
  }

  public void testRenderIndexUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    new GridColumn( grid, SWT.NONE, 0 );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "index" ) );
  }

  public void testRenderInitialLeft() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "left" ) == -1 );
  }

  public void testRenderLeft() throws IOException {
    GridColumn column2 = new GridColumn( grid, SWT.NONE, 0 );
    column2.setWidth( 50 );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 50, message.findSetProperty( column, "left" ).asInt() );
  }

  public void testRenderLeftUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    GridColumn column2 = new GridColumn( grid, SWT.NONE, 0 );
    column2.setWidth( 50 );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "left" ) );
  }

  public void testRenderInitialWidth() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 10, message.findCreateProperty( column, "width" ).asInt() );
  }

  public void testRenderWidth() throws IOException {
    column.setWidth( 50 );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( 50, message.findSetProperty( column, "width" ).asInt() );
  }

  public void testRenderWidthUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setWidth( 50 );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "width" ) );
  }

  public void testRenderInitialAlignment() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "alignment" ) == -1 );
  }

  public void testRenderAlignment() throws IOException {
    column.setAlignment( SWT.RIGHT );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "right", message.findSetProperty( column, "alignment" ).asString() );
  }

  public void testRenderAlignmentUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setAlignment( SWT.RIGHT );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "alignment" ) );
  }

  public void testRenderInitialResizable() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "resizable" ) == -1 );
  }

  public void testRenderResizable() throws IOException {
    column.setResizeable( false );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( column, "resizable" ) );
  }

  public void testRenderResizableUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setResizeable( false );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "resizable" ) );
  }

  public void testRenderInitialMoveable() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "moveable" ) == -1 );
  }

  public void testRenderMoveable() throws IOException {
    column.setMoveable( true );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( column, "moveable" ) );
  }

  public void testRenderMoveableUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setMoveable( true );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "moveable" ) );
  }

  public void testRenderInitialVisible() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "visibility" ) == -1 );
  }

  public void testRenderVisible() throws IOException {
    column.setVisible( false );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findSetProperty( column, "visibility" ) );
  }

  public void testRenderVisibleUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setVisible( false );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "visibility" ) );
  }

  public void testRenderInitialCheck() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "check" ) == -1 );
  }

  public void testRenderCheck() throws IOException {
    column = new GridColumn( grid, SWT.CHECK );

    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findSetProperty( column, "check" ) );
  }

  public void testRenderCheckUnchanged() throws IOException {
    column = new GridColumn( grid, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "check" ) );
  }

  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Fixture.preserveWidgets();

    column.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( column, "Selection" ) );
  }

  public void testRenderRemoveSelectionListener() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    column.addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Fixture.preserveWidgets();

    column.removeSelectionListener( listener );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( column, "Selection" ) );
  }

  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Fixture.preserveWidgets();

    column.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( column, "Selection" ) );
  }

  public void testReadWidth() {
    final List<Event> events = new LinkedList<Event>();
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    columns[ 0 ].addListener( SWT.Resize, new LoggingControlListener( events ) );
    columns[ 1 ].addListener( SWT.Move, new LoggingControlListener( events ) );

    // Simulate request that initializes widgets
    Fixture.fakeNewRequest();
    Fixture.executeLifeCycleFromServerThread();
    // Simulate request that changes column width
    int newWidth = columns[ 0 ].getWidth() + 2;
    int newLeft = column.getWidth() + newWidth;
    Fixture.fakeNewRequest();
    JsonObject parameters = new JsonObject().add( "width", newWidth );
    Fixture.fakeCallOperation( getId( columns[ 0 ] ), "resize", parameters );
    Fixture.executeLifeCycleFromServerThread();

    assertEquals( 2, events.size() );
    Event event = events.get( 0 );
    assertSame( columns[ 0 ], event.widget );
    assertEquals( newWidth, columns[ 0 ].getWidth() );
    event = events.get( 1 );
    assertSame( columns[ 1 ], event.widget );
    Message message = Fixture.getProtocolMessage();
    assertEquals( newWidth, message.findSetProperty( columns[ 0 ], "width" ).asInt() );
    assertEquals( newLeft, message.findSetProperty( columns[ 1 ], "left" ).asInt() );
  }

  public void testReadLeft() {
    final List<Event> events = new LinkedList<Event>();
    GridColumn[] columns = createGridColumns( grid, 2, SWT.NONE );
    column.addListener( SWT.Move, new LoggingControlListener( events ) );
    columns[ 0 ].addListener( SWT.Move, new LoggingControlListener( events ) );
    columns[ 1 ].addListener( SWT.Move, new LoggingControlListener( events ) );

    // Simulate request that initializes widgets
    Fixture.fakeNewRequest();
    Fixture.executeLifeCycleFromServerThread();
    // Simulate request that changes column left
    int newLeft = 3;
    Fixture.fakeNewRequest();
    JsonObject parameters = new JsonObject().add( "left", newLeft );
    Fixture.fakeCallOperation( getId( columns[ 0 ] ), "move", parameters );
    Fixture.executeLifeCycleFromServerThread();

    assertEquals( 2, events.size() );
    Event event = events.get( 0 );
    assertSame( columns[ 0 ], event.widget );
    event = events.get( 1 );
    assertSame( column, event.widget );
    Message message = Fixture.getProtocolMessage();
    assertEquals( 20, message.findSetProperty( column, "left" ).asInt() );
    assertEquals( 0, message.findSetProperty( columns[ 0 ], "left" ).asInt() );
  }

  public void testMoveColumn_1() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60 (as created)
    // Move Col 1 over Col 0 (left half), thereafter order should be:
    // Col 1, Col 0, Col 2
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 1 ], 3 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 1, columnOrder[ 0 ] );
    assertEquals( 0, columnOrder[ 1 ] );
    assertEquals( 2, columnOrder[ 2 ] );
  }

  public void testMoveColumn_2() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 1: 0..20, Col 0: 21..30, Col 2: 31..60
    // Move Col 1 over Col 0 (right half), thereafter order should be:
    // Col 0, Col 1, Col 2
    grid.setColumnOrder( new int[]{
      1, 0, 2
    } );

    GridColumnLCA.moveColumn( columns[ 1 ], 27 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 0, columnOrder[ 0 ] );
    assertEquals( 1, columnOrder[ 1 ] );
    assertEquals( 2, columnOrder[ 2 ] );
  }

  public void testMoveColumn_3() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60
    // Move Col 2 over Col 1 (left half), thereafter order should be:
    // Col 0, Col 2, Col 1
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 2 ], 13 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 0, columnOrder[ 0 ] );
    assertEquals( 2, columnOrder[ 1 ] );
    assertEquals( 1, columnOrder[ 2 ] );
  }

  public void testMoveColumn_4() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60
    // Move Col 2 over Col 1 (right half), thereafter order should be:
    // Col 2, Col 0, Col 1
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 2 ], 3 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 2, columnOrder[ 0 ] );
    assertEquals( 0, columnOrder[ 1 ] );
    assertEquals( 1, columnOrder[ 2 ] );
  }

  public void testMoveColumn_5() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60
    // Move Col 2 way left of Col 0, thereafter order should be:
    // Col 2, Col 0, Col 1
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 2 ], -30 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 2, columnOrder[ 0 ] );
    assertEquals( 0, columnOrder[ 1 ] );
    assertEquals( 1, columnOrder[ 2 ] );
  }

  public void testMoveColumn_6() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60
    // Move Col 0 way right of Col 2, thereafter order should be:
    // Col 1, Col 2, Col 0
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 0 ], 100 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 1, columnOrder[ 0 ] );
    assertEquals( 2, columnOrder[ 1 ] );
    assertEquals( 0, columnOrder[ 2 ] );
  }

  public void testMoveColumn_7() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60
    // Move Col 1 onto itself (left half), order should stay unchanged:
    // Col 1, Col 2, Col 0
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 1 ], 13 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 0, columnOrder[ 0 ] );
    assertEquals( 1, columnOrder[ 1 ] );
    assertEquals( 2, columnOrder[ 2 ] );
  }

  public void testMoveColumn_8() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 3, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60
    // Move Col 0 over Col 2 (left half), order should be:
    // Col 1, Col 0, Col 2
    grid.setColumnOrder( new int[]{
      0, 1, 2
    } );

    GridColumnLCA.moveColumn( columns[ 0 ], 33 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 1, columnOrder[ 0 ] );
    assertEquals( 0, columnOrder[ 1 ] );
    assertEquals( 2, columnOrder[ 2 ] );
  }

  public void testMoveColumn_MoveIntoGroup() {
    column.dispose();
    createGridColumns( grid, 2, SWT.NONE );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    createGridColumns( group, 2, SWT.NONE );
    grid.getColumn( 0 ).setWidth( 10 );
    grid.getColumn( 1 ).setWidth( 20 );
    grid.getColumn( 2 ).setWidth( 30 );
    grid.getColumn( 3 ).setWidth( 40 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60, Col 3: 61..100
    // Move Col 0 between Col 2 and Col 3 (inside the group)
    // Movement should be ignored
    grid.setColumnOrder( new int[]{
      0, 1, 2, 3
    } );

    GridColumnLCA.moveColumn( grid.getColumn( 0 ), 55 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 0, columnOrder[ 0 ] );
    assertEquals( 1, columnOrder[ 1 ] );
    assertEquals( 2, columnOrder[ 2 ] );
    assertEquals( 3, columnOrder[ 3 ] );
  }

  public void testMoveColumn_MoveOutsideGroup() {
    column.dispose();
    createGridColumns( grid, 2, SWT.NONE );
    GridColumnGroup group = new GridColumnGroup( grid, SWT.NONE );
    createGridColumns( group, 2, SWT.NONE );
    grid.getColumn( 0 ).setWidth( 10 );
    grid.getColumn( 1 ).setWidth( 20 );
    grid.getColumn( 2 ).setWidth( 30 );
    grid.getColumn( 3 ).setWidth( 40 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: 31..60, Col 3: 61..100
    // Move Col 3 between Col 0 and Col 1 (outside the group)
    // Movement should be ignored
    grid.setColumnOrder( new int[]{
      0, 1, 2, 3
    } );

    GridColumnLCA.moveColumn( grid.getColumn( 3 ), 15 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 0, columnOrder[ 0 ] );
    assertEquals( 1, columnOrder[ 1 ] );
    assertEquals( 2, columnOrder[ 2 ] );
    assertEquals( 3, columnOrder[ 3 ] );
  }

  public void testMoveColumn_WithInvisibleColumns() {
    column.dispose();
    GridColumn[] columns = createGridColumns( grid, 4, SWT.NONE );
    columns[ 0 ].setWidth( 10 );
    columns[ 1 ].setWidth( 20 );
    columns[ 2 ].setWidth( 30 );
    columns[ 2 ].setVisible( false );
    columns[ 3 ].setWidth( 40 );
    // Current order: Col 0: 0..10, Col 1: 11..30, Col 2: not visible, Col 3: 31..70
    // Move Col 0 over Col 3 (left half), order should be:
    // Col 1, Col 2, Col 0, Col 3
    grid.setColumnOrder( new int[]{
      0, 1, 2, 3
    } );

    GridColumnLCA.moveColumn( grid.getColumn( 0 ), 33 );

    int[] columnOrder = grid.getColumnOrder();
    assertEquals( 1, columnOrder[ 0 ] );
    assertEquals( 2, columnOrder[ 1 ] );
    assertEquals( 0, columnOrder[ 2 ] );
    assertEquals( 3, columnOrder[ 3 ] );
  }

  public void testReadSelectionEvent() {
    final List<SelectionEvent> events = new LinkedList<SelectionEvent>();
    column.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        events.add( event );
      }
    } );

    Fixture.fakeNewRequest();
    Fixture.fakeNotifyOperation( getId( column ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( column );

    assertEquals( 1, events.size() );
    SelectionEvent event = events.get( 0 );
    assertSame( column, event.widget );
  }

  public void testRenderInitialFont() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "font" ) == -1 );
  }

  public void testRenderFont() throws IOException {
    column.setHeaderFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( column, "font" );
    assertEquals( JsonArray.readFrom( "[[\"Arial\"], 20, true, false]" ), actual );
  }

  public void testRenderFontUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setHeaderFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "font" ) );
  }

  public void testRenderInitialFooterFont() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "footerFont" ) == -1 );
  }

  public void testRenderFooterFont() throws IOException {
    column.setFooterFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    JsonValue actual = message.findSetProperty( column, "footerFont" );
    assertEquals( JsonArray.readFrom( "[[\"Arial\"], 20, true, false]" ), actual );
  }

  public void testRenderFooterFontUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setFooterFont( new Font( display, "Arial", 20, SWT.BOLD ) );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "footerFont" ) );
  }

  public void testRenderInitialFooterText() throws IOException {
    lca.render( column );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( column );
    assertTrue( operation.getPropertyNames().indexOf( "footerText" ) == -1 );
  }

  public void testRenderFooterText() throws IOException {
    column.setFooterText( "foo" );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( column, "footerText" ).asString() );
  }

  public void testRenderFooterTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );

    column.setFooterText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "footerText" ) );
  }

  public void testRenderInitialFooterImage() throws IOException {
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "footerImage" ) );
  }

  public void testRenderFooterImage() throws IOException {
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    column.setFooterImage( image );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    JsonArray expected = JsonArray.readFrom( "[\"" + imageLocation + "\", 100, 50 ]" );
    assertEquals( expected, message.findSetProperty( column, "footerImage" ) );
  }

  public void testRenderFooterImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );

    column.setFooterImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( column, "footerImage" ) );
  }

  public void testRenderFooterImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( column );
    Image image = loadImage( display, Fixture.IMAGE_100x50 );
    column.setFooterImage( image );

    Fixture.preserveWidgets();
    column.setFooterImage( null );
    lca.renderChanges( column );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( column, "footerImage" ) );
  }

  //////////////////
  // Helping classes

  private static class LoggingControlListener implements Listener {
    private final List<Event> events;
    private LoggingControlListener( List<Event> events ) {
      this.events = events;
    }
    public void handleEvent( Event event ) {
      events.add( event );
    }
  }

}
