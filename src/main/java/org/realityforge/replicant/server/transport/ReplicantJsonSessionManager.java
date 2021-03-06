package org.realityforge.replicant.server.transport;

import org.realityforge.replicant.server.json.JsonEncoder;

/**
 * Base class for session managers.
 */
public abstract class ReplicantJsonSessionManager<T extends ReplicantSession>
  extends ReplicantSessionManager<T>
{
  protected String pollJsonData( final T session, final int lastSequenceAcked )
  {
    final Packet packet = pollPacket( session, lastSequenceAcked );
    if ( null != packet )
    {
      return JsonEncoder.
        encodeChangeSet( packet.getSequence(), packet.getRequestID(), packet.getETag(), packet.getChangeSet() );
    }
    else
    {
      return null;
    }
  }
}
