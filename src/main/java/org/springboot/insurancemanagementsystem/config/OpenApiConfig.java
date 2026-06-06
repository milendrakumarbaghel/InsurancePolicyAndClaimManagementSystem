package org.springboot.insurancemanagementsystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Insurance Policy and Claim Management System",
                description = """
                        A REST API backend system for managing insurance products, policy plans, customers,
                        policies, premium payments, and insurance claims.
                        
                        **User Roles:**
                        - **Admin** — Manages products, plans, users, and makes final claim decisions.
                        - **Agent** — Reviews claims, recommends decisions, and issues policies.
                        - **Customer** — Purchases policies, records payments, and raises claims.
                        
                        **Authentication:**
                        All protected APIs require a JWT Bearer token.
                        Obtain the token by calling the `/api/auth/login` endpoint.
                        Pass the token in the Authorization header as: `Bearer <token>`
                        """,
                version = "v1.0.0",
                contact = @Contact(
                        name = "Insurance Management System Support",
                        email = "support@insurancesystem.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                )
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = """
                JWT Bearer Token Authentication.
                Login via POST /api/auth/login to receive your token.
                Enter the token below in the format: Bearer <your_token>
                """,
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .tags(List.of(
                        new Tag()
                                .name("Authentication")
                                .description("Public APIs for customer registration and user login. " +
                                        "No token required for these endpoints."),

                        new Tag()
                                .name("User Management")
                                .description("Admin-only APIs to view, create, activate, and deactivate user accounts. " +
                                        "Requires ADMIN role."),

                        new Tag()
                                .name("Customer Management")
                                .description("APIs for customer profile creation, update, and retrieval. " +
                                        "Customers can manage only their own profiles. " +
                                        "Admin and Agent can view customer details."),

                        new Tag()
                                .name("Insurance Products")
                                .description("APIs for managing insurance products such as Health, Motor, Life, and Travel. " +
                                        "Create, update, and deactivate operations require ADMIN role. " +
                                        "Viewing active products is available to all authenticated users."),

                        new Tag()
                                .name("Policy Plans")
                                .description("APIs for managing policy plans under insurance products. " +
                                        "Create, update, and deactivate operations require ADMIN role. " +
                                        "Viewing active plans is available to all authenticated users."),

                        new Tag()
                                .name("Policies")
                                .description("APIs for policy purchase, issuance, viewing, and cancellation. " +
                                        "Customers can purchase and view their own policies. " +
                                        "Agents and Admins can issue and cancel policies."),

                        new Tag()
                                .name("Premium Payments")
                                .description("APIs for recording and viewing simulated premium payments. " +
                                        "Customers can record and view payments for their own policies. " +
                                        "Agents and Admins can view all payment records."),

                        new Tag()
                                .name("Claims")
                                .description("APIs for claim submission, review, recommendation, and final decision. " +
                                        "Customers raise claims. Agents review and recommend. Admins approve or reject finally."),

                        new Tag()
                                .name("Claim Status History")
                                .description("APIs for viewing claim status change history. " +
                                        "Admin and Agent can view all histories. " +
                                        "Customers can view history for their own claims only.")
                ));
    }
}
