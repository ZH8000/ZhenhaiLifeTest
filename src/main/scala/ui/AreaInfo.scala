package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

import java.util.concurrent._

/**
 *  用來顯示各區塊的狀態的 Composite 元件
 *  
 *  @param      mainWindowShell   主視窗的 Shell 物件
 *  @param      parent            上一層的 Composite 物件
 *  @param      areaNumber        區塊編號
 *  @param      daughterBoard     子板編號
 *  @param      testingBoard      測試板編號
 */
class AreaInfo(mainWindowShell: Shell, parent: Composite, areaNumber: Int, 
               daughterBoard: Int, testingBoard: Int) extends Composite(parent, SWT.NONE) {

  /**
   *  用來存放測試單狀態，若此區塊有測試單的資料，則為 Some(測試單)，否則為 None
   */
  var orderInfoHolder: Option[TestingOrder] = None

  val gridLayout = new GridLayout(1, true)
  val areaButton = createAreaButton()
  val (partNo, okCount, testedTime, status) = createInfoBox()

  /**
   *  建立區塊編號的按鈕
   *
   *  @return     區塊編號的按鈕
   */
  def createAreaButton() = {
    val areaButton = new Button(this, SWT.PUSH)
    val areaButtonLayoutData = new GridData

    areaButtonLayoutData.horizontalAlignment = GridData.FILL
    areaButtonLayoutData.verticalAlignment = GridData.BEGINNING
    areaButtonLayoutData.grabExcessHorizontalSpace = true
    areaButtonLayoutData.grabExcessVerticalSpace = false
    areaButtonLayoutData.heightHint = 50
    areaButton.setText(s"區塊 $areaNumber")
    areaButton.setLayoutData(areaButtonLayoutData)
    areaButton
  }

  /**
   *  建立電料號、良品數、測試時間與狀態的資訊 Group 
   *  
   *  @return       (料號, 良品數, 測試時間, 狀態) 的文字方塊的 Tuple
   */
  def createInfoBox() = {

    val areaInfoGroup = new Group(this, SWT.SHADOW_ETCHED_IN)
    val areaInfoLayoutData = new GridData

    areaInfoLayoutData.horizontalAlignment = GridData.FILL
    areaInfoLayoutData.verticalAlignment = GridData.FILL
    areaInfoLayoutData.grabExcessHorizontalSpace = true
    areaInfoLayoutData.grabExcessVerticalSpace = true
    areaInfoLayoutData.minimumHeight = 50
    areaInfoGroup.setLayoutData(areaInfoLayoutData)

    val areaInfoFillLayout = new GridLayout(1, true)

    areaInfoFillLayout.marginWidth = 10
    areaInfoFillLayout.marginHeight = 10

    areaInfoGroup.setLayout(areaInfoFillLayout)

    val partNo = new TextEntryField("料號：", true, true, areaInfoGroup)
    val okCount = new TextEntryField("良品：", true, true, areaInfoGroup)
    val testedTime = new TextEntryField("測試時間：", true, true, areaInfoGroup)
    val status = new TextEntryField("狀態：", true, false, areaInfoGroup)

    partNo.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))
    okCount.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))
    testedTime.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))
    status.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false))

    areaButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        MainWindow.appendLog(s"點選「${areaButton.getText}」")
        MainWindow.pushComposite(
          new OrderStatusSummary(
            orderInfoHolder.isEmpty, areaNumber, 
            daughterBoard, testingBoard, mainWindowShell
          )
        )
      }
    })

    (partNo, okCount, testedTime, status)
  }

  /**
   *  初始化與設定 Layout
   */
  def init() {
    this.setLayout(gridLayout)
    updateAreaInfo()
  }

  /**
   *  依照資料庫裡的資料來更新測試區塊的狀態
   */
  def updateAreaInfo() {
    this.orderInfoHolder = TestSetting.db.getTestingOrderByBlock(daughterBoard, testingBoard)
    orderInfoHolder.foreach { orderInfo =>
      val goodCount = TestSetting.db.getGoodCount(orderInfo.id)
      val isNotDisposed = !partNo.isDisposed && !okCount.isDisposed && !testedTime.isDisposed && !status.isDisposed

      if (isNotDisposed) {
        partNo.setText(orderInfo.partNo)
        okCount.setText(goodCount.toString)
        testedTime.setText(orderInfo.duration)
        status.setText(orderInfo.statusDescription)
      }
    }

  }

  init()
}

