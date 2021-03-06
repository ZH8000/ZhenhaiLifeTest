package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import zhenhai.lifetest.controller.device._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import java.util.NoSuchElementException
import org.slf4j.LoggerFactory

/**
 *  電源供應器的狀態
 *
 *  @param    isOutput      是否開啟電源輸出
 *  @param    voltage       設定的電壓值
 */
case class PowerSupplyStatus(isOutput: Boolean, voltage: Double)

class MainServerThread extends Thread {

  
  var shouldStopped = false
  val logger = LoggerFactory.getLogger("LifeTest")

  // 新豐廠迷你電腦（因沒有接 LC 所以沒有 LC 的 Port）
  // val mainBoardPort  = "/dev/serial/by-path/pci-0000:00:10.1-usb-0:1:1.0-port0"
  // val power1Port = "/dev/serial/by-path/pci-0000:00:10.0-usb-0:2:1.0-port0"
  // val lcrMeterPort   = "/dev/serial/by-path/pci-0000:00:10.0-usb-0:1:1.0-port0"

  // Toshiba Z30
  val mainBoardPort = LifeTestOptions.getMainBoardRS232Port.get
  val lcrMeterPort  = LifeTestOptions.getLCRRS232Port.get
  val lcMeterPort   = LifeTestOptions.getLCRS232Port.get

  var powerSuppliesPort: Map[Int, String] = getPowerSupplyPorts

  val daughterBoardCount = LifeTestOptions.getMaxDaughterBoardCount       // 總共有幾組測試子板
  val capaciyCount = LifeTestOptions.getMaxCapacityCount                  // 一組的電容有幾顆
  val rtDaughterBoard = LifeTestOptions.getRoomTemperatureDaughterBoard   // 室溫測試板的子板編號
  val rtTestingBoard = LifeTestOptions.getRoomTemperatureTestingBoard     // 室溫測試板的烤箱板編號

  val db = LifeTestOptions.db

  val mainBoard = new MainBoard(mainBoardPort)
  val lcrMeter = new LCRMeter(lcrMeterPort)
  val lcMeter = new RSTLCChecker(lcMeterPort)
  val powerSupplies: Map[Int, PowerSupplyInterface] = initPowerSupplyRS232()
  var powerSuppliesStatus: Map[Int, PowerSupplyStatus] = Map.empty

  def getPowerSupplyPorts() = {
    var powerSuppliesPort: Map[Int, String] = Map.empty
    LifeTestOptions.getPower1RS232Port.foreach{ port => powerSuppliesPort += (0 -> port) }
    LifeTestOptions.getPower2RS232Port.foreach{ port => powerSuppliesPort += (1 -> port) }
    LifeTestOptions.getPower3RS232Port.foreach{ port => powerSuppliesPort += (2 -> port) }
    LifeTestOptions.getPower4RS232Port.foreach{ port => powerSuppliesPort += (3 -> port) }
    LifeTestOptions.getPower5RS232Port.foreach{ port => powerSuppliesPort += (4 -> port) }
    powerSuppliesPort
  }

  /**
   *  初始化各子板的電源供應器 RS232 介面
   */
  def initPowerSupplyRS232(): Map[Int, PowerSupplyInterface] = {
    var result: Map[Int, PowerSupplyInterface] = Map.empty
    for (daughterBoard <- 0 until daughterBoardCount) {

      val powerSupply: PowerSupplyInterface = powerSuppliesPort.get(daughterBoard) match {
        case None       => new DummyPowerSupply(daughterBoard)
        case Some(port) => new GENH600(port)
      }

      result += (daughterBoard -> powerSupply)
    }
    result

  }
  
