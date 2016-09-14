package elastic.demo

import constants.Weight
import elasticClasses.Country
import elasticClasses.Establishment
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

    def createEstablishments() {
        Establishment.establishments().each {
            String json = it.convertObjectToString()
            TransportClient client = TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
            Boolean indexExists = client.admin().indices().prepareExists("hotel").execute().actionGet().isExists()
            if (!indexExists) {
                client.admin().indices().prepareCreate("hotel").get()
                client.admin().indices().preparePutMapping("hotel").setType("externalHotel").setSource([externalHotel: [properties: [location: [type: "geo_point"]]]]).execute().actionGet()
            }
            IndexResponse response = client.prepareIndex("hotel", "externalHotel").setSource(json).get()
        }
        render([success: true] as JSON)
    }

    def searchEstablishment() {
        TransportClient client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
        List<Establishment> locationEstablishments = []
        List<Establishment> titleEstablishments = []
        List<Establishment> addressEstablishments = []

        SearchResponse locationSearch = client.prepareSearch("hotel").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.geoDistanceQuery("location").point(28.089380, -16.734130).distance(1, DistanceUnit.KILOMETERS)).setSize(3).execute().actionGet()
        locationSearch.hits.eachWithIndex { val, i ->
            println "location:${val.id}"
            Establishment establishment = new Establishment()
            val.source.id = val.id
            establishment = establishment.convertMapToObject(val.source) as Establishment
            establishment.score = Weight.locationWeights?.get(i)
            println ">>>>>>>>>>>>>${establishment.score}"
            locationEstablishments.add(establishment)
        }

        SearchResponse titleSearch = client.prepareSearch("hotel").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchQuery("title", "Labranda")).setSize(3).execute().actionGet()
        titleSearch.hits.eachWithIndex { val, i ->
            println "title:${val.id}>>>>>${val.score}"
            Establishment establishment = new Establishment()
            val.source.id = val.id
            establishment = establishment.convertMapToObject(val.source) as Establishment
            establishment.score = Weight.titleWeights?.get(i)
            titleEstablishments.add(establishment)
        }

        SearchResponse addressSearch = client.prepareSearch("hotel").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("address", "Av. Brusel, 8, Costa Adeje"))
                .must(QueryBuilders.matchQuery("countryName", "Spain"))
                .must(QueryBuilders.matchQuery("locationName", "Costa Adeje")))
                .setSize(3).execute().actionGet()
        addressSearch.hits.eachWithIndex { val, i ->
            println "address:${val.id}?????????????????${val.score}"
            Establishment establishment = new Establishment()
            val.source.id = val.id
            establishment = establishment.convertMapToObject(val.source) as Establishment
            establishment.score = Weight.addressWeights?.get(i)
            addressEstablishments.add(establishment)
        }

        List<Establishment> commonList = locationEstablishments.findAll {
            it.id in titleEstablishments*.id
        }.findAll {
            it.id in addressEstablishments*.id
        }
        commonList.each { establishment ->
            establishment.finalScore = locationEstablishments.find {
                it.id == establishment.id
            }?.score + titleEstablishments.find { it.id == establishment.id }?.score + addressEstablishments.find {
                it.id == establishment.id
            }?.score
            establishment.percentMatch = (establishment.finalScore * 100) / (Weight.locationWeights?.first() + Weight.addressWeights?.first() + Weight.titleWeights?.first())
            println ">>>>>>>>>>>>>>>>>>>>>>>>>>${establishment.percentMatch}"
        }

        render([success: true] as JSON)
    }

    def searchData() {
        TransportClient client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
        SearchResponse searchResponse = client.prepareSearch("country")
                .setTypes("externalCountry").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.fuzzyQuery("name", "Sheraton")).setSize(3).execute().actionGet()
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
        Country country = new Country().convertMapToObject(searchResponse1.hits?.first()?.source) as Country
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>${country.properties}"
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>${country.location.properties}"
        render([success: true] as JSON)
    }
}
