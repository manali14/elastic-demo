enum Weight {
    TITLE(50, 40, 30),
    LOCATION(80, 70, 60),
    ADDRESS(30, 20, 10),
    GEOCODE_ACCURACY(80, 70, 60)

    Integer firstRankScore
    Integer secondRankScore
    Integer thirdRankScore

    Weight(Integer first, Integer second, Integer third) {
        this.firstRankScore = first
        this.secondRankScore = second
        this.thirdRankScore = third
    }
}