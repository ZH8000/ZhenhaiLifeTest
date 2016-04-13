package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class EditParameter(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() = {

    val gridLayout = new GridLayout(1, true)

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

    val parameter1Label = new Label(group, SWT.RIGHT)
    val parameter1TextEntry = new Text(group, SWT.BORDER)
    parameter1Label.setText("參數一：")
    parameter1Label.setLayoutData(createLabelLayoutData)
    parameter1TextEntry.setLayoutData(createTextEntryLayoutData)

    val parameter2Label = new Label(group, SWT.RIGHT)
    val parameter2TextEntry = new Text(group, SWT.BORDER)
    parameter2Label.setText("參數二：")
    parameter2Label.setLayoutData(createLabelLayoutData)
    parameter2TextEntry.setLayoutData(createTextEntryLayoutData)

    val parameter3Label = new Label(group, SWT.RIGHT)
    val parameter3TextEntry = new Text(group, SWT.BORDER)
    parameter3Label.setText("參數三：")
    parameter3Label.setLayoutData(createLabelLayoutData)
    parameter3TextEntry.setLayoutData(createTextEntryLayoutData)

    val parameter4Label = new Label(group, SWT.RIGHT)
    val parameter4TextEntry = new Text(group, SWT.BORDER)
    parameter4Label.setText("參數四：")
    parameter4Label.setLayoutData(createLabelLayoutData)
    parameter4TextEntry.setLayoutData(createTextEntryLayoutData)

    val parameter5Label = new Label(group, SWT.RIGHT)
    val parameter5TextEntry = new Text(group, SWT.BORDER)
    parameter5Label.setText("參數五：")
    parameter5Label.setLayoutData(createLabelLayoutData)
    parameter5TextEntry.setLayoutData(createTextEntryLayoutData)

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
        MainWindow.appendLog("按下「確定」按鈕")
        MainWindow.appendLog(s"  參數一：${parameter1TextEntry.getText}")
        MainWindow.appendLog(s"  參數二：${parameter2TextEntry.getText}")
        MainWindow.appendLog(s"  參數三：${parameter3TextEntry.getText}")
        MainWindow.appendLog(s"  參數四：${parameter4TextEntry.getText}")
        MainWindow.appendLog(s"  參數五：${parameter5TextEntry.getText}")
        MainWindow.popComposite()
      }
    })

    MainWindow.mainWindowShell.setDefaultButton(okButton)
    okButton.setFocus()
  }

  init()
}
