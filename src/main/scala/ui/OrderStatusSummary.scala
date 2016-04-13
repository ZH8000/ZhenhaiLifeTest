package tw.com.zhenhai.lifetest;

import zhenhai.lifetest.controller.model._
import java.util.Date
import java.text.SimpleDateFormat
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.concurrent._

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

  def marginOfErrorCodeToFullText(code: String) = {
    marginOfErrorList.filter(_.startsWith(code))(0)
  }

  Class.forName("org.sqlite.JDBC")

  val db = new Database("sample.db")
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

  def setText(text: String) {
    combo.setText(text)
  }
}

class TextEntryField(title: String, isReadOnly: Boolean, isEqualWidth: Boolean, parent: Composite) extends Composite(parent, SWT.NONE) {
  val gridLayout = new GridLayout(2, isEqualWidth)
  val titleLabel = new Label(this, SWT.NONE)
  val attributes = if (isReadOnly) {SWT.BORDER|SWT.READ_ONLY} else {SWT.BORDER}
  val entry = new Text(this, attributes)

  gridLayout.verticalSpacing = 10
  this.setLayout(gridLayout)

  entry.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true , false))
  titleLabel.setText(title)
  
  override def setBackground(color: org.eclipse.swt.graphics.Color) {
    entry.setBackground(color)
  }

  def setText(text: String) {
    entry.setText(text)
  }
}

class TestControl(parent: Composite) extends Composite(parent, SWT.NONE) {

  var orderInfoHolder: Option[TestingOrder] = None
  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  this.setLayout(new FillLayout)
  groupFrame.setLayout(new GridLayout(6, true))
  groupFrame.setText("測試控制")

  val startRoomTemperatureTestButton = new Button(groupFrame, SWT.PUSH)
  val startOvenTestButton = new Button(groupFrame, SWT.PUSH)
  val stopTestButton = new Button(groupFrame, SWT.PUSH)
  val startDate = new TextEntryField("開始日期：", true, false, groupFrame)
  val testedTime = new TextEntryField("測試時間：", true, false, groupFrame)
  val startTime = new TextEntryField("開始時間：", true, false, groupFrame)

  val buttonLayoutData1 = new GridData(SWT.FILL, SWT.FILL, true, true)
  val buttonLayoutData2 = new GridData(SWT.FILL, SWT.FILL, true, true)
  val buttonLayoutData3 = new GridData(SWT.FILL, SWT.FILL, true, true)
  buttonLayoutData1.horizontalSpan = 2
  buttonLayoutData2.horizontalSpan = 2
  buttonLayoutData3.horizontalSpan = 2

  val timeLayoutData1 = new GridData(SWT.FILL, SWT.FILL, true, false)
  val timeLayoutData2 = new GridData(SWT.FILL, SWT.FILL, true, false)
  val timeLayoutData3 = new GridData(SWT.FILL, SWT.FILL, true, false)

  timeLayoutData1.horizontalSpan = 3
  timeLayoutData2.horizontalSpan = 3
  timeLayoutData3.horizontalSpan = 3


  startRoomTemperatureTestButton.setText("室溫測試")
  startRoomTemperatureTestButton.setLayoutData(buttonLayoutData1)

  startOvenTestButton.setText("烤箱測試")
  startOvenTestButton.setLayoutData(buttonLayoutData2)

  stopTestButton.setText("中止測試")
  stopTestButton.setLayoutData(buttonLayoutData3)

  startDate.setLayoutData(timeLayoutData1)
  testedTime.setLayoutData(timeLayoutData2)
  startTime.setLayoutData(timeLayoutData1)

  stopTestButton.addSelectionListener(new SelectionAdapter() {
    override def widgetSelected(e: SelectionEvent) {
      orderInfoHolder.foreach { orderInfo => 
        println("中止測試……")
        TestSetting.db.abortTest(orderInfo.id) 
        stopTestButton.setEnabled(false)
      }
    }
  })

  def updateController(orderInfoHolder: Option[TestingOrder]) {
    this.orderInfoHolder = orderInfoHolder
    updateTimeInfo(orderInfoHolder)
    orderInfoHolder.foreach { orderInfo =>

      val isDisposed = startRoomTemperatureTestButton.isDisposed || startOvenTestButton.isDisposed || stopTestButton.isDisposed

      if (!isDisposed) {
      
        if (!orderInfo.isRoomTemperatureTested) {
          startRoomTemperatureTestButton.setEnabled(true)
          startOvenTestButton.setEnabled(false)
          stopTestButton.setEnabled(false)
        } else {
          startRoomTemperatureTestButton.setEnabled(false)
          val shouldEnableOvenTestButton = orderInfo.currentStatus == 0
          val shouldEnableStopButton = orderInfo.currentStatus != 6 && orderInfo.currentStatus != 7
          startOvenTestButton.setEnabled(shouldEnableOvenTestButton)
          stopTestButton.setEnabled(shouldEnableStopButton)
        }
      }
    }
  }

