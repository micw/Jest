package io.searchbox.core;

import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.common.AbstractIntegrationTest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes = 1)
public class GetIntegrationTest extends AbstractIntegrationTest {

    @Before
    public void setup() throws Exception {
        IndexResponse indexResponse = client().index(new IndexRequest(
                "twitter",
                "tweet",
                "1")
                .source("{\"user\":\"tweety\"}"))
                .actionGet();
        assertTrue(indexResponse.isCreated());
    }

    @Test
    public void getWithSpecialCharacterInDocId() throws IOException {
        IndexResponse indexResponse = client().index(new IndexRequest(
                "twitter",
                "tweet",
                "asd/qwe")
                .source("{\"user\":\"tweety\"}"))
                .actionGet();
        assertNotNull(indexResponse);

        JestResult result = client.execute(new Get.Builder("twitter", "asd/qwe")
                        .type("tweet")
                        .build()
        );
        assertTrue(result.getErrorMessage(), result.isSucceeded());
    }

    @Test
    public void get() throws IOException {
        Get get = new Get.Builder("twitter", "1").type("tweet").build();
        JestResult result = client.execute(get);
        assertTrue(result.getErrorMessage(), result.isSucceeded());
    }

    @Test
    public void getAsynchronously() throws InterruptedException, ExecutionException, IOException {
        client.executeAsync(new Get.Builder("twitter", "1").type("tweet").build(), new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                assertTrue(result.getErrorMessage(), result.isSucceeded());
            }

            @Override
            public void failed(Exception ex) {
                fail("failed execution of asynchronous get call");
            }
        });

        //wait for asynchronous call
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getWithType() throws Exception {
        TestArticleModel article = new TestArticleModel();
        article.setId("testid1");
        article.setName("Jest");
        Index index = new Index.Builder(article).index("articles").type("article").refresh(true).build();
        JestResult indexResult = client.execute(index);
        assertTrue(indexResult.getErrorMessage(), indexResult.isSucceeded());

        JestResult result = client.execute(new Get.Builder("articles", "testid1").type("article").build());
        TestArticleModel articleResult = result.getSourceAsObject(TestArticleModel.class);

        assertEquals(result.getJsonMap().get("_id"), articleResult.getId());
    }

    @Test
    public void getWithoutType() throws Exception {
        TestArticleModel article = new TestArticleModel();
        article.setId("testid1");
        article.setName("Jest");
        Index index = new Index.Builder(article).index("articles").type("article").refresh(true).build();
        JestResult indexResult = client.execute(index);
        assertTrue(indexResult.getErrorMessage(), indexResult.isSucceeded());

        JestResult result = client.execute(new Get.Builder("articles", "testid1").build());
        TestArticleModel articleResult = result.getSourceAsObject(TestArticleModel.class);

        assertEquals(result.getJsonMap().get("_id"), articleResult.getId());
    }
}
