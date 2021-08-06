package graphs

import org.scalajs.dom.Node

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSGlobalScope}

@js.native
@JSGlobal
class CSVFile(fileurl: String) extends js.Object {
	def fetch(callback: js.Function1[parseCSVResult, Unit]): Unit = js.native
}

@js.native
//@JSGlobal
trait parseCSVResult extends js.Array[js.Dictionary[js.Any]] {
	var columns: js.Array[String] = js.native
}

@js.native
@JSGlobalScope
object DOMGlobalScope extends js.Object {
	def plotFigure1(dataset: parseCSVResult, routes: js.Dictionary[String], config:js.Dynamic): Node = js.native
	def plotFigure2(dataset: parseCSVResult, routes: js.Dictionary[String], config:js.Dynamic): Node = js.native
}

object ExcessGraphing {
	val lifeExpectancyFile: CSVFile = new CSVFile(js.Dynamic.global.jsRoutes.controllers.Assets.versioned("data/df_ex.csv").url.toString)
}
