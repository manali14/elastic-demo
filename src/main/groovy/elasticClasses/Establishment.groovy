package elasticClasses

class Establishment extends BaseElastic {
    String id
    String hotelId
    String title
    String address
    String locationName
    String countryName
    String geoCodeAccuracy
    Location location

    static List<Establishment> establishments() {
        return [
                new Establishment(hotelId: "111756", title: "Sheraton Deira", locationName: "Deira", countryName: "United Arab Emirates", address: "Al Muteena Street P.O. BOX 5772 5772", geoCodeAccuracy: "8", location: new Location(lat: "25.274900", lon: "55.327500")),
                new Establishment(hotelId: "115979", title: "Labranda Los Cocoteros", locationName: "Puerto del Carmen", countryName: "Spain", address: "Av. de las Playas, 23, Puerto Del Carmen", geoCodeAccuracy: "9", location: new Location(lat: "28.920490", lon: "-13.660700")),
                new Establishment(hotelId: "115990", title: "Labranda El Dorado", locationName: "Puerto del Carmen", countryName: "Spain", address: "La Graciosa, 7, Puerto Del Carmen", geoCodeAccuracy: "9", location: new Location(lat: "28.922100", lon: "-13.668000")),
                new Establishment(hotelId: "117316", title: "Alborada Beach Club", locationName: "Costa del Silencio", countryName: "Spain", address: "Costa del Silencio Las Galletas Costa del Silencio-Arona Tenerife, Spain, 38630", geoCodeAccuracy: "8", location: new Location(lat: "28.007400", lon: "-16.652600")),
                new Establishment(hotelId: "117404", title: "Labranda Isla Bonita", locationName: "Costa Adeje", countryName: "Spain", address: "Av. Bruselas, 8, Costa Adeje", geoCodeAccuracy: "9", location: new Location(lat: "28.089380", lon: "-16.734130"))
        ]
    }

}
