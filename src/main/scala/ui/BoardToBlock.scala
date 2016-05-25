package tw.com.zhenhai.lifetest

/**
 *  從子板和烤箱板的編號，來轉成 UI 上顯示的區塊編號
 */
object BoardToBlock {

  /**
   *  從（子板、烤相板）的編號方式，來推出在監控程式上顯示的區塊編號
   *
   *  @param    daughterBoard       子板編號
   *  @param    testingBoard        烤箱板編號
   *  @return                       區塊編號
   */
  def from(daughterBoard: Int, testingBoard: Int): Int = {
    (daughterBoard, testingBoard) match {
      case (0, 0) => 1
      case (0, 1) => 2
      case (1, 0) => 3
      case (1, 1) => 4
      case (2, 0) => 5
      case (2, 1) => 6
      case (3, 0) => 7
      case (3, 1) => 8 
      case (4, 0) => 9
      case (4, 1) => 10
      case (5, 0) => 11
      case (5, 1) => 12
      case _      => -1
    }
  }
}

