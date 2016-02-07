package org.dynamic.rest;

import org.dynamic.DummyInputDataSets;
import org.dynamic.cache.CacheException;
import org.dynamic.core.RevenueManager;
import org.dynamic.core.ValidationException;
import org.dynamic.data.Input;
import org.dynamic.data.Output;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.mockito.Mockito.*;

/**
 */
public class MaxRevenueResourceTest {

    private static final RevenueManager mock = mock(RevenueManager.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new MaxRevenueResource(mock))
        .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
        .build();

    private final Input set1 = DummyInputDataSets.dataSet1();

    @Before
    public void setup() throws CacheException, ValidationException {
        Output out1 = DummyInputDataSets.dummyOutputFromInput(set1);
        when(mock.compute(set1)).thenReturn(out1);
    }

    @After
    public void tearDown() {
        reset(mock);
    }

    @Test
    public void testGetPerson() {
        Output out1 = DummyInputDataSets.dummyOutputFromInput(set1);
        Entity<Input> inEntry = Entity.entity(set1, MediaType.APPLICATION_JSON);
        Output post = resources.getJerseyTest().target("/compute").request(MediaType.APPLICATION_JSON).post(inEntry, Output.class);
        Assert.assertEquals(post, out1);
    }
}
