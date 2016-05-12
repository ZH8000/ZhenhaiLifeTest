import zhenhai.lifetest.controller.device._

object StableTesting {

  val mainBoard = new MainBoard("/dev/serial/by-path/pci-0000:00:14.0-usb-0:1.7:1.0-port0")
  val powerSupply = new GENH600("/dev/serial/by-path/pci-0000:00:14.0-usb-0:1.5:1.0-port0")

  def main(args: Array[String]) {
  
    var count = 0L

    println("開啟主板……")
    mainBoard.open()

    println("開啟電源供應器……")
    powerSupply.open()

    println("設定電壓……")
    powerSupply.setVoltage(50)


    while (true) {
      println(s"[$count] 測試……")
      println(s"  ===> 關閉充放電電路：" + mainBoard.setChargeMode(0, 0, 0).get)
      println(s"  ===> 關閉 LC 通道：" + mainBoard.setLCChannel(0, 0, 0).get)
      println(s"  ===> 開啟充電電路：" + mainBoard.setChargeMode(0, 0, 1).get)
      println(s"  ===> 開啟電源供應器：" + powerSupply.setOutput(true).get)
      println(s"  ===> 等待一分鐘……")
      println("     ===> 還有 60 秒....")
      Thread.sleep(1000 * 10)
      println("     ===> 還有 50 秒....")
      Thread.sleep(1000 * 10)
      println("     ===> 還有 40 秒....")
      Thread.sleep(1000 * 10)
      println("     ===> 還有 30 秒....")
      Thread.sleep(1000 * 10)
      println("     ===> 還有 20 秒....")
      Thread.sleep(1000 * 10)
      println("     ===> 還有 10 秒....")
      Thread.sleep(1000 * 10)
      println("     ===> 還有  0 秒....")
      println(s"  ===> 關閉電源供應器：" + powerSupply.setOutput(false).get)
      println(s"  ===> 關閉充電電路：" + mainBoard.setChargeMode(0, 0, 0).get)
      println(s"  ===> 開啟放電電路：" + mainBoard.setChargeMode(0, 0, 2).get)
      println(s"  ===> 等待十秒")
      Thread.sleep(1000 * 10)
      println(s"  ===> 關閉放電電路：" + mainBoard.setChargeMode(0, 0, 0).get)
      println(s"  ===> 切換 LC 通道至第一顆電容：" + mainBoard.setLCChannel(0, 0, 1).get)
      println(s"  ===> 切換 LC 通道至第二顆電容：" + mainBoard.setLCChannel(0, 0, 2).get)
      println(s"  ===> 切換 LC 通道至第三顆電容：" + mainBoard.setLCChannel(0, 0, 3).get)
      println(s"  ===> 關閉 LC 通道：" + mainBoard.setLCChannel(0, 0, 0).get)

      count += 1
    }


  }
}
