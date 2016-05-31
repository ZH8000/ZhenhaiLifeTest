package zhenhai.lifetest.controller.device

import java.io.File

class RS232Prober {

  var isAbort: Boolean = false

  def abort() {
    isAbort = true
  }

  def getRS232Interfaces: Set[File] = {
    new File("/dev/serial/by-path").listFiles match {
      case null => Set.empty
      case files => files.toSet
    }
  }

  def startProbe(pollingTimeInMillSeconds: Int = 300)(callback: Option[File] => Any)  {
    
    val oldRS232Interface = getRS232Interfaces
    var newRS232Interface: Option[File] = None

    val thread = new Thread() {
      override def run() {
        while (newRS232Interface.isEmpty && isAbort == false) {
          println("Probe....")
          newRS232Interface = (getRS232Interfaces -- oldRS232Interface).headOption
          Thread.sleep(pollingTimeInMillSeconds)
        }
        callback(newRS232Interface)
      }
    }

    thread.start()
  }



}
