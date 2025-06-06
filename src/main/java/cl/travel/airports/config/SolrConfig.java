package cl.travel.airports.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

    @Value("${solr.host}")
    private String solrHost;

    @Value("${solr.core}")
    private String solrCore;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(solrHost + "/" + solrCore).build();
    }
}
