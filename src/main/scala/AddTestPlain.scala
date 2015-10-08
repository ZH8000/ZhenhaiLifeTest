package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object AddTestPlain {

  def createWindow(parentShell: Shell) = {

    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(2, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    shell.setLayout(gridLayout)

    val navigationButtons = new NavigationButtons(shell)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.horizontalSpan = 2
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val group = new Group(shell, SWT.SHADOW_ETCHED_IN)
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

    val group2 = new Group(shell, SWT.SHADOW_ETCHED_IN)
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
    dfLabel.setText("漏電電流值：")
    dfLabel.setLayoutData(createLabelLayoutData)
    dfTextEntry.setLayoutData(createTextEntryLayoutData)

    val okButton = new Button(shell, SWT.PUSH)
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
        val summaryWindow = OrderStatusSummary.createWindow(shell)
        summaryWindow.open()
      }
    })


    okButton.setFocus()
    shell.setDefaultButton(okButton)
    shell.setMaximized(true)
    shell
  }
}
