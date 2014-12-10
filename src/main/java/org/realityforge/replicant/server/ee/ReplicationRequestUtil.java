package org.realityforge.replicant.server.ee;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.replicant.server.ChangeSet;
import org.realityforge.replicant.server.EntityMessage;
import org.realityforge.replicant.server.EntityMessageEndpoint;
import org.realityforge.replicant.server.EntityMessageSet;
import org.realityforge.replicant.shared.transport.ReplicantContext;

/**
 * Utility class for interacting with replication request infrastructure.
 */
public final class ReplicationRequestUtil
{
  /**
   * The key used to access the registry to get the current call depth.
   */
  private static final String REPLICATION_TX_DEPTH = "ReplicationTxDepth";

  private ReplicationRequestUtil()
  {
  }

  /**
   * Start a replication context.
   *
   * @param sessionID the id of the session that initiated change if any.
   * @param requestID the id of the request in the session that initiated change..
   */
  public static void startReplication( @Nonnull final TransactionSynchronizationRegistry registry,
                                       @Nullable final String sessionID,
                                       @Nullable final String requestID )
  {
    if ( null != sessionID )
    {
      registry.putResource( ReplicantContext.SESSION_ID_KEY, sessionID );
    }
    if ( null != requestID )
    {
      registry.putResource( ReplicantContext.REQUEST_ID_KEY, requestID );
    }
  }

  /**
   * Complete a replication context and submit changes for replication.
   *
   * @return true if the request is complete and did not generate any change messages, false otherwise.
   */
  public static boolean completeReplication( @Nonnull final TransactionSynchronizationRegistry registry,
                                             @Nonnull final EntityManager entityManager,
                                             @Nonnull final EntityMessageEndpoint endpoint )
  {
    if ( Status.STATUS_ACTIVE == registry.getTransactionStatus() &&
         entityManager.isOpen() &&
         !registry.getRollbackOnly() )
    {
      final String sessionID = (String) registry.getResource( ReplicantContext.SESSION_ID_KEY );
      final String requestID = (String) registry.getResource( ReplicantContext.REQUEST_ID_KEY );
      boolean requestComplete = true;
      entityManager.flush();
      final EntityMessageSet messageSet = EntityMessageCacheUtil.removeEntityMessageSet( registry );
      final ChangeSet changeSet = EntityMessageCacheUtil.removeSessionChanges( registry );
      if ( null != messageSet || null != changeSet )
      {
        final Collection<EntityMessage> messages =
          null == messageSet ? Collections.<EntityMessage>emptySet() : messageSet.getEntityMessages();
        if ( null != changeSet || messages.size() > 0 )
        {
          requestComplete = !endpoint.saveEntityMessages( sessionID, requestID, messages, changeSet );
        }
      }
      final Boolean complete = (Boolean) registry.getResource( ReplicantContext.REQUEST_COMPLETE_KEY );
      return !( null != complete && !complete ) && requestComplete;
    }
    else
    {
      return true;
    }
  }

  /**
   * Set the current call depth in replication request.
   */
  public static void setReplicationCallDepth( final int depth )
  {
    if ( 0 == depth )
    {
      ReplicantContextHolder.remove( REPLICATION_TX_DEPTH );
    }
    else
    {
      ReplicantContextHolder.put( REPLICATION_TX_DEPTH, depth );
    }
  }

  /**
   * Return the current call depth in the replication request.
   */
  public static int getReplicationCallDepth()
  {
    final Integer depth = (Integer) ReplicantContextHolder.get( REPLICATION_TX_DEPTH );
    if ( null == depth )
    {
      return 0;
    }
    else
    {
      return depth;
    }
  }
}