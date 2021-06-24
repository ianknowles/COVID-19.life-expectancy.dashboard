package models

import controllers.routes

object Team {
	val people: List[TeamMember] = List(
		TeamMember(
			name = "Ian Knowles",
			job = "DevOp",
			desc = "Ian is a data engineer with an academic background and wide-ranging experience in industry. He has " +
				"development experience in most major languages and has worked on projects that range from embedded device " +
				"firmware and applications, to desktop and mobile applications, and on to full stack web server development " +
				"and operations.\n" +
				"He developed the website backend, frontend, and data visualisations.",
			imageURL = routes.Assets.versioned("images/staff/ian.jpg"),
			links = List(Link(icon = "github", url = "https://github.com/ianknowles", text = "ianknowles"))
		)
	)
}
