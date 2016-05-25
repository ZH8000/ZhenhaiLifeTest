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

  /**
   *  取得主板回應訊息
   *
   *  @return     Success(主板回傳的訊息), Failure(失敗原因)
   */
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

  /**
   *  重試某個 block 裡的動作
   *
   *  @param    maxTries        最多試幾次
   *  @param    interval        每次重試時要間隔幾秒
   */
  def retry[T](maxTries: Int, interval: Int = 60)(block: => Try[T]): Try[T] = {
    var result: Try[T] = Failure(new Exception("No response at all"))
    var count = 0

    while (result.isFailure && count < maxTries) {
      if (count > 0) {
        println(s"Retry ${count}")
        Thread.sleep(interval * 1000)
        this.close()
        this.open()
      }
      result = block
      count += 1
    }
    result
  }

  /**
   *  取得某塊烤箱板的 UUID
   *
   *  @param    daughterBoard     子板編號
   *  @param    testBoard         烤箱板編號
   *  @return                     Success(烤箱板的 UUID) / Failure(失敗原因)
   */
  def getUUID(daughterBoard: Int, testBoard: Int): Try[String] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$f$$$"

    retry(6) {
      sendCommand(command)
      getResponse.map(_.drop(7).dropRight(1))
    }
  }

  /**
   *  取得烤箱板高壓 Relay 是否正常
   *
   *  @param    daughterBoard     子板編號
   *  @param    testBoard         烤箱板編號
   *  @return                     Success(高壓 Relay 是否正常的 Boolean 值) / Failure(失敗原因)
   */
  def isHVRelayOK(daughterBoard: Int, testBoard: Int): Try[Boolean] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$d$$$"
    retry(6) {
      sendCommand(command)
      getResponse.map(_ == s"#$daughterBoard#$testBoard#d#1#").filter(_ == true)
    }
  }

  /**
   *  設定充放電模式
   *
   *  @param    daughterBoard       子板編號
   *  @param    testBoard           烤箱板編號
   *  @param    chargeMode          0 = 關閉充放電電路 / 1 = 開啟充電電路 / 2 = 開啟放電電路
   *  @param    waitAfterSetting    成功設定之後等幾秒才返回
   *  @return                       Success(是否成功設定充放電模式) / Failure(失敗原因)
   */
  def setChargeMode(daughterBoard: Int, testBoard: Int, chargeMode: Int, waitAfterSetting: Int = 0): Try[Boolean] = {
    val command = "$" + daughterBoard + "$" + testBoard + "$c$" + chargeMode + "$"

    retry(6) {
      sendCommand(command)
      val result = getResponse.map(line => line == command.replace("$", "#"))
      if (result.isSuccess) {
        Thread.sleep(waitAfterSetting * 1000)
      }
      result.filter(_ == true)
    }
  }

  /**
   *  設定 LCR 通道
   *
   *  @param    daughterBoard       子板編號
   *  @param    testBoard           烤箱板編號
   *  @param    capacityNumber      電容編號，0 為全部關閉
   *  @return                       Success(是否成功設定 LCR 通道) / Failure(失敗原因)
   */
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

  /**
   *  設定 LC 通道
   *
   *  @param    daughterBoard       子板編號
   *  @param    testBoard           烤箱板編號
   *  @param    capacityNumber      電容編號，0 為全部關閉
   *  @return                       Success(是否成功設定 LC 通道) / Failure(失敗原因)
   */
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


