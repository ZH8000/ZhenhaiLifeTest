package zhenhai.lifetest.controller.device

/**
 *  LCR 測定結果
 *
 *  @param  capacityValue     電容值，單位為 uF
 *  @param  capacityStatus    電容值判定結果
 *  @param  dxValue           dx 值
 *  @param  dxStatus          dx 值判定結果
 *  @param  totalStatus       整體判定結果
 */
case class LCRResult(capacityValue: BigDecimal, capacityStatus: String, dxValue: BigDecimal, dxStatus: String, totalStatus: String) {

  /**
   *  將使用者設定的誤差值轉為上下限百方比
   *
   *  @param    marginOfError     誤差值代碼（A / B / D / K / M / Y）
   *  @return                     (下限百分比, 上限百分比) 的 Tuple
   */
  def codeToMargin(marginOfError: String): (BigDecimal, BigDecimal) = marginOfError match {
    case "A" => (0,             20/100.0)
    case "B" => (-20/100.0,     0)
    case "D" => (-25/100.0,     20/100.0)
    case "K" => (-10/100.0,     10/100.0)
    case "M" => (-20/100.0,     20/100.0)
    case "Y" => (-10/100.0,     20/100.0)
    case _   => (0,             0)
  }

  /**
   *  檢查電容值是否在誤差範圍內
   *
   *  @param    standardCapacity      使用者設定的標準電容值
   *  @param    marginOfError         誤差範圍代碼
   *  @return                         如果在誤差範圍內就是 true，否則為 false
   */
  def isCapacityOK(standardCapacity: BigDecimal, marginOfError: String) = {
    val (lowerBound, higherBound) = codeToMargin(marginOfError)
    capacityValue >= standardCapacity + (standardCapacity * lowerBound) &&
    capacityValue <= standardCapacity + (standardCapacity * higherBound)
  }

  /**
   *  檢查電容值是否在誤差範圍內
   *
   *  @param    standardCapacity      使用者設定的標準 DX（tanδ / 誤差角） 值
   *  @param    marginOfError         誤差範圍代碼
   *  @return                         如果在誤差範圍內就是 true，否則為 false
   */
  def isDXValueOK(standardDXValue: BigDecimal, marginOfError: String) = {
    val (lowerBound, higherBound) = codeToMargin(marginOfError)

    dxValue >= standardDXValue + (standardDXValue * lowerBound) &&
    dxValue <= standardDXValue + (standardDXValue * higherBound)

    true
  }
}

