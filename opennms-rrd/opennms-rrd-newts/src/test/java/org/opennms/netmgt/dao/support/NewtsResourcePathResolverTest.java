package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.cassandra.search.CassandraSearcher;

public class NewtsResourcePathResolverTest {

    @Test
    public void canFindNodeSourceDirectories() {
        SearchResults searchResults = new SearchResults();
        searchResults.addResult(new Resource("snmp:fs:a:1:group"), Collections.emptyList());

        CassandraSearcher searcher = EasyMock.createNiceMock(CassandraSearcher.class);
        EasyMock.expect(searcher.search(EasyMock.anyObject())).andReturn(searchResults);
        EasyMock.replay(searcher);

        NewtsResourcePathResolver npr = new NewtsResourcePathResolver();
        npr.setSearcher(searcher);
        
        Set<String> nodeSourceDirectories = npr.findNodeSourceDirectories();
        
        EasyMock.verify(searcher);
        assertTrue(nodeSourceDirectories.contains("a:1"));
    }
}
