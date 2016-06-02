package zhenhai.lifetest.controller.device

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import scala.util.Try
import scala.util.Failure
import java.text.SimpleDateFormat
import java.util.Date

/**
 *  電源供應器的界面定義
 */
trait PowerSupplyInterface {

  /**
   *  設定電壓
   *
   *  @param    voltage   電壓值
   *  @return             如果設定成功就回傳 Success(true) 
   */
  def setVoltage(voltage: Double): Try[Boolean]

  /**
   *  設定是否進行直流電輸出
   *
   *  @param  isOutput    如果要輸出直流電就傳 true
   *  @return             如果設定成功就回傳 Success(true) 
   */
  def setOutput(isOutput: Boolean): Try[Boolean]

  /**
   *  取得電壓設定
   *
   *  @return       Success(目前設定的電壓值) / Failure(失敗原因)
   */
  def getVoltageSetting(): Try[Double]

  /**
   *  取得是否開啟電源輸出
   *
   *  @return       Success(是否輸出) / Failure(失敗原因)
   */
  def getIsOutput(): Try[Boolean]

  /**
   *  開啟電源供應器通訊介面
   *
   *  @return       Success(是否成功開啟) / Failure(失敗原因)
   */
  def open(): Try[Boolean]

  /**
   *  關閉
   */
  def close(): Unit

}

/**
 *  因為在測試流程 Daemon 中，必須每一個子板都有一個電源供應器可用，
 *  但現在實際上只有一台，因此使用此 DummyPowerSupply 做為模擬。
 *
 *  @param    daughterBoard       該子板的編號
 */
class DummyPowerSupply(daughterBoard: Int) extends PowerSupplyInterface {

  var voltage: Double = 0
  var isOutput: Boolean = false 
  def setVoltage(voltage: Double): Try[Boolean] = Try { 
    this.voltage = voltage
    println(s"Set voltage to $voltage in $daughterBoard dummy....")
    true
  }

  def setOutput(isOutput: Boolean): Try[Boolean] = Try {
    println(s"Set isOutput to $isOutput in $daughterBoard dummy....")
    this.isOutput = isOutput
    true
  }

  def getVoltageSetting(): Try[Double] = Try {
    println(s"Reply voltage for $daughterBoard: $voltage")
    voltage
  }

  def getIsOutput(): Try[Boolean] = Try {
    println(s"Reply isOutput for $daughterBoard: $isOutput")
    isOutput
  }

  def open(): Try[Boolean] = Try {
    println(s"Open dummy power supply for $daughterBoard")
    true
  }
  def close(): Unit = {
    println(s"Close dummy power supply for $daughterBoard")
  }

}


/**
 *  GENH600 電源供應器的 RS232 介面
 *
 *  @param    port              RS232 連接埠
 *  @param    deviceAddress     電源供應器本身設定的 Address
 *  @param    baudRate          連接速率
 *  @param    waitForResponse   送出指令的等待間隔
 */
class GENH600(port: String, deviceAddress: Int = 0, baudRate: Int = SerialPort.BAUDRATE_9600, waitForResponse: Int = 300) extends PowerSupplyInterface{

  val serialPort = new SerialPort(port)
  var responseMessage: Option[String] = None

  /**
   *  設定機台回應訊息時的 Callback
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
            responseMessage  = Some(line)
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
   *  送出指令並取回結果，因為 GENH 的所有指令都一定會有回應（例如 OK），
   *  所以我們必須在送出指令後就開始等待 Queue 裡是否有回傳值。
   */
  def sendCommand(command: String, maxTries: Int = 10): Try[String] = retry(maxTries) {
    Try {

      var numberOfTries = 0

      responseMessage = None
      serialPort.writeBytes(s"$command\r".getBytes)

      while (responseMessage.isEmpty && numberOfTries <= 10) {
        numberOfTries += 1
        Thread.sleep(waitForResponse)
      }

      if (numberOfTries > 10) {
        throw PowerSupplyRS232Timeout
      } else {
        val response = responseMessage.get
        responseMessage = None
        response
      }
    }
  }

  /**
   *  關閉 RS232 連接埠
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
        println(s"[${dateTimeFormatter.format(new Date)}] PowerSupply Retry ${count}")
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
   *  開啟 RS232 連接埠
   */
  def open(): Try[Boolean] = {
    serialPort.openPort()
    serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    setResponseCallback()

    // Power Supply 有時會有 BUG，導致開機後的第一個 RS232 指令不會有回應，所以
    // 要送一個 dummy 的指令，讓 Power Supply 回復正常。
    sendCommand("ADR %02d".format(deviceAddress), 1)

    // 這個指令才真的會有來自 Power Supply 的回覆訊息
    sendCommand("ADR %02d".format(deviceAddress)).map(_ == "OK")
  }

  /**
   *  設定是否開啟電流輸出
   *
   *  @param    enable    如果要打開則為 true，否則為 false
   */
  def setOutput(enable: Boolean): Try[Boolean] = {
    enable match {
      case true  => sendCommand("OUT 1").map(_ == "OK")
      case false => sendCommand("OUT 0").map(_ == "OK")
    }
  }

  /**
   *  設定電壓
   *
   *  @param    voltage   額定輸出電壓
   */
  def setVoltage(voltage: Double): Try[Boolean] = {
    sendCommand("PV " + voltage).map(_ == "OK")
  }

  /**
   *  取得設定的電壓值
   */
  def getVoltageSetting(): Try[Double] = { sendCommand("PV?").map(_.toDouble) }

  /**
   *  取得實際的電壓值
   */
  def getRealVoltage(): Try[Double] = { sendCommand("MV?").map(_.toDouble) }

  /**
   *  設定機器是否進到遠端控制模式（只能用 RS232 控制，前端面板無法操作）
   */
  def setRemoteMode(inRemoteMode: Boolean): Try[Boolean] = {
    inRemoteMode match {
      case false => sendCommand("RMT 0").map(_ == "OK")
      case true  => sendCommand("RMT 1").map(_ == "OK")
    }
  }

  /**
   *  設定額定電流
   *
   *  @param    current     額定電流
   */
  def setCurrent(current: Double): Try[Boolean] = {
    sendCommand("PC " + current).map(_ == "OK")
  }

  /**
   *  取得設定的電流值
   */
  def getCurrentSetting(): Try[Double] = { sendCommand("PC?").map(_.toDouble) }

  /**
   *  取得實際的電流值
   */
  def getRealCurrent: Try[Double] = { sendCommand("MC?").map(_.toDouble) }

  /**
   *  取得遠端模式的設定值
   */
  def getRemoteModeSetting = { sendCommand("RMT?") }

  /**
   *  取得目前是否有開啟電源輸出
   */
  def getIsOutput = { sendCommand("OUT?").map(_ == "ON") }


}

