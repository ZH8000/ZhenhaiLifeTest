package tw.com.zhenhai.lifetest

import org.apache.commons.daemon.Daemon
import org.apache.commons.daemon.DaemonContext
import org.apache.commons.daemon.DaemonInitException


class LifeTestDaemon extends Daemon {

  val serverThread = new MainServerThread()

  /*================================================
   *  JSVC Daemon 標準 API 界面實作
   *==============================================*/
  override def start() {
    serverThread.start()
  }

  override def stop() {
    serverThread.shouldStopped = true
    serverThread.join(1000 * 5)
  }

  override def init(context: DaemonContext) { }
  override def destroy() { }


}

