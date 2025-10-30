Feature: Validate API functionality

  Background:
    Given the base API URL is "https://jsonplaceholder.typicode.com"
    And the request header includes authorization token "THIS-IS-A-FAKE-TOKEN"

  @SmokeTest
  Scenario: End-to-end validation of Customer, Post, and Facility workflow
    When Fetch a random customer from "/users"
    Then Print the customer's id and email address

    When Fetch the posts for the customer from "/posts"
    Then Validate post ids should be between 1 and 100
    And Print each post's title and id

    Given Random post from the customer's portfolio
    When Update the post details using "/posts/{id}"
    Then Print the updated post id and title from the response

    When Create a new post for that customer using "/posts"
    Then Verify response (status code 201) is returned
