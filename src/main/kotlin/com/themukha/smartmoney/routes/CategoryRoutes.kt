package com.themukha.smartmoney.routes

import com.themukha.smartmoney.auth.getUserIdFromToken
import com.themukha.smartmoney.dto.CategoryDto
import com.themukha.smartmoney.dto.CreateCategoryRequest
import com.themukha.smartmoney.dto.ErrorResponse
import com.themukha.smartmoney.dto.UpdateCategoryRequest
import com.themukha.smartmoney.services.CategoryService
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.delete
import io.github.smiley4.ktorswaggerui.dsl.routing.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.categoryRoutes() {
    val categoryService by inject<CategoryService>()

    route("/categories") {
        authenticate("auth-jwt") {
            post({
                description = "Create a new category"
                request {
                    body<CreateCategoryRequest>()
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Category created"
                        body<CategoryDto>()
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid request"
                    }
                }
            }) {
                val userId = call.getUserIdFromToken()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
                    return@post
                }

                val request = call.receive<CreateCategoryRequest>()
                try {

                    val categoryDto = categoryService.createCategory(request, userId)
                    call.respond(HttpStatusCode.Created, categoryDto)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Failed to create category"))
                }

            }

            get({
                description = "Get categories"
                response {
                    HttpStatusCode.OK to {
                        description = "Categories retrieved"
                        body<List<CategoryDto>>()
                    }
                }

            }) {
                val userId = call.getUserIdFromToken()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
                    return@get
                }

                val walletIdParameter = call.request.queryParameters["walletId"]
                val walletId = walletIdParameter?.let { UUID.fromString(it) }

                try {
                    val categories = categoryService.getCategoriesForUser(userId, walletId)
                    call.respond(HttpStatusCode.OK, categories)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Failed to retrieve categories"))
                }
            }


            get("/{categoryId}", {
                description = "Get category by ID"
                response {
                    HttpStatusCode.OK to {
                        description = "Category details"
                        body<CategoryDto>()
                    }
                    HttpStatusCode.NotFound to { description = "Category not found" }
                }
            }) {
                val categoryId = call.parameters["categoryId"]?.let { UUID.fromString(it) }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid category ID"))

                val category = categoryService.getCategoryById(categoryId)
                if (category != null) {
                    call.respond(HttpStatusCode.OK, category)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Category not found"))
                }
            }


            put("/{categoryId}", {
                description = "Update category"
                request { body<UpdateCategoryRequest>() }
                response {
                    HttpStatusCode.OK to { description = "Category updated"; body<CategoryDto>() }
                    HttpStatusCode.NotFound to { description = "Category not found" }
                    HttpStatusCode.BadRequest to { description = "Invalid request" }
                }
            }) {
                val categoryId = call.parameters["categoryId"]?.let { UUID.fromString(it) }
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid category ID"))
                val request = call.receive<UpdateCategoryRequest>()

                val updatedCategory = categoryService.updateCategory(categoryId, request)
                if (updatedCategory != null) {
                    call.respond(HttpStatusCode.OK, updatedCategory)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Category not found"))
                }
            }

            delete("/{categoryId}", {
                description = "Delete category"
                response {
                    HttpStatusCode.OK to { description = "Category deleted" }
                    HttpStatusCode.NotFound to { description = "Category not found" }
                }
            }) {
                val categoryId = call.parameters["categoryId"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid category ID"))

                if (categoryService.deleteCategory(categoryId)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Category not found"))
                }
            }
        }
    }
}
