package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class MainMenu(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() {
    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

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


    val monitorButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val historyButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val queryButton   = new Button(this, SWT.PUSH|SWT.NO_FOCUS)
    val settingButton = new Button(this, SWT.PUSH|SWT.NO_FOCUS)

    val layoutData1 = new GridData
    layoutData1.horizontalAlignment = GridData.FILL
    layoutData1.verticalAlignment = GridData.FILL
    layoutData1.grabExcessHorizontalSpace = true
    layoutData1.grabExcessVerticalSpace = true

    monitorButton.setText("即時監控")
    monitorButton.setLayoutData(layoutData1)
    monitorButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「即時監控」")
        MainWindow.pushComposite(new MonitorWindow(mainWindowShell))
      }
    })

    val layoutData2 = new GridData
    layoutData2.horizontalAlignment = GridData.FILL
    layoutData2.verticalAlignment = GridData.FILL
    layoutData2.grabExcessHorizontalSpace = true
    layoutData2.grabExcessVerticalSpace = true

    historyButton.setText("歷史資料")
    historyButton.setLayoutData(layoutData2)
    historyButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「歷史資料」")
        MainWindow.pushComposite(new HistoryQuery(MainWindow.mainWindowShell))
      }
    })

    val layoutData3 = new GridData
    layoutData3.horizontalAlignment = GridData.FILL
    layoutData3.verticalAlignment = GridData.FILL
    layoutData3.grabExcessHorizontalSpace = true
    layoutData3.grabExcessVerticalSpace = true

    queryButton.setText("單號查詢")
    queryButton.setLayoutData(layoutData3)
    queryButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「單號查詢」")
        MainWindow.pushComposite(new OrderIDQuery(mainWindowShell))
      }
    })

    val layoutData4 = new GridData
    layoutData4.horizontalAlignment = GridData.FILL
    layoutData4.verticalAlignment = GridData.FILL
    layoutData4.grabExcessHorizontalSpace = true
    layoutData4.grabExcessVerticalSpace = true

    settingButton.setText("儀器設定")
    settingButton.setLayoutData(layoutData4)
    settingButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        MainWindow.appendLog("點選「儀器設定」")
        MainWindow.pushComposite(new EditParameter(MainWindow.mainWindowShell))
      }
    })
  }

  init()
}

