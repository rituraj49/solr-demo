package cl.travel.airports.controller;

import cl.travel.airports.Airport;
import cl.travel.airports.service.AirportSolrService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@RestController
public class AirportController {
    private AirportSolrService airportSolrService;

    @Autowired
    public AirportController(AirportSolrService airportSolrService) {
        this.airportSolrService = airportSolrService;
    }

    @PostMapping("bulk-upload")
    @Operation(
            summary = "Bulk-upload airports CSV",
            description = """
            Accepts a CSV file, parses it into `Airport` beans and
            performs a bulk upload into the `airports` index.
            The CSV columns must map to the fields of the `Airport` class.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload succeeded",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "server error",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> bulkUploadAirports(
            @RequestParam("file") MultipartFile file) throws IOException {
        try(Reader reader = new InputStreamReader(file.getInputStream())) {
            CsvToBean<Airport> csvToBean = new CsvToBeanBuilder<Airport>(reader)
                    .withType(Airport.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<Airport> airportsList = csvToBean.parse();
            airportSolrService.upload(airportsList);
        } catch (Exception e) {
            System.out.println("caught exception");
            throw new RuntimeException(e);
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Something went wrong...");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Data uploaded successfully");
    }

    @GetMapping("search")
    @Operation(
            summary = "Search airports",
            description = """
            Performs a text search on the `airports` index.
            """
    )
    @Parameters({
            @Parameter(name = "q",
                    description = "Search term",
                    example = "delhi",
                    required = true)
    })
    @ApiResponse(responseCode = "200", description = "Upload succeeded")
    public ResponseEntity<List<Airport>> searchAirports(@RequestParam String q) throws SolrServerException, IOException {
            List<Airport> airports = airportSolrService.searchAirports(q);
            return ResponseEntity.status(HttpStatus.OK).body(airports);
    }

}
