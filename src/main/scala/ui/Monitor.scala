package tw.com.zhenhai.lifetest;

import zhenhai.lifetest.controller.model._

import org.eclipse.swt._
import org.eclipse.swt.widgets._
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

import java.util.concurrent._

class MonitorWindow(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  class AreaInfo(parent: Composite, areaNumber: Int, 
                 daughterBoard: Int, testingBoard: Int, hasInfo: Boolean) extends Composite(parent, SWT.NONE) {

    var orderInfoHolder: Option[TestingOrder] = None
    val areaButton = createButton
    val (partNo, okCount, testedTime, status) = createInfoBox
    val gridLayout = new GridLayout(1, true)

    def init() {
      this.setLayout(gridLayout)
      updateAreaInfo()
    }

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

    def createButton = {
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
  
    def createInfoBox = {
  
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
          MainWindow.pushComposite(new OrderStatusSummary(orderInfoHolder.isEmpty, areaNumber, daughterBoard, testingBoard, mainWindowShell))
        }
      })
  
      (partNo, okCount, testedTime, status)
    }
    init()
  }

  val scheduler = new ScheduledThreadPoolExecutor(1)
  val scheduledUpdate = initWindowAndScheduleUpdate()

  def initWindowAndScheduleUpdate() = {

    val gridLayout = new GridLayout(6, true)

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
    navigationButtonsLayoutData.horizontalSpan = 6

    navigationButtons.setLayoutData(navigationButtonsLayoutData)

    val areas = Array(
      new AreaInfo(this, 1, 0, 0, true), 
      new AreaInfo(this, 2, 0, 1, true),
      new AreaInfo(this, 3, 1, 0, false),
      new AreaInfo(this, 4, 1, 1, true),
      new AreaInfo(this, 5, 2, 0, false),
      new AreaInfo(this, 6, 2, 1, true),
      new AreaInfo(this, 7, 3, 0, true), 
      new AreaInfo(this, 8, 3, 1, true),
      new AreaInfo(this, 9, 4, 0, false),
      new AreaInfo(this, 10, 4, 1, true),
      new AreaInfo(this, 11, 5, 0, false),
      new AreaInfo(this, 12, 5, 1, true)
    )

    def createGridData = {
      val gridData = new GridData
      gridData.horizontalAlignment = GridData.FILL
      gridData.verticalAlignment = GridData.FILL
      gridData.grabExcessHorizontalSpace = true
      gridData.grabExcessVerticalSpace = true
      gridData
    } 

    areas.foreach(_.setLayoutData(createGridData))
    val updater = new Runnable() {
      override def run() {
        Display.getDefault.asyncExec(new Runnable() {
          override def run() {
            areas.foreach(_.updateAreaInfo())
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

