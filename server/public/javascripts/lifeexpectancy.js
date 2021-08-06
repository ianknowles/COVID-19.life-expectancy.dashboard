class CSVFile {
	constructor(fileurl) {
		this.fileurl = fileurl
	}

	fetch(callback) {
		d3.csv(this.fileurl).then(callback);
	}
}

//TODO eventually the server should provide json at least in the format provided by parsing the csv
//TODO the json may be filtered by passing json queries to the server
//TODO eventually the json should be produced and either permanently saved or stored using the play-cache
//TODO the template engine is capable of producing xml and csv. https://www.playframework.com/documentation/2.8.x/ScalaTemplates

//TODO button to change theme from light to dark https://www.playframework.com/documentation/2.8.x/ScalaResults#Setting-and-discarding-cookies
function difference(setA, setB) {
	let _difference = new Set(setA)
	for (let elem of setB) {
		_difference.delete(elem)
	}
	return _difference
}

var age_pickers = [{"id": "0", "value": "0"}, {"id": "1", "value": "60"}]

// figure 1 -- absolute levels of life expectancy
function plotFigure1(dataset, routes, config) {
	d3.select(window).on('resize', function () {
		plotFigure1(dataset, routes, config);
	});
	// Sequence generator function (commonly referred to as "range", e.g. Clojure, PHP etc)
	const range = (start, stop, step) => Array.from({length: (stop - start) / step + 1}, (_, i) => start + (i * step));
	let all_ages = new Set(range(0, 85, 10).concat(85))
	let figureID = "Figure-1"
	d3.select(`#${figureID}-select-more`).on('click', () => {
		if (age_pickers.length < 10) {
			let id = age_pickers.length
			let idstring = `${figureID}-select-age-${id}`
			let sibling = `${figureID}-select-age-${age_pickers.length - 1}`
			let selected_ages = new Set(age_pickers.map((x) => Number(x.value)))
			let unselected_ages = [...difference(all_ages, selected_ages)]
			let newage = unselected_ages[unselected_ages.length - 1].toString()
			age_pickers.push({"id": id, "value": newage});

			d3.select(`#${figureID}-select-container`)
				.insert('select', `#${sibling} + *`)
				.attr('id', idstring)
				.attr('class', "custom-select btn-outline-secondary h-100 w-auto")
				.attr('data-style', "btn-primary");
			plotFigure1(dataset, routes, config);
		}
	})
	d3.select(`#${figureID}-select-less`).on('click', () => {
		let id = age_pickers.pop().id
		let idstring = `${figureID}-select-age-${id}`;
		d3.select(`#${idstring}`).remove()
		plotFigure1(dataset, routes, config);
	})

	var ages = []
	for (picker of age_pickers) {
		let pickerElem = d3.select(`#${figureID}-select-age-${picker.id}`);
		pickerElem.selectAll('option').remove();
		pickerElem
			.selectAll('option')
			.data(all_ages)
			.enter()
			.append('option')
			.text(x => x)
			.attr('value', x => x);
		ages.push(picker.value)
		document.getElementById(`${figureID}-select-age-${picker.id}`).value = picker.value;
		pickerElem.on('change', function () {
			age_pickers.find(picker => this.id == `${figureID}-select-age-${picker.id}`).value = this.value;
			plotFigure1(dataset, routes, config);
		})
	}

	let nf = new Intl.NumberFormat(navigator.language, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	})

	try {
		nf = new Intl.NumberFormat(navigator.language, {
			style: 'unit',
			unit: 'year',
			unitDisplay: 'long',
			minimumFractionDigits: 2,
			maximumFractionDigits: 2
		})
	} catch (e) {
		if (e instanceof RangeError) {
			// safari v10 etc, can't format
			nf = new Intl.NumberFormat(navigator.language, {
				minimumFractionDigits: 2,
				maximumFractionDigits: 2
			})
		} else {
			throw e;
		}
	}

	let sortCol = "rank_e0f19"

	const urlParams = new URLSearchParams(window.location.search);
	if (urlParams.has('ages')) {
		ages = urlParams.getAll('ages');
	}
	let ageFilteredData = dataset.filter(row => ages.includes(row.age))
	let filteredData = ageFilteredData.filter(row => row.name !== "NA")
	let year2015 = filteredData.map(row => ({
		"code": row.code,
		"name": row.name,
		"sex": row.sex,
		"age": row.age,
		"ex": row.ex_2015,
		"col": "ex_2015",
		"year": "2015",
		sortCol: row[sortCol]
	}))
	let year2019 = filteredData.map(row => ({
		"code": row.code,
		"name": row.name,
		"sex": row.sex,
		"age": row.age,
		"ex": row.ex_2019,
		"col": "ex_2019",
		"year": "2019",
		sortCol: row[sortCol]
	}))
	let year2020 = filteredData.map(row => ({
		"code": row.code,
		"name": row.name,
		"sex": row.sex,
		"age": row.age,
		"ex": row.ex_2020,
		"col": "ex_2020",
		"year": "2020",
		sortCol: row[sortCol]
	}))
	let unsortedData = year2015.concat(year2019, year2020).filter(row => row.ex !== "NA")
	let data = unsortedData.sort((a, b) => d3.descending(parseFloat(a.sortCol), parseFloat(b.sortCol)))
	let names = [...new Set(data.map(row => row.name))];
	let namemap = {}
	data.forEach(row => namemap[row.name] = row.code);

	const svg = d3.select(`#${figureID}-SVG`);
	svg.selectAll("*").remove();
	let width = $(`#${figureID}`).width();
	let height = $(`#${figureID}`).height() - $(`#${figureID}-chart-caption`).height();
	svg.attr("viewBox", [0, 0, width, height]);
	let svgbound = svg.node().getBoundingClientRect()

	let margin = {left: 10, right: 205, top: 30, bottom: 10}

	let x_scales = d3.scaleBand()
		.domain(ages)
		.paddingInner(0.15)
		.rangeRound([margin.left, width - margin.right]);

	let x = ages.map(age => {
		let agevals = data.filter(row => age === row.age).map(row => row.ex)
		let ageMin = Math.floor(Math.min(...agevals))
		let ageMax = Math.ceil(Math.max(...agevals))
		return d3.scaleLinear()
			.domain([ageMin, ageMax])
			.range([x_scales(age), x_scales(age) + x_scales.bandwidth()])
	})

	let x_scale = d3.scaleOrdinal(ages, x)

	let sexes = ["Male", "Female"]
	let colour = d3.scaleOrdinal(sexes, ["#64B6EEFF", "#B5223BFF"])

	let years = ["ex_2015", "ex_2019", "ex_2020"]
	let shape = d3.scaleOrdinal(years, d3.symbols)
	let symbol = d3.symbol()

	let legend = svg.append("g")
		.attr("id", "legend")
	let legendr = legend.append("rect").attr("fill", "#eaeaea").attr('width', '100%').attr('height', margin.top)//.attr('y', margin.top)
	legend.selectAll("g")

	var legendx = 0
	var legendy = 15

	sexes.forEach(sex => {
		years.forEach(year => {
			let g = legend.append("g").attr("transform", `translate(${legendx + margin.left}, ${legendy})`)
			g.append('path')
				.attr('d', d => symbol.type(shape(year))())
				.attr("fill", d => colour(sex))
				.style("stroke-width", 2)
				.style("stroke", "none")
				.style("opacity", 0.8)
			let t = g.append("text")
				.text(d => `${sex} ${config.headers[year].label}`)
				.attr('x', 10)
				.attr('y', 5)
				.attr("class", "svgtext")
				.style("font-size", "12px")
				.style("font-weight", "bold")
			if (t.node().getBoundingClientRect().right > svgbound.right) {
				legendy += 15
				legendx = 0
				g.attr("transform", `translate(${legendx + margin.left}, ${legendy})`)
			}
			legendx = t.node().getBoundingClientRect().right + 5 - svgbound.left
		})
	})
	margin.top = legend.node().getBoundingClientRect().bottom + 5 - svgbound.top
	legendr.attr('height', margin.top)
	margin.top += 20

	let y = d3.scaleBand()
		.domain(names)
		.padding(0.15)
		.rangeRound([margin.top, height - margin.bottom])

	let yaxis = svg.append("g")
		.attr("id", "yAxis")
		.attr("transform", `translate(${width - margin.right}, 0)`)
	yaxis.append("rect").attr("fill", "#eaeaea").attr('width', '100%').attr('height', height - margin.top - margin.bottom).attr('y', margin.top)
	yaxis.call(d3.axisRight(y).tickSize(0).tickPadding(35))
	yaxis.selectAll("g")
		.attr('id', name => `yaxis-${namemap[name]}`)
		.append("image")
		.attr('width', '25px')
		.attr('height', y.bandwidth() - 1)
		.attr("x", 5)
		.attr('y', -(y.bandwidth() / 2))
		.attr('class', 'flag')
		.attr('href', name => (namemap[name] === "GB-EAW") || (namemap[name] === "GB-NIR") ? '' : routes[namemap[name]])
	d3.select("#yaxis-GB-EAW")
		.append("image")
		.attr('width', '25px')
		.attr('height', y.bandwidth() - 1)
		.attr("x", 5)
		.attr('y', -(y.bandwidth() / 2))
		.attr('class', 'flag')
		.attr('href', routes["GB-ENG"])
	d3.select("#yaxis-GB-EAW")
		.append("image")
		.attr('width', '25px')
		.attr('height', y.bandwidth() - 1)
		.attr("x", 35)
		.attr('y', -(y.bandwidth() / 2))
		.attr('class', 'flag')
		.attr('href', routes["GB-WLS"])
	d3.select("#yaxis-GB-EAW>text").attr("x", 65)

	var rowmouseover = function (event, d) {
		d3.select(this)
			.attr("fill", "black")
			.style("opacity", 0.5)
	}

	var rowmouseleave = function (event, d) {
		d3.select(this)
			.attr("fill", ((d3.select(this).attr("even") === "1") ? "white" : "#eaeaea"))
			.style("opacity", 1)
	}

	svg.append("g")
		.attr("id", "rowHighlights")
		.selectAll("g")
		.data(names)
		.join("rect")
		.attr("x", margin.left)
		.attr('y', d => y(d))
		.attr("width", width - margin.right - margin.left)
		.attr("height", y.bandwidth() - 1)
		.attr("fill", (d, i) => (i % 2 ? "white" : "#eaeaea"))
		.attr("even", (d, i) => (i % 2))
		.on("mouseenter", rowmouseover)
		.on("mouseout", rowmouseleave)

	x.forEach(xs => {
		svg.append("g")
			.attr("class", "xAxis")
			.attr("transform", `translate(0, ${margin.top})`)
			.call(d3.axisTop(xs).ticks(3, "s").tickSizeOuter(10))
	})

	let selectline = svg.append("g").attr("class", "selectline").attr("transform", `translate(0, ${margin.top})`).append("rect")
	selectline.attr("fill", "black")
		.attr('width', '1px')
		.attr('height', height - margin.bottom)
		.attr('y', 0)
		.attr('x', 0)
		.attr('class', 'd-none')
	// create a tooltip
	var Tooltip = d3.select(`#${figureID}-chart-area`)
		.append("div")
		.attr("class", "d3tooltip shadow d-none")
		.style("position", "fixed")
		.style("z-index", "9001")
		.style("background-color", "white")
		.style("border", "solid")
		.style("border-width", "2px")
		.style("border-radius", "5px")
		.style("padding", "5px")

	// Three function that change the tooltip when user hover / move / leave a cell
	var mouseover = function (d) {
		selectline.attr("class", "d-block")
		Tooltip
			.attr("class", "d-block")
		d3.select(this)
			.style("stroke", "black")
			.style("opacity", 1)

	}
	var mousemove = function (event, d) {
		let bound = d3.select(this).node().getBoundingClientRect()
		selectline.attr('x', bound.right - svgbound.left - ((bound.right - bound.left) / 2))
		Tooltip
			.html(`
		<div class="">
			<div class="d-flex flex-row">
				${d.code === "GB-NIR" ? '' : d.code === "GB-EAW" ? `<div class="pr-1"><img class="shadow" src=${routes["GB-ENG"]} width="25px" alt="England"><img class="shadow" src=${routes["GB-WLS"]} width="25px" alt="Wales"></div>` : `<div class="pr-1"><img class="shadow" src=${routes[d.code]} width="25px" alt=${d.name}></div>`}
				<div class="flex-fill">${d.name}</div>
			</div>
			<div class="d-flex flex-row">
				<div class="pr-1">${d.sex} ${d.year}</div>
			</div>
			<div class="d-flex flex-row">
				<div class=""><strong>${nf.format(d.ex)}</strong></div>
			</div>
		</div>
		`)
			.style("left", (event.clientX + 35) + "px")
			.style("top", (event.clientY) + "px")
	}

	var mouseleave = function (d) {
		selectline.attr("class", "d-none")
		Tooltip
			.attr("class", "d-none")
		d3.select(this)
			.style("stroke", "none")
			.style("opacity", 0.8)
	}

	let gpoint = svg.append("g").attr("id", "points")
		.selectAll("g")
		.data(data)
		.join("g")
		.attr("transform", (d, i) => `translate(${(x_scale(d.age))(d.ex)}, ${y(d.name) + (y.bandwidth() / 2)})`)
	gpoint.append('path')
		.attr('d', d => symbol.type(shape(d.col))())
		.attr("fill", d => colour(d.sex))
		.style("stroke-width", 2)
		.style("stroke", "none")
		.style("opacity", 0.8)
		.on("mouseover", mouseover)
		.on("mousemove", mousemove)
		.on("mouseleave", mouseleave)

	let agelabels = svg.append("g")
		.selectAll("text")
		.data(ages)
		.enter()
	agelabels.append("text")
		.text(d => "Age")
		.attr('x', d => x_scales(d) + 20)
		.attr('y', margin.top + 40)
		.attr("class", "svgtext")
		.style("font-size", "45px")
		.style("font-weight", "bold")

	agelabels
		.append("text")
		.text(d => d)
		.attr('x', d => x_scales(d) + 20)
		.attr('y', margin.top + 40 + 50)
		.attr("class", "svgtext")
		.style("font-size", "45px")
		.style("font-weight", "bold")

	d3.selectAll(".tick > text")
		.style("font-size", "15px")
		.style("font-weight", "bold")
	return svg.node();
}

