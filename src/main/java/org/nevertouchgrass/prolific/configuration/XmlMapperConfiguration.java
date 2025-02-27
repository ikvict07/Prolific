package org.nevertouchgrass.prolific.configuration;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XmlMapperConfiguration {

    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper mapper =  new XmlMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
