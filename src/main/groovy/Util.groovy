import com.fasterxml.jackson.databind.ObjectMapper

class Util {
    static Object convertMapToObject(Map map, Class aClass) {
        ObjectMapper objectMapper = new ObjectMapper()
        objectMapper.convertValue(map, aClass)
    }
}
