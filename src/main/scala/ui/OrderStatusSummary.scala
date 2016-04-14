package tw.com.zhenhai.lifetest;

import zhenhai.lifetest.controller.model._
import java.util.Date
import java.text.SimpleDateFormat
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.concurrent._
import scala.util.Try

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

  def deselectAll() {
    combo.deselectAll()
  }

  def setText(text: String)  = combo.setText(text)
  def getText() = combo.getText()
  def getSelection() = Option(combo.getSelectionIndex).filter(_ != -1)
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

  def isEmpty() = entry.getText.trim.size == 0
  def getText() = entry.getText

  def setText(text: String) {
    entry.setText(text)
  }
}

class TestControl(orderStatusSummary: OrderStatusSummary) extends Composite(orderStatusSummary, SWT.NONE) {

  var orderInfoHolder: Option[TestingOrder] = None
  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val startRoomTemperatureTestButton = createButton("室溫測試")
  val startOvenTestButton = createButton("烤箱測試")
  val stopTestButton = createButton("中止測試")
  val startDate = createTextEntry("開始日期：")
  val testedTime = createTextEntry("測試時間：")
  val startTime = createTextEntry("開始時間：")
  val currentStatus = createTextEntry("現在狀態：")

  def createButton(title: String) = {
    val button = new Button(groupFrame, SWT.PUSH)
    val buttonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    buttonLayoutData.horizontalSpan = 2
    button.setLayoutData(buttonLayoutData)
    button.setText(title)
    button
  }

  def createTextEntry(title: String) = {
    val dateTimeEntry = new TextEntryField(title, true, false, groupFrame)
    val layoutData = new GridData(SWT.FILL, SWT.FILL, true, false)
    layoutData.horizontalSpan = 3
    dateTimeEntry.setLayoutData(layoutData)
    dateTimeEntry
  }

  class OvenTestingDialog(orderInfo: TestingOrder, parent: Shell, style: Int) extends Dialog(parent, style) {

    var hasMessageBox: Boolean = false
    val scheduler = new ScheduledThreadPoolExecutor(1)
    val shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)

    def showMessageBox(message: String, style: Int) = {
      hasMessageBox = true
      val messageBox = new MessageBox(shell, style)
      messageBox.setMessage(message)
      val responseCode = messageBox.open()
      hasMessageBox = false
      responseCode
    }

    def startTestingSequence() {
      TestSetting.db.insertOvenUUIDCheckingQueue(orderInfo.id)
    }

    def abortTest() {
      TestSetting.db.deleteOvenUUIDCheckingQueue(orderInfo.id)
    }

    val updater = new Runnable() {
      var count = 0L
      override def run() {
        for {
          queueData <- TestSetting.db.getOvenUUIDCheckingQueue(orderInfo.id)
        } {
          
          val currentStatus = queueData.currentStatus

          // 已完成測試
          if (currentStatus == 9) {
            shell.getDisplay.asyncExec(new Runnable() {
              override def run() {
                if (!hasMessageBox && !shell.isDisposed) {
                  showMessageBox("烤箱測試程序已正常啟動", SWT.OK)
                  shell.dispose()
                }
              }
            })
          }

          // 發生錯誤
          if (currentStatus >= 2 && currentStatus <= 6) {
            val message = currentStatus match {
              case 2 => "資料庫中找不到測試單，系統異常，請連絡技術人員。"
              case 3 => "烤箱板高壓 Relay 損換，請更換烤箱板。"
              case 4 => "找不到測試板，請確認是否已連接後重試。"
              case 5 => "主板 RS232 回應逾時。"
              case 6 => "電源供應器 RS232 回應逾時，請檢查電源是否開啟及通訊線是否正常連接。"
              case 7 => "發生其他異常錯誤，請連絡技述人員。"
              case 8 => "烤箱板編號與室溫測試時不同，請確認為同一組烤箱板。"
            }

            shell.getDisplay.asyncExec(new Runnable() {
              override def run() {
                if (!hasMessageBox && !shell.isDisposed) {

                  val responseCode = showMessageBox(message, SWT.RETRY|SWT.CANCEL)
                  if (responseCode == SWT.CANCEL) {
                    abortTest()
                    shell.dispose()           
                  } else {
                    TestSetting.db.updateOvenUUIDCheckingQueue(queueData.copy(currentStatus = 0))
                  }
                }
              }
            })
          }
        }
        count += 1
      }
    }

