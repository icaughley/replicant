package org.realityforge.replicant.server.json.jackson;

import java.util.Calendar;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.realityforge.replicant.client.RDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class DateSerializerTest
{
  @DataProvider( name = "validDates" )
  public Object[][] validDates()
  {
    return new Object[][]{
      { "2001-01-01", 2001, 1, 1, new RDate( 2001, 1, 1 ) },
      { "2001-10-01", 2001, 10, 1, new RDate( 2001, 10, 1 ) },
      { "2001-02-28", 2001, 2, 28, new RDate( 2001, 2, 28 ) },
      { "2021-12-31", 2021, 12, 31, new RDate( 2021, 12, 31 ) },
    };
  }

  @Test( dataProvider = "validDates" )
  public void serialize( final String expectedFormat, final int year, final int month, final int day, final RDate date )
    throws Exception
  {
    final Calendar instance = Calendar.getInstance();
    instance.set( Calendar.YEAR, year );
    instance.set( Calendar.MONTH, month - 1 );
    instance.set( Calendar.DAY_OF_MONTH, day );
    instance.set( Calendar.HOUR, 0 );
    instance.set( Calendar.MINUTE, 0 );
    instance.set( Calendar.SECOND, 0 );
    instance.set( Calendar.MILLISECOND, 0 );

    final DateSerializer serializer = new DateSerializer();
    final JsonGenerator generator = mock( JsonGenerator.class );
    final SerializerProvider provider = mock( SerializerProvider.class );

    serializer.serialize( instance.getTime(), generator, provider );

    verify( generator ).writeString( expectedFormat );
    assertEquals( RDate.parse( expectedFormat ), date );
  }
}
