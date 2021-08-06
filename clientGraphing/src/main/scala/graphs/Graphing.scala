package graphs

import graphs.DOMGlobalScope.{plotFigure1, plotFigure2}
import org.scalajs.dom
import org.scalajs.dom.{Element, MouseEvent, Node, NodeList, document}
import org.scalajs.dom.raw.{HTMLElement, HTMLInputElement, HTMLTableElement}
import org.scalajs.dom.ext._

import scala.scalajs.js
import scala.scalajs.js.RegExp

//TODO improve and break down table class, probably to subclasses dealing with header, footer, rows, cols, cells
//https://developer.mozilla.org/en-US/docs/Web/API/HTMLTableElement
//https://developer.mozilla.org/en-US/docs/Web/HTML/Element/tfoot
//https://developer.mozilla.org/en-US/docs/Web/HTML/Element/colgroup
//https://developer.mozilla.org/en-US/docs/Web/HTML/Element/data
//https://developer.mozilla.org/en-US/docs/Web/API/HTMLDataElement
class Table(element: HTMLTableElement, dataset: parseCSVResult, config:js.Dynamic) {
	var sortCol = ""
	var sort = ""
	//TODO get rid of this unattached elem
	var sortElem: HTMLElement = document.createElement("th").asInstanceOf[HTMLElement]
	def tbody: Element = element.querySelector("tbody")
	def rows: NodeList = tbody.querySelectorAll("tr")

