package models

import controllers.routes.Assets

object Acknowledgements {
	val primary: List[Acknowledgement] = List(
		Acknowledgement(
			href = "https://www.sociology.ox.ac.uk/",
			alt = "University of Oxford logo",
			image = Assets.versioned("images/logos/oxweb-logo-rect.svg"),
			imageCSSClass = "oxford",
		),
		Acknowledgement(
			href = "https://www.demographicscience.ox.ac.uk/",
			alt = "LCDS logo",
			image = Assets.versioned("images/logos/LCDS_Logo_SVG_Blue.svg"),
			imageCSSClass = "lcds",
		),
		Acknowledgement(
			href = "https://www.sdu.dk/en/forskning/forskningsenheder/samf/cpop/",
			alt = "Cpop logo",
			image = Assets.versioned("images/logos/cpop_sdu_bw.png"),
			imageCSSClass = "cpop",
		)
	)
}
