package zhenhai.lifetest.controller.model

import java.time.LocalTime

/**
 *  測試單資料
 *
 *  用來代表使用者輸入的要執行測試的測試單
 *
 *  currentStatus 的狀態列表如下：
 *
 *   - 0 = 新加入
 *   - 1 = 測試中
 *   - 2 = 高壓 Relay 損壞
 *   - 3 = 找不到烤箱板
 *   - 4 = 其他錯誤
 *   - 5 = 烤箱板 UUID 不符
 *   - 6 = 使用者中止測試
 *   - 7 = 測試完成
 *
 *
 *  @param  id                        測試單號
 *  @param  partNo                    料號
 *  @param  capacity                  電容容量標準值（uF）
 *  @param  voltage                   充電時的電壓
 *  @param  leakCurrent               漏電流標準值
 *  @param  dxValue                   dx 的標準值
 *  @param  marginOfError             可容許誤差級別
 *  @param  testingTime               總測試時間（小時）
 *  @param  testingInterval           每次測試的間隔（小時）
 *  @param  daughterBoard             在第幾號子板（0 到 7）
 *  @param  testingBoard              在第幾號烤箱板（0 或 1）
 *  @param  tbUUID                    測試板的 UUID
 *  @param  startTime                 測試開始時間
 *  @param  lastTestTime              最後一次測試的時間
 *  @param  currentStatus             目前的狀態(0 = 新加入 / 1 = 可執行烤箱測試)
 *  @param  isRoomTemperatureTested   是否已執行過室溫初始測試
 */
case class TestingOrder(
  id: Long, partNo: String, capacity: Double, voltage: Double, 
  leakCurrent: String, dxValue: Double, marginOfError: String, 
  testingTime: Int, testingInterval: Int, daughterBoard: Int, 
  testingBoard: Int, tbUUID: String, startTime: Long, 
  lastTestTime: Long, currentStatus: Int, 
  isRoomTemperatureTested: Boolean
) {

  def duration = {
    val durationInSeconds = (lastTestTime - startTime) / 1000
    LocalTime.ofSecondOfDay(durationInSeconds).toString

  }
}