	def tabulate(selectedrows: js.Array[js.Dictionary[js.Any]] = dataset): Unit = {
		def rowsort(event: MouseEvent): Unit = {
			//TODO why is target missing attrs? had to switch to currentTarget
			val elem: HTMLElement = event.currentTarget.asInstanceOf[HTMLElement]
			if (sortCol != elem.dataset("col")) {
				sortElem.classList.remove("active")
				sortElem = elem
				sortElem.classList.add("active")
				sortCol = elem.dataset("col")
				sort = this.config.headers.selectDynamic(sortCol).selectDynamic("type").toString match {
					case "Float" => "FloatAscending"
					case "Integer" => "IntegerAscending"
					case _ => "StringAscending"
				}
			}
			val nodeArray = sort match {
				case "FloatAscending" => sort = "FloatDescending"
					//TODO set active class and display classes
					sortElem.classList.remove("descending")
					sortElem.classList.add("ascending")
					//TODO add a rowid to eliminate using queryselector and lookup data in the dataset
					rows.sortWith((a, b) => a.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toFloat < b.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toFloat)
				case "FloatDescending" => sort = "FloatAscending"
					sortElem.classList.remove("ascending")
					sortElem.classList.add("descending")
					rows.sortWith((a, b) => a.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toFloat > b.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toFloat)
				case "IntegerAscending" => sort = "IntegerDescending"
					sortElem.classList.remove("descending")
					sortElem.classList.add("ascending")
					rows.sortWith((a, b) => a.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toInt < b.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toInt)
				case "IntegerDescending" => sort = "IntegerAscending"
					sortElem.classList.remove("ascending")
					sortElem.classList.add("descending")
					rows.sortWith((a, b) => a.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toInt > b.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent.toInt)
				case "StringDescending" => sort = "StringAscending"
					sortElem.classList.remove("ascending")
					sortElem.classList.add("descending")
					rows.sortWith((a, b) => a.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent > b.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent)
				case _ => sort = "StringDescending"
					sortElem.classList.remove("descending")
					sortElem.classList.add("ascending")
					rows.sortWith((a, b) => a.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent < b.asInstanceOf[Element].querySelector(s"[data-th=$sortCol").textContent)
			}
			nodeArray.foreach((node: Node) => tbody.appendChild(node))
		}
		element.querySelectorAll("thead, tbody").foreach(child => element.removeChild(child))

		val tableHeaderRow = element.appendChild(document.createElement("thead")).appendChild(document.createElement("tr"))
		val cellElement = document.createElement("th")
		tableHeaderRow.appendChild(cellElement)
		for (columnName <- dataset.columns) {
			if (config.headers.selectDynamic(columnName).selectDynamic("label").toString != "") {
				val cellElement = document.createElement("th")
				cellElement.setAttribute("data-col", columnName)
				cellElement.setAttribute("class", "header")
				cellElement.addEventListener("click", (event: dom.MouseEvent) => {
					rowsort(event)
				})
				val contain = document.createElement("div")
				contain.setAttribute("class", "d-flex align-items-end")
				val textdiv = document.createElement("div")
				var label = config.headers.selectDynamic(columnName).selectDynamic("label").toString
				if (label == "") {
					label = columnName
				}
				textdiv.textContent = label

				val (icondown, iconup) = config.headers.selectDynamic(columnName).selectDynamic("type").toString match {
					case "Integer" | "Float" => ("sort-numeric-down", "sort-numeric-up")
					case "String" => ("sort-alpha-down", "sort-alpha-up")
					case _ => ("sort-down", "sort-up")
				}
				val svgcontain = document.createElement("div")
				val svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
				svg.classList.add("bi")
				svg.classList.add("ml-1")
				svg.classList.add("indicator-sort-down")
				svg.setAttribute("width", "16px")
				svg.setAttribute("height", "16px")
				val uselink = document.createElementNS("http://www.w3.org/2000/svg", "use")
				uselink.setAttribute("href", s"${js.Dynamic.global.jsRoutes.controllers.Assets.versioned("lib/bootstrap-icons/bootstrap-icons.svg").url.toString}#$icondown")
				svg.appendChild(uselink)

				val svgup = document.createElementNS("http://www.w3.org/2000/svg", "svg")
				svgup.classList.add("bi")
				svgup.classList.add("ml-1")
				svgup.classList.add("indicator-sort-up")
				svgup.setAttribute("width", "16px")
				svgup.setAttribute("height", "16px")
				val uselinkup = document.createElementNS("http://www.w3.org/2000/svg", "use")
				uselinkup.setAttribute("href", s"${js.Dynamic.global.jsRoutes.controllers.Assets.versioned("lib/bootstrap-icons/bootstrap-icons.svg").url.toString}#$iconup")
				svgup.appendChild(uselinkup)

				svgcontain.appendChild(svg)
				svgcontain.appendChild(svgup)
				contain.appendChild(textdiv)
				contain.appendChild(svgcontain)
				cellElement.appendChild(contain)
				tableHeaderRow.appendChild(cellElement)
			}
		}

		val tableBody = element.appendChild(document.createElement("tbody"))
		for (dataRow <- selectedrows) {

				val rowElement = document.createElement("tr")
				rowElement.setAttribute("id", dataRow(dataset.columns(0)).toString)
				rowElement.setAttribute("class", "row-anchor")

			val cellElement = document.createElement("td")
			dataRow("code").toString match {
				case "GB-NIR" =>
				case "GB-EAW" =>
					val image = document.createElement("img")
					image.setAttribute("class", "flag shadow")
					image.setAttribute("src", js.Dynamic.global.jsRoutes.controllers.Assets.versioned(s"images/flags/gb-eng.svg").url.toString)
					image.setAttribute("width", "40px")
					cellElement.appendChild(image)
					val image2 = document.createElement("img")
					image2.setAttribute("class", "flag shadow")
					image2.setAttribute("src", js.Dynamic.global.jsRoutes.controllers.Assets.versioned(s"images/flags/gb-wls.svg").url.toString)
					image2.setAttribute("width", "40px")
					cellElement.appendChild(image2)
				case _ =>
					val image = document.createElement("img")
					image.setAttribute("class", "flag shadow")
					image.setAttribute("src", js.Dynamic.global.jsRoutes.controllers.Assets.versioned(s"images/flags/${dataRow("code").toString.toLowerCase}.svg").url.toString)
					image.setAttribute("width", "40px")
					cellElement.appendChild(image)
			}
			rowElement.appendChild(cellElement)

				for (columnName <- dataset.columns) {
					val label = config.headers.selectDynamic(columnName).selectDynamic("label").toString
					if (label != "") {
						val cellElement = document.createElement("td")
						cellElement.setAttribute("id", s"$columnName-${dataRow(dataset.columns(0)).toString}")
						cellElement.setAttribute("data-th", columnName)
						cellElement.setAttribute("data-label", label)
						//TODO eventually the dataset will have correct types and we can type match here
						cellElement.textContent = config.headers.selectDynamic(columnName).selectDynamic("type").toString match {
							case "Float" => f"${dataRow(columnName).toString.toFloat}%1.2f"
							case _ => dataRow(columnName).toString
						}
						rowElement.appendChild(cellElement)
					}
				}
				tableBody.appendChild(rowElement)
		}

		document.getElementById("figure-table-shade").setAttribute("class", "d-none")
		//scrollToWindowHash()
	}