  def updateTimeInfo(orderInfoHolder: Option[TestingOrder]) {
    orderInfoHolder.foreach { orderInfo =>
      val isDisposed = startDate.isDisposed || startTime.isDisposed || testedTime.isDisposed
      if (!isDisposed) {
        startDate.setText(orderInfo.formattedStartDate)
        startTime.setText(orderInfo.formattedStartTime)
        testedTime.setText(orderInfo.duration)
      }
    }
  }


}


class TestSetting(parent: Composite) extends Composite(parent, SWT.NONE) {
  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  val partNoEntry = new TextEntryField("料　　號：", false, false, groupFrame)
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

  def updateSettingInfo(testingOrderHolder: Option[TestingOrder]) {

    val isDisposed = partNoEntry.isDisposed || voltage.isDisposed || testingTime.isDisposed || capacity.isDisposed || leakCurrent.isDisposed || testingInterval.isDisposed || marginOfError.isDisposed || dx.isDisposed

    if (!isDisposed) {

      testingOrderHolder match {
        case None =>
          this.partNoEntry.setText("")
          this.voltage.setText("")
          this.testingTime.setText("")
          this.capacity.setText("")
          this.leakCurrent.setText("")
          this.testingInterval.setText("")
          this.marginOfError.setText("")
          this.dx.setText("")
        case Some(testingOrder) =>
          this.setEnabled(false)
          this.partNoEntry.setText(testingOrder.partNo)
          this.voltage.setText(testingOrder.voltage.toString)
          this.testingTime.setText(testingOrder.testingTime.toString)
          this.capacity.setText(testingOrder.capacity.toString)
          this.leakCurrent.setText(testingOrder.leakCurrent)
          this.testingInterval.setText(testingOrder.testingInterval.toString)
          this.marginOfError.setText(TestSetting.marginOfErrorCodeToFullText(testingOrder.marginOfError))
          this.dx.setText(testingOrder.dxValue.toString)
      }
    }
  }
}

class CapacityBlock(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

  class CapacityButton(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

    val gridLayout = new GridLayout(2, false)
    gridLayout.verticalSpacing = 2
    this.setLayout(gridLayout)

    val titleButton = createTitleButton(title)

    val capacityInfo = new TextEntryField("電容值：", true, false, this)
    val capacityStatusLabel = new Label(this, SWT.NONE)
    val dxValueInfo = new TextEntryField("損耗角：", true , false, this)
    val dxValueStatusLabel = new Label(this, SWT.NONE)
    val leakCurrentInfo = new TextEntryField("漏電流：", true, false, this)
    val leakCurrentStatusLabel = new Label(this, SWT.NONE)

    val titleButtonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    titleButtonLayoutData.horizontalSpan = 2
    titleButton.setLayoutData(titleButtonLayoutData)
    capacityInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    dxValueInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
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

    val greenColor = Display.getCurrent.getSystemColor(SWT.COLOR_GREEN)
    val redColor = Display.getCurrent.getSystemColor(SWT.COLOR_RED)

    def updateStatus(testingResultHolder: Option[TestingResult]) {


      testingResultHolder.foreach { testingResult =>

        val isDisposed = capacityInfo.isDisposed || dxValueInfo.isDisposed || capacityStatusLabel.isDisposed || dxValueStatusLabel.isDisposed || titleButton.isDisposed

        if (!isDisposed) {

          capacityInfo.setText(testingResult.capacity.toString)
          dxValueInfo.setText(testingResult.dxValue.toString)
        
          val isCapacityOKIcon = if (testingResult.isCapacityOK) "O" else "X"
          val isDXValueOKIcon = if (testingResult.isDXValueOK) "O" else "X"
        
          capacityStatusLabel.setText(isCapacityOKIcon)
          dxValueStatusLabel.setText(isDXValueOKIcon)

          val titleButtonColor = if (testingResult.isOK) greenColor else redColor
          titleButton.setBackground(titleButtonColor)
        }
      }
    }
  }


  val groupFrame = createGroupFrame()
  val buttons = createCapcityInfos()

  def init() {
    this.setLayout(new FillLayout)
  }

  def createGroupFrame() = {
    val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
    val gridLayout = new GridLayout(5, true)
    gridLayout.horizontalSpacing = 50
    groupFrame.setLayout(gridLayout)
    groupFrame.setText(title)
    groupFrame
  }

  def createCapcityInfos() = {
    val buttons = Array(
      new CapacityButton("電容 1", groupFrame),
      new CapacityButton("電容 2", groupFrame),
      new CapacityButton("電容 3", groupFrame),
      new CapacityButton("電容 4", groupFrame),
      new CapacityButton("電容 5", groupFrame),
      new CapacityButton("電容 6", groupFrame),
      new CapacityButton("電容 7", groupFrame),
      new CapacityButton("電容 8", groupFrame),
      new CapacityButton("電容 9", groupFrame),
      new CapacityButton("電容 10", groupFrame)
    )

    buttons.foreach(_.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true)))
    buttons
  }


  def updateCapacityInfo(orderInfoHolder: Option[TestingOrder]) {

    for {
      orderInfo <- orderInfoHolder
      capacityID <- 1 to 10
      button = buttons(capacityID-1)
    } {
      button.updateStatus(TestSetting.db.getTestingResult(orderInfo.id, capacityID))
    }

  }

  init()
}

