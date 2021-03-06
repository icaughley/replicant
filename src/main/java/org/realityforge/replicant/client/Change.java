package org.realityforge.replicant.client;

import java.util.Date;

/**
 * A change to an entity.
 */
public interface Change
{
  /**
   * @return the unique discriminator or designator for the entity. Typically this is the primary key of the entity in the database.
   */
  int getDesignatorAsInt();

  /**
   * @return the unique discriminator or designator for the entity. Typically this is the primary key of the entity in the database.
   */
  String getDesignatorAsString();

  /**
   * @return a code indicating the type of the entity changed.
   */
  int getTypeID();

  /**
   * @return true if the change is an update, false if it is a remove.
   */
  boolean isUpdate();

  /**
   * Return true if data for the attribute identified by the key is present in the change.
   *
   * @param key the attribute key.
   * @return true if the data is present.
   */
  boolean containsKey( String key );

  /**
   * Return true if data for the attribute identified by the key is null.
   *
   * @param key the attribute key.
   * @return true if the data is null.
   */
  boolean isNull( String key );

  int getIntegerValue( String key );

  Date getDateValue( String key );

  String getStringValue( String key );

  boolean getBooleanValue( String key );

  /**
   * @return the number of channels on which the change is sent. Must be &gt; 1.
   */
  int getChannelCount();

  /**
   * Return the channel id at specific index.
   *
   * @param index the index of the channel.
   * @return the channel id.
   */
  int getChannelID( int index );

  /**
   * @param index the index of the channel.
   * @return the sub-channel id.
   */
  Object getSubChannelID( int index );
}
