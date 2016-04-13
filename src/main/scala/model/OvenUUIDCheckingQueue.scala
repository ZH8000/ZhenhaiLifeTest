package zhenhai.lifetest.controller.model

/**
 *  烤箱板 UUID 確認佇列
 *
 *  當使用者在 GUI  程式中按下「開始烤箱測試」時，會先將測試單號寫入這個佇列資料表中，
 *  讓 Daemon 程式可以確認使用者插入的烤箱板是否與原來在做室溫初始測試時的測試板相同。
 *
 *  若一切正常，會將 TestingOrder 資料表中的狀態設為可執行測試，並開啟高壓充電通路。
 *
 *  其測試結果會寫入資料表的 currentStatus 欄位中，若 GUI  部份的程式發現 currentStatus  
 *  的狀態是代碼有以下異常，需提示使用者：
 *  
 *  - 2 = 找不到此測試單號 
 *  - 3 = 高壓 Relay 異常
 *  - 4 = 找不到測試板
 *  - 5 = 主板 RS232 回應逾時
 *  - 6 = 電源供應器 RS232 回應逾時
 *  - 7 = 其他錯誤
 *  - 8 = 測試完畢，UUID 不同
 *
 *  若 currentStatus 為 9 則代表測試結束，UUID 相同，且進入充電模式
 *
 *  @param    testingID         測試單號
 *  @param    insertTime        新增時間
 *  @param    currentStatus     目前狀態
 */
case class OvenUUIDCheckingQueue(testingID: Long, insertTime: Long, currentStatus: Int)

