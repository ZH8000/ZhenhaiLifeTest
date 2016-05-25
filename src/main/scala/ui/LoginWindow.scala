package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  用來顯示登入頁面的類別
 *
 *  @param    parent    上一層的 Composite
 */
class LoginWindow(parent: Composite) extends Composite(parent, SWT.NONE) {

  val (usernameLabel, usernameTextEntry) = createEntryRow("帳號：")
  val (passwordLabel, passwordTextEntry) = createEntryRow("密碼：", SWT.PASSWORD)
  val loginButton = createLoginButton()

  /**
   *  建立附帶文字標籤的文字方塊
   *
   *  @param    title                 標題
   *  @param    textEntryStyle        文字方塊的 Style（例如設定 SWT.PASSWORD 的話就會是輸入密碼的風格）
   *  @return                         (文字標籤, 文字方塊) 的 Tuple
   */
  def createEntryRow(title: String, textEntryStyle: Int = SWT.NONE) = {

    val label = new Label(this, SWT.LEFT)
    val textEntry = new Text(this, SWT.LEFT|SWT.SINGLE|SWT.BORDER|textEntryStyle)

    label.setText(title)

    val textLayoutData = new GridData
    textLayoutData.horizontalAlignment = GridData.FILL
    textLayoutData.widthHint = 200
    textEntry.setLayoutData(textLayoutData)

    (label, textEntry)

  }

  /**
   *  建立「登入」的按鈕
   *
   *  @return       登入的按鈕
   */
  def createLoginButton() {
    val button = new Button(this, SWT.PUSH)
    val buttonLayoutData = new GridData
    buttonLayoutData.horizontalSpan = 2
    buttonLayoutData.horizontalAlignment = GridData.FILL
    button.setLayoutData(buttonLayoutData)
    button.setText("登入")
    this.getShell.setDefaultButton(button)
    button.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        processLogin(usernameTextEntry.getText, passwordTextEntry.getText)
      }
    })
  }

  /**
   *  處理使用者登入
   *
   *  @param    username      輸入使用者名稱
   *  @param    password      輸入的密碼
   */
  def processLogin(username: String, password: String) {

    if (username == "test" && password == "test") {
      val child = MainWindow.createWindow(this.getShell)
      child.open()
      //child.setFullScreen(true)
    } else {
      val dialog = new MessageBox(this.getShell, SWT.ICON_ERROR|SWT.OK)
      dialog.setText("帳號密碼錯誤")
      dialog.setMessage("無法以此帳號密碼登入系統，請檢查帳號密碼後重試一次")
      dialog.open()
    }
  }

  this.setLayout(new GridLayout(2, false))

}
