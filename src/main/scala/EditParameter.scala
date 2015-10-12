package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object EditParameter {

  def createWindow(parentShell: Shell) = {

    val shell = new Shell(parentShell.getDisplay, SWT.SHELL_TRIM| SWT.APPLICATION_MODAL)
    val gridLayout = new GridLayout(1, true)

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
    orderLabel.setText("參數一：")
    orderLabel.setLayoutData(createLabelLayoutData)
    orderTextEntry.setLayoutData(createTextEntryLayoutData)

    val companyLabel = new Label(group, SWT.RIGHT)
    val companyTextEntry = new Text(group, SWT.BORDER)
    companyLabel.setText("參數二：")
    companyLabel.setLayoutData(createLabelLayoutData)
    companyTextEntry.setLayoutData(createTextEntryLayoutData)

    val faradLabel = new Label(group, SWT.RIGHT)
    val faradTextEntry = new Text(group, SWT.BORDER)
    faradLabel.setText("參數三：")
    faradLabel.setLayoutData(createLabelLayoutData)
    faradTextEntry.setLayoutData(createTextEntryLayoutData)

    val voltageLabel = new Label(group, SWT.RIGHT)
    val voltageTextEntry = new Text(group, SWT.BORDER)
    voltageLabel.setText("參數四：")
    voltageLabel.setLayoutData(createLabelLayoutData)
    voltageTextEntry.setLayoutData(createTextEntryLayoutData)

    val totalTimeLabel = new Label(group, SWT.RIGHT)
    val totalTimeTextEntry = new Text(group, SWT.BORDER)
    totalTimeLabel.setText("參數五：")
    totalTimeLabel.setLayoutData(createLabelLayoutData)
    totalTimeTextEntry.setLayoutData(createTextEntryLayoutData)

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
        shell.dispose()
      }
    })


    okButton.setFocus()
    shell.setDefaultButton(okButton)
    shell.setMaximized(true)
    shell
  }
}
