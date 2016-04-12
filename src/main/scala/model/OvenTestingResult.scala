package zhenhai.lifetest.controller.model

/**
 *  烤箱測試結果
 *
 *  @param    testingID     測試單號
 *  @param    capacityID    第幾顆電容
 *  @param    capacity      電容容量（uF）
 *  @param    dxValue       電容 dx 值
 *  @param    isOK          電容是否良好的判定結果
 *  @param    timestamp     測試的時間
 */
case class OvenTestingResult(testingID: Long, capacityID: Int, capacity: Double, dxValue: Double, isOK: Boolean, timestamp: Long)

