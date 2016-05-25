package zhenhai.lifetest.controller.device

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener

/**
 *  LC Checker 測定結果
 *
 *  currentStatus 的判定結果有以下幾個可能：
 *
 *  - HI    上限值外              不良品
 *  - GO    上下限內              良品
 *  - LO    下限值內              不良品
 *  - OV    測定值 Overleak       不良品
 *  - UN    測定值 Under          不良品
 *  - OV    UN 並且測定數據為 0000E-00
 *
 *  @param    coeffection       係數
 *  @param    exponential       指數
 *  @param    currentStatus     判定結果
 *  @param    isOverleakOK      Overleak 判定，true 為 OK 的狀態，false 為 NG
 *  @param    isConductiveOK    true 為 OK，false 為 NG
 */
case class LCResult(coeffection: Int, exponential: Int, currentStatus: String, isOverleakOK: Boolean, isConductiveOK: Boolean) {

  /**
   *  測定出來的漏電流，單位為 mA
   */
  def leakCurrent = BigDecimal(coeffection) * BigDecimal(Math.pow(10, exponential)) * BigDecimal(Math.pow(10, 6))
}


