package zhenhai.lifetest.controller.device

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import scala.util.Try

/**
 *  RST 的 LC Checker 的 RS232 介面
 *
 *  @param    port                RS232 連接埠
 *  @param    baudRate            連接速率
 *  @param    waitForResponse     送出指令前要等待多少個微秒
 */
class RSTLCChecker(port: String, baudRate: Int = SerialPort.BAUDRATE_9600, waitForResponse: Int = 300) {

  val serialPort = new SerialPort(port)
  val dataResultQueue = scala.collection.mutable.Queue[LCResult]()

  /**
   *  處理 LC Checker 回報的數據
   *
   *  @parm     line    LCCheker 送回來的字串
   *  @return           解析過後的結果        
   */
  def parseResult(line: String) = {
    val Array(lcValue, currentStatus, overleak, conductive) = line.drop(1).split(",")
    val isOverleakOK = overleak == "OLOK"
    val isConductiveOK = overleak == "CCHOK"
    val Array(coeffection, exponential) = lcValue.split("E").map(_.toInt)

    LCResult(coeffection, exponential, currentStatus, isOverleakOK, isConductiveOK)
  }

  /**
   *  設定 RS232 接收到資料時處理資料的 Callback
   */
  def setResponseCallback() {
    val eventMask =
      SerialPortEvent.BREAK + SerialPortEvent.CTS + SerialPortEvent.DSR + SerialPortEvent.ERR +
      SerialPortEvent.RING + SerialPortEvent.RLSD + SerialPortEvent.RXCHAR + SerialPortEvent.RXFLAG +
      SerialPortEvent.TXEMPTY

    val eventListener = new SerialPortEventListener {

      val buffer = new StringBuffer

      def processInputBuffer(byteCounts: Int) {
        val data = serialPort.readString().filter(_ != '\n')

        data.foreach { character =>
          if (character == '\r') {
            val line = buffer.toString.trim
            println(line)
            
            if ((line.size == 23 || line.size == 24 ) && line(0) == 'X') {
              dataResultQueue.enqueue(parseResult(line))
            }

            buffer.setLength(0)
          } else {
            buffer.append(character)
          }
        }
      }

      override def serialEvent(event: SerialPortEvent) {
	if (event.getEventType == SerialPortEvent.RXCHAR) {
          processInputBuffer(event.getEventValue)
        }
      }
    }

    serialPort.setEventsMask(eventMask)
    serialPort.addEventListener(eventListener)
  }

  /**
   *  設定下限值
   *
   *  @param    limit   下限值
   */
  def setLowLimit(limit: Int) {
    sendCommand("LL" + "%04d".format(limit))
  }

  /**
   *  設定上限值
   *
   *  @param    limit   上限值
   */
  def setHighLimit(limit: Int) {
    sendCommand("LH" + "%04d".format(limit))
  }

  /**
   *  設定 Overleak
   *
   *  @param    current   overleak 電流
   */
  def setOverleak(current: Int) {
    sendCommand("OL" + "%03d".format(current))
  }

  /**
   *  開始測定並取得測定結果
   *
   *  @return       測定結果
   */
  def startMeasure(): Try[LCResult] = Try {

    // 透過 RS232 送過 Trigger
    sendCommand("E")

    // 叫 LC Cheker 送回測試結果
    // ESC+D (^]D)
    sendCommand(Character.toString(27) + "D")

    var numberOfTries = 0
    while ((dataResultQueue.isEmpty) && numberOfTries <= 10) {
      numberOfTries += 1
      Thread.sleep(waitForResponse)
    }

    if (numberOfTries > 10) {
      throw LCCheckerRS232Timeout
    } else {
      dataResultQueue.dequeue
    }
  }

  /**
   *  送出結果
   *
   *  @param    command     要送給 LCCheker 的資料
   */
  def sendCommand(command: String) {
    serialPort.writeBytes("\r\n".getBytes)
    Thread.sleep(waitForResponse)
    serialPort.writeBytes(s"$command\r\n".getBytes)
    Thread.sleep(waitForResponse)
    serialPort.writeBytes("\r\n".getBytes)
  }

  /**
   *  關閉 RS232 連接埠
   */
  def close() {
    serialPort.closePort()
  }

  /**
   *  開啟 RS232 連接埠並設定 Trigger 為遠端模式
   */
  def open() {
    serialPort.openPort()
    serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    setResponseCallback()

    // 設定 LC Cheker 的 Trigger 為由 RS232 進行啟動
    sendCommand("T0")
  }

  /**
   *  設定 LCCheker 的測定 Range
   *
   *  @param    range   測試範圍
   */
  def setRange(range: RangeSetting) {
    range match {
      case Range200na => sendCommand("R1")
      case Range2ua   => sendCommand("R2")
      case Range20ua  => sendCommand("R3")
      case Range200ua => sendCommand("R4")
      case Range2ma   => sendCommand("R5")
    }
  }
}
