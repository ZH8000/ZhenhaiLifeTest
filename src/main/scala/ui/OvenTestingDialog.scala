package tw.com.zhenhai.lifetest

import java.util.concurrent._
import org.eclipse.swt._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout._
import org.eclipse.swt.widgets._
import zhenhai.lifetest.controller.model._

/**
 *  顯示烤箱初始化設定的彈出式視窗
 *
 *  @param    orderInfo   測試單的資料庫物件
 *  @param    parent      上一層的 Shell
 */
class OvenTestingDialog(orderInfo: TestingOrder, parent: Shell) extends Dialog(parent, SWT.APPLICATION_MODAL) {

  var hasMessageBox: Boolean = false
  val scheduler = new ScheduledThreadPoolExecutor(1)
  val shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)

  /**
   *  顯示訊息視窗
   *
   *  @param    message     訊息內容
   *  @param    style       訊息視窗的按鈕設定
   *  @return               使用者按下哪個按鈕
   */
  def showMessageBox(message: String, style: Int) = {
    hasMessageBox = true
    val messageBox = new MessageBox(shell, style)
    messageBox.setMessage(message)
    val responseCode = messageBox.open()
    hasMessageBox = false
    responseCode
  }

  /**
   *  將烤箱側試程的前置 UUID 檢查作頁排入資料庫的 Queue 中
   */
  def startTestingSequence() = LifeTestOptions.db.insertOvenUUIDCheckingQueue(orderInfo.id)

  /**
   *  中斷測試，將測試要求從 Queue 中移出
   */
  def abortTest() = LifeTestOptions.db.deleteOvenUUIDCheckingQueue(orderInfo.id)

  /**
   *  用來更新目前顯示的狀態的 Runnalbe 物件
   */
  val updater = new Runnable() {

    var count = 0L

    override def run() {
      for {
        queueData <- LifeTestOptions.db.getOvenUUIDCheckingQueue(orderInfo.id)
      } {
        
        val currentStatus = queueData.currentStatus

        // 已完成測試
        if (currentStatus == 9) {
          shell.getDisplay.asyncExec(new Runnable() {
            override def run() {
              if (!hasMessageBox && !shell.isDisposed) {
                showMessageBox("烤箱測試程序已正常啟動", SWT.OK)
                shell.dispose()
              }
            }
          })
        }

        // 發生錯誤
        if (currentStatus >= 2 && currentStatus <= 8) {
          val message = currentStatus match {
            case 2 => "資料庫中找不到測試單，系統異常，請連絡技術人員。"
            case 3 => "烤箱板高壓 Relay 損換，請更換烤箱板。"
            case 4 => "找不到測試板，請確認是否已連接後重試。"
            case 5 => "主板 RS232 回應逾時。"
            case 6 => "電源供應器 RS232 回應逾時，請檢查電源是否開啟及通訊線是否正常連接。"
            case 7 => "發生其他異常錯誤，請連絡技述人員。"
            case 8 => "烤箱板編號與室溫測試時不同，請確認為同一組烤箱板。"
          }

          shell.getDisplay.asyncExec(new Runnable() {
            override def run() {
              if (!hasMessageBox && !shell.isDisposed) {

                val responseCode = showMessageBox(message, SWT.RETRY|SWT.CANCEL)
                if (responseCode == SWT.CANCEL) {
                  abortTest()
                  shell.dispose()           
                } else {
                  LifeTestOptions.db.updateOvenUUIDCheckingQueue(queueData.copy(currentStatus = 0))
                }
              }
            }
          })
        }
      }
      count += 1
    }
  }

  /**
   *  開啟視窗
   */
  def open() = {
    val parent = getParent()
    val scheduledTask = scheduler.scheduleWithFixedDelay(updater, 0, 250, TimeUnit.MILLISECONDS)
    val label = new Label(shell, SWT.NONE)

    val layout = new FillLayout
    layout.marginWidth = 30
    layout.marginHeight = 30
    shell.setText("烤箱測試啟動中")
    label.setText("烤箱測試啟動檢查中，請稍候……")

    val fontData = label.getFont().getFontData()(0)
    fontData.setHeight(30)
    label.setFont( new Font(shell.getDisplay,fontData))
    shell.setLayout(layout)
    shell.pack()
    shell.open()
    shell.addShellListener(new ShellAdapter() {
      override def shellClosed(e: ShellEvent) {
        e.doit = false
      }
    })

    startTestingSequence()

    val display = parent.getDisplay()
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }
    scheduledTask.cancel(false)
    scheduler.shutdown()
  }
}

