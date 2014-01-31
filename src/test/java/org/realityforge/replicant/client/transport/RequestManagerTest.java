package org.realityforge.replicant.client.transport;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class RequestManagerTest
{
  @Test
  public void basicWorkflow()
  {
    final RequestManager rm = new RequestManager();
    final RequestEntry e = rm.newRequestRegistration( true );
    assertEquals( e.isBulkLoad(), true );

    assertEquals( rm.getRequest( e.getRequestID() ), e );
    assertEquals( rm.getRequest( "NotHere" + e.getRequestID() ), null );

    assertTrue( rm.removeRequest( e.getRequestID() ) );
    assertFalse( rm.removeRequest( e.getRequestID() ) );

    assertEquals( rm.getRequest( e.getRequestID() ), null );
  }

  @Test
  public void completedMarksAsReturned()
  {
    final RequestEntry e = new RequestEntry( "a1", true );

    assertEquals( e.hasReturned(), false );
    assertEquals( e.isCompleted(), false );
    e.complete();
    assertEquals( e.hasReturned(), true );
    assertEquals( e.isCompleted(), true );
  }
}