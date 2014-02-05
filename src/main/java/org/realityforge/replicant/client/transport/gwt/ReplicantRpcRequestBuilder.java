package org.realityforge.replicant.client.transport.gwt;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import org.realityforge.replicant.client.transport.ClientSession;
import org.realityforge.replicant.client.transport.RequestEntry;
import org.realityforge.replicant.client.transport.SessionContext;
import org.realityforge.replicant.shared.transport.ReplicantContext;

public final class ReplicantRpcRequestBuilder
  extends RpcRequestBuilder
{
  public static final ReplicantRpcRequestBuilder INSTANCE = new ReplicantRpcRequestBuilder();

  @Override
  protected void doSetCallback( final RequestBuilder rb, final RequestCallback callback )
  {
    final ClientSession session = SessionContext.getSession();
    if ( null != session )
    {
      rb.setHeader( ReplicantContext.SESSION_ID_HEADER, session.getSessionID() );
    }
    final RequestEntry entry = SessionContext.getRequest();
    if ( null == entry )
    {
      rb.setCallback( callback );
    }
    else
    {
      rb.setHeader( ReplicantContext.REQUEST_ID_HEADER, entry.getRequestID() );
      rb.setCallback( new RequestCallback()
      {
        @Override
        public void onResponseReceived( final Request request, final Response response )
        {
          final boolean messageComplete = "1".equals( response.getHeader( ReplicantContext.REQUEST_COMPLETE_HEADER ) );
          entry.setExpectingResults( !messageComplete );
          if ( null != callback )
          {
            callback.onResponseReceived( request, response );
          }
        }

        @Override
        public void onError( final Request request, final Throwable exception )
        {
          entry.setExpectingResults( false );
          if ( null != callback )
          {
            callback.onError( request, exception );
          }
        }
      } );
    }
  }
}
