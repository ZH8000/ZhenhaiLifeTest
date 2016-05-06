package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class LoginWindow(parent: Composite, style: Int) extends Composite(parent, style) {

  val (usernameLabel, usernameTextEntry) = createEntryRow("帳號：")
  val (passwordLabel, passwordTextEntry) = createEntryRow("密碼：", SWT.PASSWORD)
  val loginButton = createLoginButton()

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

  def createLoginButton() {
    val button = new Button(this, SWT.PUSH)
    val buttonLayoutData = new GridData
    buttonLayoutData.horizontalSpan = 2
    buttonLayoutData.horizontalAlignment = GridData.FILL;
    button.setLayoutData(buttonLayoutData)
    button.setText("登入")
    this.getShell.setDefaultButton(button)
    button.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        processLogin(usernameTextEntry.getText, passwordTextEntry.getText)
      }
    })
  }

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

object LoginWindow {

  def main(args: Array[String]) {
    val display = new Display
    val shell = new Shell(display)

    val layout = new GridLayout(1, false)
    shell.setLayout(layout)
   
    val loginFrame = new LoginWindow(shell, SWT.NONE)
    val gridData = new GridData
    gridData.horizontalAlignment = GridData.CENTER
    gridData.grabExcessHorizontalSpace = true
    gridData.verticalAlignment = GridData.CENTER
    gridData.grabExcessVerticalSpace = true

    loginFrame.setLayoutData(gridData)
    //shell.setSize(shell.getDisplay.getBounds.width, shell.getDisplay.getBounds.height)
    shell.setMaximized(true)
    //shell.setFullScreen(true)
    shell.open()
    
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }
    display.dispose()
  }
}
