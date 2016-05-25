package tw.com.zhenhai.lifetest

import java.util.concurrent._
import org.eclipse.swt._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout._
import org.eclipse.swt.widgets._
import zhenhai.lifetest.controller.model._

/**
 *  室溫測試狀態訊息視窗
 *
 *  @param    orderStatusSummary    測試區塊狀態視窗物件
 *  @param    parent                上一層的 Shell
 */
class RoomTemperatureTestingDialog(orderStatusSummary: OrderStatusSummary, 
                                   parent: Shell) extends Dialog(parent, SWT.APPLICATION_MODAL) {

  var orderInfoHolder: Option[TestingOrder] = None
  var hasMessageBox: Boolean = false
  val scheduler = new ScheduledThreadPoolExecutor(1)
  val shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)

  /**
   *  顯示彈出式視窗
   *
   *  @param      message         要顯示的訊息
   *  @param      style           要顯示的按鈕
   *  @return                     使用者按下什麼按鈕來關閉視窗
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
   *  開始室溫測試程序
   */
  def startTestingSequence() {
    this.orderInfoHolder = orderStatusSummary.getOrCreateOrder()
    this.orderInfoHolder match {
      case None => 
        showMessageBox("無法儲存新測試至資料庫", SWT.OK)
        shell.dispose()
      case Some(newOrder) =>       
        orderStatusSummary.isNewOrder = false
        orderStatusSummary.updateInfo()
        LifeTestOptions.db.insertRoomTemperatureTestingQueue(newOrder.id)
    }
  }

  /**
   *  將室溫測試要求從佇列中移除
   */
  def abortRoomTemperatureTest() {
    this.orderInfoHolder.foreach { orderInfo =>
      LifeTestOptions.db.deleteTemperatureTest(orderInfo.id)
    }
  }

  /**
   *  用來更新視窗內容的 Runnable 物件
   */
  val updater = new Runnable() {
    var count = 0L
    override def run() {
      for {
        orderInfo <- orderInfoHolder
        queueData <- LifeTestOptions.db.getRoomTemperatureTestingQueue(orderInfo.id)
      } {
        val currentStatus = queueData.currentStatus

        // 已完成測試
        if (currentStatus == 7) {
          shell.getDisplay.asyncExec(new Runnable() {
            override def run() {
              if (!hasMessageBox && !shell.isDisposed) {
                showMessageBox("已完成室溫初始測試", SWT.OK)
                shell.dispose()
              }
            }
          })
        }

        // 發生錯誤
        if (currentStatus >= 2 && currentStatus <= 6) {
          val message = currentStatus match {
            case 2 => "資料庫中找不到測試單，系統異常，請連絡技術人員。"
            case 3 => "烤箱板高壓 Relay 損換，請更換烤箱板。"
            case 4 => "找不到測試板，請確認是否已連接後重試。"
            case 5 => "主板 RS232 回應逾時。"
            case 6 => "發生其他異常錯誤，請連絡技述人員。"
          }

          shell.getDisplay.asyncExec(new Runnable() {
            override def run() {
              if (!hasMessageBox && !shell.isDisposed) {

                val responseCode = showMessageBox(message, SWT.RETRY|SWT.CANCEL)
                if (responseCode == SWT.CANCEL) {
                  abortRoomTemperatureTest()
                  shell.dispose()           
                } else {
                  LifeTestOptions.db.updateRoomTemperatureTestingQueue(queueData.copy(currentStatus = 0))
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
    shell.setText("室溫測試中")
    label.setText("室溫初始測試進行中，請稍候……")

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

