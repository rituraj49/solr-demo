package cl.travel.airports.service;

import cl.travel.airports.Airport;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AirportSolrService {
    @Autowired
    private SolrClient solrClient;

    public void upload(List<Airport> airportsList) throws SolrServerException, IOException {
        int batchSize = 1000;
        int total = airportsList.size();

        for(int i = 0; i <= total; i += batchSize) {
            int end = Math.min(i + batchSize, total);

            List<Airport> airportsToAdd = airportsList.subList(i, end);
            if(airportsToAdd.isEmpty()) {
                continue;
            }
            List<SolrInputDocument> docs = new ArrayList<>();
            for(Airport a: airportsToAdd) {
                SolrInputDocument doc = getSolrInputFields(a);
                docs.add(doc);
            }
            solrClient.add(docs);
            System.out.println("indexed records " + i + " to " + end);
        }

        solrClient.commit();
    }

    private static SolrInputDocument getSolrInputFields(Airport a) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("iata", a.getIata());
        doc.setField("icao", a.getIcao());
        doc.setField("name", a.getName());
        doc.setField("latitude", a.getLatitude());
        doc.setField("longitude", a.getLongitude());
        doc.setField("elevation", a.getElevation());
        doc.setField("url", a.getUrl());
        doc.setField("time_zone", a.getTime_zone());
        doc.setField("city_code", a.getCity_code());
        doc.setField("country_code", a.getCountry_code());
        doc.setField("city", a.getCity());
        doc.setField("state", a.getState());
        doc.setField("county", a.getCounty());
        doc.setField("type", a.getType());
        return doc;
    }

    public List<Airport> searchAirports(String query) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("defType", "edismax");
        solrQuery.setQuery(query);
//        solrQuery.set("qf", "iata name city_code city");
        solrQuery.set("qf", "search_autocomplete");
        solrQuery.setRows(10);
        QueryResponse response = solrClient.query(solrQuery);
//        SolrDocumentList docs = response.getResults();
//        for(SolrDocument s: docs) {
//            System.out.println("docc: " + s);
//        }
//        return response.getBeans(Airport.class);
        SolrDocumentList results = response.getResults();

        List<Airport> airports = new ArrayList<>();
        for (SolrDocument doc : results) {
            Airport airport = new Airport();
            airport.setIata(getSingle(doc, "iata"));
            airport.setIcao(getSingle(doc, "icao"));
            airport.setName(getSingle(doc, "name"));
            airport.setLatitude(getSingle(doc, "latitude"));
            airport.setLongitude(getSingle(doc, "longitude"));
            airport.setElevation(Integer.parseInt(getSingle(doc, "elevation", "0")));
            airport.setTime_zone(getSingle(doc, "time_zone"));
            airport.setCity_code(getSingle(doc, "city_code"));
            airport.setCountry_code(getSingle(doc, "country_code"));
            airport.setCity(getSingle(doc, "city"));
            airport.setState(getSingle(doc, "state"));
            airport.setCounty(getSingle(doc, "county"));
            airport.setType(getSingle(doc, "type"));
            airports.add(airport);
        }
        return airports;
    }

    private String getSingle(SolrDocument doc, String fieldName) {
        Object val = doc.getFieldValue(fieldName);
        if (val instanceof List && !((List<?>) val).isEmpty()) {
            return ((List<?>) val).get(0).toString();
        }
        return val != null ? val.toString() : null;
    }

    private String getSingle(SolrDocument doc, String fieldName, String defaultVal) {
        String value = getSingle(doc, fieldName);
        return value != null ? value : defaultVal;
    }
}
