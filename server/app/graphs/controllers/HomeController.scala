package graphs.controllers

import javax.inject._
import models.{FileOption, Team}
import play.api._
import play.api.i18n._
import play.api.mvc._
import graphs.shared.SharedMessages

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents, config: Configuration)(implicit webJarsUtil: org.webjars.play.WebJarsUtil) extends BaseController with I18nSupport {

	def index: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		Ok(views.html.index(SharedMessages.itWorks))
	}

	def lcdsredirect: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		TemporaryRedirect("https://www.demographicscience.ox.ac.uk/news/categories/covid-19")
	}

	def demrisk: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		Ok(views.html.demrisk())
	}

	def coming_soon: Action[AnyContent] = TODO

	def dash: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		//TODO should use the directory listing to filter for files present
		Environment.simple().getFile("public/data").listFiles()

		//TODO the config loader is a lot of boilerplate, fetching the node as a js object or string then json parsing would
		// allow use of the auto json convert and avoid the boilerplate
		val files: Seq[FileOption] = config.get[Seq[FileOption]]("graph_files")

		Ok(views.html.dashboard(files.toList))
	}

	def about: Action[AnyContent] = TODO

	def donut: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		val files: Seq[FileOption] = config.get[Seq[FileOption]]("graph_files")
		Ok(views.html.donut(files.toList))
	}

	def project: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		Ok(views.html.project())
	}

	def team: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		Ok(views.html.team(Team.people))
	}

	def privacy: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
		Ok(views.html.privacy())
	}
}
