package mamos

import org.junit.Test
import org.junit.Assert._

class TflWebApiTests {
  @Test
  def testExpectedArrivals() = {
    val arrivals = new TflWebApi().getArrivals("940GZZLUWLO","bakerloo")
    assert(arrivals.size > 0)
    arrivals.foreach(arrival => {
      assertEquals("bakerloo", arrival.lineId)
      assertEquals("940GZZLUWLO", arrival.naptanId)
    })
  }
}
