package zhenhai.lifetest.controller.model

/**
 *  測試結果
 *
 *  @param    testingID       測試單號
 *  @param    capacityID      第幾顆電容
 *  @param    capacity        電容容量（uF）
 *  @param    dxValue         電容 dx 值
 *  @param    leakCurrent     漏電流值
 *  @param    isCapacityOK    電容值是否在範圍內
 *  @param    isDXValueOK     電容 dx 值是否在範圍內
 *  @param    isLeakCurrentOK 漏電流值是否在範圍內
 *  @param    isOK            電容是否良好的判定結果
 *  @param    timestamp       測試的時間
 */
case class TestingResult(testingID: Long, capacityID: Int, capacity: Double, dxValue: Double, leakCurrent: Double, isCapacityOK: Boolean, isDXValueOK: Boolean, isLeakCurrentOK: Boolean, isOK: Boolean, timestamp: Long)

