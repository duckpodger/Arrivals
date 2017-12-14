package mamos

import java.time.Clock

import org.jmock.lib.concurrent.DeterministicScheduler
import org.scalatest.mockito.MockitoSugar
import org.junit.Test

class ArrivalsTests extends MockitoSugar {
  val scheduler = new DeterministicScheduler

  //todo get a clock linked to the scheduler
  val clock: Clock = ???
  val arrivalsBoard = mock[ArrivalsBoard]

  @Test def testInitialValues() = {}
  @Test def testEmptyResult() = {}
  @Test def testUpdatesToEmptyResult() = {}
  @Test def testUpdatesNextArrivalChanges() = {}
  @Test def testAnnouncements() = {}
}