package tw.com.zhenhai.lifetest;

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

object TestSetting {
  val voltageList = List(4, 6.3, 10, 16, 25, 35, 50, 63, 80, 100, 160, 200, 220, 250, 315, 350, 400, 420, 450, 500)
  val testingTimeList = List(100, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000)
  val capacityList = List(
    0.1, 0.22, 0.33, 0.47, 1, 2.2, 3.3, 4.7, 6.8, 10, 15, 18, 22,
    27, 33, 39, 47, 56, 68, 82, 100, 120, 150, 180, 220, 270, 330,
    390, 470, 560, 680, 820, 1000, 1200, 1500, 1800, 2200, 2700, 
    3300, 3900, 4700, 5600, 6800, 8200, 10000, 12000, 15000, 18000, 22000
  )
  val marginOfErrorList = List("A: 0 ~ +20", "B: -20 ~ 0", "D: -25 ~ +20", "K: -10 ~ +10", "M: -20 ~ +20", "Y: -10 ~ +20")
  val leakCurrentList = List("I=0.01CV or 3uA", "I=0.03CV or 4uA", "I=0.1CV+40", "I=0.04CV+100", "I=0.06CV+10uA", "I=0.03CV or 3uA")
  val dxList = List(0.08, 0.09, 0.1, 0.12, 0.14, 0.15, 0.16, 0.19, 0.2, 0.22, 0.24, 0.25, 0.28)
  val intervalList = List(1, 2, 3, 5, 6, 10, 12, 24, 50)
}



class DropdownField[T](title: String, selection: List[T], parent: Composite) extends Composite(parent, SWT.NONE) {
  val gridLayout = new GridLayout(2, false)
  val titleLabel = new Label(this, SWT.NONE)
  val combo = new Combo(this, SWT.DROP_DOWN|SWT.BORDER|SWT.READ_ONLY)

  gridLayout.verticalSpacing = 10
  this.setLayout(gridLayout)
  titleLabel.setText(title)
  combo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true , false))
  selection.foreach { item => combo.add(item.toString) }
}

class TextEntryField(title: String, isReadOnly: Boolean, parent: Composite) extends Composite(parent, SWT.NONE) {
  val gridLayout = new GridLayout(2, false)
  val titleLabel = new Label(this, SWT.NONE)
  val attributes = if (isReadOnly) {SWT.BORDER|SWT.READ_ONLY} else {SWT.BORDER}
  val entry = new Text(this, attributes)

  gridLayout.verticalSpacing = 10
  this.setLayout(gridLayout)

  entry.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true , false))
  titleLabel.setText(title)
}

class TestControl(parent: Composite) extends Composite(parent, SWT.NONE) {
  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  this.setLayout(new FillLayout)
  groupFrame.setLayout(new GridLayout(6, true))
  groupFrame.setText("測試控制")

  val startRoomTemperatureTestButton = new Button(groupFrame, SWT.PUSH)
  val startOvenTestButton = new Button(groupFrame, SWT.PUSH)
  val stopTestButton = new Button(groupFrame, SWT.PUSH)
  val startTime = new TextEntryField("開始時間：", true, groupFrame)
  val stopTime = new TextEntryField("測試時間：", true, groupFrame)

  val buttonLayoutData1 = new GridData(SWT.FILL, SWT.FILL, true, true)
  val buttonLayoutData2 = new GridData(SWT.FILL, SWT.FILL, true, true)
  val buttonLayoutData3 = new GridData(SWT.FILL, SWT.FILL, true, true)
  buttonLayoutData1.horizontalSpan = 2
  buttonLayoutData2.horizontalSpan = 2
  buttonLayoutData3.horizontalSpan = 2

  val timeLayoutData1 = new GridData(SWT.FILL, SWT.FILL, true, true)
  val timeLayoutData2 = new GridData(SWT.FILL, SWT.FILL, true, true)
  timeLayoutData1.horizontalSpan = 3
  timeLayoutData2.horizontalSpan = 3


  startRoomTemperatureTestButton.setText("室溫測試")
  startRoomTemperatureTestButton.setLayoutData(buttonLayoutData1)

  startOvenTestButton.setText("烤箱測試")
  startOvenTestButton.setLayoutData(buttonLayoutData2)

  stopTestButton.setText("中止測試")
  stopTestButton.setLayoutData(buttonLayoutData3)

  startTime.setLayoutData(timeLayoutData1)
  stopTime.setLayoutData(timeLayoutData2)

}


