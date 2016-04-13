package zhenhai.lifetest.controller.model

/**
 *  烤箱板測試佇列
 *
 *  當測試單中設定的測試間隔時間到了之後，會將測試單號寫入此佇列中，
 *  讓 Daemon 程式不斷從此佇列中拉出來測試。
 *
 *  測試完畢後，會將測試結果寫入 OvenTestingResult  資料表中。
 *
 *  @param  testingID     測試單號
 *  @param  insertTime    加入時間
 */
case class OvenTestingQueue(testingID: Long, insertTime: Long)

