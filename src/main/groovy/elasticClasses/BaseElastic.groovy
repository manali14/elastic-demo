package elasticClasses

import com.fasterxml.jackson.databind.ObjectMapper

class BaseElastic {

    Object convertMapToObject(Map elasticObject) {
        ObjectMapper objectMapper = new ObjectMapper()
        return objectMapper.convertValue(elasticObject, this.getClass())
    }

    String convertObjectToString() {
        ObjectMapper objectMapper = new ObjectMapper()
        objectMapper.writeValueAsString(this)
    }
}
