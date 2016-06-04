package tw.com.zhenhai.lifetest

import jssc.SerialPortList

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import scala.util.Try

/**
 *  「儀器設定」的畫面
 *
 *  @param    mainWindowShell     主視窗
 */
class EditParameter(mainWindowShell: Shell) extends Composite(mainWindowShell, SWT.NONE) {

  val gridLayout = MainGridLayout.createLayout(1)
  val navigationButtons = createNavigationButtons()

  // 數量設定區塊
  val group = createMainGroup()
  val maxDaughterBoardCount = createMaxDaughterBoardCount()
  val maxCapacityCount = createMaxCapacityCount()

  // 室溫專用插槽區塊
  val roomTemperatureGroup = createRoomTempeartureGroup()
  val roomTemperatureDaughterBoard = createRoomTemperatureDaughterBoard()
  val roomTemperatureTestingBoard = createRoomTemperatureTestingBoard()


  // RS232 區塊
  val rs232Group = createRS232Group()
  val mainBoardRS232 = createMainBoardRS232()
  val lcrRS232 = createLCRRS232()
  val lcRS232 = createLCRS232()
  val powerSupply1 = createPowerSupply1()
  val powerSupply2 = createPowerSupply2()
  val powerSupply3 = createPowerSupply3()
  val powerSupply4 = createPowerSupply4()
  val powerSupply5 = createPowerSupply5()

  // 下方「套用」的按鈕列
  val buttonRow = createButtonRow()
  
  /**
   *  建立「室溫專用測試插槽設定」的 GroupFrame
   *
   *  @return   「室溫專用測試插槽設定」的 GroupFrame
   */
  def createRoomTempeartureGroup() = {
    val group = new Group(this, SWT.SHADOW_ETCHED_IN)
    val groupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    group.setLayoutData(groupLayoutData)
    group.setLayout(new GridLayout(2, true))
    group.setText("室溫專用測試插槽設定")
    group
  }

  /**
   *  建立「室溫專用插槽」子板編號的下拉式選單
   *
   *  @return     「室溫專用插槽」子板編號的下拉式選單
   */
  def createRoomTemperatureDaughterBoard() = {
    val daughterBoard = new DropdownField("子板編號：", (0 to 6).toList, roomTemperatureGroup)
    daughterBoard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    daughterBoard.setText(LifeTestOptions.getRoomTemperatureDaughterBoard.toString)
    daughterBoard
  }

  /**
   *  建立「室溫專用插槽」烤箱板編號的下拉式選單
   *
   *  @return     「室溫專用插槽」烤箱板編號的下拉式選單
   */
  def createRoomTemperatureTestingBoard() = {
    val testingBoard = new DropdownField("測試板編號：", (0 to 1).toList, roomTemperatureGroup)
    testingBoard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    testingBoard.setText(LifeTestOptions.getRoomTemperatureTestingBoard.toString)
    testingBoard
  }

