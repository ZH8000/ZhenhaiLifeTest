package zhenhai.lifetest.controller.device

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import scala.util.Try
import scala.util.Failure

/**
 *  主板的 RS232 介面
 *
 *  @param    port                RS232 連接埠
 *  @param    baudRate            連接速率
 *  @param    waitForResponse     送出測定指令後，多久沒有回傳算 Timeout
 */
class MainBoard(port: String, baudRate: Int = SerialPort.BAUDRATE_9600, waitForResponse: Int = 100) {

  val serialPort = new SerialPort(port)
  var dataResultHolder: Option[String] = None

  def getResponse(): Try[String] = Try {
    var numberOfTries = 0
    while ((dataResultHolder.isEmpty) && numberOfTries <= 100) {
      numberOfTries += 1
      Thread.sleep(waitForResponse)
    }

    if (numberOfTries > 100) {
      dataResultHolder = None
      throw MainBoardRS232Timeout
    } else {
      val result = dataResultHolder.get
      if (result.contains("#NOTFOUND#")) {
        throw TestBoardNotFound
      }
      dataResultHolder = None
      result
    }
  }

  def retry[T](maxTries: Int)(block: => Try[T]): Try[T] = {
    var result: Try[T] = Failure(new Exception("No response at all"))
    var count = 0

    while (result.isFailure && count < maxTries) {
      if (count > 0) {
        println(s"Retry ${count}")
        this.close()
        this.open()
      }
      result = block
      count += 1
    }
    result
  }

  def getUUID(daughterBoard: Int, testBoard: Int): Try[String] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$f$$$"

    retry(6) {
      sendCommand(command)
      getResponse.map(_.drop(7).dropRight(1))
    }
  }

  def isHVRelayOK(daughterBoard: Int, testBoard: Int): Try[Boolean] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$d$$$"
    retry(6) {
      sendCommand(command)
      getResponse.map(_ == s"#$daughterBoard#$testBoard#d#1#").filter(_ == true)
    }
  }

  def setChargeMode(daughterBoard: Int, testBoard: Int, chargeMode: Int, waitAfterDischarge: Int = 0): Try[Boolean] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$c$" + chargeMode + "$"

    retry(6) {
      sendCommand(command)
      val result = getResponse.map(line => line == command.replace("$", "#"))
      if (result.isSuccess) {
        Thread.sleep(waitAfterDischarge * 1000)
      }
      result.filter(_ == true)
    }
  }

  def setLCRChannel(daughterBoard: Int, testBoard: Int, capacityNumber: Int): Try[Boolean] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$a$" + "%x".format(capacityNumber) + "$"

    retry(6) {
      sendCommand(command)
      val result = getResponse.map(_ == command.replace("$", "#"))
      if (result.isSuccess) {
        Thread.sleep(1000)
      }
      result.filter(_ == true)
    }
  }

  def setLCChannel(daughterBoard: Int, testBoard: Int, capacityNumber: Int): Try[Boolean] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$b$" + "%x".format(capacityNumber) + "$"
    retry(6) {
      sendCommand(command)
      getResponse.map(_ == command.replace("$", "#")).filter(_ == true)
    }
  }


  /**
   *  設定 RS232 事件的 Listener
   */
  def setupRS232Listener() {
    val eventMask = 
      SerialPortEvent.BREAK + SerialPortEvent.CTS + SerialPortEvent.DSR + SerialPortEvent.ERR +
      SerialPortEvent.RING + SerialPortEvent.RLSD + SerialPortEvent.RXCHAR + SerialPortEvent.RXFLAG + 
      SerialPortEvent.TXEMPTY 

    val eventListener = new SerialPortEventListener {

      val buffer = new StringBuffer

      def processInputBuffer(byteCounts: Int) {

        val data = serialPort.readString().filter(_ != '\r')

        data.foreach { character =>
          if (character == '\n') {
            val line = buffer.toString.trim
            dataResultHolder = Some(line)
            buffer.setLength(0)
          } else {
            buffer.append(character)
          }
        }
      }

      override def serialEvent(event: SerialPortEvent) {
        event.getEventType match {
          case SerialPortEvent.BREAK   => // println("SerialPortEvent.BREAK" + event.getEventValue)
          case SerialPortEvent.CTS     => // println("SerialPortEvent.CTS:" + event.getEventValue)
          case SerialPortEvent.DSR     => // println("SerialPortEvent.DSR:" + event.getEventValue)
          case SerialPortEvent.ERR     => // println("SerialPortEvent.ERR:" + event.getEventValue)
          case SerialPortEvent.RING    => // println("SerialPortEvent.RING:" + event.getEventValue)
          case SerialPortEvent.RLSD    => // println("SerialPortEvent.RLSD:" + event.getEventValue)
          case SerialPortEvent.RXFLAG  => // println("SerialPortEvent.RXFLAG:" + event.getEventValue)
          case SerialPortEvent.TXEMPTY => // println("SerialPortEvent.TXEMPTY:" + event.getEventValue)
          case SerialPortEvent.RXCHAR  => processInputBuffer(event.getEventValue)
        }
      }
    }

    serialPort.setEventsMask(eventMask)
    serialPort.addEventListener(eventListener)
  }

  /**
   *  送出命令給母板
   */
  def sendCommand(command: String) {
    serialPort.writeBytes("\n".getBytes)
    Thread.sleep(100)
    serialPort.writeBytes("\n".getBytes)
    Thread.sleep(100)
    dataResultHolder = None
    serialPort.writeBytes(s"$command\n".getBytes)
  }

  /**
   *  關閉 LCR Meter RS232 通訊埠
   */
  def close() {
    serialPort.removeEventListener()
    serialPort.closePort()
  }

  /**
   *  開啟 LCR Meter 的 RS232 介面
   */
  def open() {
    serialPort.openPort()
    serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    setupRS232Listener()
  }
}


