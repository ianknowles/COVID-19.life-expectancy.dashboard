package models

import play.api.mvc.Call

//TODO url type for href?
case class Acknowledgement(href: String, alt: String, image: Call, imageCSSClass: String)
