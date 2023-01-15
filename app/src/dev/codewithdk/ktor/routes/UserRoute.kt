package dev.codewithdk.ktor.routes

import dev.codewithdk.ktor.UserSession
import dev.codewithdk.ktor.controller.UserOnboardController
import dev.codewithdk.ktor.exception.BadRequestException
import dev.codewithdk.ktor.exception.FailureMessages
import dev.codewithdk.ktor.exception.UnauthorizedActivityException
import dev.codewithdk.ktor.models.request.ChangPasswordRequest
import dev.codewithdk.ktor.models.request.UserLogout
import dev.codewithdk.ktor.models.response.generateHttpResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.userOnboard(userOnboardController: UserOnboardController) {

    authenticate {

        route("/user") {
            post("/{id}/changePassword") {
                val userId = call.parameters["id"] ?: throw BadRequestException("Userid must not be empty")
                val changPasswordRequest = withContext(Dispatchers.IO) {
                    call.receive<ChangPasswordRequest>()
                }

                call.principal<UserSession>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

                val authResponse = userOnboardController.updatePassword(userId, changPasswordRequest)
                val response = generateHttpResponse(authResponse)

                call.respond(response.code, response.body)
            }

            post("/{id}/update") {
                // update user details
            }

            get("/{id}") {
                val userId = call.parameters["id"] ?: throw BadRequestException("User id must not be empty")

                call.principal<UserSession>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)

                val allUserView = userOnboardController.getUsersById(userId)

                print("allUserView==== $allUserView")
                val res = generateHttpResponse(allUserView)
                call.respond(res.code, res.body)
            }

            post("/{id}/logout") {
                // remove session and make user logout
                // TODO: Need to check how to logout user on JWT
                val userId = call.parameters["id"] ?: throw BadRequestException("User id must not be empty")
                val userLogoutRequest = withContext(Dispatchers.IO) {
                    call.receive<UserLogout>()
                }

                call.principal<UserSession>()
                    ?: throw UnauthorizedActivityException(FailureMessages.MESSAGE_ACCESS_DENIED)
                val logout = userOnboardController.logout(userId, userLogoutRequest.userType)
                //call.sessions.clear("")
                call.sessions.clear<UserSession>()

                val response = generateHttpResponse(logout)

                call.respond(response.code, response.body)
            }
        }
    }
}