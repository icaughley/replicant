package org.realityforge.replicant.client.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.realityforge.replicant.client.Change;
import org.realityforge.replicant.client.Linkable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataLoadActionTest
{
  @DataProvider(name = "actionDescriptions")
  public Object[][] actionDescriptions()
  {
    final List<Boolean> flags = Arrays.asList( Boolean.TRUE, Boolean.FALSE );

    final ArrayList<Object[]> objects = new ArrayList<>();
    for ( final boolean normalCompletion : flags )
    {
      for ( final boolean bulkChange : flags )
      {
        for ( final boolean useRunnable : flags )
        {
          for ( final boolean isLinkableEntity : flags )
          {
            for ( final boolean oob : flags )
            {
              for ( final boolean update : flags )
              {
                final boolean expectLink = isLinkableEntity && update;
                final TestChangeSet changeSet =
                  new TestChangeSet( 42,
                                     useRunnable ? new MockRunner() : null,
                                     bulkChange,
                                     new Change[]{ new TestChange( update ) } );
                final Object entity = isLinkableEntity ? new MockLinkable() : new Object();
                objects.add( new Object[]{ normalCompletion, oob, changeSet, entity, expectLink } );
              }
              final boolean expectLink = false;
              final TestChangeSet changeSet =
                new TestChangeSet( 42,
                                   useRunnable ? new MockRunner() : null,
                                   bulkChange,
                                   new Change[]{ new TestChange( true ), new TestChange( false ) } );
              final Object entity = isLinkableEntity ? new MockLinkable() : new Object();
              objects.add( new Object[]{ normalCompletion, oob, changeSet, entity, expectLink } );
            }
          }
        }
      }
    }

    return objects.toArray( new Object[ objects.size() ][] );
  }

  @Test(dataProvider = "actionDescriptions")
  public void verifyActionLifecycle( final boolean normalCompletion,
                                     final boolean oob,
                                     final TestChangeSet changeSet,
                                     final Object entity,
                                     final boolean expectedLink )
  {
    final MockRunner runnable = (MockRunner) changeSet.getRunnable();
    final DataLoadAction action = new DataLoadAction( "BLAH", oob );

    //Ensure the initial state is as expected
    assertEquals( action.getRawJsonData(), "BLAH" );
    assertEquals( action.getChangeSet(), null );
    assertRunCount( runnable, 0 );

    assertEquals( action.areEntityLinksCalculated(), false );
    assertEquals( action.areEntityLinksPending(), false );
    assertEquals( action.areChangesPending(), false );
    assertEquals( action.hasWorldBeenNotified(), false );

    if ( oob )
    {
      action.setRunnable( changeSet.getRunnable() );
      action.setBulkLoad( changeSet.isBulkChange() );
      action.setChangeSet( changeSet, null );
    }
    else
    {
      changeSet.setRequestID( "X" );
      final String requestID = changeSet.getRequestID();
      assertNotNull( requestID );
      final RequestEntry request = new RequestEntry( requestID, "MyOperation", null, changeSet.isBulkChange() );
      if ( normalCompletion )
      {
        request.setNormalCompletionAction( runnable );
      }
      else
      {
        request.setNonNormalCompletionAction( runnable );
      }
      action.setChangeSet( changeSet, request );
    }
    assertEquals( action.isBulkLoad(), changeSet.isBulkChange() );
    assertEquals( action.getRunnable(), runnable );

    assertEquals( action.getChangeSet(), changeSet );
    assertEquals( action.getRawJsonData(), null );

    assertEquals( action.areChangesPending(), true );
    final Change change = action.nextChange();
    assertEquals( change, changeSet.getChange( 0 ) );

    action.changeProcessed( change.isUpdate(), entity );

    if ( 1 == changeSet.getChangeCount() )
    {
      assertEquals( action.areChangesPending(), false );
    }
    else
    {
      while ( action.areChangesPending() )
      {
        action.changeProcessed( action.nextChange().isUpdate(), entity );
      }
    }

    assertEquals( action.areEntityLinksCalculated(), false );

    action.calculateEntitiesToLink();

    assertEquals( action.areEntityLinksCalculated(), true );

    if ( expectedLink )
    {
      assertEquals( action.areEntityLinksPending(), true );
      assertEquals( action.nextEntityToLink(), entity );
      assertEquals( action.areEntityLinksPending(), false );
    }
    else
    {
      assertEquals( action.areEntityLinksPending(), false );
    }

    assertEquals( action.hasWorldBeenNotified(), false );

    action.markWorldAsNotified();

    assertEquals( action.hasWorldBeenNotified(), true );
  }

  private void assertRunCount( @Nullable final MockRunner runnable, final int expected )
  {
    if ( null != runnable )
    {
      assertEquals( runnable.getRunCount(), expected );
    }
  }

  static final class MockRunner
    implements Runnable
  {
    private int _runCount;

    int getRunCount()
    {
      return _runCount;
    }

    @Override
    public void run()
    {
      _runCount++;
    }

    @Override
    public String toString()
    {
      return "Listener:" + System.identityHashCode( this );
    }
  }

  static final class MockLinkable
    implements Linkable
  {
    @Override
    public boolean isLinked()
    {
      return false;
    }

    @Override
    public void link()
    {
    }

    @Override
    public void invalidate()
    {
    }

    @Override
    public boolean isValid()
    {
      return true;
    }

    @Override
    public String toString()
    {
      return "Entity:" + System.identityHashCode( this );
    }
  }
}
