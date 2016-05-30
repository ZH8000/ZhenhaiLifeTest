import zhenhai.lifetest.controller.device._
import java.util.Date
import java.text.SimpleDateFormat

object StableTesting {

  val mainBoard = new MainBoard("/dev/serial/by-path/pci-0000:00:14.0-usb-0:1.7:1.0-port0")
  val powerSupply = new GENH600("/dev/serial/by-path/pci-0000:00:14.0-usb-0:1.5:1.0-port0")
  val lcrMeter = new LCRMeter("/dev/serial/by-path/pci-0000:00:14.0-usb-0:1.2:1.0-port0")
  //val lcChecker = new RSTLCChecker("/dev/serial/by-path/pci-0000:00:14.0-usb-0:1.4:1.0-port0")
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def waitFor(second: Int) {
    Thread.sleep(second * 1000)
  } 

  def log(message: => String) {
    println(s"[${dateFormatter.format(new Date)}] $message")
  }

  def main(args: Array[String]) {
  
    var count = 0L

    log("開啟主板……")
    mainBoard.open()
    lcrMeter.open()
    //lcChecker.open()

    log("開啟電源供應器……")
    powerSupply.open()
    powerSupply.setVoltage(0)
    powerSupply.setOutput(false)
    log("================= 安全化初始 ===========")
    log("關閉充放電電路：" + mainBoard.setChargeMode(0, 0, 0).filter(_ == true).get)
    log("等五秒")
    waitFor(5)
    log("開啟放電電路：" + mainBoard.setChargeMode(0, 0, 2).filter(_ ==  true).get)
    log("等五秒")
    waitFor(5)
    log("關閉充放電電路：" + mainBoard.setChargeMode(0, 0, 0).filter(_ == true).get)
    log("等五秒")
    waitFor(5)
    log("================= 安全化結束 ===========")

    log("設定 LCR 量測範圍")
    lcrMeter.setRange(150)

    log("設定電壓……")
    powerSupply.setVoltage(390)

    while (true) {
      log(s"[$count] 測試……")
      log(s"  ===> 開啟充電電路：" + mainBoard.setChargeMode(0, 0, 1).filter(_ == true).get)
      log(s"  ===> 開啟電源供應器：" + powerSupply.setOutput(true).get)
      log(s"  ===> 等待一分鐘……")
      waitFor(60)
      log(s"  ===> 關閉電源供應器：" + powerSupply.setOutput(false).get)
      log(s"  ===> 關閉充電電路：" + mainBoard.setChargeMode(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 等待五秒")
      waitFor(5)
      log(s"  ===> 開啟放電電路：" + mainBoard.setChargeMode(0, 0, 2).filter(_ == true).get)
      log(s"  ===> 等待五秒")
      waitFor(5)
      log(s"  ===> 關閉充放電電路：" + mainBoard.setChargeMode(0, 0, 0).filter(_== true).get)
      log(s"  ===> 切換 LCR 通道至第一顆電容：" + mainBoard.setLCRChannel(0, 0, 1).filter(_ == true).get)
      log(s"  ===> LCR 量測值：" + lcrMeter.startMeasure())
      log(s"  ===> 關閉 LCR 通道：" + mainBoard.setLCRChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 切換 LCR 通道至第二顆電容：" + mainBoard.setLCRChannel(0, 0, 2).filter(_ == true).get)
      log(s"  ===> LCR 量測值：" + lcrMeter.startMeasure())
      log(s"  ===> 關閉 LCR 通道：" + mainBoard.setLCRChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 切換 LCR 通道至第三顆電容：" + mainBoard.setLCRChannel(0, 0, 3).filter(_ == true).get)
      log(s"  ===> LCR 量測值：" + lcrMeter.startMeasure())
      log(s"  ===> 關閉 LCR 通道：" + mainBoard.setLCRChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 開啟充電電路：" + mainBoard.setChargeMode(0, 0, 1).filter(_ == true).get)
      log(s"  ===> 開啟電源供應器：" + powerSupply.setOutput(true).get)
      log(s"  ===> 等待一分鐘……")
      waitFor(60)
      log(s"  ===> 關閉電源供應器：" + powerSupply.setOutput(false).get)
      log(s"  ===> 關閉充電電路：" + mainBoard.setChargeMode(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 關閉 LC 通道：" + mainBoard.setLCChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 切換 LC 通道至第一顆電容：" + mainBoard.setLCChannel(0, 0, 1).filter(_ == true).get)
      log(s"  ===> 等待三秒")
      //log(s"  ===> LC 量測值：" + lcChecker.startMeasure())
      waitFor(3)
      log(s"  ===> 關閉 LC 通道：" + mainBoard.setLCChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 切換 LC 通道至第二顆電容：" + mainBoard.setLCChannel(0, 0, 2).filter(_ == true).get)
      log(s"  ===> 等待三秒")
      //log(s"  ===> LC 量測值：" + lcChecker.startMeasure())
      waitFor(3)
      log(s"  ===> 關閉 LC 通道：" + mainBoard.setLCChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 切換 LC 通道至第三顆電容：" + mainBoard.setLCChannel(0, 0, 3).filter(_ == true).get)
      log(s"  ===> 等待三秒")
      //log(s"  ===> LC 量測值：" + lcChecker.startMeasure())
      waitFor(3)
      log(s"  ===> 關閉 LC 通道：" + mainBoard.setLCChannel(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 關閉充放電電路：" + mainBoard.setChargeMode(0, 0, 0).filter(_ == true).get)
      log(s"  ===> 等待十秒")
      waitFor(10)
      count += 1
    }

  }
}
