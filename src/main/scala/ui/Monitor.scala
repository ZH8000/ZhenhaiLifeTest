package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => _, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._

import java.util.concurrent._

class MonitorWindow(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  val gridLayout = MainGridLayout.createLayout(6)
  val navigationButtons = createNavigationButtons()
  val areaInfoList = createAreaInfoList()
  val scheduler = new ScheduledThreadPoolExecutor(1)
  val scheduledUpdate = scheduler.scheduleWithFixedDelay(createUpdater(), 0, 1, TimeUnit.SECONDS)

  /**
   *  建立導覽按鈕
   *
   *  @return     導覽按鈕
   */
  def createNavigationButtons() = {
    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.horizontalSpan = 6
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
    navigationButtons
  }

  /**
   *  建立各測試區塊（AreaInfo）元件的列表
   *
   *  @return     List[AreaInfo]，每一個 element 是代表一個區塊
   */
  def createAreaInfoList() = {

    val areaList = List(
      new AreaInfo(mainWindowShell, this, 1, 0, 0), 
      new AreaInfo(mainWindowShell, this, 2, 0, 1),
      new AreaInfo(mainWindowShell, this, 3, 1, 0),
      new AreaInfo(mainWindowShell, this, 4, 1, 1),
      new AreaInfo(mainWindowShell, this, 5, 2, 0),
      new AreaInfo(mainWindowShell, this, 6, 2, 1),
      new AreaInfo(mainWindowShell, this, 7, 3, 0), 
      new AreaInfo(mainWindowShell, this, 8, 3, 1),
      new AreaInfo(mainWindowShell, this, 9, 4, 0),
      new AreaInfo(mainWindowShell, this, 10, 4, 1),
      new AreaInfo(mainWindowShell, this, 11, 5, 0),
      new AreaInfo(mainWindowShell, this, 12, 5, 1)
    )

    areaList.foreach { areaInfo =>
      val gridData = new GridData
      gridData.horizontalAlignment = GridData.FILL
      gridData.verticalAlignment = GridData.FILL
      gridData.grabExcessHorizontalSpace = true
      gridData.grabExcessVerticalSpace = true
      areaInfo.setLayoutData(gridData)
    }

    areaList
  }

  /**
   *  建立用來更新 AreaInfo 的 Runnable 物件
   *
   *  @return     更新 AreaInfo 的 Runnable 物件
   */
  def createUpdater() = {
    new Runnable() {
      override def run() {
        Display.getDefault.asyncExec(new Runnable() {
          override def run() {
            areaInfoList.foreach(_.updateAreaInfo())
          }
        })
      }
    }
  }

  /**
   *  初始化與設定 Layout
   */
  def init() {
    this.setLayout(gridLayout)
    this.addDisposeListener(new DisposeListener() {
      override def widgetDisposed(event: DisposeEvent) {
        scheduledUpdate.cancel(false)
        scheduler.shutdown()
      }
    })
  }

  init()

}

