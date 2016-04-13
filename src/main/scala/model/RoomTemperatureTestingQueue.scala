package zhenhai.lifetest.controller.model

/**
 *  室溫測試請求佇列
 *
 *  當使用者在 GUI  程式中按下「室溫初始測試」時，會將測試單號寫入這個佇列中，
 *  當此 Daemon 程式發現此佇列中有資料時，會進行室溫初始測試。
 *
 *  其測試狀態或發生錯誤時會寫入此表的 currentStatus  欄位。
 *
 *  若 currentStatus 為 2，代表找不到測試單，GUI 需顯示錯誤並直接終止流程。
 *  若 currentStatus 為 3，代表烤箱板 HV 高壓損壞，GUI 應顯示錯誤並終止流程並提示更換。
 *  若 currentStatus 為 4，代表找不到烤箱板，應提示使用者重插。
 *  若 currentStatus 為 5，代表主板 RS232 回應逾時
 *  若 currentStatus 為 6，代表發生其他錯誤，顯示是否重試。
 *
 *  當 currentStatus 為 7 代表正常完成測試程序，室溫測試的結果會寫入 RoomTemperatureTestingResult 
 *  資料表中。
 *
 *  @param    testingID         測試單號
 *  @param    insertTime        新增時間
 *  @param    currentStatus     目前狀態
 */
case class RoomTemperatureTestingQueue(testingID: Long, insertTime: Long, currentStatus: Int)

