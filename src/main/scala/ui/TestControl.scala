package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import java.text.SimpleDateFormat
import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  「控制測試區塊的 UI 物件」右上方，用來顯示測試狀態，以及控制測試的按鈕
 *
 *  @param    orderStatusSummary      控制測試區塊的 UI 物件
 */
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

  /**
   *  建立按鈕
   *
   *  @param      title   按鈕的標題
   *  @return             按鈕
   */
  def createButton(title: String) = {
    val button = new Button(groupFrame, SWT.PUSH)
    val buttonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    buttonLayoutData.horizontalSpan = 2
    button.setLayoutData(buttonLayoutData)
    button.setText(title)
    button
  }

  /**
   *  建立文字含標籤的文字方塊
   *
   *  @param      title   文字方塊的標題
   *  @return             含標籤的文字方塊
   */
  def createTextEntry(title: String) = {
    val dateTimeEntry = new TextEntryField(title, true, false, groupFrame)
    val layoutData = new GridData(SWT.FILL, SWT.FILL, true, false)
    layoutData.horizontalSpan = 3
    dateTimeEntry.setLayoutData(layoutData)
    dateTimeEntry
  }

  /**
   *  啟動室溫測試程序
   */
  def startRoomTemperatureTest() {

    val settingErrors = orderStatusSummary.testSetting.getSettingErrors

    if (settingErrors.isEmpty) {
      val confirmBox = new MessageBox(TestControl.this.getShell, SWT.ICON_INFORMATION|SWT.OK|SWT.CANCEL)
      confirmBox.setMessage("請將測試板插入室溫測試專用插槽中後按下確認按鈕。")
      val responseCode = confirmBox.open()

      if (responseCode == SWT.OK) {
        val dialog = new RoomTemperatureTestingDialog(orderStatusSummary, getShell)
        dialog.open()
      }

    } else {
      val messages = settingErrors.mkString("\n")
      val messageBox = new MessageBox(TestControl.this.getShell, SWT.ICON_WARNING|SWT.OK)
      messageBox.setMessage("設定錯誤：\n\n" + messages + "\n\n")
      messageBox.open()
    }
  }

  /**
   *  啟動烤箱測試程序
   */
  def startOvenTest() {
    orderStatusSummary.orderInfoHolder.foreach { orderInfo =>
      val dialog = new OvenTestingDialog(orderInfo, getShell)
      dialog.open()
    }

  }

  /**
   *  初始化 Composite 元件
   */
  def init() {
    this.setLayout(new FillLayout)
    groupFrame.setLayout(new GridLayout(6, true))
    groupFrame.setText("測試控制")
    startOvenTestButton.setEnabled(false)
    stopTestButton.setEnabled(false)

    if (orderStatusSummary.isHistory) {
      startOvenTestButton.setEnabled(false)
      startRoomTemperatureTestButton.setEnabled(false)
      stopTestButton.setEnabled(false)
      startOvenTestButton.setVisible(false)
      startRoomTemperatureTestButton.setVisible(false)
      stopTestButton.setVisible(false)
    }

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
          LifeTestOptions.db.abortTest(orderInfo.id) 
          stopTestButton.setEnabled(false)
        }
      }
    })
  }

  /**
   *  清除所有的狀態
   */
  def clear() {
    startDate.setText("")
    startTime.setText("")
    testedTime.setText("")
    currentStatus.setText("")
    stopTestButton.setEnabled(false)
    startOvenTestButton.setEnabled(false)
    startRoomTemperatureTestButton.setEnabled(true)
  }

  /**
   *  更新控制按鈕的狀態
   *
   *  @param    orderInfoHolder       測試單狀態
   */
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

  /**
   *  更新計時器的狀態
   *
   *  @param    orderInfoHolder       測試單狀態
   */
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


