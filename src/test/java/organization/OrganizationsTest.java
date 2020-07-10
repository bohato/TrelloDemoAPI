package organization;

import base.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import trello.Organization;

import static common.SharedMethods.deleteResource;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationsTest extends BaseTest {

    private static String orgId;

    @Test
    public void createNewOrganizationWithAllData() {
        Organization org = new Organization();
        org.setDisplayName("Test");
        org.setDesc("Long description of my organization");
        org.setName("unique_organization");
        org.setWebsite("https://developer.atlassian.com/");

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .queryParam("desc", org.getDesc())
                .queryParam("name", org.getName())
                .queryParam("website", org.getWebsite())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("displayName")).isEqualTo(org.getDisplayName());
        assertThat(json.getString("name")).isEqualTo(org.getName());
        assertThat(json.getString("desc")).isEqualTo(org.getDesc());
        assertThat(json.getString("website")).isEqualTo(org.getWebsite());

        orgId = json.get("id");

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void getExistingOrgById() {
        Organization org = new Organization();
        org.setDisplayName("TestOrg");

        Response createResponse = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonCreate = createResponse.jsonPath();
        orgId = jsonCreate.get("id");

        Response getResponse = given()
                .spec(reqSpec)
                .when()
                .get(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = getResponse.jsonPath();

        assertThat(jsonGet.getString("id")).isEqualTo(orgId);
        assertThat(jsonGet.getString("displayName")).isEqualTo(org.getDisplayName());

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void getProperErrorMessageWhenOrgDoesNotExist() {
        orgId = "99";

        Response response = given()
                .spec(reqSpec)
                .when()
                .get(ORGANIZATIONS + orgId)
                .then()
                .statusCode(404)
                .extract()
                .response();

        assertThat(response.asString()).isEqualTo("model not found");
    }

    @Test
    public void updateOrganization() {
        Organization org = new Organization();
        org.setDisplayName("TestOrg");

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        orgId = json.get("id");

        org.setDisplayName("NewTestOrg");

        Response responsePUT = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .when()
                .put(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonPUT = responsePUT.jsonPath();

        assertThat(jsonPUT.getString("id")).isEqualTo(orgId);
        assertThat(jsonPUT.getString("displayName")).isEqualTo(org.getDisplayName());

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void cannotAddOrgWithoutName() {
        given()
                .spec(reqSpec)
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);
    }
}