package mx.com.fredkorlabs.registration.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfiguration {

    // ADR-0009 / ADR-0004: contract enums are JSON strings. Jackson converts scalars such as
    // 1.0 into "1.0" for enums built by a String @JsonCreator, so non-string tokens are rejected here.
    @Bean
    public JacksonModule stringOnlyEnumModule() {
        return new SimpleModule("string-only-enums").setDeserializerModifier(new ValueDeserializerModifier() {
            @Override
            public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type,
                                                               BeanDescription.Supplier beanDescRef,
                                                               ValueDeserializer<?> deserializer) {
                return new StringOnlyEnumDeserializer(deserializer);
            }
        });
    }

    private static final class StringOnlyEnumDeserializer extends DelegatingDeserializer {

        StringOnlyEnumDeserializer(ValueDeserializer<?> delegate) {
            super(delegate);
        }

        @Override
        protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee) {
            return new StringOnlyEnumDeserializer(newDelegatee);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            if (!p.hasToken(JsonToken.VALUE_STRING)) {
                return ctxt.handleUnexpectedToken(handledType(), p);
            }
            return super.deserialize(p, ctxt);
        }
    }
}
