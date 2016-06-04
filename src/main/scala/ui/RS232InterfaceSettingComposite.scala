package tw.com.zhenhai.lifetest

import jssc.SerialPortList

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import scala.util.Try

/**
 *  取得 USB 轉 RS232 連接埠的元件
 *
 *  @param    title   標題
 *  @param    parent  上一層的元件
 */
class RS232InterfaceSettingComposite(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {
  
  val gridLayout = new GridLayout(3, false)
  val label = new Label(this, SWT.NONE)
  val textEntry = new Text(this, SWT.READ_ONLY)
  val button = new Button(this, SWT.PUSH)

  /**
   *  取得偵測到的連接埠
   *
   *  @return       如果有偵測到的連接埠則為 Some(連接埠檔案位置)，若無則為 None
   */
  def getPort(): Option[String] = Option(textEntry.getText).map(_.trim).filterNot(_.isEmpty)

  /**
   *  設定元件裡顯示的連接埠地址
   *
   *  @param      port      連接埠
   */
  def setPort(port: String) {
    textEntry.setText(port)
  }

  /**
   *  初始化 Layout 與設定偵測按鈕
   */
  def init() {
    this.setLayout(gridLayout)
    label.setText(title + " RS232 連接埠：")
    label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true))
    textEntry.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))
    textEntry.setEnabled(false)
    button.setText("偵測")
    button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true))
    button.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        val dialog = new RS232ProbeDialog(title, getShell)
        val portHolder = dialog.open()

        portHolder.foreach { port => 
          textEntry.setText(port.getAbsolutePath)
        }
      }
    })

  }

  init()
}

