package zhenhai.lifetest.controller.device

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener

/**
 *  測試範圍
 */
sealed trait RangeSetting

/**
 *  200nA
 */
case object Range200na extends RangeSetting

/**
 *  2uA
 */
case object Range2ua extends RangeSetting

/**
 *  20uA
 */
case object Range20ua extends RangeSetting

/**
 *  200uA
 */
case object Range200ua extends RangeSetting

/**
 *  2mA
 */
case object Range2ma extends RangeSetting

