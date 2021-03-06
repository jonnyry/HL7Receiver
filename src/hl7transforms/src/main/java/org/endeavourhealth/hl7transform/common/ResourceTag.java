package org.endeavourhealth.hl7transform.common;

import org.hl7.fhir.instance.model.*;

public enum ResourceTag {
    PatientSubject(Patient.class, ResourceType.Patient),
    MainHospitalOrganisation(Organization.class, ResourceType.Organization),
    MainHospitalLocation(Location.class, ResourceType.Location),
    MainPrimaryCareProviderOrganisation(Organization.class, ResourceType.Organization),
    MainPrimaryCareProviderPractitioner(Practitioner.class, ResourceType.Practitioner);

    private Class<? extends Resource> resourceClass;
    private ResourceType resourceType;

    public Class<? extends Resource> getResourceClass() {
        return resourceClass;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    ResourceTag(Class<? extends Resource> resourceClass, ResourceType resourceType) {
        this.resourceClass = resourceClass;
        this.resourceType = resourceType;
    }
}