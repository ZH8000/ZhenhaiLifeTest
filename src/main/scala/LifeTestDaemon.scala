package tw.com.zhenhai.lifetest

import org.apache.commons.daemon.Daemon
import org.apache.commons.daemon.DaemonContext
import org.apache.commons.daemon.DaemonInitException

/**
 *  壽命測試機的 JSVC 的 Daemon 包裝用類別
 */
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

