package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import java.util.Date
import java.text.SimpleDateFormat
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.concurrent._
import java.util.prefs.Preferences
import scala.util.Try

/**
 *  壽命測試機的相關設定
 */
object LifeTestOptions {

  Class.forName("org.sqlite.JDBC")

  val prefsNodeName = "/zhenhai/lifetest/prefs"
  val prefs = Preferences.userRoot().node(prefsNodeName)

  def setMainBoardRS232Port(port: String) {
    prefs.put("mainBoardRS232", port)
  }

  def setLCRRS232Port(port: String) {
    prefs.put("lcrRS232", port)
  }

  def setLCRS232Port(port: String) {
    prefs.put("lcRS232", port)
  }

  def setPower1RS232Port(port: String) {
    prefs.put("power1RS232", port)
  }

  def setPower2RS232Port(port: String) {
    prefs.put("power2RS232", port)
  }

  def setPower3RS232Port(port: String) {
    prefs.put("power3RS232", port)
  }

  def setPower4RS232Port(port: String) {
    prefs.put("power4RS232", port)
  }

  def setPower5RS232Port(port: String) {
    prefs.put("power5RS232", port)
  }

  def setMaxCapacityCount(count: Int) {
    prefs.putInt("maxCapacityCount", count)
  }

  def setMaxDaughterBoardCount(count: Int) {
    prefs.putInt("maxDaughterBoardCount", count)
  }

  def setRoomTemperatureDaughterBoard(count: Int) {
    prefs.putInt("roomTemperatureDaughterBoard", count)
  }

  def setRoomTemperatureTestingBoard(count: Int) {
    prefs.putInt("roomTemperatureTestingBoard", count)
  }

  def save() {
    prefs.flush()
  }

  def getRoomTemperatureDaughterBoard: Int = prefs.getInt("roomTemperatureDaughterBoard", 0)
  def getRoomTemperatureTestingBoard: Int = prefs.getInt("roomTemperatureTestingBoard", 0)

  def getMaxCapacityCount: Int = prefs.getInt("maxCapacityCount", 3)
  def getMaxDaughterBoardCount: Int = prefs.getInt("maxDaughterBoardCount", 1)
  def getMainBoardRS232Port: Option[String] = Option(prefs.get("mainBoardRS232", null))
  def getLCRRS232Port: Option[String] = Option(prefs.get("lcrRS232", null))
  def getLCRS232Port: Option[String] = Option(prefs.get("lcRS232", null))
  def getPower1RS232Port: Option[String] = Option(prefs.get("power1RS232", null))
  def getPower2RS232Port: Option[String] = Option(prefs.get("power2RS232", null))
  def getPower3RS232Port: Option[String] = Option(prefs.get("power3RS232", null))
  def getPower4RS232Port: Option[String] = Option(prefs.get("power4RS232", null))
  def getPower5RS232Port: Option[String] = Option(prefs.get("power5RS232", null))


  /**
   *  SQLite 資料庫存放的網址
   */
  val databasePath = "/home/brianhsu/WorkRoom/ZhenhaiLifeTest/test.db"
  //val databasePath = "/home/zhenhai/test.db"

  /**
   *  測試時可以選擇的電壓列表
   */
  val voltageList = List(4, 6.3, 10, 16, 25, 35, 50, 63, 80, 100, 160, 200, 220, 250, 315, 350, 400, 420, 450, 500)

  /**
   *  測試時可以選擇的測試總時間列表
   */
  val testingTimeList = List(100, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 500000)

  /**
   *  測試時可以選擇的電容值列表
   */
  val capacityList = List(
    0.1, 0.22, 0.33, 0.47, 1, 2.2, 3.3, 4.7, 6.8, 10, 15, 18, 22,
    27, 33, 39, 47, 56, 68, 82, 100, 120, 150, 180, 220, 270, 330,
    390, 470, 560, 680, 820, 1000, 1200, 1500, 1800, 2200, 2700, 
    3300, 3900, 4700, 5600, 6800, 8200, 10000, 12000, 15000, 18000, 22000
  )

  /**
   *  測試時可以選擇的誤差範圍列表
   */
  val marginOfErrorList = List("A: 0 ~ +20", "B: -20 ~ 0", "D: -25 ~ +20", "K: -10 ~ +10", "M: -20 ~ +20", "Y: -10 ~ +20")

  /**
   *  測試時可以選擇的漏電流值列表
   */
  val leakCurrentList = List("I=0.01CV or 3uA", "I=0.03CV or 4uA", "I=0.1CV+40", "I=0.04CV+100", "I=0.06CV+10uA", "I=0.03CV or 3uA")

  /**
   *  測試時可以選擇的損失角列表
   */
  val dxList = List(0.08, 0.09, 0.1, 0.12, 0.14, 0.15, 0.16, 0.19, 0.2, 0.22, 0.24, 0.25, 0.28)

  /**
   *  測試時可以選擇的測試間隔時間
   */
  val intervalList = List(1, 2, 3, 5, 6, 10, 12, 24, 50, 60, 120, 180, 300, 360, 600, 720, 1440, 3000)

  /**
   *  從誤差範圍代碼（A / B / D / K / M / Y）轉成完整的說明字串
   *
   *  @param      code      誤差範圍代碼（A / B / D / K / M / Y）
   *  @return               完整的說明字串
   */
  def marginOfErrorCodeToFullText(code: String) = {
    marginOfErrorList.filter(_.startsWith(code))(0)
  }

  /**
   *  存放測試資料的 SQLite 資料庫物件
   */
  val db = new Database(databasePath)

}

