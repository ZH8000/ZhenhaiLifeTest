package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class AddTestPlain(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() {

    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.horizontalSpan = 2
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val group = new Group(this, SWT.SHADOW_ETCHED_IN)
    val groupLayoutData = new GridData
    groupLayoutData.horizontalAlignment = GridData.FILL
    groupLayoutData.verticalAlignment = GridData.FILL
    groupLayoutData.grabExcessHorizontalSpace = true
    groupLayoutData.grabExcessVerticalSpace = true
    group.setLayoutData(groupLayoutData)
    group.setLayout(new GridLayout(2, false))

    def createTextEntryLayoutData = {
      val gridData = new GridData
      gridData.horizontalAlignment = GridData.FILL
      gridData.grabExcessHorizontalSpace = true
      gridData
    }

    def createLabelLayoutData = {
      val gridData = new GridData
      gridData.horizontalAlignment = GridData.END
      gridData.grabExcessHorizontalSpace = false
      gridData
    }

    val orderLabel = new Label(group, SWT.RIGHT)
    val orderTextEntry = new Text(group, SWT.BORDER)
    orderLabel.setText("單號：")
    orderLabel.setLayoutData(createLabelLayoutData)
    orderTextEntry.setLayoutData(createTextEntryLayoutData)

    val companyLabel = new Label(group, SWT.RIGHT)
    val companyTextEntry = new Text(group, SWT.BORDER)
    companyLabel.setText("廠牌：")
    companyLabel.setLayoutData(createLabelLayoutData)
    companyTextEntry.setLayoutData(createTextEntryLayoutData)

    val faradLabel = new Label(group, SWT.RIGHT)
    val faradTextEntry = new Text(group, SWT.BORDER)
    faradLabel.setText("電容值：")
    faradLabel.setLayoutData(createLabelLayoutData)
    faradTextEntry.setLayoutData(createTextEntryLayoutData)

    val voltageLabel = new Label(group, SWT.RIGHT)
    val voltageTextEntry = new Text(group, SWT.BORDER)
    voltageLabel.setText("電壓：")
    voltageLabel.setLayoutData(createLabelLayoutData)
    voltageTextEntry.setLayoutData(createTextEntryLayoutData)

    val totalTimeLabel = new Label(group, SWT.RIGHT)
    val totalTimeTextEntry = new Text(group, SWT.BORDER)
    totalTimeLabel.setText("總測試時間： ")
    totalTimeLabel.setLayoutData(createLabelLayoutData)
    totalTimeTextEntry.setLayoutData(createTextEntryLayoutData)

    val durationLabel = new Label(group, SWT.RIGHT)
    val durationTextEntry = new Text(group, SWT.BORDER)
    durationLabel.setText("間隔時間：")
    durationLabel.setLayoutData(createLabelLayoutData)
    durationTextEntry.setLayoutData(createTextEntryLayoutData)

    val group2 = new Group(this, SWT.SHADOW_ETCHED_IN)
    val group2LayoutData = new GridData
    group2LayoutData.horizontalAlignment = GridData.FILL
    group2LayoutData.verticalAlignment = GridData.FILL
    group2LayoutData.grabExcessHorizontalSpace = true
    group2LayoutData.grabExcessVerticalSpace = true
    group2.setLayoutData(groupLayoutData)
    group2.setLayout(new GridLayout(2, false))

    val marginOfErrorLabel = new Label(group2, SWT.RIGHT)
    val marginOfErrorTextEntry = new Text(group2, SWT.BORDER)
    marginOfErrorLabel.setText("誤差值：")
    marginOfErrorLabel.setLayoutData(createLabelLayoutData)
    marginOfErrorTextEntry.setLayoutData(createTextEntryLayoutData)

    val lostLabel = new Label(group2, SWT.RIGHT)
    val lostTextEntry = new Text(group2, SWT.BORDER)
    lostLabel.setText("漏電電流值：")
    lostLabel.setLayoutData(createLabelLayoutData)
    lostTextEntry.setLayoutData(createTextEntryLayoutData)

    val dfLabel = new Label(group2, SWT.RIGHT)
    val dfTextEntry = new Text(group2, SWT.BORDER)
    dfLabel.setText("df 值：")
    dfLabel.setLayoutData(createLabelLayoutData)
    dfTextEntry.setLayoutData(createTextEntryLayoutData)

    val okButton = new Button(this, SWT.PUSH)
    val okButtonLayoutData = new GridData
    okButtonLayoutData.horizontalAlignment = GridData.END
    okButtonLayoutData.verticalAlignment = GridData.BEGINNING
    okButtonLayoutData.grabExcessHorizontalSpace = true
    okButtonLayoutData.grabExcessVerticalSpace = true
    okButtonLayoutData.horizontalSpan = 2
    okButtonLayoutData.heightHint = 50
    okButtonLayoutData.widthHint = 150
    okButton.setLayoutData(okButtonLayoutData)
    okButton.setText("確定")
    okButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        MainWindow.appendLog(s"按下「確定」按鈕")
        MainWindow.appendLog(s"  單號：${orderTextEntry.getText}")
        MainWindow.appendLog(s"  單號：${orderTextEntry.getText}")
        MainWindow.appendLog(s"  廠旁：${companyTextEntry.getText}")
        MainWindow.appendLog(s"  容量：${faradTextEntry.getText}")
        MainWindow.appendLog(s"  電壓：${voltageTextEntry.getText}")
        MainWindow.appendLog(s"  總時間：${totalTimeTextEntry.getText}")
        MainWindow.appendLog(s"  間隔時間：${durationTextEntry.getText}")
        MainWindow.appendLog(s"  誤差值：${marginOfErrorTextEntry.getText}")
        MainWindow.appendLog(s"  漏電電流：${lostTextEntry.getText}")
        MainWindow.appendLog(s"  df 值：${dfTextEntry.getText}")
        MainWindow.popComposite()
        MainWindow.pushComposite(new OrderStatusSummary(MainWindow.mainWindowShell))
      }
    })

    mainWindowShell.setDefaultButton(okButton)
    okButton.setFocus()
  }

  init()
}
