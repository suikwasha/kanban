package models.silhouette

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

trait UserEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}