    def open() = {
      import org.eclipse.swt.graphics.Font
      val parent = getParent()
      val scheduledTask = scheduler.scheduleWithFixedDelay(updater, 0, 250, TimeUnit.MILLISECONDS)
      val label = new Label(shell, SWT.NONE)

      val layout = new FillLayout
      layout.marginWidth = 30
      layout.marginHeight = 30
      shell.setText("烤箱測試啟動中")
      label.setText("烤箱測試啟動檢查中，請稍候……")

      val fontData = label.getFont().getFontData()(0)
      fontData.setHeight(30)
      label.setFont( new Font(shell.getDisplay,fontData))
      shell.setLayout(layout)
      shell.pack()
      shell.open()
      shell.addShellListener(new ShellAdapter() {
        override def shellClosed(e: ShellEvent) {
          e.doit = false
        }
      })

      startTestingSequence()

      val display = parent.getDisplay()
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
	  display.sleep()
        }
      }
      scheduledTask.cancel(false)
      scheduler.shutdown()
    }
    
  }

  class RoomTemperatureTestingDialog(parent: Shell, style: Int) extends Dialog(parent, style) {

    var orderInfoHolder: Option[TestingOrder] = None
    var hasMessageBox: Boolean = false
    val scheduler = new ScheduledThreadPoolExecutor(1)
    val shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL)
    def this(parent: Shell) = this(parent, SWT.NONE)

    def showMessageBox(message: String, style: Int) = {
      hasMessageBox = true
      val messageBox = new MessageBox(shell, style)
      messageBox.setMessage(message)
      val responseCode = messageBox.open()
      hasMessageBox = false
      responseCode
    }

    def startTestingSequence() {
      this.orderInfoHolder = orderStatusSummary.getOrCreateOrder()
      this.orderInfoHolder match {
        case None => 
          showMessageBox("無法儲存新測試至資料庫", SWT.OK)
          shell.dispose()
        case Some(newOrder) =>       
          orderStatusSummary.isNewOrder = false
          orderStatusSummary.updateInfo()
          TestSetting.db.insertRoomTemperatureTestingQueue(newOrder.id)
      }
    }

    def abortRoomTemperatureTest() {
      this.orderInfoHolder.foreach { orderInfo =>
        TestSetting.db.deleteTemperatureTest(orderInfo.id)
      }
    }

    val updater = new Runnable() {
      var count = 0L
      override def run() {
        for {
          orderInfo <- orderInfoHolder
          queueData <- TestSetting.db.getRoomTemperatureTestingQueue(orderInfo.id)
        } {
          val currentStatus = queueData.currentStatus

          // 已完成測試
          if (currentStatus == 7) {
            shell.getDisplay.asyncExec(new Runnable() {
              override def run() {
                if (!hasMessageBox && !shell.isDisposed) {
                  showMessageBox("已完成室溫初始測試", SWT.OK)
                  shell.dispose()
                }
              }
            })
          }

          // 發生錯誤
          if (currentStatus >= 2 && currentStatus <= 6) {
            val message = currentStatus match {
              case 2 => "資料庫中找不到測試單，系統異常，請連絡技術人員。"
              case 3 => "烤箱板高壓 Relay 損換，請更換烤箱板。"
              case 4 => "找不到測試板，請確認是否已連接後重試。"
              case 5 => "主板 RS232 回應逾時。"
              case 6 => "發生其他異常錯誤，請連絡技述人員。"
            }

            shell.getDisplay.asyncExec(new Runnable() {
              override def run() {
                if (!hasMessageBox && !shell.isDisposed) {

                  val responseCode = showMessageBox(message, SWT.RETRY|SWT.CANCEL)
                  if (responseCode == SWT.CANCEL) {
                    abortRoomTemperatureTest()
                    shell.dispose()           
                  } else {
                    TestSetting.db.updateRoomTemperatureTestingQueue(queueData.copy(currentStatus = 0))
                  }
                }
              }
            })
          }
        }
        count += 1
      }
    }

    def open() = {
      import org.eclipse.swt.graphics.Font
      val parent = getParent()
      val scheduledTask = scheduler.scheduleWithFixedDelay(updater, 0, 250, TimeUnit.MILLISECONDS)
      val label = new Label(shell, SWT.NONE)

      val layout = new FillLayout
      layout.marginWidth = 30
      layout.marginHeight = 30
      shell.setText("室溫測試中")
      label.setText("室溫初始測試進行中，請稍候……")

      val fontData = label.getFont().getFontData()(0)
      fontData.setHeight(30)
      label.setFont( new Font(shell.getDisplay,fontData))
      shell.setLayout(layout)
      shell.pack()
      shell.open()
      shell.addShellListener(new ShellAdapter() {
        override def shellClosed(e: ShellEvent) {
          e.doit = false
        }
      })

      startTestingSequence()

      val display = parent.getDisplay()
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
	  display.sleep()
        }
      }
      scheduledTask.cancel(false)
      scheduler.shutdown()
    }
    
  }


  def startRoomTemperatureTest() {

    val settingErrors = orderStatusSummary.testSetting.getSettingErrors

    if (settingErrors.isEmpty) {
      val confirmBox = new MessageBox(TestControl.this.getShell, SWT.ICON_INFORMATION|SWT.OK|SWT.CANCEL)
      confirmBox.setMessage("請將測試板插入室溫測試專用插槽中後按下確認按鈕。")
      val responseCode = confirmBox.open()

      if (responseCode == SWT.OK) {
        val dialog = new RoomTemperatureTestingDialog(getShell, SWT.APPLICATION_MODAL)
        dialog.open()
      }

    } else {
      val messages = settingErrors.mkString("\n")
      val messageBox = new MessageBox(TestControl.this.getShell, SWT.ICON_WARNING|SWT.OK)
      messageBox.setMessage("設定錯誤：\n\n" + messages + "\n\n")
      messageBox.open()
    }
  }

  def startOvenTest() {
    orderStatusSummary.orderInfoHolder.foreach { orderInfo =>
      val dialog = new OvenTestingDialog(orderInfo, getShell, SWT.APPLICATION_MODAL)
      dialog.open()
    }

  }

  def init() {
    this.setLayout(new FillLayout)
    groupFrame.setLayout(new GridLayout(6, true))
    groupFrame.setText("測試控制")
    startOvenTestButton.setEnabled(false)
    stopTestButton.setEnabled(false)

    startOvenTestButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        startOvenTest()
      }
    })

    startRoomTemperatureTestButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        startRoomTemperatureTest()
      }
    })

    stopTestButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        orderInfoHolder.foreach { orderInfo => 
          TestSetting.db.abortTest(orderInfo.id) 
          stopTestButton.setEnabled(false)
        }
      }
    })
  }


  def clear() {
    startDate.setText("")
    startTime.setText("")
    testedTime.setText("")
    currentStatus.setText("")
    stopTestButton.setEnabled(false)
    startOvenTestButton.setEnabled(false)
    startRoomTemperatureTestButton.setEnabled(true)
  }

  def updateController(orderInfoHolder: Option[TestingOrder]) {
    this.orderInfoHolder = orderInfoHolder
    updateTimeInfo(orderInfoHolder)
    orderInfoHolder.foreach { orderInfo =>

      val isDisposed = 
        startRoomTemperatureTestButton.isDisposed || startOvenTestButton.isDisposed || 
        stopTestButton.isDisposed || currentStatus.isDisposed

      if (!isDisposed) {
      
        if (!orderInfo.isRoomTemperatureTested) {
          val shouldEnableStopRTButton = orderInfo.currentStatus != 6 && orderInfo.currentStatus != 7
          startRoomTemperatureTestButton.setEnabled(shouldEnableStopRTButton)
          startOvenTestButton.setEnabled(false)
          stopTestButton.setEnabled(shouldEnableStopRTButton)
        } else {
          startRoomTemperatureTestButton.setEnabled(false)
          val shouldEnableOvenTestButton = orderInfo.currentStatus == 0
          val shouldEnableStopButton = orderInfo.currentStatus != 6 && orderInfo.currentStatus != 7
          startOvenTestButton.setEnabled(shouldEnableOvenTestButton)
          stopTestButton.setEnabled(shouldEnableStopButton)
        }
        currentStatus.setText(orderInfo.statusDescription)
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

  init()


}


class TestSetting(parent: OrderStatusSummary) extends Composite(parent, SWT.NONE) {

  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  val partNoEntry = new TextEntryField("料　　號：", false, false, groupFrame)
  val voltage = new DropdownField("電壓設定：", TestSetting.voltageList, groupFrame)
  val testingTime = new DropdownField("測試時間：", TestSetting.testingTimeList, groupFrame)
  val capacity = new DropdownField("電容容量：", TestSetting.capacityList, groupFrame)
  val leakCurrent = new DropdownField("漏 電 流：", TestSetting.leakCurrentList, groupFrame)
  val testingInterval = new DropdownField("測試間隔：", TestSetting.intervalList, groupFrame)
  val marginOfError = new DropdownField("誤 差 值：", TestSetting.marginOfErrorList, groupFrame)
  val dx = new DropdownField("損 失 角：", TestSetting.dxList, groupFrame)

  def createNewOrder() = {
    TestSetting.db.insertNewTestingOrder(
      partNoEntry.getText,
      capacity.getText.toDouble,
      voltage.getText.toDouble,
      leakCurrent.getText,
      dx.getText.toDouble,
      marginOfError.getText.charAt(0).toString,
      testingTime.getText.toInt,
      testingInterval.getText.toInt,
      parent.daughterBoard,
      parent.testingBoard
    )
  }

  def init() {
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

  def clear() {
    this.partNoEntry.setText("")
    this.voltage.deselectAll()
    this.testingTime.deselectAll()
    this.capacity.deselectAll()
    this.leakCurrent.deselectAll()
    this.testingInterval.deselectAll()
    this.marginOfError.deselectAll()
    this.dx.deselectAll()
    this.setEnabled(true)
  }

  def updateSettingInfo(testingOrderHolder: Option[TestingOrder]) {

    val isDisposed = partNoEntry.isDisposed || voltage.isDisposed || testingTime.isDisposed || capacity.isDisposed || leakCurrent.isDisposed || testingInterval.isDisposed || marginOfError.isDisposed || dx.isDisposed

    if (!isDisposed) {

      testingOrderHolder match {
        case None => clear()
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

  def getSettingErrors(): List[String] = {

    var result: List[String] = Nil

    if (this.partNoEntry.isEmpty) {
      result ::= " - 未設定料號"
    }

    if (voltage.getSelection.isEmpty) {
      result ::= " - 未設定電壓"
    }

    if (testingTime.getSelection.isEmpty) {
      result ::= " - 未設定測試時間"
    }

    if (capacity.getSelection.isEmpty) {
      result ::= " - 未設定電容值"
    }

    if (leakCurrent.getSelection.isEmpty) {
      result ::= " - 未設定漏電流"
    }

    if (testingInterval.getSelection.isEmpty) {
      result ::= " - 未設定測試間隔"
    }

    if (marginOfError.getSelection.isEmpty) {
      result ::= " - 未設定誤差值"
    }

    if (dx.getSelection.isEmpty) {
      result ::= " - 未設定損失角"
    }

    val currentVoltageSettingHolder = TestSetting.db.getVoltageSetting(parent.daughterBoard)

    for {
      currentVoltageSetting <- currentVoltageSettingHolder
      newVoltageSetting <- Try(voltage.getText.toDouble)
    } {
      if (currentVoltageSetting != newVoltageSetting) {
        result ::= s" - 電壓設定需與同一子板的測試相同（$currentVoltageSetting）"
      }
    }

    result.reverse

  }

  init()
}

class CapacityBlock(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

  class CapacityInfo(title: String, parent: Composite) extends Composite(parent, SWT.NONE) {

    val greenColor = Display.getCurrent.getSystemColor(SWT.COLOR_GREEN)
    val redColor = Display.getCurrent.getSystemColor(SWT.COLOR_RED)

    val gridLayout = new GridLayout(2, false)
    val titleButton = createTitleButton(title)

    val capacityInfo = new TextEntryField("電容值：", true, false, this)
    val capacityStatusLabel = new Label(this, SWT.NONE)
    val dxValueInfo = new TextEntryField("損耗角：", true , false, this)
    val dxValueStatusLabel = new Label(this, SWT.NONE)
    val leakCurrentInfo = new TextEntryField("漏電流：", true, false, this)
    val leakCurrentStatusLabel = new Label(this, SWT.NONE)

    val titleButtonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)

    def init() {
      gridLayout.verticalSpacing = 2
      this.setLayout(gridLayout)
      titleButtonLayoutData.horizontalSpan = 2
      titleButton.setLayoutData(titleButtonLayoutData)
      capacityInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
      dxValueInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
      leakCurrentInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    }

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

    def clear() {
      capacityInfo.setText("")
      dxValueInfo.setText("")
      capacityStatusLabel.setText("")
      dxValueStatusLabel.setText("")
      titleButton.setBackground(null)
    }

    def updateStatus(testingResultHolder: Option[TestingResult]) {

      testingResultHolder.foreach { testingResult =>

        val isDisposed = 
          capacityInfo.isDisposed || dxValueInfo.isDisposed || capacityStatusLabel.isDisposed || 
          dxValueStatusLabel.isDisposed || titleButton.isDisposed

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

    init()
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
      new CapacityInfo("電容 1", groupFrame),
      new CapacityInfo("電容 2", groupFrame),
      new CapacityInfo("電容 3", groupFrame),
      new CapacityInfo("電容 4", groupFrame),
      new CapacityInfo("電容 5", groupFrame),
      new CapacityInfo("電容 6", groupFrame),
      new CapacityInfo("電容 7", groupFrame),
      new CapacityInfo("電容 8", groupFrame),
      new CapacityInfo("電容 9", groupFrame),
      new CapacityInfo("電容 10", groupFrame)
    )

    buttons.foreach(_.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true)))
    buttons
  }

  def clear() {
    buttons.foreach(_.clear())
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

class OrderStatusSummary(var isNewOrder: Boolean, blockNo: Int, val daughterBoard: Int, 
                         val testingBoard: Int, mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  var orderInfoHolder: Option[TestingOrder] = None

  val title = createTitleLabel()
  val composite = createComposite()
  val newOrderButton = createNewOrderButton()
  val navigationButtons = createNavigationButtons()
  val testSetting = createTestSetting()
  val testControl = createTestControl()
  val capacityBlock = createCapacityBlock()

  def getOrCreateOrder() = orderInfoHolder orElse testSetting.createNewOrder()

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

  def updateNewOrderButtonStatus() {
    val isTestStopped = orderInfoHolder.map(info => info.currentStatus == 6 || info.currentStatus == 7).getOrElse(false)
    if (!newOrderButton.isDisposed) {
      newOrderButton.setVisible(!isNewOrder)
      newOrderButton.setEnabled(!isNewOrder && isTestStopped)
    }
  }

  def clear() {
    this.isNewOrder = true
    this.orderInfoHolder = None
    testControl.clear()
    testSetting.clear()
    capacityBlock.clear()
    testSetting.setEnabled(true)
  }

  def createNewOrderButton() = {
    val isTestStopped = orderInfoHolder.map(info => info.currentStatus == 6 || info.currentStatus == 7).getOrElse(false)
    val newOrderButton = new Button(composite, SWT.PUSH)
    val newOrderButtonLayoutData = new GridData
    newOrderButtonLayoutData.heightHint = 50
    newOrderButtonLayoutData.widthHint = 150
    newOrderButtonLayoutData.horizontalAlignment = GridData.END
    newOrderButtonLayoutData.grabExcessHorizontalSpace = true
    newOrderButton.setLayoutData(newOrderButtonLayoutData)
    newOrderButton.setVisible(!isNewOrder)
    newOrderButton.setEnabled(!isNewOrder && isTestStopped)
    newOrderButton.setText("新測試")
    newOrderButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        clear()
      }
    })
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
    if (!isNewOrder) {
      orderInfoHolder = TestSetting.db.getTestingOrderByBlock(daughterBoard, testingBoard)
      updateNewOrderButtonStatus()
      testSetting.updateSettingInfo(orderInfoHolder)
      capacityBlock.updateCapacityInfo(orderInfoHolder)
      testControl.updateController(orderInfoHolder)
    }
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
