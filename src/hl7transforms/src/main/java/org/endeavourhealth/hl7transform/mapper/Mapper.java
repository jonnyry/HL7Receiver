package org.endeavourhealth.hl7transform.mapper;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public abstract class Mapper {

    public abstract Code mapCode(String mapContext, Code code) throws MapperException;
    public abstract UUID mapResourceUuid(ResourceType resourceType, String identifier) throws MapperException;
}