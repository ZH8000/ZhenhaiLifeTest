package zhenhai.lifetest.controller.device

/**
 *  電容值的範圍
 */
sealed trait CapacityRange

object Range400nF extends CapacityRange
object Range4uF extends CapacityRange
object Range40uF extends CapacityRange
object Range400uF extends CapacityRange
object Range4mF extends CapacityRange
object Range40mF extends CapacityRange

