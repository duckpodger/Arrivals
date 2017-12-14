package mamos

trait ArrivalsBoard {
  def send_arrival(arrival: String):Unit
  def send_following_arrivals(arrivals: Seq[String]):Unit
}
