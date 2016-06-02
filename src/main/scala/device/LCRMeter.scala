package zhenhai.lifetest.controller.device

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import scala.util.Try
import scala.util.Failure
import java.util.Date
import java.text.SimpleDateFormat

/**
 *  RST 的 LCR Meter 的 RS232 介面
 *
 *  @param    port                RS232 連接埠
 *  @param    baudRate            連接速率
 *  @param    waitForResponse     送出測定指令後，多久沒有回傳算 Timeout
 */
class LCRMeter(port: String, baudRate: Int = SerialPort.BAUDRATE_9600, waitForResponse: Int = 300) {

  val serialPort = new SerialPort(port)
  var dataResultHolder: Option[LCRResult] = None

  /**
   *  解析 LCR Meter 回傳的測試結果
   *
   *  LCR 的測試結果為以下的字串，主要以逗號分隔每個欄位：
   *
   *    - XZZZZE-YY,GO,AAAAE-BB,GO,GO
   *
   *  說明如下：
   *
   *  X    - 解析結果前綴
   *  ZZZZ - 0000 - 9999 的數字，為電容值的科學表示法的係數（單位為法拉）
   *  YY   - 00 - 99 的數字，為電容值的科學表示法的指數（單位為法拉）
   *  GO   - 電容值測定結果
   *  AAAA - dx 值的科學表式法的係數
   *  BB   - dx 值的科學表式法的指數
   *  GO   - dx 值判定結果
   *  GO   - 總體判定結果
   *
   */
  def parseResult(line: String): LCRResult = {
    val Array(capacity, capacityStatus, dx, dxStatus, totalStatus) = line.drop(1).split(",")
    val Array(capacityCoefficient, capacityExp) = capacity.split("E").map(_.toInt)
    val Array(dxCoefficient, dxExp) = dx.split("E").map(_.toInt)
    val capacityValue = capacityCoefficient * BigDecimal(Math.pow(10, capacityExp)) * BigDecimal(Math.pow(10, 6))
    val dxValue = dxCoefficient * BigDecimal(Math.pow(10, dxExp))
    println("LCRResult:" + line)
    LCRResult(capacityValue, capacityStatus, dxValue, dxStatus, totalStatus)
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

        val data = serialPort.readString().filter(_ != '\n')

        data.foreach { character =>
          if (character == '\r') {
            val line = buffer.toString.trim
            if (line.size == 27 && line(0) == 'X') {
              dataResultHolder = Some(parseResult(line))
            }
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
   *  送出命令給 LCR Meter
   *
   *  @param      command       要傳送的指令
   */
  def sendCommand(command: String) {
    serialPort.writeBytes(s"$command\r\n".getBytes)
  }

  /**
   *  關閉 LCR Meter RS232 通訊埠
   */
  def close() {
    serialPort.closePort()
  }

  /**
   *  重試某個 block 裡的動作
   *
   *  @param    maxTries        最多試幾次
   *  @param    interval        每次重試時要間隔幾秒
   */
  def retry[T](maxTries: Int, interval: Int = 60)(block: => Try[T]): Try[T] = {
    val dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    var result: Try[T] = Failure(new Exception("Power supply no response at all"))
    var count = 0

    while (result.isFailure && count < maxTries) {
      if (count > 0) {
        println(s"[${dateTimeFormatter.format(new Date)}] LCR Retry ${count}")
        Thread.sleep(interval * 1000 * count)
        this.close()
        this.open()
      }
      result = block
      count += 1
    }
    result
  }

  /**
   *  開始測定並取得測定結果
   *
   *  @return       測定結果
   */
  def startMeasure(): Try[LCRResult] = retry(10) {
    Try {

      dataResultHolder = None

      sendCommand("T0")

      // 透過 RS232 送過 Trigger
      sendCommand("E")

      // 叫 LC Cheker 送回測試結果
      // ESC+D (^]D)
      sendCommand(Character.toString(27) + "D")

      var numberOfTries = 0
      while ((dataResultHolder.isEmpty) && numberOfTries <= 10) {
        numberOfTries += 1
        Thread.sleep(waitForResponse)
      }

      if (numberOfTries > 10) {
        throw LCRMeterRS232Timeout
      } else {
        val result = dataResultHolder.get
        dataResultHolder = None
        result
      }
    }
  }

  /**
   *  設定測試電容範圍
   *
   *  @param    range           要設定的測定範圍
   */
  def setRange(range: CapacityRange): Unit = {
    range match {
      case Range400nF => sendCommand("R2")
      case Range4uF   => sendCommand("R3")
      case Range40uF  => sendCommand("R4")
      case Range400uF => sendCommand("R5")
      case Range4mF   => sendCommand("R6")
      case Range40mF  => sendCommand("R7")
    }
  }

  /**
   *  設定測試電容範圍
   *
   *  @param    capacityInUF    電容上標示的電容值（單位為 uF）
   */
  def setRange(capacityInUF: BigDecimal): Unit = {
    if (capacityInUF >= 0.02 && capacityInUF <= 0.399) {
      setRange(Range400nF)
    } else if (capacityInUF >= 0.2 && capacityInUF <= 3.999) {
      setRange(Range4uF)
    } else if (capacityInUF >= 2 && capacityInUF <= 39.99) {
      setRange(Range40uF)
    } else if (capacityInUF >= 20 && capacityInUF <= 399.9) {
      setRange(Range400uF)
    } else if (capacityInUF >= 200 && capacityInUF <= 3999) {
      setRange(Range4mF)
    } else if (capacityInUF >= 2000 && capacityInUF <= 39990) {
      setRange(Range40mF)
    }
  }

  /**
   *  開啟 LCR Meter 的 RS232 介面
   */
  def open() {
    serialPort.openPort()
    serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    setupRS232Listener()

    // 將 Trigger 設成由 RS232 控制，面板無作用
    sendCommand("T0")
  }
}


