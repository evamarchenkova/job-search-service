package domain.user

import domain.credentials.Credentials

import java.util.UUID

case class User(id: UUID, email: String, credentials: Credentials) {
  def toUserResponse: UserResponse = {
    UserResponse(id = id)
  }
}