class TestSetting(parent: Composite) extends Composite(parent, SWT.NONE) {


  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  val partNoEntry = new TextEntryField("料　　號：", false, groupFrame)
  val voltage = new DropdownField("電壓設定：", TestSetting.voltageList, groupFrame)
  val testingTime = new DropdownField("測試時間：", TestSetting.testingTimeList, groupFrame)
  val capacity = new DropdownField("電容容量：", TestSetting.capacityList, groupFrame)
  val leakCurrent = new DropdownField("漏 電 流：", TestSetting.leakCurrentList, groupFrame)
  val testingInterval = new DropdownField("測試間隔：", TestSetting.intervalList, groupFrame)
  val marginOfError = new DropdownField("誤 差 值：", TestSetting.marginOfErrorList, groupFrame)
  val dx = new DropdownField("損 失 角：", TestSetting.dxList, groupFrame)

  this.setLayout(new FillLayout)
  groupFrame.setLayout(new GridLayout(3, true))
  groupFrame.setText("測試設定")

  partNoEntry.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  voltage.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  testingTime.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  capacity.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  leakCurrent.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  testingInterval.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  marginOfError.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
  dx.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))

}

class CapacityBlock(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

  class CapacityButton(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

    val gridLayout = new GridLayout(1, false)
    gridLayout.verticalSpacing = 10
    this.setLayout(gridLayout)

    val titleButton = createTitleButton(title)
    val capacityInfo = new TextEntryField("電容值：", true, this)
    val leakCurrentInfo = new TextEntryField("漏電流：", true, this)

    capacityInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    leakCurrentInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))

    def createTitleButton(title: String) = {
      val button = new Button(this, SWT.PUSH)
      val buttonLayoutData = new GridData
      buttonLayoutData.horizontalAlignment = GridData.FILL
      buttonLayoutData.verticalAlignment = GridData.FILL
      buttonLayoutData.grabExcessHorizontalSpace = true
      buttonLayoutData.grabExcessVerticalSpace = true
      buttonLayoutData.horizontalSpan = 2
      button.setLayoutData(buttonLayoutData)
      button.setText(title)
      button.addSelectionListener(new SelectionAdapter() {
        override def widgetSelected(e: SelectionEvent) {
          MainWindow.appendLog(s"點選「${CapacityBlock.this.title}」中的「$title」按鈕")
          MainWindow.pushComposite(new OrderCapacityDetail(MainWindow.mainWindowShell))
        }
      })
      button
    }
  }


  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)

  this.setLayout(new FillLayout)
  groupFrame.setLayout(new GridLayout(5, true))
  groupFrame.setText(title)

  val button1  = new CapacityButton("電容 1", groupFrame)
  val button2  = new CapacityButton("電容 2", groupFrame)
  val button3  = new CapacityButton("電容 3", groupFrame)
  val button4  = new CapacityButton("電容 4", groupFrame)
  val button5  = new CapacityButton("電容 5", groupFrame)
  val button6  = new CapacityButton("電容 6", groupFrame)
  val button7  = new CapacityButton("電容 7", groupFrame)
  val button8  = new CapacityButton("電容 8", groupFrame)
  val button9  = new CapacityButton("電容 9", groupFrame)
  val button10 = new CapacityButton("電容 10", groupFrame)

  button1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button3.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button4.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button5.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button6.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button7.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button8.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button9.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
  button10.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true))
}

class OrderStatusSummary(blockNo: Int, mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  def init() {

    val gridLayout = new GridLayout(3, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    this.setLayout(gridLayout)

    val title = new Label(this, SWT.NONE)
    val titleLayoutData = new GridData
    titleLayoutData.horizontalAlignment = GridData.CENTER
    titleLayoutData.grabExcessHorizontalSpace = true
    titleLayoutData.horizontalSpan = 2
    title.setLayoutData(titleLayoutData)
    title.setText(s"電容測試（區域 $blockNo）")

    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val testSetting = new TestSetting(this)
    val testSettingLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    testSettingLayoutData.horizontalSpan = 2
    testSetting.setLayoutData(testSettingLayoutData)

    val testControl = new TestControl(this)
    val testControlLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    testControl.setLayoutData(testControlLayoutData)
   


    val loadBlock = new CapacityBlock("測試狀態", this)
    val loadBlockLayoutData = new GridData
    loadBlockLayoutData.horizontalAlignment = GridData.FILL
    loadBlockLayoutData.grabExcessHorizontalSpace = true
    loadBlockLayoutData.verticalAlignment = GridData.FILL
    loadBlockLayoutData.grabExcessVerticalSpace = true
    loadBlockLayoutData.horizontalSpan = 3
    loadBlock.setLayoutData(loadBlockLayoutData)
  }

  init()
}
