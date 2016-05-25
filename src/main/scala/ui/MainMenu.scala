package tw.com.zhenhai.lifetest

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics.Image

/**
 *  登入後的主選單
 *
 *  @param    mainWindowShell     父視窗的 Shell
 */
class MainMenu(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  val gridLayout = MainGridLayout.createLayout(2)
  val logoutButton = createLogoutButton()
  val monitorButton = createMonitorButton()
  val historyButton = createHistoryButton()
  val queryButton = createQueryButton()
  val settingButton = createSettingButton()

  /**
   *  從 src/main/resources/ 裡讀取檔案成為 SWT 的 Image 物件
   *
   *  @param    fileName      檔案名稱
   *  @return                 SWT 的 Image 物件
   */
  def loadImage(fileName: String) = {
    val imageFile = classOf[MainMenu].getResourceAsStream(fileName)
    new Image(null, imageFile)
  }

  /**
   *  建立「登出」按鈕
   *
   *  @return       「登出」按鈕
   */
  def createLogoutButton() = {
    val logoutButton = new Button(this, SWT.PUSH)
    val logoutButtonLayoutData = new GridData
    logoutButtonLayoutData.heightHint = 50
    logoutButtonLayoutData.widthHint = 300
    logoutButtonLayoutData.horizontalAlignment = GridData.END
    logoutButtonLayoutData.horizontalSpan = 2
    logoutButtonLayoutData.grabExcessHorizontalSpace = true
    logoutButton.setLayoutData(logoutButtonLayoutData)
    logoutButton.setText("登出")
    logoutButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        val messageBox = new MessageBox(mainWindowShell, SWT.ICON_QUESTION|SWT.OK|SWT.CANCEL)
        messageBox.setText("登出嗎？")
        messageBox.setMessage("確定要登出嗎？")
        MainWindow.appendLog("點選「登出」按鈕")
        val resultCode = messageBox.open()
        if (resultCode == SWT.OK) {
          MainWindow.appendLog("點選「確定」按鈕")
          mainWindowShell.dispose()
        } else {
          MainWindow.appendLog("取消")
        }
      }
    })
  }

  /**
   *  建立「即時監控」的按鈕
   */
  def createMonitorButton() = {
    val monitorButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val layoutData = new GridData
    layoutData.horizontalAlignment = GridData.FILL
    layoutData.verticalAlignment = GridData.FILL
    layoutData.grabExcessHorizontalSpace = true
    layoutData.grabExcessVerticalSpace = true

    monitorButton.setImage(loadImage("/icon/monitor.png"))
    monitorButton.setText("即時監控")
    monitorButton.setLayoutData(layoutData)
    monitorButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「即時監控」")
        MainWindow.pushComposite(new MonitorWindow(mainWindowShell))
      }
    })
    monitorButton
  }

  /**
   *  建立「歷史資料」的按鈕
   *
   *  @return     「歷史資料」的按鈕
   */
  def createHistoryButton() = {
    val historyButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val layoutData = new GridData
    layoutData.horizontalAlignment = GridData.FILL
    layoutData.verticalAlignment = GridData.FILL
    layoutData.grabExcessHorizontalSpace = true
    layoutData.grabExcessVerticalSpace = true

    historyButton.setImage(loadImage("/icon/history.png"))
    historyButton.setText("歷史資料")
    historyButton.setLayoutData(layoutData)
    historyButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「歷史資料」")
        MainWindow.pushComposite(new HistoryQuery(MainWindow.mainWindowShell))
      }
    })
  }

  /**
   *  建立「料號查詢」的按鈕
   *
   *  @return   「料號查詢」的按鈕
   */
  def createQueryButton() = {
    val queryButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val layoutData = new GridData
    layoutData.horizontalAlignment = GridData.FILL
    layoutData.verticalAlignment = GridData.FILL
    layoutData.grabExcessHorizontalSpace = true
    layoutData.grabExcessVerticalSpace = true

    queryButton.setImage(loadImage("/icon/find.png"))
    queryButton.setText("料號查詢")
    queryButton.setLayoutData(layoutData)
    queryButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「料號查詢」")
        MainWindow.pushComposite(new PartNoQuery(mainWindowShell))
      }
    })

    queryButton
  }

  /**
   *  建立「設定」按鈕
   *
   *  @return       「設定」按鈕
   */
  def createSettingButton() = {
    val settingButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val layoutData = new GridData
    layoutData.horizontalAlignment = GridData.FILL
    layoutData.verticalAlignment = GridData.FILL
    layoutData.grabExcessHorizontalSpace = true
    layoutData.grabExcessVerticalSpace = true

    settingButton.setImage(loadImage("/icon/config.png"))
    settingButton.setText("儀器設定")
    settingButton.setLayoutData(layoutData)
    settingButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「儀器設定」")
        MainWindow.pushComposite(new EditParameter(MainWindow.mainWindowShell))
      }
    })
    settingButton
  }

  def init() {
    this.setLayout(gridLayout)
  }

  init()
}

