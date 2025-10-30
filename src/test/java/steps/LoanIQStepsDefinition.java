package steps;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class LoanIQStepsDefinition {

    private static int customerId;
    private static int postId;
    private static Response response;
    private static String token;
    private static String baseUrl;

    @Given("the base API URL is {string}")
    public void setBaseApiUrl(String url) {
        baseUrl = url;
        RestAssured.baseURI = baseUrl;
        System.out.println("Base API URL set to: " + baseUrl);
    }

    @And("the request header includes authorization token {string}")
    public void setAuthorizationHeader(String authToken) {
        token = authToken;
        RestAssured.requestSpecification = RestAssured.given()
                .header("Authorization", token)
                .header("Content-Type", "application/json");
        System.out.println("Authorization token added: " + token);
    }

    @When("Fetch a random customer from {string}")
    public void fetchRandomCustomer(String endpoint) {
        response = RestAssured.given().get(endpoint);
        List<Map<String, Object>> allCustomers = response.jsonPath().getList("$");
        
        Random random = new Random();
        Map<String, Object> randomCustomer = allCustomers.get(random.nextInt(allCustomers.size()));
        
        customerId = (int) randomCustomer.get("id");
        String email = (String) randomCustomer.get("email");
        
        System.out.println("Random Customer id: " + customerId + " | Email id: " + email);
    }

    @Then("Print the customer's id and email address")
    public void printCustomerDetails() {
        Assert.assertTrue("Customer ID should be greater than 0", customerId > 0);
        System.out.println("Customer Id: " + customerId);
    }

    @When("Fetch the posts for the customer from {string}")
    public void fetchPostsForCustomer(String endpoint) {
        response = RestAssured.given().queryParam("userId", customerId).get(endpoint);
        System.out.println("Posts fetched from customerId: " + customerId);
    }

    @Then("Validate post ids should be between {int} and {int}")
    public void validatePostIds(Integer min, Integer max) {
        List<Integer> postIds = response.jsonPath().getList("id");
        for (int id : postIds) {
            Assert.assertTrue("Invalid Post id: " + id, id >= min && id <= max);
        }
        System.out.println("All Post ids validated successfully!");
    }

    @And("Print each post's title and id")
    public void printPostData() {
        List<Map<String, Object>> posts = response.jsonPath().getList("$");
        for (Map<String, Object> post : posts) {
            System.out.println("Post id: " + post.get("id") + " | Title: " + post.get("title"));
        }
    }

    @Given("Random post from the customer's portfolio")
    public void pickRandomPost() {
        List<Map<String, Object>> posts = response.jsonPath().getList("$");
        Assert.assertTrue("Posts are non-empty", posts.size() > 0);
        
        Map<String, Object> randomPost = posts.get(new Random().nextInt(posts.size()));
        postId = (int) randomPost.get("id");
        
        System.out.println("Random Post selected: " + postId);
    }

    @When("Update the post details using {string}")
    public void updatePost(String endpoint) {
        String newTitle = "Updated Post Title " + new Random().nextInt(1000);
        response = RestAssured.given()
                .body("{\"title\":\"" + newTitle + "\"}")
                .put(endpoint.replace("{id}", String.valueOf(postId)));
        
        System.out.println("Post details updated with: " + newTitle);
    }

    @Then("Print the updated post id and title from the response")
    public void printUpdatedPostDetails() {
        int id = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");
        
        System.out.println("Updated Post id: " + id + " | Title: " + title);
    }


    @When("Create a new post for that customer using {string}")
    public void createNewPost(String endpoint) {
        String body = String.format("{\"userId\": %d, \"title\": \"New post\", \"body\": \"Post details...\"}", customerId);
        response = RestAssured.given().body(body).post(endpoint);
        
        System.out.println("New post created for the customer: " + customerId);
    }

    @Then("Verify response \\(status code {int}\\) is returned")
    public void verifyResponseCode(Integer expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        Assert.assertEquals("Unexpected response code!", expectedStatusCode.intValue(), actualStatusCode);
        System.out.println("Verified status code: " + actualStatusCode);
    }
}