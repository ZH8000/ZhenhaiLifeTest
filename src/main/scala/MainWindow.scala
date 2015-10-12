package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object MainWindow {
  def createWindow(parentShell: Shell) = {
    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200
   
    shell.setLayout(gridLayout)

    val monitorButton = new Button(shell, SWT.PUSH|SWT.NO_FOCUS)
    val historyButton = new Button(shell, SWT.PUSH|SWT.NO_FOCUS)
    val queryButton   = new Button(shell, SWT.PUSH|SWT.NO_FOCUS)
    val settingButton = new Button(shell, SWT.PUSH|SWT.NO_FOCUS)

    val layoutData1 = new GridData
    layoutData1.horizontalAlignment = GridData.FILL
    layoutData1.verticalAlignment = GridData.FILL
    layoutData1.grabExcessHorizontalSpace = true
    layoutData1.grabExcessVerticalSpace = true

    monitorButton.setText("即時監控")
    monitorButton.setLayoutData(layoutData1)
    monitorButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(evnet: SelectionEvent) {
        val monitorWindow = MonitorWindow.createWindow(shell)
        monitorWindow.open()
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
        val historyQueryWindow = HistoryQuery.createWindow(shell)
        historyQueryWindow.open()
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
        val queryWindow = OrderIDQuery.createWindow(shell)
        queryWindow.open()
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
        val settingWindow = EditParameter.createWindow(shell)
        settingWindow.open()
      }
    })

    shell.setSize(shell.getDisplay.getBounds.width, shell.getDisplay.getBounds.height)
    shell
  }
}
