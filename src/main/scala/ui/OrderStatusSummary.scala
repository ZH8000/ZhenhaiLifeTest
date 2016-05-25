package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import java.util.Date
import java.text.SimpleDateFormat
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import java.util.concurrent._
import scala.util.Try

/**
 *  即時監控後各區塊點下去後出現的，列出測試單和每顆電容最新測試結果的頁面
 *
 *  @param      isNewOrder        是否是在輸入新的測試單的狀態
 *  @param      blockNo           區塊編號
 *  @param      daughterBoard     子板編號
 *  @param      testingBoard      測試板編號
 *  @param      mainWindowShell   主視窗的 Shell 物件
 *  @param      orderInfoHolder   目前此區塊正在執行的測試單資訊
 */
class OrderStatusSummary(var isNewOrder: Boolean, val blockNo: Int, val daughterBoard: Int, 
                         val testingBoard: Int, mainWindowShell: Shell, 
                         var orderInfoHolder: Option[TestingOrder] = None) extends Composite(mainWindowShell, SWT.NONE) {

  val isHistory = blockNo == -1

  val gridLayout = MainGridLayout.createLayout(3)

  val title = createTitleLabel()
  val composite = createComposite()
  val newOrderButton = createNewOrderButton()
  val navigationButtons = createNavigationButtons()
  val testSetting = createTestSetting()
  val testControl = createTestControl()
  val capacityBlock = createCapacityBlock()

  val scheduler = new ScheduledThreadPoolExecutor(1)
  val scheduledUpdate = initWindowAndScheduleUpdate()

  /**
   *  取得目前的測試單，若還無測試單，則建立一個新的測試單
   */
  def getOrCreateOrder() = orderInfoHolder orElse testSetting.createNewOrder()

  /**
   *  建立此頁面自身的 Composite
   */
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

  /**
   *  更新「新測試單」按鈕的狀態
   */
  def updateNewOrderButtonStatus() {
    val isTestStopped = orderInfoHolder.map(info => info.currentStatus == 6 || info.currentStatus == 7).getOrElse(false)
    if (!newOrderButton.isDisposed) {
      newOrderButton.setVisible(!isNewOrder && !isHistory)
      newOrderButton.setEnabled(!isNewOrder && isTestStopped)
    }
  }

  /**
   *  清除所有狀態
   */
  def clear() {
    this.isNewOrder = true
    this.orderInfoHolder = None
    testControl.clear()
    testSetting.clear()
    capacityBlock.clear()
    testSetting.setEnabled(true)
  }

  /**
   *  建立「新測試單」的按鈕
   */
  def createNewOrderButton() = {
    val isTestStopped = orderInfoHolder.map(info => info.currentStatus == 6 || info.currentStatus == 7).getOrElse(false)
    val newOrderButton = new Button(composite, SWT.PUSH)
    val newOrderButtonLayoutData = new GridData
    newOrderButtonLayoutData.heightHint = 50
    newOrderButtonLayoutData.widthHint = 150
    newOrderButtonLayoutData.horizontalAlignment = GridData.END
    newOrderButtonLayoutData.grabExcessHorizontalSpace = true
    newOrderButton.setLayoutData(newOrderButtonLayoutData)
    newOrderButton.setVisible(!isNewOrder && !isHistory)
    newOrderButton.setEnabled(!isNewOrder && !isHistory && isTestStopped)
    newOrderButton.setText("新測試")
    newOrderButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        clear()
      }
    })
    newOrderButton
  }

  /**
   *  建立標題的文字標籤
   */
  def createTitleLabel() = {
    val title = new Label(this, SWT.NONE)
    val titleLayoutData = new GridData
    titleLayoutData.horizontalAlignment = GridData.CENTER
    titleLayoutData.grabExcessHorizontalSpace = true
    titleLayoutData.horizontalSpan = 2
    title.setLayoutData(titleLayoutData)
    if (isHistory) {
      title.setText(s"電容測試（歷史資料）")
    } else {
      title.setText(s"電容測試（區域 $blockNo）")
    }
    title
  }

  /**
   *  建立導覽按鈕
   */
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

  /**
   *  建立左上方設定測試資料的區塊
   */
  def createTestSetting() = {
    val testSetting = new TestSetting(this)
    val testSettingLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    testSettingLayoutData.horizontalSpan = 2
    testSetting.setLayoutData(testSettingLayoutData)
    testSetting
  }

  /**
   *  建立右上方控制測試流程的控制項的區塊
   */
  def createTestControl() = {
    val testControl = new TestControl(this)
    val testControlLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    testControl.setLayoutData(testControlLayoutData)
    testControl
  }

  /**
   *  建立測試結果顯示區塊
   */
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

  /**
   *  更新 UI 上的資訊
   */
  def updateInfo() {
    if (!isNewOrder) {

      if (!isHistory) {
        orderInfoHolder = LifeTestOptions.db.getTestingOrderByBlock(daughterBoard, testingBoard)
      }
      updateNewOrderButtonStatus()
      testSetting.updateSettingInfo(orderInfoHolder)
      capacityBlock.updateCapacityInfo(orderInfoHolder)
      testControl.updateController(orderInfoHolder)
    }
  }

  /**
   *  初始化 Layout 與更新 UI 的 Thread
   */
  def initWindowAndScheduleUpdate() = {

    this.setLayout(gridLayout)
    updateInfo()

    val updater = new Runnable { 
      def run() { 
        Display.getDefault.asyncExec(new Runnable() {
          override def run() {
            if (!isHistory) {
              updateInfo()
            }
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
