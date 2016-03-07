import javax.inject.Inject

import filter.AccessLogFilter
import play.api.http.HttpFilters

class Filters @Inject()(access: AccessLogFilter) extends HttpFilters {

  val filters = Seq(access)
}