	def addSearch(element: HTMLInputElement): Unit = {
		element.addEventListener("keyup", (event: dom.KeyboardEvent) => {
			val text = element.value.trim();
			var searchResults = dataset.filter(r => {
				val regex = new RegExp("^" + text + ".*", "i")
				regex.test(r(config.selectDynamic("search_col").toString).toString)
			})
			tabulate(searchResults)
		})
	}
}

object Graphing {
	def parseFloatForNumberSort(x: String): Float = {
		val y = x.toFloat
		if (y.isNaN) 0 else y
	}

	def plot(dataset: parseCSVResult): Unit = {
		val config:js.Dynamic = js.Dynamic.literal(
			"id" -> "excess",
			"label" -> "Excess",
			"filename" -> ".csv",
			"search_col" -> "name",
			"headers" -> js.Dynamic.literal(
				"anchorkey" -> "ccg19cd",
				"desckey" -> "name",
				"code" -> js.Dictionary("label" -> "ISO 3166-1 alpha-2", "type" -> "String"),
				"sex" -> js.Dictionary("label" -> "Sex", "type" -> "String"),
				"year" -> js.Dictionary("label" -> "", "type" -> "Integer"),
				"age" -> js.Dictionary("label" -> "Age", "type" -> "Integer"),
				"ex" -> js.Dictionary("label" -> "", "type" -> "Float"),
				"ex_diff" -> js.Dictionary("label" -> "Life Expectancy Change (2020-2019)", "type" -> "Float"),
				"ex_diff_1519" -> js.Dictionary("label" -> "Life Expectancy Change (2019-2015)", "type" -> "Float"),
				"ex_2015" -> js.Dictionary("label" -> "Life Expectancy (2015)", "type" -> "Float"),
				"ex_2019" -> js.Dictionary("label" -> "Life Expectancy (2019)", "type" -> "Float"),
				"ex_2020" -> js.Dictionary("label" -> "Life Expectancy (2020)", "type" -> "Float"),
				"name" -> js.Dictionary("label" -> "Country", "type" -> "String"),
				"code_hmd" -> js.Dictionary("label" -> "", "type" -> "String"),
				"rank_e0f19" -> js.Dictionary("label" -> "", "type" -> "Integer"),
				"rank_d0m20" -> js.Dictionary("label" -> "", "type" -> "Integer"),
				"avg_ex_diff_1519" -> js.Dictionary("label" -> "Average Life Expectancy Change (2019-2015)", "type" -> "Float"),
				"ex_diff_1920" -> js.Dictionary("label" -> "Life Expectancy Change (2020-2019)", "type" -> "Float"),
				"ex_diff_1920_q025" -> js.Dictionary("label" -> "", "type" -> "Float"),
				"ex_diff_1920_q975" -> js.Dictionary("label" -> "", "type" -> "Float"),
			)
		)
		val t = new Table(document.getElementById("figure-table").asInstanceOf[HTMLTableElement], dataset, config)
		t.tabulate()
		t.addSearch(document.getElementById("table-filter").asInstanceOf[HTMLInputElement])
		val countries = Array("AT", "BE", "BG", "CH", "CL", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GB-ENG", "GB-SCT", "GB-WLS", "GR", "HR", "HU", "IS", "IT", "LT", "NL", "NO", "PL", "PT", "SE", "SI", "SK", "US")
		val cdict = js.Dictionary[String]()
		countries.foreach(c => cdict(c) = js.Dynamic.global.jsRoutes.controllers.Assets.versioned(s"images/flags/${c.toLowerCase}.svg").url.toString)
		document.querySelector("figure").getAttribute("id") match {
			case "Figure-2" =>
				plotFigure2(dataset, cdict, config)
			case _ =>
				plotFigure1(dataset, cdict, config)
		}
	}

	def main(args: Array[String]): Unit = {
		graphs.ExcessGraphing.lifeExpectancyFile.fetch(plot)
	}
}
