package integration

import play.api.{Application, Play}

object FakeApplicationHelper {


  def withApplication(app: Application)(call: () => Unit): Unit = {
    Play.start(app)
    try {
      call()
    }
    finally {
      Play.stop(app)
    }
  }

}