function numberFormat(value, decimalPlaces) {
	return Number(Math.round(parseFloat(value + 'e' + decimalPlaces)) + 'e-' + decimalPlaces).toFixed(decimalPlaces)
}


// figure 2 -- changes in life expectancy
function plotFigure2(dataset, routes, config) {
	d3.select(window).on('resize', function () {
		plotFigure2(dataset, routes, config);
	});
	let figureID = "Figure-2"
	d3.select(`#${figureID}-select-more`).on('click', () => {
		if (age_pickers.length < 10) {
			let id = age_pickers.length
			let idstring = `${figureID}-select-age-${id}`
			let sibling = `${figureID}-select-age-${age_pickers.length - 1}`
			let selected_ages = new Set(age_pickers.map((x) => Number(x.value)))
			let unselected_ages = [...difference(all_ages, selected_ages)]
			let newage = unselected_ages[unselected_ages.length - 1].toString()
			age_pickers.push({"id": id, "value": newage});

			d3.select(`#${figureID}-select-container`)
				.insert('select', `#${sibling} + *`)
				.attr('id', idstring)
				.attr('class', "custom-select btn-outline-secondary h-100 w-auto")
				.attr('data-style', "btn-primary");
			plotFigure2(dataset, routes, config);
		}
	})
	d3.select(`#${figureID}-select-less`).on('click', () => {
		let id = age_pickers.pop().id
		let idstring = `${figureID}-select-age-${id}`;
		d3.select(`#${idstring}`).remove()
		plotFigure2(dataset, routes, config);
	})

	// Sequence generator function (commonly referred to as "range", e.g. Clojure, PHP etc)
	const range = (start, stop, step) => Array.from({length: (stop - start) / step + 1}, (_, i) => start + (i * step));

	let ages = []
	let all_ages = range(0, 85, 10).concat(85)
	for (picker of age_pickers) {
		let pickerElem = d3.select(`#${figureID}-select-age-${picker.id}`);
		pickerElem.selectAll('option').remove();
		pickerElem
			.selectAll('option')
			.data(all_ages)
			.enter()
			.append('option')
			.text(x => x)
			.attr('value', x => x);
		ages.push(picker.value)
		document.getElementById(`${figureID}-select-age-${picker.id}`).value = picker.value;
		pickerElem.on('change', function () {
			age_pickers.find(picker => this.id === `${figureID}-select-age-${picker.id}`).value = this.value;
			plotFigure2(dataset, routes, config);
		})
	}

	let nf = new Intl.NumberFormat(navigator.language, {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	})
	try {
		nf = new Intl.NumberFormat(navigator.language, {
			style: 'unit',
			unit: 'year',
			unitDisplay: 'long',
			minimumFractionDigits: 2,
			maximumFractionDigits: 2
		})
	} catch (e) {
		if (e instanceof RangeError) {
			// safari v10 etc, can't format
			nf = new Intl.NumberFormat(navigator.language, {
				minimumFractionDigits: 2,
				maximumFractionDigits: 2
			})
		} else {
			throw e;
		}
	}
	let sortCol = "rank_d0m20"

	const urlParams = new URLSearchParams(window.location.search);
	if (urlParams.has('ages')) {
		ages = urlParams.getAll('ages');
	}
	let ageFilteredData = dataset.filter(row => ages.includes(row.age))
	let filteredData = ageFilteredData.filter(row => row.name !== "NA")
	let avg_ex_diff_1519 = filteredData.map(row => ({
		"code": row.code,
		"name": row.name,
		"sex": row.sex,
		"age": row.age,
		"ex": row.avg_ex_diff_1519,
		"col": "avg_ex_diff_1519",
		sortCol: row[sortCol]
	}))
	let ex_diff = filteredData.map(row => ({
		"code": row.code,
		"name": row.name,
		"sex": row.sex,
		"age": row.age,
		"ex": row.ex_diff_1920,
		"col": "ex_diff",
		sortCol: row[sortCol]
	}))

	let unsortedData = avg_ex_diff_1519.concat(ex_diff).filter(row => row.ex !== "NA")
	let data = unsortedData.sort((a, b) => d3.descending(parseFloat(a.sortCol), parseFloat(b.sortCol)))

	let names = [...new Set(data.map(row => row.name))];
	let namemap = {}
	dataset.forEach(row => namemap[row.name] = row.code);

	const svg = d3.select(`#${figureID}-SVG`);
	svg.selectAll("*").remove();
	let width = $(`#${figureID}`).width();
	let height = $(`#${figureID}`).height() - $(`#${figureID}-chart-caption`).height();
	svg.attr("viewBox", [0, 0, width, height]);
	let svgbound = svg.node().getBoundingClientRect()

	let margin = {left: 10, right: 205, top: 10, bottom: 10}

	let x_scales = d3.scaleBand()
		.domain(ages)
		.paddingInner(0.15)
		.rangeRound([margin.left, width - margin.right]);

	let x = ages.map(age => {
		let agevals = data.filter(row => age == row.age).map(row => row.ex)
		let ageMin = Math.floor(Math.min(...agevals))
		let ageMax = Math.ceil(Math.max(...agevals))
		return d3.scaleLinear()
			.domain([ageMin, ageMax])
			.range([x_scales(age), x_scales(age) + x_scales.bandwidth()])
	})

	let x_scale = d3.scaleOrdinal(ages, x)

	let sexes = ["Male", "Female"]
	let colour = d3.scaleOrdinal(sexes, ["#64B6EEFF", "#B5223BFF"])

	let years = ["ex_diff", "avg_ex_diff_1519"]
	let shape = d3.scaleOrdinal(years, d3.symbols)

	let legend = svg.append("g")
		.attr("id", "legend")
	let legendr = legend.append("rect").attr("fill", "#eaeaea").attr('width', '100%').attr('height', margin.top)//.attr('y', margin.top)
	legend.selectAll("g")

	var legendx = 0
	var legendy = 15
	let symbol = d3.symbol()
	sexes.forEach(sex => {
		years.forEach(year => {
			let g = legend.append("g").attr("transform", `translate(${legendx + margin.left}, ${legendy})`)
			g.append('path')
				.attr('d', d => symbol.type(shape(year))())
				.attr("fill", d => colour(sex))
				.style("stroke-width", 2)
				.style("stroke", "none")
				.style("opacity", 0.8)
			let t = g.append("text")
				.text(d => `${sex} ${config.headers[year].label}`)
				.attr('x', 10)
				.attr('y', 5)
				.attr("class", "svgtext")
				.style("font-size", "12px")
				.style("font-weight", "bold")
			if (t.node().getBoundingClientRect().right > svgbound.right) {
				legendy += 15
				legendx = 0
				g.attr("transform", `translate(${legendx + margin.left}, ${legendy})`)
			}
			legendx = t.node().getBoundingClientRect().right + 5 - svgbound.left
		})
	})
	margin.top = legend.node().getBoundingClientRect().bottom + 5 - svgbound.top
	legendr.attr('height', margin.top)
	margin.top += 20

	let y = d3.scaleBand()
		.domain(names)
		.padding(0.15)
		.rangeRound([margin.top, height - margin.bottom])
	let yaxis = svg.append("g")
		.attr("id", "yAxis")
		.attr("transform", `translate(${width - margin.right}, 0)`)
	yaxis.append("rect").attr("fill", "#eaeaea").attr('width', margin.right).attr('height', height - margin.top - margin.bottom).attr('y', margin.top)
	yaxis.call(d3.axisRight(y).tickSize(0).tickPadding(35))
	yaxis.selectAll("g")
		.attr('id', name => `yaxis-${namemap[name]}`)
		.append("image")
		.attr('width', '25px')
		.attr('height', y.bandwidth() - 1)
		.attr("x", 5)
		.attr('y', -(y.bandwidth() / 2))
		.attr('class', 'flag')
		.attr('href', name => (namemap[name] == "GB-EAW") || (namemap[name] == "GB-NIR") ? '' : routes[namemap[name]])
	d3.select("#yaxis-GB-EAW")
		.append("image")
		.attr('width', '25px')
		.attr('height', y.bandwidth() - 1)
		.attr("x", 5)
		.attr('y', -(y.bandwidth() / 2))
		.attr('class', 'flag')
		.attr('href', routes["GB-ENG"])
	d3.select("#yaxis-GB-EAW")
		.append("image")
		.attr('width', '25px')
		.attr('height', y.bandwidth() - 1)
		.attr("x", 35)
		.attr('y', -(y.bandwidth() / 2))
		.attr('class', 'flag')
		.attr('href', routes["GB-WLS"])
	d3.select("#yaxis-GB-EAW>text").attr("x", 65)

	var rowmouseover = function (event, d) {
		d3.select(this)
			.attr("fill", "black")
			.style("opacity", 0.5)
	}

	var rowmouseleave = function (event, d) {
		d3.select(this)
			.attr("fill", ((d3.select(this).attr("even") === "1") ? "white" : "#eaeaea"))
			.style("opacity", 1)
	}

	svg.append("g")
		.attr("id", "rowHighlights")
		.selectAll("g")
		.data(names)
		.join("rect")
		.attr("x", margin.left)
		.attr('y', d => y(d))
		.attr("width", width - margin.right - margin.left)
		.attr("height", y.bandwidth() - 1)
		.attr("fill", (d, i) => (i % 2 ? "white" : "#eaeaea"))
		.attr("even", (d, i) => (i % 2))
		.on("mouseenter", rowmouseover)
		.on("mouseout", rowmouseleave)

	x.forEach(xs => {
		xs.xaxis = svg.append("g")
			.attr("class", "xAxis")
			.attr("transform", `translate(0, ${margin.top})`)
		xs.xaxis.call(d3.axisTop(xs).ticks(3, "s").tickSizeOuter(10))
		xs.xaxis.append("rect")
			.attr("fill", "black")
			.attr('width', '1px')
			.attr('height', height - margin.bottom)
			.attr('y', 0)
			.attr('x', xs(0))
	})

	let selectline = svg.append("g").attr("class", "selectline").attr("transform", `translate(0, ${margin.top})`).append("rect")
	selectline.attr("fill", "black")
		.attr('width', '1px')
		.attr('height', height - margin.bottom)
		.attr('y', 0)
		.attr('x', 0)
		.attr('class', 'd-none')


	// create a tooltip
	var Tooltip = d3.select(`#${figureID}-chart-area`)
		.append("div")
		.attr("class", "d3tooltip shadow d-none")
		.style("position", "fixed")
		.style("z-index", "9001")
		.style("background-color", "white")
		.style("border", "solid")
		.style("border-width", "2px")
		.style("border-radius", "5px")
		.style("padding", "5px")

	// Three function that change the tooltip when user hover / move / leave a cell
	var mouseover = function (d) {
		selectline.attr("class", "d-block")
		Tooltip
			.attr("class", "d-block")
		d3.select(this)
			.style("stroke", "black")
			.style("opacity", 1)
	}

	var mousemove = function (event, d) {
		let bound = d3.select(this).node().getBoundingClientRect()
		selectline.attr('x', bound.right - svgbound.left - ((bound.right - bound.left) / 2))
		Tooltip
			.html(`
		<div class="">
			<div class="d-flex flex-row">
				${d.code === "GB-NIR" ? '' : d.code === "GB-EAW" ? `<div class="pr-1"><img class="shadow" src=${routes["GB-ENG"]} width="25px" alt="England"><img class="shadow" src=${routes["GB-WLS"]} width="25px" alt="Wales"></div>` : `<div class="pr-1"><img class="shadow" src=${routes[d.code]} width="25px" alt=${d.name}></div>`}
				<div class="flex-fill">${d.name}</div>
			</div>
			<div class="d-flex flex-row">
				<div class="pr-1">${d.sex} ${config.headers[d.col].label}</div>
			</div>
			<div class="d-flex flex-row">
				<div class=""><strong>${nf.format(d.ex)}</strong></div>
			</div>
		</div>
		`)
			.style("left", (event.clientX + 35) + "px")
			.style("top", (event.clientY) + "px")
	}

	var mouseleave = function (d) {
		Tooltip
			.attr("class", "d-none")
		selectline.attr("class", "d-none")
		d3.select(this)
			.style("stroke", "none")
			.style("opacity", 0.8)
	}

	let gpoint = svg.append("g").attr("id", "points")
		.selectAll("g")
		.data(data)
		.join("g")
		.attr("transform", (d, i) => `translate(${(x_scale(d.age))(d.ex)}, ${y(d.name) + (y.bandwidth() / 2)})`)
	gpoint.append('path')
		.attr('d', d => symbol.type(shape(d.col))())
		.attr("fill", d => colour(d.sex))
		.style("stroke-width", 2)
		.style("stroke", "none")
		.style("opacity", 0.8)
		.on("mouseover", mouseover)
		.on("mousemove", mousemove)
		.on("mouseleave", mouseleave)


	let agelabels = svg.append("g")
		.selectAll("text")
		.data(ages)
		.enter()
	agelabels.append("text")
		.text(d => "Age")
		.attr('x', d => x_scales(d) + 20)
		.attr('y', margin.top + 40)
		.attr("class", "svgtext")
		.style("font-size", "45px")
		.style("font-weight", "bold")

	agelabels
		.append("text")
		.text(d => d)
		.attr('x', d => x_scales(d) + 20)
		.attr('y', margin.top + 40 + 50)
		.attr("class", "svgtext")
		.style("font-size", "45px")
		.style("font-weight", "bold")

	d3.selectAll(".tick > text")
		.style("font-size", "15px")
		.style("font-weight", "bold")

	return svg.node();
}

/**
 * Decimal adjustment of a number.
 *
 * @param {String}  type  The type of adjustment.
 * @param {Number}  value The number.
 * @param {Integer} exp   The exponent (the 10 logarithm of the adjustment base).
 * @returns {Number} The adjusted value.
 */
function decimalAdjust(type, value, exp) {
	// If the exp is undefined or zero...
	if (typeof exp === 'undefined' || +exp === 0) {
		return Math[type](value);
	}
	value = +value;
	exp = +exp;
	// If the value is not a number or the exp is not an integer...
	if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0)) {
		return NaN;
	}
	// Shift
	value = value.toString().split('e');
	value = Math[type](+(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp)));
	// Shift back
	value = value.toString().split('e');
	return +(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp));
}
