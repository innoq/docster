package integration

import java.net.ServerSocket
import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.matching.RequestPattern._
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import integration.FakeApplicationHelper.withApplication
import play.api.test.FakeApplication

object WireMockHelper {

  def findCapturedRequest(server: WireMockServer): LoggedRequest = {
    server.findRequestsMatching(everything()).getRequests.get(0)
  }

  def withAppAndMock(app: FakeApplication, server: WireMockServer, call: () => Unit): Unit = {
    withApplication(app) {
      () => withWireMock(server) {
        () => {
          call()
        }
      }
    }
  }

  def randomUri: String = {
    "/" + UUID.randomUUID()
  }


  def withWireMock(server: WireMockServer)(call: () => Unit): Unit = {
    server.start()
    try {
      call()
    } finally {
      server.shutdownServer()
    }
  }


  def freePort(): Int = {
    val socket = new ServerSocket(0)
    val port = socket.getLocalPort
    socket.close()
    port
  }

}
