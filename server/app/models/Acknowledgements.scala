package models

import controllers.routes.Assets

object Acknowledgements {
	val primary: List[Acknowledgement] = List(
		Acknowledgement(
			href = "https://www.ox.ac.uk/",
			alt = "University of Oxford logo",
			image = Assets.versioned("images/oxweb-logo-rect.svg"),
			imageCSSClass = "oxford",
		),
	)
}