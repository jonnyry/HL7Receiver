package org.endeavourhealth.hl7transform.transforms.homerton.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7parser.ParseException;
import org.endeavourhealth.hl7parser.messages.AdtMessage;
import org.endeavourhealth.hl7parser.segments.Pv1Segment;
import org.endeavourhealth.hl7transform.common.ResourceContainer;
import org.endeavourhealth.hl7transform.common.ResourceTag;
import org.endeavourhealth.hl7transform.common.ResourceTransformBase;
import org.endeavourhealth.hl7transform.common.transform.EpisodeOfCareCommon;
import org.endeavourhealth.hl7transform.transforms.homerton.transforms.constants.HomertonConstants;
import org.endeavourhealth.hl7transform.common.converters.DateTimeHelper;
import org.endeavourhealth.hl7transform.common.converters.IdentifierConverter;
import org.endeavourhealth.hl7transform.mapper.Mapper;
import org.endeavourhealth.hl7transform.mapper.exceptions.MapperException;
import org.endeavourhealth.hl7transform.common.TransformException;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HomertonEpisodeOfCareTransform extends ResourceTransformBase {

    private static final Logger LOG = LoggerFactory.getLogger(HomertonEpisodeOfCareTransform.class);

    public HomertonEpisodeOfCareTransform(Mapper mapper, ResourceContainer targetResources) {
        super(mapper, targetResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.EpisodeOfCare;
    }

    public EpisodeOfCare transform(AdtMessage source) throws TransformException, MapperException, ParseException {

        if (!source.hasPv1Segment())
            return null;

        EpisodeOfCare target = new EpisodeOfCare();

        setId(source, target);

        setIdentifiers(source, target);

        // set status

        setPatient(target);

        setManagingOrganisation(source, target);

        setPeriod(source, target);

        return target;
    }

    protected void setId(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {

        String patientIdentifierValue = HomertonPatientTransform.getHomertonPrimaryPatientIdentifierValue(source);
        String episodeIdentifierValue = EpisodeOfCareCommon.getEpisodeIdentifierValueByAssigningAuthority(source, HomertonConstants.primaryEpisodeIdentifierAssigningAuthority);

        UUID episodeUuid = mapper.getResourceMapper().mapEpisodeUuid(
                HomertonConstants.primaryPatientIdentifierTypeCode,
                null,
                patientIdentifierValue,
                null,
                HomertonConstants.primaryEpisodeIdentifierAssigningAuthority,
                episodeIdentifierValue);

        target.setId(episodeUuid.toString());
    }

    private void setIdentifiers(AdtMessage source, EpisodeOfCare target) throws TransformException, MapperException {

        Identifier visitNumber = IdentifierConverter.createIdentifier(source.getPv1Segment().getVisitNumber(), getResourceType(), mapper);

        if (visitNumber != null)
            target.addIdentifier(visitNumber);

        Identifier alternateVisitId = IdentifierConverter.createIdentifier(source.getPv1Segment().getAlternateVisitID(), getResourceType(), mapper);

        if (alternateVisitId != null)
            target.addIdentifier(alternateVisitId);
    }

    private void setPatient(EpisodeOfCare target) throws TransformException {
        target.setPatient(targetResources.getResourceReference(ResourceTag.PatientSubject, Patient.class));
    }

    private void setManagingOrganisation(AdtMessage source, EpisodeOfCare target) throws TransformException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        String servicingFacilityName = StringUtils.trim(pv1Segment.getServicingFacility()).toUpperCase();

        if (StringUtils.isNotBlank(servicingFacilityName))
            if (!servicingFacilityName.equals(HomertonConstants.servicingFacility))
                throw new TransformException("Hospital servicing facility of " + servicingFacilityName + " not recognised");

        target.setManagingOrganization(targetResources.getResourceReference(ResourceTag.MainHospitalOrganisation, Organization.class));
    }

    private static void setPeriod(AdtMessage source, EpisodeOfCare target) throws ParseException {
        Pv1Segment pv1Segment = source.getPv1Segment();

        Period period = DateTimeHelper.createPeriod(pv1Segment.getAdmitDateTime(), pv1Segment.getDischargeDateTime());

        if (period != null)
            target.setPeriod(period);
    }
}