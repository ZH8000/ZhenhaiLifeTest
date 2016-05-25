package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

/**
 *  每個電容的最新測試的資料的區塊（Group Frame）
 *
 *  @param    title                 電容標題
 *  @param    orderStatusSummary    此區塊所在的 OrderStatusSummary 視窗的物件
 */
class CapacityBlock(title: String, orderStatusSummary: OrderStatusSummary) extends Composite(orderStatusSummary, SWT.NONE) {

  val groupFrame = createGroupFrame()
  val buttons = createCapcityInfos()

  /**
   *  每個電容的資料的區塊（按鈕／電容值／損失角／漏電容標示）
   *
   *  @param    title           電容標題
   *  @param    capacityID      電容的編號
   *  @param    parent          上一層的 Composite
   */
  class CapacityInfo(title: String, capacityID: Int, parent: Composite) extends Composite(parent, SWT.NONE) {

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

    /**
     *  初始化
     */
    def init() {
      gridLayout.verticalSpacing = 2
      this.setLayout(gridLayout)
      titleButtonLayoutData.horizontalSpan = 2
      titleButton.setLayoutData(titleButtonLayoutData)
      capacityInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
      dxValueInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
      leakCurrentInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    }

    /**
     *  建立標題按鈕
     *
     *  @param    title   按鈕內的文字
     *  @return           按鈕 Widget 的物件
     */
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

          orderStatusSummary.orderInfoHolder match {
            case None =>
              val messageBox = new MessageBox(getShell, SWT.OK)
              messageBox.setText("尚無資料")
              messageBox.setMessage("此區塊尚無測試資料")
              messageBox.open()
            case Some(orderInfo) =>
              val detailPage = new OrderCapacityDetail(orderStatusSummary.blockNo, orderInfo, capacityID, MainWindow.mainWindowShell)
              MainWindow.pushComposite(detailPage)
            }
        }
      })
      button
    }

    /**
     *  清除 UI 上的測試資訊
     */
    def clear() {
      capacityInfo.setText("")
      dxValueInfo.setText("")
      leakCurrentInfo.setText("")
      capacityStatusLabel.setText("")
      dxValueStatusLabel.setText("")
      titleButton.setBackground(null)
    }

    /**
     *  依照傳入的測試結果物件來更新 UI 上的資料
     *
     *  @param    testingResultHolder     測試結果
     */
    def updateStatus(testingResultHolder: Option[TestingResult]) {

      testingResultHolder.foreach { testingResult =>

        val isDisposed = 
          capacityInfo.isDisposed || dxValueInfo.isDisposed || capacityStatusLabel.isDisposed || 
          dxValueStatusLabel.isDisposed || titleButton.isDisposed || leakCurrentInfo.isDisposed

        if (!isDisposed) {

          capacityInfo.setText(testingResult.capacity.toString)
          dxValueInfo.setText(testingResult.dxValue.toString)
        
          val isCapacityOKIcon = if (testingResult.isCapacityOK) "O" else "X"
          val isDXValueOKIcon = if (testingResult.isDXValueOK) "O" else "X"
        
          capacityStatusLabel.setText(isCapacityOKIcon)
          dxValueStatusLabel.setText(isDXValueOKIcon)

          val titleButtonColor = if (testingResult.isOK) greenColor else redColor
          titleButton.setBackground(titleButtonColor)

          if (testingResult.leakCurrent != -1) {
            leakCurrentInfo.setText(testingResult.leakCurrent.toString)
          }
        }
      }
    }

    init()
  }

  /**
   *  初始化
   */
  def init() {
    this.setLayout(new FillLayout)
  }

  /**
   *  建立 Group Frame
   *
   *  @return       GroupFrame 的物件
   */
  def createGroupFrame() = {
    val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
    val gridLayout = new GridLayout(5, true)
    gridLayout.horizontalSpacing = 50
    groupFrame.setLayout(gridLayout)
    groupFrame.setText(title)
    groupFrame
  }

  /**
   *  建立電容資訊矩陣
   *
   *  @return         電容測試結果的矩陣
   */
  def createCapcityInfos() = {
    val buttons = Array(
      new CapacityInfo("電容 1", 1, groupFrame),
      new CapacityInfo("電容 2", 2, groupFrame),
      new CapacityInfo("電容 3", 3, groupFrame),
      new CapacityInfo("電容 4", 4, groupFrame),
      new CapacityInfo("電容 5", 5, groupFrame),
      new CapacityInfo("電容 6", 6, groupFrame),
      new CapacityInfo("電容 7", 7, groupFrame),
      new CapacityInfo("電容 8", 8, groupFrame),
      new CapacityInfo("電容 9", 9, groupFrame),
      new CapacityInfo("電容 10", 10, groupFrame)
    )

    buttons.foreach(_.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true)))
    buttons
  }

  /**
   *  清除所有電容測試的資料
   */
  def clear() {
    buttons.foreach(_.clear())
  }

  /**
   *  依照傳傳入的測試單資訊來更新 UI 上的訊息
   */
  def updateCapacityInfo(orderInfoHolder: Option[TestingOrder]) {

    for {
      orderInfo <- orderInfoHolder
      capacityID <- 1 to 10
      button = buttons(capacityID-1)
    } {
      button.updateStatus(LifeTestOptions.db.getTestingResult(orderInfo.id, capacityID))
    }

  }

  init()
}

