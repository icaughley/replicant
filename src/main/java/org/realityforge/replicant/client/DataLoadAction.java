package org.realityforge.replicant.client;

import java.util.HashSet;
import java.util.LinkedList;
import javax.annotation.Nullable;
import org.realityforge.replicant.client.transport.RequestEntry;

/**
 * A simple class encapsulating the process of loading data from a json change set.
 */
final class DataLoadAction
  implements Comparable<DataLoadAction>
{
  /**
   * The raw data string data prior to parsing. Null-ed after parsing.
   */
  @Nullable
  private String _rawJsonData;

  /**
   * The array of changes after parsing. Null prior to parsing.
   */
  @Nullable
  private ChangeSet _changeSet;

  /**
   * The current index into changes.
   */
  private int _changeIndex;

  private LinkedList<Linkable> _updatedEntities = new LinkedList<>();
  private HashSet<Linkable> _removedEntities = new HashSet<>();
  private LinkedList<Linkable> _entitiesToLink;
  private boolean _entityLinksCalculated;
  private boolean _worldNotified;
  private boolean _brokerPaused;
  private RequestEntry _request;

  public DataLoadAction( final String rawJsonData )
  {
    _rawJsonData = rawJsonData;
  }

  public boolean isBulkLoad()
  {
    return null != _request && _request.isBulkLoad();
  }

  @Nullable
  public String getRawJsonData()
  {
    return _rawJsonData;
  }

  public void setChangeSet( @Nullable final ChangeSet changeSet, @Nullable final RequestEntry request )
  {
    _request = request;
    _changeSet = changeSet;
    _rawJsonData = null;
    _changeIndex = 0;
  }

  public RequestEntry getRequest()
  {
    return _request;
  }

  public boolean areChangesPending()
  {
    return null != _changeSet && _changeIndex < _changeSet.getChangeCount();
  }

  public boolean needsBrokerPause()
  {
    return areChangesPending() && !_brokerPaused;
  }

  public boolean hasBrokerBeenPaused()
  {
    return _brokerPaused;
  }

  public void markBrokerPaused()
  {
    _brokerPaused = true;
  }

  public Change nextChange()
  {
    if ( areChangesPending() )
    {
      assert null != _changeSet;
      final Change change = _changeSet.getChange( _changeIndex );
      _changeIndex++;
      return change;
    }
    else
    {
      return null;
    }
  }

  public void changeProcessed( final boolean isUpdate, final Object entity )
  {
    if ( entity instanceof Linkable )
    {
      if ( isUpdate )
      {
        _updatedEntities.add( (Linkable) entity );
      }
      else
      {
        _removedEntities.add( (Linkable) entity );
      }
    }
  }

  public boolean areEntityLinksCalculated()
  {
    return _entityLinksCalculated;
  }

  public void calculateEntitiesToLink()
  {
    _entityLinksCalculated = true;
    _entitiesToLink = new LinkedList<>();
    for ( final Linkable entity : _updatedEntities )
    {
      // In some circumstances a create and remove can appear in same change set so guard against this
      if ( !_removedEntities.contains( entity ) )
      {
        _entitiesToLink.add( entity );
      }
    }
    _updatedEntities = null;
    _removedEntities = null;
  }

  public boolean areEntityLinksPending()
  {
    return null != _entitiesToLink && !_entitiesToLink.isEmpty();
  }

  public Linkable nextEntityToLink()
  {
    if ( areEntityLinksPending() )
    {
      assert null != _entitiesToLink;
      return _entitiesToLink.remove();
    }
    else
    {
      _entitiesToLink = null;
      return null;
    }
  }

  @Nullable
  public ChangeSet getChangeSet()
  {
    return _changeSet;
  }

  @Nullable
  public Runnable getRunnable()
  {
    if( null == _request )
    {
      return null;
    }
    else
    {
      assert _request.isCompletionDataPresent();
      return _request.getRunnable();
    }
  }

  public void markWorldAsNotified()
  {
    _worldNotified = true;
  }

  public boolean hasWorldBeenNotified()
  {
    return _worldNotified;
  }

  @Override
  public String toString()
  {
    return "DataLoad[" +
           "IsBulk=" + isBulkLoad() +
           ",RawJson.null?=" + ( _rawJsonData == null ) +
           ",ChangeSet.null?=" + ( _changeSet == null ) +
           ",ChangeIndex=" + _changeIndex +
           ",Runnable.null?=" + ( getRunnable() == null ) +
           ",UpdatedEntities.size=" + ( _updatedEntities != null ? _updatedEntities.size() : null ) +
           ",RemovedEntities.size=" + ( _removedEntities != null ? _removedEntities.size() : null ) +
           ",EntitiesToLink.size=" + ( _entitiesToLink != null ? _entitiesToLink.size() : null ) +
           ",EntityLinksCalculated=" + _entityLinksCalculated +
           "]";
  }

  @Override
  public int compareTo( final DataLoadAction other )
  {
    final ChangeSet changeSet1 = getChangeSet();
    assert null != changeSet1;
    final ChangeSet changeSet2 = other.getChangeSet();
    assert null != changeSet2;
    return changeSet1.getSequence() - changeSet2.getSequence();
  }
}
