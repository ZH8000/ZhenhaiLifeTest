package tw.com.zhenhai.lifetest

import jssc.SerialPortList

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

class EditParameter(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def createButtonGroup() = {
    
    val composite = new Composite(this, SWT.NONE)

    composite.setLayout(new GridLayout(2, true))

    val okButton = new Button(composite, SWT.PUSH)
    val okButtonLayoutData = new GridData
    okButtonLayoutData.horizontalAlignment = GridData.FILL
    okButtonLayoutData.verticalAlignment = GridData.FILL
    okButtonLayoutData.grabExcessHorizontalSpace = true
    okButtonLayoutData.grabExcessVerticalSpace = true
    okButton.setLayoutData(okButtonLayoutData)
    okButton.setText("套用")
    okButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        val dialog = new RS232ProbeDialog("主板", mainWindowShell)
        dialog.open()
        val dialog2 = new RS232ProbeDialog("LCR 容量計", mainWindowShell)
        dialog2.open()
        val dialog3 = new RS232ProbeDialog("LC 漏電流測試儀", mainWindowShell)
        dialog3.open()

      }
    })

    val cancelButton = new Button(composite, SWT.PUSH)
    val cancelButtonLayoutData = new GridData
    cancelButtonLayoutData.horizontalAlignment = GridData.FILL
    cancelButtonLayoutData.verticalAlignment = GridData.FILL
    cancelButtonLayoutData.grabExcessHorizontalSpace = true
    cancelButtonLayoutData.grabExcessVerticalSpace = true
    cancelButton.setLayoutData(cancelButtonLayoutData)
    cancelButton.setText("取消")
    cancelButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        MainWindow.appendLog("按下「取消」按鈕")
        MainWindow.popComposite()
      }
    })

    composite
  }

  val gridLayout = MainGridLayout.createLayout(1)
  val navigationButtons = createNavigationButtons()
  val group = createMainGroup()
  val rs232Group = createRS232Group()
  val buttonRow = createButtonRow()

  def createNavigationButtons() = {
    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.horizontalSpan = 2
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
    navigationButtons
  }

  def createMainGroup() = {
    val group = new Group(this, SWT.SHADOW_ETCHED_IN)
    val groupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    group.setLayoutData(groupLayoutData)
    group.setLayout(new GridLayout(2, true))
    group.setText("基本設定")
    group
  }

  def createRS232Group() = {
    val rs232Group = new Group(this, SWT.SHADOW_ETCHED_IN)
    val rs232GroupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    val detectButton = new Button(rs232Group, SWT.PUSH)
    rs232Group.setLayoutData(rs232GroupLayoutData)
    rs232Group.setLayout(new GridLayout(1, true))
    rs232Group.setText("儀器 RS232 設定")

    detectButton.setText("偵測")
    detectButton.setLayoutData(new GridData(SWT.END, GridData.FILL, true, true))

    rs232Group
  }

  def createButtonRow() = {
    val buttonGroup = createButtonGroup()
    val buttonGroupLayoutData = new GridData(SWT.END, SWT.FILL, true, false)
    buttonGroup.setLayoutData(buttonGroupLayoutData)
    buttonGroupLayoutData.heightHint = 50
    buttonGroupLayoutData.widthHint = 300
    buttonGroup
  }


  def init() = {

    this.setLayout(gridLayout)

    val daughterBoardCount = new DropdownField("子板數目：", List(1, 2, 3, 4, 5, 6, 7), group)
    val capacityCount = new DropdownField("子板上最大電容數：", (1 to 10).toList, group)

    daughterBoardCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    capacityCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))

  }

  init()
}
