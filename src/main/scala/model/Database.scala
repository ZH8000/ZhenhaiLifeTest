package zhenhai.lifetest.controller.model

import scala.io.Source
import java.io.File
import java.sql.DriverManager


/**
 *  用來代表壽命測試機的 SQLite 資料庫
 *
 *  @param    filename      SQLite 資料庫的檔案名稱
 */
class Database(filename: String) {

  private lazy val connection = DriverManager.getConnection(s"jdbc:sqlite:$filename")

  /**
   *  取得資料庫的資料表的 Schema 定義
   *
   *  @retrun   各個資料表的 SQL CREATE TABLE 敘述句
   */
  def getDBSchemaList: List[String] = {
    val inputStream = this.getClass.getResourceAsStream("/dbSchema.sql")

    if (inputStream != null) {
      val schemaSQL = Source.fromInputStream(inputStream).getLines.mkString("\n")
      schemaSQL.split(";").map(_.trim).toList
    }
    else {
      Nil
    }
  }

  /**
   *  取得 SQLite 資料庫中的資料表名稱列表
   *
   *  @return   資料表名稱的列表
   */
  def getTables(): List[String] = {
    var tableNames: List[String] = Nil
    val result = connection.getMetaData.getTables(null, null, null, null)
    while (result.next()) {
      tableNames ::= result.getString(3)
    }
    result.close
    tableNames
  }

  /**
   *  初始化資料庫
   *
   *  如果開啟的 SQLite 資料庫中沒有償何資料表，那就進行資料表的建立，
   *  以便後續的操作。
   */
  def initDB() {
    if (getTables.size == 0) {
      println("Creating Database Tables....")
      for (createTableSQL <- getDBSchemaList.filter(_.trim.size > 0)) {
        val statement = connection.prepareStatement(createTableSQL)
        println("===================")
        println("Start create table...")
        println(createTableSQL)
        statement.executeUpdate()
        statement.close()
        println("===================")
      }
    }
  }

  def insertOvenUUIDCheckingQueue(testingID: Long) {
    var resultList: List[RoomTemperatureTestingQueue] = Nil
    val statement = connection.prepareStatement(
      "INSERT INTO OvenUUIDCheckingQueue VALUES(?, ?, 0)"
    )
    statement.setLong(1, testingID)
    statement.setLong(2, System.currentTimeMillis)
    statement.executeUpdate()
  }

  def getOvenUUIDCheckingQueue(testingID: Long): Option[OvenUUIDCheckingQueue] = {
    var resultList: List[OvenUUIDCheckingQueue] = Nil
    val statement = connection.prepareStatement(
      """SELECT testingID, insertTime, currentStatus FROM OvenUUIDCheckingQueue WHERE testingID=?"""
    )
    statement.setLong(1, testingID)
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      resultList ::= OvenUUIDCheckingQueue(
        cursor.getLong(1),
        cursor.getLong(2),
        cursor.getInt(3)
      )
    }

    cursor.close()
    statement.close()

