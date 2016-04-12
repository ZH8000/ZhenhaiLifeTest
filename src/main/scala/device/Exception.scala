package zhenhai.lifetest.controller.device

object TestBoardNotFound extends Exception("找不到烤箱板")
object MainBoardRS232Timeout extends Exception("主板 RS232 回應逾時")
object PowerSupplyRS232Timeout extends Exception("GENH 電源供應器 RS232 回應逾時")
object LCRMeterRS232Timeout extends Exception("LCR 測試儀 RS232 回應逾時")

