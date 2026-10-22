package mx.com.fredkorlabs.registration.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
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
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedMethod;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.LogicalType;

import java.lang.reflect.Field;
import java.util.Arrays;

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

    // Request scalars keep their contract type: no text <-> number/boolean or number -> boolean conversions.
    @Bean
    public JsonMapperBuilderCustomizer strictScalarCoercion() {
        return builder -> builder
            .withCoercionConfig(LogicalType.Boolean, config -> config
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail))
            .withCoercionConfig(LogicalType.Integer, config -> config
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
            .withCoercionConfig(LogicalType.Float, config -> config
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
            .withCoercionConfig(LogicalType.Textual, config -> config
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }

    // Generated response models mark optional (non-required) properties as @Nullable fields. Omit them
    // when they have no value; required nullable properties keep being sent as null (OpenAPI semantics).
    @Bean
    public JsonMapperBuilderCustomizer omitAbsentOptionalProperties() {
        return builder -> builder.annotationIntrospector(new GeneratedModelInclusionIntrospector());
    }

    private static final class GeneratedModelInclusionIntrospector extends JacksonAnnotationIntrospector {

        private static final String GENERATED_MODEL_PACKAGE =
            "mx.com.fredkorlabs.registration.adapter.in.web.generated.model";

        @Override
        public JsonInclude.Value findPropertyInclusion(MapperConfig<?> config, Annotated annotated) {
            JsonInclude.Value inclusion = super.findPropertyInclusion(config, annotated);
            if (annotated instanceof AnnotatedMethod getter && isOptionalGeneratedProperty(getter)) {
                return inclusion.withValueInclusion(JsonInclude.Include.NON_NULL);
            }
            return inclusion;
        }

        private static boolean isOptionalGeneratedProperty(AnnotatedMethod getter) {
            Class<?> model = getter.getDeclaringClass();
            JsonProperty property = getter.getAnnotation(JsonProperty.class);
            if (!GENERATED_MODEL_PACKAGE.equals(model.getPackageName()) || property == null) {
                return false;
            }
            try {
                Field field = model.getDeclaredField(property.value());
                return Arrays.stream(field.getAnnotations())
                    .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("Nullable"))
                    || Arrays.stream(field.getAnnotatedType().getAnnotations())
                    .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("Nullable"));
            } catch (NoSuchFieldException e) {
                return false;
            }
        }
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
