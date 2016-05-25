package tw.com.zhenhai.lifetest

import zhenhai.lifetest.controller.model._
import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import scala.util.Try

/**
 *  設定測試單資料的 Composite 元件
 *
 *  @param    parent      上一層的 OrderStatusSummary 元件
 */
class TestSetting(parent: OrderStatusSummary) extends Composite(parent, SWT.NONE) {

  val groupFrame = new Group(this, SWT.SHADOW_ETCHED_IN)
  val partNoEntry = new TextEntryField("料　　號：", false, false, groupFrame)
  val voltage = new DropdownField("電壓設定：", LifeTestOptions.voltageList, groupFrame)
  val testingTime = new DropdownField("測試時間：", LifeTestOptions.testingTimeList, groupFrame)
  val capacity = new DropdownField("電容容量：", LifeTestOptions.capacityList, groupFrame)
  val leakCurrent = new DropdownField("漏 電 流：", LifeTestOptions.leakCurrentList, groupFrame)
  val testingInterval = new DropdownField("測試間隔：", LifeTestOptions.intervalList, groupFrame)
  val marginOfError = new DropdownField("誤 差 值：", LifeTestOptions.marginOfErrorList, groupFrame)
  val dx = new DropdownField("損 失 角：", LifeTestOptions.dxList, groupFrame)

  /**
   *  在資料庫裡建立新的測試單
   *
   *  @return         新的測試單
   */
  def createNewOrder() = {
    LifeTestOptions.db.insertNewTestingOrder(
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

  /**
   *  初始化此 Composite
   */
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

  /**
   *  清除 UI 上所有的設定
   */
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

  /**
   *  依照傳入的測試單狀態來更新 UI 畫面上的資料
   *
   *  @param      testingOrderHolder      測試單狀態
   */
  def updateSettingInfo(testingOrderHolder: Option[TestingOrder]) {

    val isDisposed = 
      partNoEntry.isDisposed || voltage.isDisposed || 
      testingTime.isDisposed || capacity.isDisposed || 
      leakCurrent.isDisposed || testingInterval.isDisposed || 
      marginOfError.isDisposed || dx.isDisposed

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
          this.marginOfError.setText(LifeTestOptions.marginOfErrorCodeToFullText(testingOrder.marginOfError))
          this.dx.setText(testingOrder.dxValue.toString)
      }
    }
  }

  /**
   *  取得設定錯誤的資料
   *
   *  @return     錯誤訊息的 List
   */
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

    val currentVoltageSettingHolder = LifeTestOptions.db.getVoltageSetting(parent.daughterBoard)

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

