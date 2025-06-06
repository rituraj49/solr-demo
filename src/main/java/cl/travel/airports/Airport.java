package cl.travel.airports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Airport {
    @Field private String iata;
    @Field private String icao;
    @Field private String name;
    @Field private String latitude;
    @Field private String longitude;
    @Field private int elevation;
    @Field private String url;
    @Field private String time_zone;
    @Field private String city_code;
    @Field private String country_code;
    @Field private String city;
    @Field private String state;
    @Field private String county;
    @Field private String type;
}
