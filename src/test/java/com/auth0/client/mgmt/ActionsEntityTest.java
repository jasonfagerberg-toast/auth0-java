package com.auth0.client.mgmt;

import com.auth0.client.MockServer;
import com.auth0.json.mgmt.actions.Action;
import com.auth0.json.mgmt.actions.Dependency;
import com.auth0.json.mgmt.actions.Secret;
import com.auth0.json.mgmt.actions.Trigger;
import com.auth0.net.Request;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.auth0.client.MockServer.bodyFromRequest;
import static com.auth0.client.RecordedRequestMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("unchecked")
public class ActionsEntityTest extends BaseMgmtEntityTest {

    @Test
    public void getActionShouldThrowOnNullActionId() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("action ID");
        api.actions().get(null);
    }

    @Test
    public void shouldGetAction() throws Exception {
        Request<Action> request = api.actions().get("action-id");
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MockServer.ACTION, 200);
        Action response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("GET", "/api/v2/actions/actions/action-id"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void createActionShouldThrowOnNullAction() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("action");
        api.actions().create(null);
    }

    @Test
    public void shouldCreateAction() throws Exception {
        Trigger trigger = new Trigger();
        trigger.setId("post-login");
        trigger.setVersion("v2");

        Dependency dependency = new Dependency();
        dependency.setVersion("v2");
        dependency.setName("some-dep");
        dependency.setRegistryUrl("some-registry-url");

        Secret secret = new Secret("secret-name", "secret-value");
        Action action = new Action("my action", Collections.singletonList(trigger));
        action.setCode("some code");
        action.setRuntime("node16");
        action.setDependencies(Collections.singletonList(dependency));
        action.setSecrets(Collections.singletonList(secret));

        Request<Action> request = api.actions().create(action);
        assertThat(request, is(notNullValue()));

        server.jsonResponse(MockServer.ACTION, 200);
        Action response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("POST", "/api/v2/actions/actions"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));

        Map<String, Object> body = bodyFromRequest(recordedRequest);
        assertThat(body, aMapWithSize(6));
        assertThat(body, hasEntry("name", "my action"));
        assertThat(body, hasEntry("code", "some code"));
        assertThat(body, hasEntry("runtime", "node16"));

        assertThat(body, hasEntry(is("supported_triggers"), is(notNullValue())));
        List<Map<String, Object>> triggersOnRequest = (ArrayList<Map<String, Object>>) body.get("supported_triggers");
        assertThat(triggersOnRequest, hasSize(1));
        assertThat(triggersOnRequest.get(0), is(aMapWithSize(2)));
        assertThat(triggersOnRequest.get(0), hasEntry("version", trigger.getVersion()));
        assertThat(triggersOnRequest.get(0), hasEntry("id", trigger.getId()));

        assertThat(body, hasEntry(is("dependencies"), is(notNullValue())));
        List<Map<String, Object>> dependenciesOnRequest = (ArrayList<Map<String, Object>>) body.get("dependencies");
        assertThat(dependenciesOnRequest, hasSize(1));
        assertThat(dependenciesOnRequest.get(0), is(aMapWithSize(3)));
        assertThat(dependenciesOnRequest.get(0), hasEntry("version", dependency.getVersion()));
        assertThat(dependenciesOnRequest.get(0), hasEntry("name", dependency.getName()));
        assertThat(dependenciesOnRequest.get(0), hasEntry("registry_url", dependency.getRegistryUrl()));

        assertThat(body, hasEntry(is("secrets"), is(notNullValue())));
        List<Map<String, Object>> secretsOnRequest = (ArrayList<Map<String, Object>>) body.get("secrets");
        assertThat(secretsOnRequest, hasSize(1));
        assertThat(secretsOnRequest.get(0), is(aMapWithSize(2)));
        assertThat(secretsOnRequest.get(0), hasEntry("name", secret.getName()));
        assertThat(secretsOnRequest.get(0), hasEntry("value", secret.getValue()));

        assertThat(response, is(notNullValue()));
    }

    @Test
    public void deleteActionShouldThrowWhenActionIdIsNull() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("action ID");
        api.actions().delete(null);
    }

    @Test
    public void shouldDeleteAction() throws Exception {
        Request request = api.actions().delete("action-id");
        assertThat(request, is(notNullValue()));

        server.emptyResponse(204);
        request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("DELETE", "/api/v2/actions/actions/action-id"));
        assertThat(recordedRequest, hasQueryParameter("force", "false"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));
    }

    @Test
    public void shouldForceDeleteAction() throws Exception {
        Request request = api.actions().delete("action-id", true);
        assertThat(request, is(notNullValue()));

        server.emptyResponse(204);
        request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest, hasMethodAndPath("DELETE", "/api/v2/actions/actions/action-id"));
        assertThat(recordedRequest, hasQueryParameter("force", "true"));
        assertThat(recordedRequest, hasHeader("Content-Type", "application/json"));
        assertThat(recordedRequest, hasHeader("Authorization", "Bearer apiToken"));
    }
}