class OrderStatusSummary(blockNo: Int, daughterBoard: Int, testingBoard: Int, mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  val title = createTitleLabel()
  val composite = createComposite()
  val newOrderButton = createNewOrderButton()
  val navigationButtons = createNavigationButtons()
  val testSetting = createTestSetting()
  val testControl = createTestControl()
  val capacityBlock = createCapacityBlock()
  var orderInfoHolder: Option[TestingOrder] = None 

  def createComposite() = {
    val composite = new Composite(this, SWT.NONE)
    val compositeLayoutData = new GridData
    compositeLayoutData.horizontalAlignment = GridData.END
    compositeLayoutData.grabExcessHorizontalSpace = true
    composite.setLayoutData(compositeLayoutData)
    val gridLayout = new GridLayout(2, false)
    gridLayout.horizontalSpacing = 25
    composite.setLayout(gridLayout)
    composite
  }

  def createNewOrderButton() = {
    val newOrderButton = new Button(composite, SWT.PUSH)
    val newOrderButtonLayoutData = new GridData
    newOrderButtonLayoutData.heightHint = 50
    newOrderButtonLayoutData.widthHint = 150
    newOrderButtonLayoutData.horizontalAlignment = GridData.END
    newOrderButtonLayoutData.grabExcessHorizontalSpace = true
    newOrderButton.setLayoutData(newOrderButtonLayoutData)
    newOrderButton.setText("新測試")
    newOrderButton
  }

  def createTitleLabel() = {
    val title = new Label(this, SWT.NONE)
    val titleLayoutData = new GridData
    titleLayoutData.horizontalAlignment = GridData.CENTER
    titleLayoutData.grabExcessHorizontalSpace = true
    titleLayoutData.horizontalSpan = 2
    title.setLayoutData(titleLayoutData)
    title.setText(s"電容測試（區域 $blockNo）")
    title
  }

  def createNavigationButtons() = {
    val navigationButtons = new NavigationButtons(composite)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
    navigationButtons
  }

  def createTestSetting() = {
    val testSetting = new TestSetting(this)
    val testSettingLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    testSettingLayoutData.horizontalSpan = 2
    testSetting.setLayoutData(testSettingLayoutData)
    testSetting
  }

  def createTestControl() = {
    val testControl = new TestControl(this)
    val testControlLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    testControl.setLayoutData(testControlLayoutData)
    testControl
  }

  def createCapacityBlock() = {
    val block = new CapacityBlock("測試狀態", this)
    val blockLayoutData = new GridData
    blockLayoutData.horizontalAlignment = GridData.FILL
    blockLayoutData.grabExcessHorizontalSpace = true
    blockLayoutData.verticalAlignment = GridData.FILL
    blockLayoutData.grabExcessVerticalSpace = true
    blockLayoutData.horizontalSpan = 3
    block.setLayoutData(blockLayoutData)
    block
  }

  def updateInfo() {
    orderInfoHolder = TestSetting.db.getTestingOrderByBlock(daughterBoard, testingBoard)
    testSetting.updateSettingInfo(orderInfoHolder)
    capacityBlock.updateCapacityInfo(orderInfoHolder)
    testControl.updateController(orderInfoHolder)
  }

  val scheduler = new ScheduledThreadPoolExecutor(1)
  val scheduledUpdate = initWindowAndScheduleUpdate()

  def initWindowAndScheduleUpdate() = {

    val gridLayout = new GridLayout(3, true)

    gridLayout.horizontalSpacing = 20
    gridLayout.verticalSpacing = 20
    gridLayout.marginWidth = 200
    gridLayout.marginHeight = 200

    this.setLayout(gridLayout)
    updateInfo()

    val updater = new Runnable { 
      def run() { 
        Display.getDefault.asyncExec(new Runnable() {
          override def run() {
            updateInfo()
          }
        })
      } 
    }
    scheduler.scheduleWithFixedDelay(updater, 0, 1, TimeUnit.SECONDS)
  }

  this.addDisposeListener(new DisposeListener() {
    override def widgetDisposed(event: DisposeEvent) {
      scheduledUpdate.cancel(false)
      scheduler.shutdown()
    }
  })



}
