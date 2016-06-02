package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.device._
import java.util.concurrent._
import org.eclipse.swt._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout._
import org.eclipse.swt.widgets._
import zhenhai.lifetest.controller.model._
import java.io.File

/**
 *  USB 轉 RS232 偵測對話視窗
 *
 *  @param    parent                上一層的 Shell
 */
class RS232ProbeDialog(title: String, parent: Shell) extends Dialog(parent, SWT.APPLICATION_MODAL) {

  var result: Option[File] = None
  var hasMessageBox: Boolean = false
  val shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
  val label = new Label(shell, SWT.NONE)
  val prober = new RS232Prober()

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
   *  偵測 RS232 的連接埠，當偵測到時，則把視窗關掉，並返回取得的路徑
   */
  def detectPort() {
    
    val returnCode = showMessageBox(s"請先移除 $title 的 RS232 轉 USB 連接線", SWT.OK|SWT.CANCEL)

    if (returnCode == SWT.OK) {
      label.setText(s"請將 $title 的 RS232 轉 USB 連接線連上")
      prober.startProbe(300) { e =>
        println(e)
        result = e
        if (!shell.isDisposed) {
          shell.getDisplay.asyncExec(new Runnable() {
            override def run() {
              shell.dispose()
            }
          })
        }
      }
    } else {
      shell.dispose()
    }

  }

  /**
   *  開啟視窗
   */
  def open(): Option[File] = {
    val parent = getParent()
    val layout = new FillLayout
    layout.marginWidth = 30
    layout.marginHeight = 30
    shell.setText("偵測 USB 轉 RS232 介面中")
    label.setText(s"偵測「$title」的 USB 連接埠中，請依指式操作……")
    shell.setLayout(layout)
    shell.pack()
    shell.open()
    shell.addShellListener(new ShellAdapter() {
      override def shellClosed(e: ShellEvent) {
        prober.abort()
        super.shellClosed(e)
      }
    })

    detectPort()

    val display = parent.getDisplay()
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }

    result
  }
  
}