    resultList.headOption
  }



  def insertRoomTemperatureTestingQueue(testingID: Long) {
    var resultList: List[RoomTemperatureTestingQueue] = Nil
    val statement = connection.prepareStatement(
      "INSERT INTO RoomTemperatureTestingQueue VALUES(?, ?, 0)"
    )
    statement.setLong(1, testingID)
    statement.setLong(2, System.currentTimeMillis)
    statement.executeUpdate()
  }

  def getRoomTemperatureTestingQueue(testingID: Long): Option[RoomTemperatureTestingQueue] = {
    var resultList: List[RoomTemperatureTestingQueue] = Nil
    val statement = connection.prepareStatement(
      """SELECT testingID, insertTime, currentStatus FROM RoomTemperatureTestingQueue WHERE testingID=?"""
    )
    statement.setLong(1, testingID)
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      resultList ::= RoomTemperatureTestingQueue(
        cursor.getLong(1),
        cursor.getLong(2),
        cursor.getInt(3)
      )
    }

    cursor.close()
    statement.close()

    resultList.headOption
  }

  /**
   *  取得室溫測試佇列中時間最近的一筆資料
   *
   *  註：理論上同一時間內此資料表只會有一筆 currentStatus=0，也就是待處理的資料
   *
   *  @return     室溫測試佇列中的最新一筆待處理的資料
   */
  def getRoomTemperatureTestingQueue: Option[RoomTemperatureTestingQueue] = {
    var resultList: List[RoomTemperatureTestingQueue] = Nil
    val statement = connection.prepareStatement(
      """SELECT testingID, insertTime, currentStatus FROM RoomTemperatureTestingQueue WHERE currentStatus=0 ORDER BY insertTime"""
    )
    val cursor = statement.executeQuery()

    while (cursor.next()) {
      resultList ::= RoomTemperatureTestingQueue(
        cursor.getLong(1),
        cursor.getLong(2),
        cursor.getInt(3)
      )
    }

    cursor.close()
    statement.close()

    resultList.headOption
  }

  /**
   *  取得烤箱 UUID 確認佇列中時間最近的一筆資料
   *
   *  註：理論上同一時間內此資料表只會有一筆資料
   *
   *  @return     烤箱 UUID 確認佇列中的最新一筆資料
   */
  def getOvenUUIDCheckingQueue: Option[OvenUUIDCheckingQueue] = {
    var resultList: List[OvenUUIDCheckingQueue] = Nil
    val statement = connection.prepareStatement(
      """SELECT testingID, insertTime, currentStatus FROM OvenUUIDCheckingQueue WHERE currentStatus=0 ORDER BY insertTime"""
    )
    val cursor = statement.executeQuery()

    while (cursor.next()) {
      resultList ::= OvenUUIDCheckingQueue(
        cursor.getLong(1),
        cursor.getLong(2),
        cursor.getInt(3)
      )
    }

    cursor.close()
    statement.close()

    resultList.headOption
  }

  /**
   *  取得烤箱測試要求佇列中的資料，並依照插入時間遞增排序。
   *
   *  @return     烤箱測試要求佇列中的資料
   */
  def getOvenTestingQueue: List[OvenTestingQueue] = {
    var resultList: List[OvenTestingQueue] = Nil
    val statement = connection.prepareStatement(
      """SELECT testingID, insertTime FROM OvenTestingQueue ORDER BY insertTime DESC"""
    )
    val cursor = statement.executeQuery()

    while (cursor.next()) {
      resultList ::= OvenTestingQueue(
        cursor.getLong(1),
        cursor.getLong(2)
      )
    }

    cursor.close()
    statement.close()

    resultList
  }

  /**
   *  寫入測試結果資料表
   *
   *  @param    tableName 資料表名稱
   *  @param    result    要寫入的室溫測試結果
   */
  def insertTestingResult(tableName: String, result: TestingResult) {
    val statement = connection.prepareStatement(
      s"INSERT INTO $tableName " +
       "(testingID, capacityID, capacity, dxValue, leakCurrent, isCapacityOK, isDXValueOK, isLeakCurrentOK, isOK, timestamp) VALUES " +
       "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
    statement.setLong(1, result.testingID)
    statement.setInt(2, result.capacityID)
    statement.setDouble(3, result.capacity)
    statement.setDouble(4, result.dxValue)
    statement.setDouble(5, result.leakCurrent)
    statement.setBoolean(6, result.isCapacityOK)
    statement.setBoolean(7, result.isDXValueOK)
    statement.setBoolean(8, result.isLeakCurrentOK)
    statement.setBoolean(9, result.isOK)
    statement.setLong(10, result.timestamp)
    statement.executeUpdate()
    statement.close()

  }

  /**
   *  更新測試結果資料表中的 LC 相關數值
   *
   *  @param    tableName 資料表名稱
   *  @param    result    要寫入的室溫測試結果
   */
  def updateTestingResultForLC(tableName: String, result: TestingResult) {
    val statement = connection.prepareStatement(
      s"UPDATE $tableName SET leakCurrent=?, isLeakCurrentOK=? WHERE testingID=? AND capacityID=? AND timestamp=?"
    )
    statement.setDouble(1, result.leakCurrent)
    statement.setBoolean(2, result.isLeakCurrentOK)
    statement.setLong(3, result.testingID)
    statement.setInt(4, result.capacityID)
    statement.setLong(5, result.timestamp)
    statement.executeUpdate()
    statement.close()
  }


  /**
   *  寫入室溫測試結果資料表
   *
   *  @param    result    要寫入的室溫測試結果
   */
  def insertRoomTemperatureTestingResult(result: TestingResult) {
    insertTestingResult("RoomTemperatureTestingResult", result)
  }

  /**
   *  寫入烤箱測試結果資料表
   *
   *  @param    result    要寫入的烤箱測試結果
   */
  def insertOvenTestingResult(result: TestingResult) {
    insertTestingResult("OvenTestingResult", result)
  }

  /**
   *  更新室溫測試結果資料表中的 LC 相關數值
   *
   *  @param    result    要寫入的室溫測試結果
   */
  def updateRoomTemperatureTestingResultForLC(result: TestingResult) {
    updateTestingResultForLC("RoomTemperatureTestingResult", result)
  }

  /**
   *  更新烤箱測試結果資料表中的 LC 相關數值
   *
   *  @param    result    要寫入的烤箱測試結果
   */
  def updateOvenTestingResultForLC(result: TestingResult) {
    updateTestingResultForLC("OvenTestingResult", result)
  }


  /**
   *  將測試單號加入烤箱測試佇列中
   *
   *  @param      testingID     測試單號
   */
  def insertOvenTestingQueue(testingID: Long) {
    val statement = connection.prepareStatement(
      "INSERT INTO OvenTestingQueue (testingID, insertTime) VALUES (?, ?)"
    )
    statement.setLong(1, testingID)
    statement.setLong(2, System.currentTimeMillis)
    statement.executeUpdate()
    statement.close()
  }


  /**
   *  取得測試單資料
   *
   *  @param    testingID       測試單號
   *  @return                   若查無資料則為 {{{None}}}，否則為 {{{Some(測試單資料)}}}
   */
  def getTestingOrder(testingID: Long): Option[TestingOrder] = {
    var result: List[TestingOrder] = Nil
    val statement = connection.prepareStatement(
      "SELECT id, partNo, capacity, voltage, leakCurrent, dxValue, marginOfError, " +
      "testingTime, testingInterval, daughterBoard, testingBoard, tbUUID, startTime, " +
      "lastTestTime, currentStatus, isRoomTemperatureTested FROM TestingOrder WHERE id=?"
    )
    statement.setLong(1, testingID)
    val cursor = statement.executeQuery()
    
    while (cursor.next()) {
      result ::= TestingOrder(
        cursor.getLong(1),
        cursor.getString(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getString(5),
        cursor.getDouble(6),
        cursor.getString(7),
        cursor.getInt(8),
        cursor.getInt(9),
        cursor.getInt(10),
        cursor.getInt(11),
        cursor.getString(12),
        cursor.getLong(13),
        cursor.getLong(14),
        cursor.getInt(15),
        cursor.getBoolean(16)
      )
    }
    cursor.close()
    statement.close()
    result.headOption
  }

  /**
   *  更新測試單資料表中的資料
   *
   *  @param    updated     更新過後的測試單資料
   */
  def updateTestingOrder(updated: TestingOrder) {
    val statement = connection.prepareStatement(
      "UPDATE TestingORDER " +
      "SET " +
      "partNo = ?, capacity = ?, voltage = ?, leakCurrent = ?, dxValue = ?, marginOfError = ?, " +
      "testingTime = ?, testingInterval = ?, daughterBoard = ?, testingBoard = ?, tbUUID = ?, " +
      "startTime = ?, lastTestTime = ?, currentStatus = ?, isRoomTemperatureTested = ? " +
      "WHERE id=?"
    )
    statement.setString(1, updated.partNo)
    statement.setDouble(2, updated.capacity)
    statement.setDouble(3, updated.voltage)
    statement.setString(4, updated.leakCurrent)
    statement.setDouble(5, updated.dxValue)
    statement.setString(6, updated.marginOfError)
    statement.setLong(7, updated.testingTime)
    statement.setLong(8, updated.testingInterval)
    statement.setInt(9, updated.daughterBoard)
    statement.setInt(10, updated.testingBoard)
    statement.setString(11, updated.tbUUID)
    statement.setLong(12, updated.startTime)
    statement.setLong(13, updated.lastTestTime)
    statement.setInt(14, updated.currentStatus)
    statement.setBoolean(15, updated.isRoomTemperatureTested)
    statement.setLong(16, updated.id)
    statement.executeUpdate()
    statement.close()
  }

  /**
   *  刪除烤箱測試佇列中的測試單
   *
   *  @param    testingID     要刪除的測試單號
   */
  def deleteOvenTestingQueue(testingID: Long) {
    val statement = connection.prepareStatement(
      "DELETE FROM OvenTestingQueue WHERE testingID=?"
    )
    statement.setLong(1, testingID)
    statement.executeUpdate()
    statement.close()
  }

  def deleteTestingOrder(testingID: Long) {
    val statement = connection.prepareStatement(
      "DELETE FROM TestingOrder WHERE id=?"
    )
    statement.setLong(1, testingID)
    statement.executeUpdate()
    statement.close()
  }

  def deleteOvenUUIDCheckingQueue(testingID: Long) {
    val statement = connection.prepareStatement(
      "DELETE FROM OvenUUIDCheckingQueue WHERE testingID=?"
    )
    statement.setLong(1, testingID)
    statement.executeUpdate()
    statement.close()
  }



  def deleteTemperatureTest(testingID: Long) {
    val statement = connection.prepareStatement(
      "DELETE FROM RoomTemperatureTestingQueue WHERE testingID=?"
    )
    statement.setLong(1, testingID)
    statement.executeUpdate()
    statement.close()
  }

  /**
   *  更烤箱 UUID 確認佇列資料表中的資料
   *
   *  @param    updated     更新過後的佇列資料
   */
  def updateOvenUUIDCheckingQueue(updated: OvenUUIDCheckingQueue) {
    val statement = connection.prepareStatement(
      "UPDATE OvenUUIDCheckingQueue SET insertTime=?, currentStatus=? WHERE testingID=?"
    )
    statement.setLong(1, updated.insertTime)
    statement.setInt(2, updated.currentStatus)
    statement.setLong(3, updated.testingID)
    statement.executeUpdate()
    statement.close()
  }

  /**
   *  更室溫測試佇列資料表中的資料
   *
   *  @param    updated     更新過後的佇列資料
   */
  def updateRoomTemperatureTestingQueue(updated: RoomTemperatureTestingQueue) {
    val statement = connection.prepareStatement(
      "UPDATE RoomTemperatureTestingQueue SET insertTime=?, currentStatus=? WHERE testingID=?"
    )
    statement.setLong(1, updated.insertTime)
    statement.setInt(2, updated.currentStatus)
    statement.setLong(3, updated.testingID)
    statement.executeUpdate()
    statement.close()
  }

  /**
   *  取得排程時間已到的測試單列表
   *
   *  @return   已經到達時間，需要進行測試的測試單
   */
  def getScheduledTestingOrder(): List[TestingOrder] = {
    var result: List[TestingOrder] = Nil
    val currentTime = System.currentTimeMillis
    val timeIntervalScalar = 1000 * 60
    val statement = connection.prepareStatement(
      "SELECT * FROM TestingOrder " +
      "WHERE (lastTestTime + (?*testingInterval) < ?) " + 
      "AND id NOT IN (SELECT testingID FROM OvenTestingQueue) " + 
      "AND isRoomTemperatureTested AND currentStatus IN (1, 3, 4, 5)"
    )
    statement.setInt(1, timeIntervalScalar)
    statement.setLong(2, currentTime)
    
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      result ::= TestingOrder(
        cursor.getLong(1),
        cursor.getString(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getString(5),
        cursor.getDouble(6),
        cursor.getString(7),
        cursor.getInt(8),
        cursor.getInt(9),
        cursor.getInt(10),
        cursor.getInt(11),
        cursor.getString(12),
        cursor.getLong(13),
        cursor.getLong(14),
        cursor.getInt(15),
        cursor.getBoolean(16)
      )
    }

    cursor.close()
    statement.close()
    result
  }

  /**
   *  取得測試總時間已到達設定值的測試單列表
   *
   *  @return   測試總時間已經到達，需標記成已完成的測試單
   */
  def getCompletedTestingOrder(): List[TestingOrder] = {
    var result: List[TestingOrder] = Nil
    val currentTime = System.currentTimeMillis
    val testingTimeScalar = 1000 * 60
    val postponeInMinutes = 1000 * 60 * 1
    val statement = connection.prepareStatement(
      "SELECT * FROM TestingOrder " +
      "WHERE (startTime + (testingTime * ?) + ? < ?) " + 
      "AND isRoomTemperatureTested AND currentStatus IN (1, 3, 4, 5)"
    )
    statement.setInt(1, testingTimeScalar)
    statement.setInt(2, postponeInMinutes)
    statement.setLong(3, currentTime)
    
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      result ::= TestingOrder(
        cursor.getLong(1),
        cursor.getString(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getString(5),
        cursor.getDouble(6),
        cursor.getString(7),
        cursor.getInt(8),
        cursor.getInt(9),
        cursor.getInt(10),
        cursor.getInt(11),
        cursor.getString(12),
        cursor.getLong(13),
        cursor.getLong(14),
        cursor.getInt(15),
        cursor.getBoolean(16)
      )
    }

    cursor.close()
    statement.close()
    result
  }


  /**
   *  取得仍然在測試中的測試單的電壓設定值
   *
   *  這個函式會返回仍然在進行測試的測試單號的電壓值的設定
   *
   *  @return     Set[測試單號, 設定電壓值]
   */
  def getStillTestingDaughterBoardVoltage(): Set[(Int, Double)] = {

    var result: Set[(Int, Double)] = Set.empty
    val statement = connection.prepareStatement(
      "SELECT daughterBoard, voltage FROM TestingOrder " +
      "WHERE isRoomTemperatureTested AND currentStatus IN (1, 3, 4, 5)"
    )
    
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      val daughterBoard = cursor.getInt(1)
      val voltage = cursor.getDouble(2)
      result += ((daughterBoard, voltage))
    }
    cursor.close()
    statement.close()
    result
  }

  /**
   *  取得特定資料表中某個測試單標記為良品的電容
   *
   *  @param    tableName   資料表名稱
   *  @param    testingID   測試單號
   *  @return               已損壞的電容編號
   */
  def getGoodCapacity(tableName: String, testingID: Long): Set[Int] = {
    var damaged = Set.empty[Int]
    val statement = connection.prepareStatement(
      s"SELECT DISTINCT(capacityID) FROM $tableName " +
      "WHERE testingID=? AND isOK=1"
    )

    statement.setLong(1, testingID)

    val cursor = statement.executeQuery()

    while (cursor.next()) {
      damaged += cursor.getInt(1)
    }

    cursor.close()
    statement.close()

    damaged
  }


  /**
   *  取得特定資料表中某個測試單標記為損壞的電容
   *
   *  @param    tableName   資料表名稱
   *  @param    testingID   測試單號
   *  @return               已損壞的電容編號
   */
  def getDamagedCapacity(tableName: String, testingID: Long): Set[Int] = {
    var damaged = Set.empty[Int]
    val statement = connection.prepareStatement(
      s"SELECT DISTINCT(capacityID) FROM $tableName " +
      "WHERE testingID=? AND isOK=0"
    )

    statement.setLong(1, testingID)

    val cursor = statement.executeQuery()

    while (cursor.next()) {
      damaged += cursor.getInt(1)
    }

    cursor.close()
    statement.close()

    damaged
  }

  /**
   *  取得某個測試單標記為損壞的電容
   *
   *  @param    testingID   測試單號
   *  @return               已損壞的電容編號
   */
  def getDamagedCapacity(testingID: Long): Set[Int] = {
    getDamagedCapacity("RoomTemperatureTestingResult", testingID) ++
    getDamagedCapacity("OvenTestingResult", testingID)
  }

  /**
   *  取得某個測試單標記為良品的電容
   *
   *  @param    testingID   測試單號
   *  @return               已損壞的電容編號
   */
  def getGoodCapacity(testingID: Long): Set[Int] = {
    getGoodCapacity("RoomTemperatureTestingResult", testingID) ++
    getGoodCapacity("OvenTestingResult", testingID)
  }

  /**
   *  新增電容烤箱測試錯誤訊息記錄
   
   *  @param  testingID   測試單號
   *  @param  capacityID  電容編號
   *  @param  message     錯誤訊息
   */
  def insertOvenTestingErrorLog(testingID: Long, capacityID: Int, message: String) {
    val statement = connection.prepareStatement(
      "INSERT INTO OvenTestingErrorLog VALUES(?, ?, ?, ?)"
    )

    statement.setLong(1, testingID)
    statement.setInt(2, capacityID)
    statement.setString(3, message)
    statement.setLong(4, System.currentTimeMillis)
    statement.executeUpdate()
    statement.close()
  }
  
  /**
   *  新增電容室溫測試錯誤訊息記錄
   *
   *  @param  testingID   測試單號
   *  @param  capacityID  電容編號
   *  @param  message     錯誤訊息
   */
  def insertRoomTemperatureTestingErrorLog(testingID: Long, capacityID: Int, message: String) {
    val statement = connection.prepareStatement(
      "INSERT INTO RoomTemperatureTestingErrorLog VALUES(?, ?, ?, ?)"
    )

    statement.setLong(1, testingID)
    statement.setInt(2, capacityID)
    statement.setString(3, message)
    statement.setLong(4, System.currentTimeMillis)
    statement.executeUpdate()
    statement.close()
  }


  def getTestingOrderByBlock(daughterBoard: Int, testBoard: Int): Option[TestingOrder] = {
    var result: List[TestingOrder] = Nil
    val statement = connection.prepareStatement(
      "SELECT * FROM TestingOrder WHERE daughterBoard=? and testingBoard=? ORDER BY id DESC"
    )
    statement.setInt(1, daughterBoard)
    statement.setInt(2, testBoard)
    
    val cursor = statement.executeQuery()
    if (cursor.next()) {
      result ::= TestingOrder(
        cursor.getLong(1),
        cursor.getString(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getString(5),
        cursor.getDouble(6),
        cursor.getString(7),
        cursor.getInt(8),
        cursor.getInt(9),
        cursor.getInt(10),
        cursor.getInt(11),
        cursor.getString(12),
        cursor.getLong(13),
        cursor.getLong(14),
        cursor.getInt(15),
        cursor.getBoolean(16)
      )
    }

    cursor.close()
    statement.close()
    result.headOption
  }

  def getGoodCount(testingID: Long): Int = {
    val goodCapacity = getGoodCapacity(testingID)
    val damagedCapacity = getDamagedCapacity(testingID)
    (goodCapacity -- damagedCapacity).size
  }

  def getTestingForDate(dateString: String): List[TestingOrder] = {
    var result: List[TestingOrder] = Nil
    val statement = connection.prepareStatement(
      "SELECT *, date(startTime/1000, 'unixepoch') as startDate, date(lastTestTime/1000, 'unixepoch') as lastDate " +
      "FROM TestingOrder where startDate <= ? AND lastDate >= ?"
    )
    statement.setString(1, dateString)
    statement.setString(2, dateString)
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      result ::= TestingOrder(
        cursor.getLong(1),
        cursor.getString(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getString(5),
        cursor.getDouble(6),
        cursor.getString(7),
        cursor.getInt(8),
        cursor.getInt(9),
        cursor.getInt(10),
        cursor.getInt(11),
        cursor.getString(12),
        cursor.getLong(13),
        cursor.getLong(14),
        cursor.getInt(15),
        cursor.getBoolean(16)
      )
    }
    cursor.close()
    statement.close()
    result
  }

  def getAllTestingResult(testingID: Long, capacityID: Int): List[TestingResult] = {
    var result: List[TestingResult] = Nil
    val statement = connection.prepareStatement(
      "SELECT * FROM RoomTemperatureTestingResult where testingID=? AND capacityID=? UNION SELECT * FROM OvenTestingResult where testingID=? AND capacityID=? ORDER BY timestamp DESC"
    )

    statement.setLong(1, testingID)
    statement.setInt(2, capacityID)
    statement.setLong(3, testingID)
    statement.setInt(4, capacityID)

    val cursor = statement.executeQuery()
    while (cursor.next()) {
      result ::= TestingResult(
        cursor.getLong(1),
        cursor.getInt(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getDouble(5),
        cursor.getBoolean(6),
        cursor.getBoolean(7),
        cursor.getBoolean(8),
        cursor.getBoolean(9),
        cursor.getLong(10)
      )
    }
    cursor.close()
    statement.close()
    result
  }

  def getTestingResult(testingID: Long, capacityID: Int): Option[TestingResult] = {
    var result: Option[TestingResult] = None
    val statement = connection.prepareStatement(
      "SELECT * FROM RoomTemperatureTestingResult where testingID=? AND capacityID=? UNION SELECT * FROM OvenTestingResult where testingID=? AND capacityID=? ORDER BY timestamp DESC"
    )

    statement.setLong(1, testingID)
    statement.setInt(2, capacityID)
    statement.setLong(3, testingID)
    statement.setInt(4, capacityID)

    val cursor = statement.executeQuery()
    if (cursor.next()) {
      result = Some(
        TestingResult(
          cursor.getLong(1),
          cursor.getInt(2),
          cursor.getDouble(3),
          cursor.getDouble(4),
          cursor.getDouble(5),
          cursor.getBoolean(6),
          cursor.getBoolean(7),
          cursor.getBoolean(8),
          cursor.getBoolean(9),
          cursor.getLong(10)
        )
      )
    }
    cursor.close()
    statement.close()
    result
  }

  def abortTest(testingID: Long) {
    val statement = connection.prepareStatement(
      "UPDATE TestingOrder SET currentStatus=6 WHERE id=?"
    )
    statement.setLong(1, testingID)
    statement.executeUpdate()
  }

  def getVoltageSetting(daughterBoard: Int): Option[Double] = {
    var result: Option[Double] = None
    val statement = connection.prepareStatement(
      "SELECT DISTINCT(voltage) FROM TestingOrder WHERE daughterBoard=? AND currentStatus <= 5"
    )

    statement.setInt(1, daughterBoard)
    
    val cursor = statement.executeQuery()

    if (cursor.next()) {
      result = Some(cursor.getDouble(1))
    }
    cursor.close()
    statement.close()
    result
  }

  def insertNewTestingOrder(partNo: String, capacity: Double, voltage: Double, leakCurrent: String, dxValue: Double, marginOfError: String, testingTime: Int, testingInterval: Int, daughterBoard: Int, testingBoard: Int) = {
    val statement = connection.prepareStatement(
      "INSERT INTO TestingOrder VALUES(NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, 0, 0)"
    )
    statement.setString(1, partNo)
    statement.setDouble(2, capacity)
    statement.setDouble(3, voltage)
    statement.setString(4, leakCurrent)
    statement.setDouble(5, dxValue)
    statement.setString(6, marginOfError)
    statement.setInt(7, testingTime)
    statement.setInt(8, testingInterval)
    statement.setInt(9, daughterBoard)
    statement.setInt(10, testingBoard)
    statement.executeUpdate()
    getTestingOrderByBlock(daughterBoard, testingBoard)
  }

  def getTestingOrderByPartNo(partNo: String) = {
    var result: List[TestingOrder] = Nil
    val statement = connection.prepareStatement(
      "SELECT * FROM TestingOrder where partNo LIKE ? ORDER BY id DESC"
    )
    statement.setString(1, "%" + partNo + "%")
    val cursor = statement.executeQuery()
    while (cursor.next()) {
      result ::= TestingOrder(
        cursor.getLong(1),
        cursor.getString(2),
        cursor.getDouble(3),
        cursor.getDouble(4),
        cursor.getString(5),
        cursor.getDouble(6),
        cursor.getString(7),
        cursor.getInt(8),
        cursor.getInt(9),
        cursor.getInt(10),
        cursor.getInt(11),
        cursor.getString(12),
        cursor.getLong(13),
        cursor.getLong(14),
        cursor.getInt(15),
        cursor.getBoolean(16)
      )
    }
    cursor.close()
    statement.close()
    result
  }

  initDB()

}