  /**
   *  檢查烤箱板的 UUID 是否與測試單上室溫測試時的一樣
   *
   *  @param    request     烤箱板 UUID 確認佇列的內容
   */
  def checkOvenUUID(request: OvenUUIDCheckingQueue) {
    logger.info("  ==> 烤箱 UUID 確認")

    val testingOrderHolder = db.getTestingOrder(request.testingID)

    db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 1))

    testingOrderHolder match {
      case None => db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 2))
      case Some(testingOrder) =>

        logger.info("  ==> 測試單：" + testingOrder)
        val daughterBoard = testingOrder.daughterBoard
        val testingBoard = testingOrder.testingBoard

        val initialCheckingAndUUID = for {
          powerSupply             <- Try(powerSupplies(testingOrder.daughterBoard))
          isHVRelayOK             <- mainBoard.isHVRelayOK(daughterBoard, testingBoard) //! if isHVRelayOK
          setPowerOff             <- powerSupply.setOutput(false)
          disableChargeDischarge  <- mainBoard.setChargeMode(daughterBoard, testingBoard, 0)
          setVoltage              <- powerSupply.setVoltage(testingOrder.voltage)
          uuid                    <- mainBoard.getUUID(0, 0)
          enableCharge            <- mainBoard.setChargeMode(daughterBoard, testingBoard, 1)
          disableLCRChannel       <- mainBoard.setLCRChannel(daughterBoard, testingBoard, 0)
          disableLCChannel        <- mainBoard.setLCChannel(daughterBoard, testingBoard, 0)
        } yield (uuid, powerSupply)

        initialCheckingAndUUID match {
          case Failure(e: NoSuchElementException) => 
            db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 3))
            db.insertOvenTestingErrorLog(testingOrder.id, -1, e.toString)
          case Failure(TestBoardNotFound)         => 
            db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 4))
            db.insertOvenTestingErrorLog(testingOrder.id, -1, TestBoardNotFound.toString)
          case Failure(MainBoardRS232Timeout)     => 
            db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 5))
            db.insertOvenTestingErrorLog(testingOrder.id, -1, MainBoardRS232Timeout.toString)
          case Failure(PowerSupplyRS232Timeout)   => 
            db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 6))
            db.insertOvenTestingErrorLog(testingOrder.id, -1, PowerSupplyRS232Timeout.toString)
          case Failure(e)                         => 
            db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 7))
            db.insertOvenTestingErrorLog(testingOrder.id, -1, e.toString)
          case Success((uuid,_)) if uuid != testingOrder.tbUUID => db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 8))
          case Success((uuid, powerSupply)) if uuid == testingOrder.tbUUID => 
            val currentTimestamp = System.currentTimeMillis
            powerSupply.setOutput(true)
            db.updateOvenUUIDCheckingQueue(request.copy(currentStatus = 9))
            db.updateTestingOrder(testingOrder.copy(currentStatus = 1, startTime = currentTimestamp, lastTestTime = currentTimestamp))
            db.insertOvenTestingQueue(testingOrder.id)
        }
        powerSuppliesStatus = powerSuppliesStatus.updated(daughterBoard, PowerSupplyStatus(false, 0))
    }
  }

  /**
   *  開始進行 LC 漏電流測定
   *
   *  @param    powerSupply         該子板的 PowerSupply 的 RS232 物件
   *  @param    testingOrder        測試單號
   *  @param    isRoomTemperature   是否是在進行室溫測試
   *  @param    lcrResultMap        LCR 測定結果（Map[測試時間的 Timestamp, 測試結果]）
   */
  def startLCMeasurement(powerSupply: PowerSupplyInterface, testingOrder: TestingOrder, 
                         isInRoomTemperature: Boolean, lcrResultMap: Map[Int, TestingResult]) {

    logger.info("  ==> LC 測試")

    val damagedCapacity = db.getDamagedCapacity(testingOrder.id)
    val capacityList = (1 to capaciyCount).filterNot(damagedCapacity contains _)
    val daughterBoard = if (isInRoomTemperature) rtDaughterBoard else testingOrder.daughterBoard
    val testingBoard = if (isInRoomTemperature) rtTestingBoard else testingOrder.testingBoard

    logger.info("    ==> 開啟充電模式，充電一分鐘……")

    logger.info("      ==> 關閉充放電電路")
    mainBoard.setChargeMode(daughterBoard, testingBoard, 0)

    logger.info("      ==> 開啟電源供應器輸出")
    powerSupply.setVoltage(testingOrder.voltage)
    powerSupply.setOutput(true)

    logger.info("      ==> 開啟充電電路")
    mainBoard.setChargeMode(daughterBoard, testingBoard, 1)

    logger.info("      ==> 等待一分鐘")
    Thread.sleep(60 * 1000)

    logger.info("   ==> 關閉充電模式，開始測試 LC 數值")

    logger.info("     ==> 關閉電源供應器輸出")
    powerSupply.setOutput(false)

    logger.info("     ==> 關閉充放電電路")
    mainBoard.setChargeMode(daughterBoard, testingBoard, 0)

    for (capacityID <- capacityList) {

      logger.info(s"    ==> 測試編號 $capacityID 的電容")

      //lcMeter.setRange(testingOrder.capacity)

      val testingResult = for {
        lcChannel <- mainBoard.setLCChannel(daughterBoard, testingBoard, capacityID)
        lcResult  <- lcMeter.startMeasure()
      } yield lcResult

      logger.info(testingResult.toString)

      testingResult match {
        case Failure(e) if isInRoomTemperature  => db.insertRoomTemperatureTestingErrorLog(testingOrder.id, capacityID, e.toString)
        case Failure(e) if !isInRoomTemperature => db.insertOvenTestingErrorLog(testingOrder.id, capacityID, e.toString)
        case Success(result) =>

          val originalTestResult = lcrResultMap.get(capacityID).getOrElse(
            TestingResult(
              testingOrder.id, capacityID, 0, 9999,  -1,
              false, false, false, false,
              System.currentTimeMillis
            )
          )
          val updatedTestResult = originalTestResult.copy(
            leakCurrent = result.leakCurrent.toDouble,
            isLeakCurrentOK = true
          )

          if (isInRoomTemperature) {
            db.updateRoomTemperatureTestingResultForLC(updatedTestResult)
          } else {
            db.updateOvenTestingResultForLC(updatedTestResult)
          }
      }
      logger.info(s"      ==> 測試完畢，等待三秒鐘")
      Thread.sleep(1000 * 3)
    }

    logger.info("  ==> LC 測試完畢")

    if (isInRoomTemperature) {
      logger.info("  ==> 室溫測試 LC 完成，放電中……")
      mainBoard.setChargeMode(daughterBoard, testingBoard, 2)
      Thread.sleep(10 * 1000)
      mainBoard.setChargeMode(daughterBoard, testingBoard, 0)
      logger.info("  ==> 放電完成")
    }


  }

  /**
   *  開始進行 LCR 測試
   *
   *  @param    testingOrder              測試單資料
   *  @param    isInRoomTemperature       是否為室溫測試
   *  @return                             LCR 測試儀的測試結果的 HashMap，Key 為電容編號，值為側試結果
   */
  def startLCRMeasurement(testingOrder: TestingOrder, isInRoomTemperature: Boolean): Map[Int, TestingResult] = {

    logger.info("  ==> LCR 測試")

    var testingResultMap: Map[Int, TestingResult] = Map.empty
    val damagedCapacity = db.getDamagedCapacity(testingOrder.id)
    val capacityList = (1 to capaciyCount).filterNot(damagedCapacity contains _)
    val daughterBoard = if (isInRoomTemperature) rtDaughterBoard else testingOrder.daughterBoard
    val testingBoard = if (isInRoomTemperature) rtTestingBoard else testingOrder.testingBoard

    for (capacityID <- capacityList) {

      logger.info(s"    ==> 測試編號 $capacityID 的電容")

      lcrMeter.setRange(testingOrder.capacity)

      val testingResult = for {
        lcrChannel <- mainBoard.setLCRChannel(daughterBoard, testingBoard, capacityID)
        lcrResult  <- lcrMeter.startMeasure()
      } yield lcrResult

      logger.info(testingResult.toString)

      testingResult match {
        case Failure(e) if isInRoomTemperature  => db.insertRoomTemperatureTestingErrorLog(testingOrder.id, capacityID, e.toString)
        case Failure(e) if !isInRoomTemperature => db.insertOvenTestingErrorLog(testingOrder.id, capacityID, e.toString)
        case Success(result) =>
          val isCapacityOK = result.isCapacityOK(testingOrder.capacity, testingOrder.marginOfError)
          val isDXValueOK = result.isDXValueOK(testingOrder.capacity, testingOrder.marginOfError)
          val isLeakCurrentOK = true
          //val isOK = isCapacityOK && isDXValueOK && isLeakCurrentOK
          //val isOK = result.capacityValue != 0 
          val isOK = true
          val testingResult = TestingResult(
            testingOrder.id, capacityID, 
            result.capacityValue.toDouble, result.dxValue.toDouble,  -1,
            isCapacityOK, isDXValueOK, isLeakCurrentOK, isOK,
            System.currentTimeMillis
          )
          
          if (isInRoomTemperature) {
            db.insertRoomTemperatureTestingResult(testingResult)
          } else {
            db.insertOvenTestingResult(testingResult)
          }
          testingResultMap += (capacityID -> testingResult)
      }
    }

    mainBoard.setLCRChannel(daughterBoard, testingBoard, 0)
    testingResultMap
  }

  def runWithLog[T](log: String, block: => Try[T]): Try[T] = {
    logger.info(log)
    block
  }

  /**
   *  執行室溫初始測試
   *
   *  @param    request     室溫初始測試的佇列資料
   */
  def runRoomTemperatureTesting(request: RoomTemperatureTestingQueue) {
    logger.info("  ==> 室溫初始測試")

    val testingOrderHolder = db.getTestingOrder(request.testingID)

    db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 1))

    testingOrderHolder match {
      case None => db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 2))
      case Some(testingOrder) =>

        logger.info("  ==> 測試單：" + testingOrder)

        val initialCheckingAndUUID = for {
          powerSupply            <- Try(powerSupplies(testingOrder.daughterBoard))
          setPowerOff            <- runWithLog("  ==> 關閉電源供應器輸出", powerSupply.setOutput(false))
          isHVRelayOK            <- runWithLog("  ==> 檢查 HV Relay……", mainBoard.isHVRelayOK(rtDaughterBoard, rtTestingBoard))
          disableChargeDischarge <- runWithLog("  ==> 關閉充放電電路", mainBoard.setChargeMode(rtDaughterBoard, rtTestingBoard, 0))
          uuid <- mainBoard.getUUID(0, 0)
        } yield (uuid, powerSupply)

        logger.info("  ==> " + initialCheckingAndUUID)

        initialCheckingAndUUID match {
          case Failure(e: NoSuchElementException) => 
            db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 3))
            db.insertRoomTemperatureTestingErrorLog(testingOrder.id, -1, e.toString)
          case Failure(TestBoardNotFound)         => 
            db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 4))
            db.insertRoomTemperatureTestingErrorLog(testingOrder.id, -1, TestBoardNotFound.toString)
          case Failure(MainBoardRS232Timeout)     => 
            db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 5))
            db.insertRoomTemperatureTestingErrorLog(testingOrder.id, -1, MainBoardRS232Timeout.toString)
          case Failure(e)     => 
            db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 6))
            db.insertRoomTemperatureTestingErrorLog(testingOrder.id, -1, e.toString)
          case Success((uuid, powerSupply))  => 
            val lcrResult = startLCRMeasurement(testingOrder, true)
            startLCMeasurement(powerSupply, testingOrder, true, lcrResult)
            db.updateRoomTemperatureTestingQueue(request.copy(currentStatus = 7))
            db.updateTestingOrder(testingOrder.copy(tbUUID = uuid, isRoomTemperatureTested = true))
        }
        powerSuppliesStatus = powerSuppliesStatus.updated(testingOrder.daughterBoard, PowerSupplyStatus(false, 0))

    }
  }

  /**
   *  執行烤箱測試
   *
   *  @param    request     烤箱測試的佇列資料
   */
  def runOvenTesting(request: OvenTestingQueue) {
    logger.info("  ==> 烤箱測試")

    val testingOrderHolder = db.getTestingOrder(request.testingID)

    testingOrderHolder match {
      case None => // 如果找不到測試單，自動忽略任何動作，因為之後會把 OvenTestingQueue 裡的東西刪掉，因此不需特別處理
      case Some(testingOrder) =>

        val daughterBoard = testingOrder.daughterBoard
        val testingBoard = testingOrder.testingBoard
        logger.info("  ==> 測試單：" + testingOrder)
        logger.info("  ==> testingOrder.currentStatus:" + testingOrder.currentStatus)

        // 有可能測試已經排入烤箱測試佇列後，使用者又在 GUI 上將此測試單取消，或總時間已經到了，
        // 因此在進行烤箱測試前，需先排除這兩個狀態，否則會造成此測試單回復成「測試中」的狀態，
        // 而無法中止。
        if (testingOrder.currentStatus != 6 && testingOrder.currentStatus != 7) {
          
          logger.info("  ==> 真的開始測試")
          db.updateTestingOrder(testingOrder.copy(lastTestTime = System.currentTimeMillis, currentStatus = 1))

          val initialCheckingAndUUID = for {
            powerSupply             <- Try(powerSupplies(daughterBoard))
            setPowerOff             <- runWithLog("  ==> 關閉電源供應器輸出", powerSupply.setOutput(false))
            isHVRelayOK             <- runWithLog("  ==> 檢查高壓 Relay", mainBoard.isHVRelayOK(daughterBoard, testingBoard))
            disableChargeDischarge  <- runWithLog("  ==> 關閉充放電電路", mainBoard.setChargeMode(daughterBoard, testingBoard, 0))
            waitFor5Sec             <- runWithLog("  ==> 等五秒", Try{Thread.sleep(1000 * 5)})
            enableDischarge         <- runWithLog("  ==> 開啟放電電路", mainBoard.setChargeMode(daughterBoard, testingBoard, 2))
            waitFor5Sec             <- runWithLog("  ==> 等五秒", Try{Thread.sleep(1000 * 5)})
            disableChargeDischarge  <- runWithLog("  ==> 關閉充放電電路", mainBoard.setChargeMode(daughterBoard, testingBoard, 0))
            uuid            <- mainBoard.getUUID(0, 0)
          } yield (uuid, powerSupply)

          logger.info(initialCheckingAndUUID.toString)

          initialCheckingAndUUID match {
            case Failure(e: NoSuchElementException) => 
              db.updateTestingOrder(testingOrder.copy(currentStatus = 2))
              db.insertOvenTestingErrorLog(testingOrder.id, -2, e.toString)
            case Failure(TestBoardNotFound)         => 
              db.updateTestingOrder(testingOrder.copy(currentStatus = 3))
              db.insertOvenTestingErrorLog(testingOrder.id, -2, TestBoardNotFound.toString)
            case Failure(MainBoardRS232Timeout)     => 
              db.updateTestingOrder(testingOrder.copy(currentStatus = 4))
              db.insertOvenTestingErrorLog(testingOrder.id, -2, MainBoardRS232Timeout.toString)
            case Failure(e)                         => 
              db.updateTestingOrder(testingOrder.copy(currentStatus = 5))
              db.insertOvenTestingErrorLog(testingOrder.id, -2, e.toString)
            case Success((uuid, _)) if uuid != testingOrder.tbUUID => 
              db.updateTestingOrder(testingOrder.copy(currentStatus = 6))
              db.insertOvenTestingErrorLog(testingOrder.id, -2, "UUID Not match")
            case Success((uuid, powerSupply)) => 
              val lcrResult = startLCRMeasurement(testingOrder, false)
              startLCMeasurement(powerSupply, testingOrder, false, lcrResult)

              logger.info("  ==> LCR / LC 測試完成，開啟充電電路")
              mainBoard.setChargeMode(testingOrder.daughterBoard, testingOrder.testingBoard, 1)
          }

        } else {
          logger.info("  ==> 這是已完成或已中止的測試單")
        }
        powerSuppliesStatus = powerSuppliesStatus.updated(testingOrder.daughterBoard, PowerSupplyStatus(false, 0))
        db.deleteOvenTestingQueue(request.testingID)
    }

  }

  /**
   *  將總時間已到的測試單標記成已完成
   */
  def markTestingOrderCompleted() {
    val completedTestingOrders = db.getCompletedTestingOrder
    completedTestingOrders.foreach { testingOrder =>
      logger.info(s"  ==> 已完成測試單 ${testingOrder.id}，標記為已完成。")
      db.updateTestingOrder(testingOrder.copy(currentStatus = 7))
    }
  }

  /**
   *  將已到測試間隔時間的測試單排程進烤箱測試佇列中
   */
  def scheduleTestingOrder() {
    val scheduledTestingOrders = db.getScheduledTestingOrder
    scheduledTestingOrders.foreach { testingOrder =>
      logger.info(s"  ==> 排程測試單：${testingOrder.id}")
      db.insertOvenTestingQueue(testingOrder.id)
    }

  }

  /**
   *  初始化各子板電源供應器
   *
   *  這個函式會開啟各子板的電源供應器，並且取得目前的設定值，
   *  將其暫存至 powerSuppliesStatus  的狀態中，方便之後的驗
   *  證。
   *
   */
  def initilizePowerSupplies() {
    powerSupplies.foreach { case (daughterBoard, rs232Interface) => 
      rs232Interface.open() 
      val currentVoltageSetting = rs232Interface.getVoltageSetting.getOrElse(0D)
      val currentOutputSetting = rs232Interface.getIsOutput.getOrElse(false)
      logger.info(s"===> currentVol: $currentVoltageSetting, $currentOutputSetting")
      powerSuppliesStatus += (daughterBoard -> PowerSupplyStatus(currentOutputSetting, currentVoltageSetting))
    }
  }

  /**
   *  開啟正在使用中的電源供應器
   *
   *  此函式會抓取資料庫中正在執行的測試單，並取出其設定的電壓值，
   *  並且與程式中的電源供應器暫存狀態比較，並且開啟應該開啟但卻沒
   *  有開的電源供應器，並設定正確的電壓。
   *
   */
  def openUsedPowerSupplies() {

    for {
      (daughterBoard, voltage) <- db.getStillTestingDaughterBoardVoltage
      currentStatus <- powerSuppliesStatus.get(daughterBoard)
      powerSupply <- powerSupplies.get(daughterBoard) if currentStatus.isOutput == false || currentStatus.voltage != voltage
    } {
      logger.info(s"  ==> 設定 $daughterBoard 電壓到 $voltage：")
      logger.info(powerSupply.setVoltage(voltage).toString)
      logger.info(s"  ==> 開啟 $daughterBoard 電壓輸出：")
      logger.info(powerSupply.setOutput(true).toString)

      for {
        newVoltage <- powerSupply.getVoltageSetting
        newOutputState <- powerSupply.getIsOutput
      } {
        logger.info(s" ==> 更新 $daughterBoard 電源供應器狀態")
        powerSuppliesStatus = powerSuppliesStatus.updated(daughterBoard, PowerSupplyStatus(newOutputState, newVoltage))
      }

    }
  }

  /**
   *  關閉沒有在用的電源供應器
   *
   *  此函式會取得目前沒有需要開啟的電源供應器，並且與程式中的電源供應器暫存
   *  狀態比較，並且開啟應該關閉但卻沒有關的電源供應器，並設將電壓設成 0V 並
   *  關閉電源供應器的輸出。
   */
  def shutdownNonUsedPowerSupplies() {
    // 已經沒有執行任何測試的子板 = 全部的子板，扣到有正在執行測試的子板
    val allDaughterBoard = (0 until daughterBoardCount).toSet
    val stillTestingDaughterBoard = db.getStillTestingDaughterBoardVoltage.map(_._1)
    val notTestingDaughterBoard = allDaughterBoard -- stillTestingDaughterBoard

    for {
      daughterBoard <- notTestingDaughterBoard
      currentStatus <- powerSuppliesStatus.get(daughterBoard)
      powerSupply   <- powerSupplies.get(daughterBoard) if (currentStatus.isOutput != false || currentStatus.voltage != 0)
    } {
      logger.info(s"  ==> 設定 $daughterBoard 電壓到 0V：")
      logger.info(powerSupply.setVoltage(0).toString)
      logger.info(s"  ==> 關閉 $daughterBoard 電壓輸出：")
      logger.info(powerSupply.setOutput(false).toString)
        
      for {
        newVoltage <- powerSupply.getVoltageSetting
        newOutputState <- powerSupply.getIsOutput
      } {
        logger.info(s" ==> 更新 $daughterBoard 電源供應器狀態")
        powerSuppliesStatus = powerSuppliesStatus.updated(daughterBoard, PowerSupplyStatus(newOutputState, newVoltage))
      }
    }

  }

  override def run() {

    var count = 0

    mainBoard.open()
    lcrMeter.open()
    lcMeter.open()
    initilizePowerSupplies()
    logger.info("初始化電源供應器及設備完成……")

    while(!shouldStopped) {

      //logger.info(s"==> 進入主迴圈[${count}]")

      val ovenUUIDCheckingRequest = db.getOvenUUIDCheckingQueue
      val roomTemperatureTestingRequest = db.getRoomTemperatureTestingQueue
      val firstInOvenTestingQueue = db.getOvenTestingQueue.headOption

      shutdownNonUsedPowerSupplies()
      openUsedPowerSupplies()

      //
      // 為了讓 GUI 的反應速度更快，每一次進到主迴圈時，都
      // 應該只處理一個佇列中的事情。
      //
      // 且由於各佇列的處理時間方別為：
      //
      //  1. 烤箱板 UUID 確認（幾乎立刻）
      //  2. 室溫初始測試（理論上佇列中只會有一筆資料，最多十個電容）
      //  3. 烤箱測試的第一筆（最慢，且可以延遲處理）
      //
      // 所以在下面的程式碼中，只會依序挑其中一個處理，處理
      // 完後立即進行下一次的迴圈
      //
      if (ovenUUIDCheckingRequest.isDefined) {
        checkOvenUUID(ovenUUIDCheckingRequest.get)
      } else if (roomTemperatureTestingRequest.isDefined) {
        runRoomTemperatureTesting(roomTemperatureTestingRequest.get)
      } else if (firstInOvenTestingQueue.isDefined) {
        runOvenTesting(firstInOvenTestingQueue.get)
      }

      markTestingOrderCompleted()
      scheduleTestingOrder()

      count += 1
      Thread.sleep(100)
    }

    mainBoard.close()
    lcrMeter.close()
    powerSupplies.foreach { case (daughterBoard, rs232Interface) => 
      rs232Interface.close() 
    }

  }
}
