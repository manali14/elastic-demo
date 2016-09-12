package elastic.demo

import grails.converters.JSON
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.index.query.QueryBuilders

class UtilController {

    def insertData() {
        TransportClient client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
        println "******************************************"
        client.admin().indices().prepareCreate("country").get()
        println "******************************************"
        PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping("country").setType("externalCountry").setSource([externalCountry: [properties: [location: [type: "geo_point"]]]]).execute().actionGet();
        println "******************************************"
        render([success: true] as JSON)
    }

    def createData() {
        TransportClient client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
//        String json = [name: "Sheraton Deira", "locationId": "253015", location: ["lat": "55.327500", "lon": "25.274900"], "geoCodeAccuracy": "55.327500"] as JSON
//        String json1 = [name: "Sheraton Jumeirah Beach Resort and Towers", "locationId": "320783", location: ["lat": "55.128900", "lon": "25.073200"], "geoCodeAccuracy": "55.128900"] as JSON
        String json2 = [name: "Labranda Los Cocoteros", "locationId": "218928", location: ["lat": "-13.660700", "lon": "28.920490"], "geoCodeAccuracy": "-13.660700"] as JSON
        String json3 = [name: "Labranda El Dorado", "locationId": "218928", location: ["lat": "-13.668000", "lon": "28.922100"], "geoCodeAccuracy": "-13.668000"] as JSON
        String json4 = [name: "Labranda Isla Bonita", "locationId": "219217", location: ["lat": "-16.734130", "lon": "28.089380"], "geoCodeAccuracy": "-16.734130"] as JSON
//        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>${json}"
//        IndexResponse response = client.prepareIndex("country", "externalCountry").setSource(json2).get()
//        IndexResponse response1 = client.prepareIndex("country", "externalCountry").setSource(json1).get()
//        IndexResponse response2 = client.prepareIndex("country", "externalCountry").setSource(json2).get()
//        IndexResponse response3 = client.prepareIndex("country", "externalCountry").setSource(json3).get()
        IndexResponse response4 = client.prepareIndex("country", "externalCountry").setSource(json4).get()
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>${response.properties}"
        render([success: true] as JSON)
    }

    def searchData() {
        TransportClient client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
        SearchResponse searchResponse = client.prepareSearch("country")
                .setTypes("externalCountry").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.fuzzyQuery("name", "Sheraton")).addIndexBoost("0", 20).addIndexBoost("1", 10).setSize(3).execute().actionGet()
        searchResponse.hits.each {
            println("*******${it.properties}*******")
        }
        SearchResponse searchResponse1 = client.prepareSearch("country")
                .setTypes("externalCountry").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.geoDistanceQuery("location").point(-13.660700, 28.920490).distance(1, DistanceUnit.KILOMETERS)).setSize(3).execute().actionGet()
        println "searchResponse:${searchResponse1.properties}"
        searchResponse1.hits.each {
            println "++++++++++++++++++++++++++++${it.properties}"
        }
        render([success: true] as JSON)
    }
}