  /**
   *  建立「套用」按鈕的按鈕列
   *
   *  @return     「套用」按鈕的按鈕列
   */
  def createButtonGroup() = {
    
    val composite = new Composite(this, SWT.NONE)

    composite.setLayout(new GridLayout(1, true))

    val okButton = new Button(composite, SWT.PUSH)
    val okButtonLayoutData = new GridData
    okButtonLayoutData.horizontalAlignment = GridData.FILL
    okButtonLayoutData.verticalAlignment = GridData.FILL
    okButtonLayoutData.grabExcessHorizontalSpace = true
    okButtonLayoutData.grabExcessVerticalSpace = true
    okButton.setLayoutData(okButtonLayoutData)
    okButton.setText("套用")
    okButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        mainBoardRS232.getPort.foreach { port => LifeTestOptions.setMainBoardRS232Port(port) }
        lcrRS232.getPort.foreach { port => LifeTestOptions.setLCRRS232Port(port) }
        lcRS232.getPort.foreach { port => LifeTestOptions.setLCRS232Port(port) }
        powerSupply1.getPort.foreach { port => LifeTestOptions.setPower1RS232Port(port) }
        powerSupply2.getPort.foreach { port => LifeTestOptions.setPower2RS232Port(port) }
        powerSupply3.getPort.foreach { port => LifeTestOptions.setPower3RS232Port(port) }
        powerSupply4.getPort.foreach { port => LifeTestOptions.setPower4RS232Port(port) }
        powerSupply5.getPort.foreach { port => LifeTestOptions.setPower5RS232Port(port) }
        Try(maxDaughterBoardCount.getText.toInt).foreach { maxCount => LifeTestOptions.setMaxDaughterBoardCount(maxCount) }
        Try(maxCapacityCount.getText.toInt).foreach { maxCount => LifeTestOptions.setMaxCapacityCount(maxCount) }
        Try(roomTemperatureDaughterBoard.getText.toInt).foreach { n => LifeTestOptions.setRoomTemperatureDaughterBoard(n) }
        Try(roomTemperatureTestingBoard.getText.toInt).foreach { n => LifeTestOptions.setRoomTemperatureTestingBoard(n) }
        LifeTestOptions.save()
        Runtime.getRuntime.exec("gksudo -m 請輸入管理員密碼重新開機，以使新設定生效 reboot")
      }
    })

    composite
  }

  /**
   *  建立導覽按鈕列
   *
   *  @return       建立導覽按鈕列
   */
  def createNavigationButtons() = {
    val navigationButtons = new NavigationButtons(this)
    val navigationButtonsLayoutData = new GridData
    navigationButtonsLayoutData.heightHint = 50
    navigationButtonsLayoutData.widthHint = 300
    navigationButtonsLayoutData.horizontalAlignment = GridData.END
    navigationButtonsLayoutData.horizontalSpan = 2
    navigationButtonsLayoutData.grabExcessHorizontalSpace = true
    navigationButtons.setLayoutData(navigationButtonsLayoutData)
    navigationButtons
  }

  /**
   *  建立「基本設定」的 Group Frame
   *
   *  @return         建立「基本設定」的 Group Frame
   */
  def createMainGroup() = {
    val group = new Group(this, SWT.SHADOW_ETCHED_IN)
    val groupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)
    group.setLayoutData(groupLayoutData)
    group.setLayout(new GridLayout(2, true))
    group.setText("基本設定")
    group
  }

  /**
   *  建立主板 RS232 連接埠偵測介面
   *
   *  @return     建立主板 RS232 連接埠偵測介面
   */
  def createMainBoardRS232() = {
    val mainBoardRS232 = new RS232InterfaceSettingComposite("主板", rs232Group)
    mainBoardRS232.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getMainBoardRS232Port.foreach(mainBoardRS232.setPort _)
    mainBoardRS232
  }

  /**
   *  建立 LCR 容量計 RS232 連接埠偵測介面
   *
   *  @return     建立 LCR 容量計 RS232 連接埠偵測介面
   */
  def createLCRRS232() = {
    val lcrRS232 = new RS232InterfaceSettingComposite("LCR 容量計", rs232Group)
    lcrRS232.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getLCRRS232Port.foreach(lcrRS232.setPort _)
    lcrRS232
  }

  /**
   *  建立 LC 漏電流 RS232 連接埠偵測介面
   *
   *  @return     建立 LC 漏電流 RS232 連接埠偵測介面
   */
  def createLCRS232() = {
    val lcRS232 = new RS232InterfaceSettingComposite("LC 漏電流測試儀", rs232Group)
    lcRS232.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getLCRS232Port.foreach(lcRS232.setPort _)
    lcRS232
  }

  /**
   *  建立電源供應器 1 的 RS232 連接埠偵測介面
   *
   *  @return     建立電源供應器 1 的 RS232 連接埠偵測介面
   */
  def createPowerSupply1() = {
    val power1Supply = new RS232InterfaceSettingComposite("電源供應器 1", rs232Group)
    power1Supply.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getPower1RS232Port.foreach(power1Supply.setPort _)
    power1Supply
  }

  /**
   *  建立電源供應器 2 的 RS232 連接埠偵測介面
   *
   *  @return     建立電源供應器 2 的 RS232 連接埠偵測介面
   */
  def createPowerSupply2() = {
    val power2Supply = new RS232InterfaceSettingComposite("電源供應器 2", rs232Group)
    power2Supply.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getPower2RS232Port.foreach(power2Supply.setPort _)
    power2Supply
  }

  /**
   *  建立電源供應器 3 的 RS232 連接埠偵測介面
   *
   *  @return     建立電源供應器 3 的 RS232 連接埠偵測介面
   */
  def createPowerSupply3() = {
    val power3Supply = new RS232InterfaceSettingComposite("電源供應器 3", rs232Group)
    power3Supply.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getPower3RS232Port.foreach(power3Supply.setPort _)
    power3Supply
  }

  /**
   *  建立電源供應器 4 的 RS232 連接埠偵測介面
   *
   *  @return     建立電源供應器 4 的 RS232 連接埠偵測介面
   */
  def createPowerSupply4() = {
    val power4Supply = new RS232InterfaceSettingComposite("電源供應器 4", rs232Group)
    power4Supply.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getPower4RS232Port.foreach(power4Supply.setPort _)
    power4Supply
  }

  /**
   *  建立電源供應器 5 的 RS232 連接埠偵測介面
   *
   *  @return     建立電源供應器 5 的 RS232 連接埠偵測介面
   */
  def createPowerSupply5() = {
    val power5Supply = new RS232InterfaceSettingComposite("電源供應器 5", rs232Group)
    power5Supply.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false))
    LifeTestOptions.getPower5RS232Port.foreach(power5Supply.setPort _)
    power5Supply
  }

  /**
   *  建立「儀器 RS232 設定」的 Group Frame
   *
   *  @return         「儀器 RS232 設定」的 Group Frame
   */
  def createRS232Group() = {
    val rs232Group = new Group(this, SWT.SHADOW_ETCHED_IN)
    val rs232GroupLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false)

    rs232Group.setLayoutData(rs232GroupLayoutData)
    rs232Group.setLayout(new GridLayout(1, true))
    rs232Group.setText("儀器 RS232 設定")

    rs232Group
  }

  /**
   *  建立下方「套用」的按鈕列
   *  
   *  @return     下方「套用」的按鈕列
   */
  def createButtonRow() = {
    val buttonGroup = createButtonGroup()
    val buttonGroupLayoutData = new GridData(SWT.END, SWT.FILL, true, false)
    buttonGroup.setLayoutData(buttonGroupLayoutData)
    buttonGroupLayoutData.heightHint = 50
    buttonGroupLayoutData.widthHint = 300
    buttonGroup
  }

  /**
   *  建立「子板上最大電容數」的下拉式選單
   *
   *  @return       「子板上最大電容數」的下拉式選單
   */
  def createMaxCapacityCount() = {
    val capacityCount = new DropdownField("子板上最大電容數：", (1 to 10).toList, group)
    capacityCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    capacityCount.setText(LifeTestOptions.getMaxCapacityCount.toString)
    capacityCount
  }

  /**
   *  建立「子板數目」的下拉式選單
   *
   *  @return       「子板數目」的下拉式選單
   */
  def createMaxDaughterBoardCount() = {
    val daughterBoardCount = new DropdownField("子板數目：", List(1, 2, 3, 4, 5, 6, 7), group)
    daughterBoardCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    daughterBoardCount.setText(LifeTestOptions.getMaxDaughterBoardCount.toString)
    daughterBoardCount
  }

  /**
   *  初始化與設定 Layout 方式
   */
  def init() = {
    this.setLayout(gridLayout)
  }

  init()
}
