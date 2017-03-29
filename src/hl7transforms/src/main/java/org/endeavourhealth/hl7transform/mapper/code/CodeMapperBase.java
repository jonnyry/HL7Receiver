package org.endeavourhealth.hl7transform.mapper.code;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.mapper.exceptions.UncheckedMapperException;

public class CodeMapperBase {

    public interface CodeSystemAccessor<T, R extends String> {
        R getCodeSystem(T enumValue);
    }

    public interface EnumFactory<T extends String, R> {
        R fromString(T value) throws Exception;
    }

    private Mapper mapper;

    public CodeMapperBase(Mapper mapper) {
        this.mapper = mapper;
    }

    protected MappedCode mapCode(CodeContext codeContext, String code) throws MapperException {
        Validate.notNull(codeContext);

        if (StringUtils.isEmpty(StringUtils.defaultString(code).trim()))
            return null;

        MappedCode mappedCode = this.mapper.mapCode(codeContext.name(), code, null, null);

        switch (mappedCode.getAction()) {
            case NOT_MAPPED_FAIL_TRANSFORMATION:
                throw new MapperException("Code '" + code + "' in context " + codeContext.name() + " received action of " + mappedCode.getAction().name());
            case NOT_MAPPED_INCLUDE_ONLY_SOURCE_TERM:
                throw new MapperException("Code '" + code + "' in context " + codeContext + " received unsupported action of " + mappedCode.getAction().name());
            case NOT_MAPPED_EXCLUDE:
                return null;
            case MAPPED_INCLUDE:
                return mappedCode;
            default:
                throw new MapperException("CodeMappingAction not recognised");
        }
    }

    protected <T extends String, R> R mapCodeToEnum(CodeContext codeContext, String code, EnumFactory<T, R> enumFromCode, CodeSystemAccessor<R, T> enumCodeSystemFromCode) throws MapperException {
        MappedCode mappedCode = mapCode(codeContext, code);

        if (mappedCode == null)
            return null;

        R enumValue;

        try {
            enumValue = enumFromCode.fromString((T)mappedCode.getCode());
        } catch (Exception e) {
            throw new MapperException(e.getMessage(), e);
        }

        String enumCodeSystem = enumCodeSystemFromCode.getCodeSystem(enumValue);

        if (!mappedCode.getSystem().equals(enumCodeSystem))
            throw new MapperException("Conversion to enum failed.  Mapped code system '" + mappedCode.getSystem() + "' does not match enum code system '" + enumCodeSystem + "'");

        return enumValue;
    }
}
