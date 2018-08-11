package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import javax.inject.Named
import models.shilhouette.{PasswordInfoRepository, UserEnv, UserIdentityService, UserRepository}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.mvc.CookieHeaderEncoding

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Silhouette[UserEnv]].to[SilhouetteProvider[UserEnv]]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher())
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoRepository]
    bind[Clock].toInstance(Clock())
    bind[EventBus].toInstance(EventBus())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[UserRepository].to[UserRepository]
  }

  @Provides
  def credentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry
  ): CredentialsProvider =
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  @Provides
  def authInfoRepository(passwordDao: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository =
    new DelegableAuthInfoRepository(passwordDao)

  @Provides
  def passwordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry =
    PasswordHasherRegistry(passwordHasher)

  @Provides
  def environment(
    userIdentityService: UserIdentityService,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus
  ): Environment[UserEnv] =
    Environment[UserEnv](
      userIdentityService,
      authenticatorService,
      Seq(),
      eventBus
    )

  @Provides
  def cookieAuthenticator(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock
  ): AuthenticatorService[CookieAuthenticator] = {
    import scala.concurrent.duration._
    val config = CookieAuthenticatorSettings(
      cookieName = "id",
      cookiePath = "/",
      cookieDomain = None,
      secureCookie = false,
      httpOnlyCookie = true,
      useFingerprinting = true,
      cookieMaxAge = None,
      authenticatorIdleTimeout = None,
      authenticatorExpiry = 12 hours
    )

    val encoder = new CrypterAuthenticatorEncoder(crypter)
    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, encoder, fingerprintGenerator, idGenerator, clock)
  }

  @Provides
  @Named("authenticator-signer")
  def authenticatorSigner(configuration: Configuration): Signer =
    new JcaSigner(JcaSignerSettings("SecretKey"))

  @Provides
  @Named("authenticator-crypter")
  def authenticatorCrypter(configuration: Configuration): Crypter =
    new JcaCrypter(JcaCrypterSettings("SecretKey"))
}
