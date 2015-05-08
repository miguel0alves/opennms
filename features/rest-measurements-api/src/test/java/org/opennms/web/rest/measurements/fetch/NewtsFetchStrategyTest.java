package org.opennms.web.rest.measurements.fetch;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.newts.NewtsUtils;
import org.opennms.web.rest.measurements.model.Source;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NewtsFetchStrategyTest {

    @Test
    public void fetchIt() throws Exception {
        System.setProperty(NewtsUtils.PORT_PROPERTY, "19042");
        
        RrdGraphAttribute attribute = new RrdGraphAttribute("icmp", "", "response:127.0.0.1:icmp");
        
        Set<OnmsAttribute> attributes = Sets.newHashSet(attribute);
        OnmsResourceType type = EasyMock.createNiceMock(OnmsResourceType.class);
        OnmsResource resource = new OnmsResource("", "", type, attributes);
        
        ResourceDao resourceDao = EasyMock.createNiceMock(ResourceDao.class);
        EasyMock.expect(resourceDao.getResourceById("nodeSource[NODES:1430502148137].responseTime[127.0.0.1]")).andReturn(resource);
        EasyMock.replay(resourceDao);

        NewtsFetchStrategy nfs = new NewtsFetchStrategy(resourceDao);

        Source source = new Source();
        source.setAggregation("AVERAGE");
        source.setAttribute("icmp");
        source.setLabel("icmp");
        source.setResourceId("nodeSource[NODES:1430502148137].responseTime[127.0.0.1]");
        source.setTransient(false);
        
        List<Source> sources = Lists.newArrayList(source);

        FetchResults results = nfs.fetch(1431047069000L - (60 * 60 * 1000), 1431047069000L, 300 * 1000, 0, sources);
        assertEquals(1, results.getColumns().keySet().size());

        EasyMock.verify(resourceDao);
    }
}
