package hal

import play.api.mvc.Result
import services.{Documentation, Transformer}

import scala.concurrent.Future

object HalTransformer extends Transformer{

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def transform(serverResult: Future[Result]): Future[Documentation] = {
    serverResult.map { _ =>
      Documentation("Hello World")
    }
  }
}
