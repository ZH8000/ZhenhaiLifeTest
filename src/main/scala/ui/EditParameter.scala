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
        MainWindow.appendLog("按下「確定」按鈕")
        MainWindow.popComposite()
      }
    })

    composite
  }

  def init() = {

    val gridLayout = new GridLayout(1, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    // gridLayout.marginWidth = 200
    // gridLayout.marginHeight = 200

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
    val groupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, true)
    group.setLayoutData(groupLayoutData)
    group.setLayout(new GridLayout(2, true))
    group.setText("基本設定")

    val powerGroup = new Group(this, SWT.SHADOW_ETCHED_IN)
    val powerGroupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, true)
    powerGroup.setLayoutData(powerGroupLayoutData)
    powerGroup.setLayout(new GridLayout(1, true))
    powerGroup.setText("電源供應器")

    def createLayoutData = {
      val gridData = new GridData
      gridData.horizontalAlignment = GridData.FILL
      gridData.grabExcessHorizontalSpace = true
      gridData
    }

    println("===> Serial:" + SerialPortList.getPortNames.toList)

    val daughterBoardCount = new DropdownField("子板數目：", List(1, 2, 3, 4, 5, 6, 7), group)
    val capacityCount = new DropdownField("子板上最大電容數：", (1 to 10).toList, group)
    val mainBoardTTY = new DropdownField("主板序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), group)
    val lcrMeterTTY = new DropdownField("容量計序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), group)
    val lcMeterTTY = new DropdownField("漏電流儀序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), group)
    val powerSuppliesTTY = Array(
      new DropdownField("電源供應器 1 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup),
      new DropdownField("電源供應器 2 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup),
      new DropdownField("電源供應器 3 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup),
      new DropdownField("電源供應器 4 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup),
      new DropdownField("電源供應器 5 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup),
      new DropdownField("電源供應器 6 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup),
      new DropdownField("電源供應器 7 序列埠：", List("/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3"), powerGroup)
    )
    val buttonGroup = createButtonGroup()
    val buttonGroupLayoutData = new GridData(SWT.END, SWT.FILL, true, true)
    buttonGroup.setLayoutData(buttonGroupLayoutData)
    buttonGroupLayoutData.heightHint = 50
    buttonGroupLayoutData.widthHint = 300

    daughterBoardCount.setLayoutData(createLayoutData)
    capacityCount.setLayoutData(createLayoutData)
    mainBoardTTY.setLayoutData(createLayoutData)
    lcrMeterTTY.setLayoutData(createLayoutData)
    lcMeterTTY.setLayoutData(createLayoutData)
    powerSuppliesTTY.foreach(_.setLayoutData(createLayoutData))

  }

  init()
}
